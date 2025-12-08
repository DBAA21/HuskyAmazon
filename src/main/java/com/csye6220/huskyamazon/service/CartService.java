package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;

public interface CartService {

    // 获取用户的购物车 (如果没有则自动创建)
    Cart getCartByUser(User user);

    // 添加商品到购物车 (如果已存在则增加数量)
    void addItemToCart(User user, Long productId, int quantity);

    // 从购物车移除商品
    void removeItemFromCart(User user, Long productId);

    // 更新购物车中某商品的数量 (如果数量<=0则删除)
    void updateItemQuantity(User user, Long productId, int quantity);

    // 清空购物车
    void clearCart(User user);
}