package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Cart;

public interface CartDAO {
    void update(Cart cart);
    Cart findById(Long id);
}