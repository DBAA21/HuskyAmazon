package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.CartItem;

public interface CartItemDAO {
    void save(CartItem item);
    void delete(CartItem item);
    CartItem findById(Long id);
}