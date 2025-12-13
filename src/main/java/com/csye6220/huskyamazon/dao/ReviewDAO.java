package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Review;
import java.util.List;

public interface ReviewDAO {
    void save(Review review);
    // HQL 不会automaticload Eager association，so我们用 Fetch Join
    List<Review> findByProductId(Long productId);
}