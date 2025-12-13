package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WishlistController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/wishlist/toggle/{productId}")
    public String toggleWishlist(@PathVariable Long productId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        wishlistService.toggleFavorite(user, productId);

        // Operation完重定向回productpage
        return "redirect:/product/" + productId;
    }
}