package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 显示购物车页面
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // 使用 Service 获取（包含初始化 items 的逻辑）
        Cart cart = cartService.getCartByUser(user);
        model.addAttribute("cart", cart);

        return "cart";
    }

    // 添加商品
    @GetMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // 默认每次添加 1 个
        cartService.addToCart(user, productId, 1);

        return "redirect:/cart";
    }

    // 移除商品
    @GetMapping("/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        cartService.removeFromCart(user, cartItemId);

        return "redirect:/cart";
    }
}