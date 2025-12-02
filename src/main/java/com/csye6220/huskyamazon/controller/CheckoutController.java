package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CartService;
import com.csye6220.huskyamazon.service.EmailService;
import com.csye6220.huskyamazon.service.OrderService;
import com.csye6220.huskyamazon.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public CheckoutController(CartService cartService, OrderService orderService, EmailService emailService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.emailService = emailService;
        this.userService = userService;
    }

    // 1. 展示支付页面
    @GetMapping("/payment")
    public String showPaymentPage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        Cart cart = cartService.getCartByUser(user);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart?empty";
        }

        model.addAttribute("cart", cart);
        return "payment";
    }

    // 2. 处理支付
    @PostMapping("/process")
    public String processPayment(@RequestParam String cardNumber,
                                 @RequestParam String expiryDate,
                                 @RequestParam String cvv,
                                 Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        // 模拟支付验证
        if (!cardNumber.matches("\\d{16}")) {
            model.addAttribute("error", "Invalid Card Number (Must be 16 digits)");
            model.addAttribute("cart", cartService.getCartByUser(user));
            return "payment";
        }

        try {

            // 下单
            Order order = orderService.placeOrder(user);

            // 发送邮件 (异步)
            emailService.sendOrderConfirmation(user, order);

            // ⭐ 修改点：跳转到专门的成功页面，并带上订单ID
            return "redirect:/checkout/success?orderId=" + order.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Transaction Failed: " + e.getMessage());
            model.addAttribute("cart", cartService.getCartByUser(user));
            return "payment";
        }
    }

    // --- ⭐ 新增：支付成功页面 ---
    @GetMapping("/success")
    public String showSuccessPage(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "payment-success"; // 对应 payment-success.html
    }
}