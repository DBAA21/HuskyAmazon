package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.dao.ReviewDAO;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.Review;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDAO reviewDAO;
    private final ProductDAO productDAO;

    @Autowired
    public ReviewServiceImpl(ReviewDAO reviewDAO, ProductDAO productDAO) {
        this.reviewDAO = reviewDAO;
        this.productDAO = productDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsForProduct(Long productId) {
        return reviewDAO.findByProductId(productId);
    }

    @Override
    @Transactional
    public void addReview(User user, Long productId, int rating, String comment) {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        reviewDAO.save(review);
    }

    @Override
    public List<Review> getReviewsByProduct(Long id) {
        return reviewDAO.findByProductId(id);
    }
}