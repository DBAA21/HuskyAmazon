package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CartService;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        Cart cart = cartService.getCartByUser(user);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @GetMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        cartService.addItemToCart(user, productId, 1);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        cartService.removeItemFromCart(user, productId);
        return "redirect:/cart";
    }

    // --- ⭐ 新增：处理更新数量请求 ---
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        cartService.updateItemQuantity(user, productId, quantity);

        return "redirect:/cart";
    }
}