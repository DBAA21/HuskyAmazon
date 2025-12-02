package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.OrderService;
import com.csye6220.huskyamazon.service.ProductService;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public AdminController(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    // ... (Products & Orders methods remain unchanged) ...
    @GetMapping("/products")
    public String productManagement(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @GetMapping
    public String dashboard() { return "redirect:/admin/products"; }

    @GetMapping("/product/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/product-form";
    }

    @GetMapping("/product/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/admin/products?error=NotFound";
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/product/save")
    public String saveProduct(@ModelAttribute Product product, @RequestParam Long categoryId) {
        if (product.getId() == null) {
            productService.addProduct(product, categoryId);
        } else {
            productService.updateProduct(product, categoryId);
        }
        return "redirect:/admin/products?success";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products?deleted";
    }

    @GetMapping("/orders")
    public String orderManagement(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/order/ship/{id}")
    public String shipOrder(@PathVariable Long id) {
        orderService.updateOrderStatus(id, "SHIPPED");
        return "redirect:/admin/orders?shipped";
    }

    // --- 3. 用户管理 (Users) ---

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // --- ⭐ 新增：展示用户编辑表单 ---
    @GetMapping("/user/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/users?error=NotFound";
        }
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    // --- ⭐ 新增：保存用户修改 ---
    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user) {
        userService.updateUser(user);
        return "redirect:/admin/users?success";
    }
}