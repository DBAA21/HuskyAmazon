package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;

public interface CartDAO {
    void save(Cart cart);
    void update(Cart cart);
    void delete(Cart cart);
    Cart findById(Long id);
    Cart findByUser(User user);
}