package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;

public interface CartService {

    // Get user's cart (create automatically if it doesn't exist)
    Cart getCartByUser(User user);

    // Add product to cart (increase quantity if already exists)
    void addItemToCart(User user, Long productId, int quantity);

    // Remove product from cart
    void removeItemFromCart(User user, Long productId);

    // Update product quantity in cart (delete if quantity <= 0)
    void updateItemQuantity(User user, Long productId, int quantity);

    // Clear cart
    void clearCart(User user);
}
