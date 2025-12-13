package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.Coupon;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final EmailService emailService;
    private final UserService userService;
    private final CouponService couponService;

    @Autowired
    public CheckoutController(CartService cartService, OrderService orderService,
                              EmailService emailService, UserService userService,
                              CouponService couponService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.emailService = emailService;
        this.userService = userService;
        this.couponService = couponService;
    }

    @GetMapping("/payment")
    public String showPaymentPage(Model model, Principal principal, HttpSession session) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());
        Cart cart = cartService.getCartByUser(user);
        if (cart.getItems().isEmpty()) return "redirect:/cart?empty";

        // frontend展示逻辑：只responsible for算给user看，不影响database
        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        double finalTotal = cart.getTotalAmount();
        double discountAmount = 0.0;

        if (appliedCoupon != null) {
            try {
                // Validate
                couponService.getValidCoupon(appliedCoupon.getCode(), cart.getTotalAmount());
                // calculate展示用的amount
                discountAmount = cart.getTotalAmount() * appliedCoupon.getDiscountPercent();
                finalTotal = cart.getTotalAmount() - discountAmount;
            } catch (Exception e) {
                session.removeAttribute("appliedCoupon");
                model.addAttribute("couponError", "Coupon removed: " + e.getMessage());
            }
        }

        model.addAttribute("cart", cart);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("appliedCoupon", appliedCoupon);

        return "payment";
    }

    @PostMapping("/coupon/apply")
    public String applyCoupon(@RequestParam String code, Principal principal, HttpSession session) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());
        Cart cart = cartService.getCartByUser(user); // Getcurrentcarttotal价used forValidate

        try {
            Coupon coupon = couponService.getValidCoupon(code, cart.getTotalAmount());
            session.setAttribute("appliedCoupon", coupon);
            return "redirect:/checkout/payment?couponSuccess";
        } catch (Exception e) {
            return "redirect:/checkout/payment?couponError=" + e.getMessage();
        }
    }

    @GetMapping("/coupon/remove")
    public String removeCoupon(HttpSession session) {
        session.removeAttribute("appliedCoupon");
        return "redirect:/checkout/payment";
    }

    @PostMapping("/process")
    public String processPayment(@RequestParam String cardNumber,
                                 Model model, Principal principal, HttpSession session) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName());

        if (!cardNumber.matches("\\d{16}")) return "redirect:/checkout/payment?error=InvalidCard";

        try {
            // mock支付latency
            Thread.sleep(1000);

            // ⭐ Getcoupon
            Coupon coupon = (Coupon) session.getAttribute("appliedCoupon");

            // ⭐ CoreModified：直接将coupon传给 Service，由 Service responsible forcalculatefinalamount和扣stock
            Order order = orderService.placeOrder(user, coupon);

            // 支付successful后清理 Session
            session.removeAttribute("appliedCoupon");

            emailService.sendOrderConfirmation(user, order);
            return "redirect:/checkout/success?orderId=" + order.getId();

        } catch (Exception e) {
            return "redirect:/checkout/payment?error=" + e.getMessage();
        }
    }

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "payment-success";
    }
}