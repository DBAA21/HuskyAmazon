package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/review/add/{productId}")
    public String addReview(@PathVariable Long productId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            HttpSession session) {

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            reviewService.addReview(user, productId, rating, comment);
        } catch (Exception e) {
            // 简单处理，可以优化为带错误信息
            return "redirect:/product/" + productId + "?reviewError";
        }

        // 提交评论后，重定向回商品详情页
        return "redirect:/product/" + productId;
    }
}