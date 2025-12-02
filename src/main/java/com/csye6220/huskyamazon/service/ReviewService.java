package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Review;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface ReviewService {
    List<Review> getReviewsForProduct(Long productId);
    void addReview(User user, Long productId, int rating, String comment);
}