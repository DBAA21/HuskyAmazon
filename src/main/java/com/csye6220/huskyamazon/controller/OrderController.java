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
    private final UserService userService; // Inject UserService 以support Security

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    // 1. 旧的结账interface (修正报错)
    // 建议：去 cart.html 把 action 改成 /checkout/payment
    // 这里我们做一个兼容：If还在用旧link，直接重定向到新支付page
    @PostMapping("/checkout")
    public String checkout() {
        // ⭐ 修正逻辑：不再直接下单，而是导向新的支付+couponpage面
        return "redirect:/checkout/payment";
    }

    /* If你非要Keep“一键直接下单” (不推荐)，请用下面的代码替换上面的 checkout method：

    @PostMapping("/checkout")
    public String checkout(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        try {
            // ⭐ 修复报错Key点：传入 null 作为couponparameters
            orderService.placeOrder(user, null);
            return "redirect:/order/history?success";
        } catch (Exception e) {
            return "redirect:/cart?error";
        }
    }
    */

    // 2. display历史order (适配 Spring Security)
    @GetMapping("/history")
    public String showOrderHistory(Model model, Principal principal) {
        // 使用 Principal Getcurrentloginuser (比 Session 更稳健)
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(principal.getName());

        List<Order> orders = orderService.getOrderHistory(user);
        model.addAttribute("orders", orders);

        return "order-history"; // ensure你有 templates/order-history.html
    }
}