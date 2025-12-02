package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;

public interface CartService {
    Cart getCartByUser(User user);
    void addToCart(User user, Long productId, int quantity);
    void removeFromCart(User user, Long cartItemId);
    void clearCart(User user);
}