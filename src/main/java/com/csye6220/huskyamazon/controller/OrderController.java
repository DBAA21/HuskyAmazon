package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.OrderService;
import com.csye6220.huskyamazon.service.UserService; // 引入 UserService
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal; // 引入 Principal
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService; // 注入 UserService 以支持 Security

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // 1. 旧的结账接口 (修正报错)
    // 建议：去 cart.html 把 action 改成 /checkout/payment
    // 这里我们做一个兼容：如果还在用旧链接，直接重定向到新支付页
    @PostMapping("/checkout")
    public String checkout() {
        // ⭐ 修正逻辑：不再直接下单，而是导向新的支付+优惠券页面
        return "redirect:/checkout/payment";
    }

    /* 如果你非要保留“一键直接下单” (不推荐)，请用下面的代码替换上面的 checkout 方法：

    @PostMapping("/checkout")
    public String checkout(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        try {
            // ⭐ 修复报错关键点：传入 null 作为优惠券参数
            orderService.placeOrder(user, null);
            return "redirect:/order/history?success";
        } catch (Exception e) {
            return "redirect:/cart?error";
        }
    }
    */

    // 2. 显示历史订单 (适配 Spring Security)
    @GetMapping("/history")
    public String showOrderHistory(Model model, Principal principal) {
        // 使用 Principal 获取当前登录用户 (比 Session 更稳健)
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName());

        List<Order> orders = orderService.getOrderHistory(user);
        model.addAttribute("orders", orders);

        return "order-history"; // 确保你有 templates/order-history.html
    }
}