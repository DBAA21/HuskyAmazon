package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 处理结账请求 (点击 "Proceed to Checkout" 按钮)
    @PostMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            orderService.placeOrder(user);
            // 结账成功，跳到历史订单页
            return "redirect:/order/history?success";
        } catch (Exception e) {
            // 如果购物车为空等错误
            return "redirect:/cart?error";
        }
    }

    // 显示历史订单
    @GetMapping("/history")
    public String showOrderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrderHistory(user);
        model.addAttribute("orders", orders);

        return "order-history";
    }
}