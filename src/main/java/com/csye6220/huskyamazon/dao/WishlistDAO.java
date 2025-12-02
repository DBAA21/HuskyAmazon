package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.Wishlist;

import java.util.List;

public interface WishlistDAO {
    void save(Wishlist item);
    void delete(Wishlist item);
    Wishlist findByUserAndProduct(Long userId, Long productId);
    List<Wishlist> findByUser(User user);
}