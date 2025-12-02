package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.User;

import java.util.List;

public interface UserService {
    // 注册：包含创建用户 + 分配购物车
    void registerUser(User user);

    // 登录：验证用户名密码
    User login(String username, String password);

    // 查找
    User findByUsername(String username);

    void updateLoginToken(User user, String token);

    User findByLoginToken(String token);

    User findById(Long id);

    List<User> getAllUsers();

    void updateUser(User user); // 用于管理员更新信息

    boolean changePassword(User user, String oldPassword, String newPassword);
}