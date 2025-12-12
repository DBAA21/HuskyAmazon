package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.User;

import java.util.List;

public interface UserService {
    // Register: includes creating user + assigning cart
    void registerUser(User user);

    // Login: validate username and password
    User login(String username, String password);

    // Find user
    User findByUsername(String username);

    void updateLoginToken(User user, String token);

    User findByLoginToken(String token);

    User findById(Long id);

    List<User> getAllUsers();

    void updateUser(User user); // For admin to update user information

    boolean changePassword(User user, String oldPassword, String newPassword);
}
