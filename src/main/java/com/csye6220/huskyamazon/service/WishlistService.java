package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.Wishlist;

import java.util.List;

public interface WishlistService {
    boolean isFavorite(User user, Long productId);
    void toggleFavorite(User user, Long productId);
    List<Wishlist> getWishlistForUser(User user);
    boolean isProductInWishlist(User user, Product product);
}