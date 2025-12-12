package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.*;
import com.csye6220.huskyamazon.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;
    @Autowired
    private CouponService couponService;

    @Autowired
    public AdminController(ProductService productService,
                           OrderService orderService,
                           UserService userService,
                           CategoryService categoryService,
                           FileStorageService fileStorageService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    // ==========================================
    // 1. Dashboard & Products (Product Management)
    // ==========================================

    @GetMapping
    public String dashboard() {
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public String productManagement(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @GetMapping("/product/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @GetMapping("/product/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/products?error=NotFound";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    /**
     * ⭐ Merged save method: supports saving with or without image
     */
    @PostMapping("/product/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult bindingResult,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        // 1. Form validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/product-form";
        }

        try {
            // 2. Handle category
            if (categoryId == null) {
                redirectAttributes.addFlashAttribute("error", "Please select a category");
                return "redirect:/admin/product/add";
            }

            // 3. Handle image upload (if there's a new image)
            if (imageFile != null && !imageFile.isEmpty()) {
                // Save file to disk
                String fileName = fileStorageService.storeFile(imageFile);

                // Generate access URL (e.g., /uploads/uuid-abc.jpg)
                String imageUrl = "/uploads/" + fileName;

                // Set to Product object
                product.setImageUrl(imageUrl);

            } else {
                // If user didn't upload new image
                if (product.getId() != null) {
                    // Update operation: keep old image
                    Product oldProduct = productService.getProductById(product.getId());
                    if (oldProduct != null && oldProduct.getImageUrl() != null) {
                        product.setImageUrl(oldProduct.getImageUrl());
                    }
                } else {
                    // Add operation: if no image uploaded, use default image
                    if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
                        product.setImageUrl("https://via.placeholder.com/300");
                    }
                }
            }

            // 4. Save or update product
            if (product.getId() == null) {
                productService.addProduct(product, categoryId);
                redirectAttributes.addFlashAttribute("message", "Product added successfully!");
            } else {
                productService.updateProduct(product, categoryId);
                redirectAttributes.addFlashAttribute("message", "Product updated successfully!");
            }

            return "redirect:/admin/products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save product: " + e.getMessage());
            return "redirect:/admin/product/add";
        }
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ==========================================
    // 2. Orders (Order Management)
    // ==========================================

    @GetMapping("/orders")
    public String orderManagement(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/order/ship/{id}")
    public String shipOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, "SHIPPED");
            redirectAttributes.addFlashAttribute("message", "Order shipped successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to ship order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ==========================================
    // 3. Users (User Management)
    // ==========================================

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/user/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        if (user == null) {
            return "redirect:/admin/users?error=NotFound";
        }
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("message", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==========================================
    // 4. Categories (Category Management)
    // ==========================================

    @GetMapping("/categories")
    public String categoryManagement(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @PostMapping("/category/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam String description,
                              RedirectAttributes redirectAttributes) {
        try {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);

            categoryService.addCategory(category);

            redirectAttributes.addFlashAttribute("message", "Category added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // --- ⭐ New: Coupon Management ---

    @GetMapping("/coupons")
    public String couponManagement(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        model.addAttribute("newCoupon", new Coupon());
        return "admin/coupons";
    }

    @PostMapping("/coupon/add")
    public String addCoupon(@ModelAttribute Coupon coupon) {
        couponService.saveCoupon(coupon);
        return "redirect:/admin/coupons?success";
    }

    @GetMapping("/coupon/delete/{id}")
    public String deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return "redirect:/admin/coupons?deleted";
    }
}
