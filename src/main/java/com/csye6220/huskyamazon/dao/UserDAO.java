package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface UserDAO {
    void save(User user);
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll();
    void update(User user);
    void delete(User user);
    User findByLoginToken(String token);
}