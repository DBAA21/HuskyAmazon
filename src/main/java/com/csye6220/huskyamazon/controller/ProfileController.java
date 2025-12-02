package com.csye6220.huskyamazon.controller;

// ... (Imports 保持不变) ...
import com.csye6220.huskyamazon.entity.Address;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.Wishlist;
import com.csye6220.huskyamazon.service.AddressService;
import com.csye6220.huskyamazon.service.OrderService;
import com.csye6220.huskyamazon.service.UserService;
import com.csye6220.huskyamazon.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    // ... (Fields 和 Constructor 保持不变) ...
    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final WishlistService wishlistService;

    @Autowired
    public ProfileController(UserService userService, AddressService addressService, OrderService orderService, WishlistService wishlistService) {
        this.userService = userService;
        this.addressService = addressService;
        this.orderService = orderService;
        this.wishlistService = wishlistService;
    }

    // ... (保留 viewProfile, addAddress, deleteAddress 方法) ...
    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        User managedUser = userService.findById(user.getId());
        List<Address> addresses = addressService.getAddressesByUser(managedUser);
        List<Order> orders = orderService.getOrderHistory(managedUser);
        List<Wishlist> wishlist = wishlistService.getWishlistForUser(managedUser);

        model.addAttribute("user", managedUser);
        model.addAttribute("addresses", addresses);
        model.addAttribute("orders", orders);
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("newAddress", new Address());
        return "profile";
    }

    @PostMapping("/address/add")
    public String addAddress(@Valid @ModelAttribute("newAddress") Address newAddress,
                             BindingResult result,
                             HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";
        if (result.hasErrors()) return viewProfile(session, model);
        addressService.addAddress(user, newAddress);
        return "redirect:/profile#addresses";
    }

    @GetMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";
        try { addressService.deleteAddress(id, user); } catch (Exception e) {}
        return "redirect:/profile#addresses";
    }

    // --- ⭐ 新增：处理修改密码 ---
    @PostMapping("/password/change")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        boolean success = userService.changePassword(user, oldPassword, newPassword);

        if (success) {
            return "redirect:/profile?passSuccess";
        } else {
            return "redirect:/profile?passError";
        }
    }
}