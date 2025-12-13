package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.Review;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.ProductService;
import com.csye6220.huskyamazon.service.ReviewService;
import com.csye6220.huskyamazon.service.UserService;
import com.csye6220.huskyamazon.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final WishlistService wishlistService;

    @Autowired
    public ProductController(ProductService productService, ReviewService reviewService, UserService userService, WishlistService wishlistService) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable Long id, Model model, Principal principal) {
        Product product = productService.getProductById(id);

        List<Review> reviews = reviewService.getReviewsByProduct(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);

        // loadfavoritestate & record历史
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            boolean isFavorite = wishlistService.isProductInWishlist(user, product);
            model.addAttribute("isFavorite", isFavorite);
            productService.recordViewHistory(user, id);
        }

        // ⭐⭐ CoreModified：Get推荐product (Bought Together) ⭐⭐
        List<Product> recommendedProducts = productService.getRecommendedProducts(id);
        model.addAttribute("recommendedProducts", recommendedProducts);

        return "product-detail";
    }
}