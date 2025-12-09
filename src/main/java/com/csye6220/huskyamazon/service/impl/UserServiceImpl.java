package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 * <p>
 * 负责处理用户相关的核心业务逻辑，包括用户注册、登录验证、
 * 自动登录（记住我功能）、密码修改等功能
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 用户注册
     * <p>
     * 核心业务逻辑：
     * 1. 为新用户自动创建一个空购物车
     * 2. 建立用户与购物车的双向关联
     * 3. 利用JPA级联保存特性，一次保存用户和购物车
     * </p>
     *
     * @param user 待注册的用户对象（包含用户名、密码、邮箱等信息）
     * @throws org.springframework.dao.DataIntegrityViolationException 如果用户名已存在
     */
    @Override
    @Transactional // 事务保证：用户和购物车要么同时保存成功，要么同时失败回滚
    public void registerUser(User user) {
        // 业务逻辑：每个新用户自动分配一个空购物车
        Cart cart = new Cart();
        cart.setUser(user); // 购物车关联用户
        user.setCart(cart); // 用户关联购物车（双向关联）

        // 因为User实体配置了CascadeType.ALL，只需保存User，Cart会自动级联保存
        userDAO.save(user);
    }

    /**
     * 用户登录验证
     * <p>
     * 验证用户名和密码是否匹配
     * </p>
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 验证成功返回用户对象，失败返回null
     * @apiNote 生产环境应使用BCrypt等加密算法进行密码哈希比对，此处简化为明文比对
     */
    @Override
    @Transactional(readOnly = true)
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // 注意：实际项目中密码应该使用BCrypt哈希比对，这里简化为明文比对
            return user;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    /**
     * 更新用户的自动登录令牌
     * <p>
     * 用于实现"记住我"功能，将生成的令牌保存到数据库，
     * 同时该令牌也会存储在浏览器Cookie中用于自动登录验证
     * </p>
     *
     * @param user  用户对象
     * @param token 生成的唯一令牌（通常为UUID）
     */
    @Override
    @Transactional
    public void updateLoginToken(User user, String token) {
        // 先查出持久化状态的User对象（确保在Hibernate Session管理范围内）
        User managedUser = userDAO.findById(user.getId());
        if (managedUser != null) {
            managedUser.setLoginToken(token);
            userDAO.update(managedUser);
        }
    }

    /**
     * 根据自动登录令牌查找用户
     * <p>
     * 用于"记住我"功能的自动登录，通过Cookie中的令牌验证用户身份
     * </p>
     *
     * @param token 自动登录令牌
     * @return 匹配的用户对象，未找到返回null
     */
    @Override
    @Transactional(readOnly = true)
    public User findByLoginToken(String token) {
        return userDAO.findByLoginToken(token);
    }

    /**
     * 根据用户ID查找用户
     *
     * @param id 用户ID
     * @return 用户对象，未找到返回null
     */
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userDAO.findById(id);
    }

    /**
     * 获取所有用户列表
     * <p>
     * 主要用于管理员后台查看所有用户
     * </p>
     *
     * @return 所有用户的列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * 更新用户信息（管理员功能）
     * <p>
     * 核心业务逻辑：
     * 1. 先从数据库查询最新的用户数据（避免覆盖其他字段）
     * 2. 只更新允许修改的字段（邮箱、角色）
     * 3. 敏感字段如密码、关联关系保持不变
     * </p>
     *
     * @param user 包含待更新信息的用户对象
     */
    @Override
    @Transactional
    public void updateUser(User user) {
        // 关键逻辑：先查出数据库中的持久化对象，避免直接更新导致数据丢失
        User existingUser = userDAO.findById(user.getId());
        if (existingUser != null) {
            // 只更新允许修改的字段（白名单策略）
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());

            // 注意：密码、购物车、订单等关联信息保持不变，防止误修改

            userDAO.update(existingUser);
        }
    }

    /**
     * 修改用户密码
     * <p>
     * 核心业务逻辑：
     * 1. 从数据库获取最新的用户信息（防止使用过期数据）
     * 2. 验证旧密码是否正确（安全校验）
     * 3. 旧密码正确才允许更新为新密码
     * </p>
     *
     * @param user        当前用户对象
     * @param oldPassword 旧密码（用于验证）
     * @param newPassword 新密码
     * @return true-修改成功，false-旧密码错误或用户不存在
     */
    @Override
    @Transactional
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        // 1. 获取数据库中最新的用户信息（确保数据一致性）
        User currentUser = userDAO.findById(user.getId());

        if (currentUser != null) {
            // 2. 验证旧密码是否正确（安全校验，防止未授权修改）
            if (currentUser.getPassword().equals(oldPassword)) {
                // 3. 更新为新密码
                currentUser.setPassword(newPassword);
                userDAO.update(currentUser);
                return true; // 修改成功
            }
        }
        return false; // 旧密码错误或用户不存在
    }
}
