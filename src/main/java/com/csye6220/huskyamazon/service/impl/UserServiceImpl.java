package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @Transactional // 事务：原子操作。如果保存 User 失败，Cart 也不会被保存
    public void registerUser(User user) {
        // 业务逻辑：每个新用户自动分配一个空购物车
        Cart cart = new Cart();
        cart.setUser(user); // 购物车关联用户
        user.setCart(cart); // 用户关联购物车 (双向关联)

        // 因为我们在 User 实体上配置了 CascadeType.ALL，
        // 所以只需要保存 User，Cart 会自动被保存到 carts 表
        userDAO.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // 注意：实际项目中密码应该哈希比对，这里简化为明文比对
            return user;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    @Transactional
    public void updateLoginToken(User user, String token) {
        // 先查出持久化状态的 User
        User managedUser = userDAO.findById(user.getId());
        if (managedUser != null) {
            managedUser.setLoginToken(token);
            userDAO.update(managedUser);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByLoginToken(String token) {
        return userDAO.findByLoginToken(token);
    }

    // --- ⭐ 新增实现 ---

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        // 关键逻辑：先查出数据库里的旧数据
        User existingUser = userDAO.findById(user.getId());
        if (existingUser != null) {
            // 只更新允许修改的字段
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());

            // 注意：密码、购物车等关联信息保持不变
            // 如果需要修改用户名，也可以在这里加：existingUser.setUsername(user.getUsername());

            userDAO.update(existingUser);
        }
    }

    @Override
    @Transactional
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        // 1. 获取数据库中最新的用户信息
        User currentUser = userDAO.findById(user.getId());

        if (currentUser != null) {
            // 2. 验证旧密码是否正确
            if (currentUser.getPassword().equals(oldPassword)) {
                // 3. 更新为新密码
                currentUser.setPassword(newPassword);
                userDAO.update(currentUser);
                return true; // 修改成功
            }
        }
        return false; // 旧密码错误
    }
}