package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.service.CategoryService;
import com.csye6220.huskyamazon.service.ProductService;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap; // Imported HashMap
import java.util.List;
import java.util.Map;     // Imported Map

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public HomeController(ProductService productService, CategoryService categoryService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    // Home Page
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        addCommonAttributes(model, principal);
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // ⭐ FIX: Add an empty filters map to avoid null pointer in Thymeleaf
        model.addAttribute("filters", new HashMap<String, Object>());

        return "home";
    }

    // Search functionality (Fixed: Accessible by guests)
    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword,
                         Model model, Principal principal) {
        addCommonAttributes(model, principal);

        List<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProducts(keyword);
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("searchKeyword", keyword); // Keep the search keyword in the input box

        // ⭐ FIX: Add an empty filters map here too
        model.addAttribute("filters", new HashMap<String, Object>());

        return "home"; // Reuse the home template
    }

    // ⭐ New: Handle Filter Requests (This was missing based on your HTML form action="/filter")
    @GetMapping("/filter")
    public String filter(@RequestParam Map<String, String> allParams,
                         Model model, Principal principal) {
        addCommonAttributes(model, principal);

        // Convert String params to Object for the service layer
        Map<String, Object> filters = new HashMap<>(allParams);

        // Handle numeric conversions manually if needed, or let the DAO/Service handle strings carefully.
        // For simplicity in this fix, we pass the map as is, but ensure type safety in DAO.
        // Note: The DAO expects specific types (Long for IDs, Double for prices).
        // A robust solution would parse them here.
        Map<String, Object> typedFilters = parseFilters(allParams);

        List<Product> products = productService.getProductsWithFilters(typedFilters);

        model.addAttribute("products", products);
        model.addAttribute("filters", typedFilters); // Pass back to view to keep checkboxes checked
        model.addAttribute("searchKeyword", allParams.get("keyword"));

        return "home";
    }

    // Helper method: Add common attributes like categories and current user
    private void addCommonAttributes(Model model, Principal principal) {
        // Load categories for the sidebar
        model.addAttribute("categories", categoryService.getAllCategories());

        // Load user info if logged in
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("currentUser", userService.findByUsername(username));
        }
        // If guest (principal is null), do nothing. Thymeleaf handles nulls gracefully.
    }

    // Helper to parse string parameters to correct types for DAO
    private Map<String, Object> parseFilters(Map<String, String> params) {
        Map<String, Object> filters = new HashMap<>();
        if (params.containsKey("categoryId") && !params.get("categoryId").isEmpty()) {
            try { filters.put("categoryId", Long.parseLong(params.get("categoryId"))); } catch (NumberFormatException ignored) {}
        }
        if (params.containsKey("maxPrice") && !params.get("maxPrice").isEmpty()) {
            try { filters.put("maxPrice", Double.parseDouble(params.get("maxPrice"))); } catch (NumberFormatException ignored) {}
        }
        if (params.containsKey("minRating") && !params.get("minRating").isEmpty()) {
            try { filters.put("minRating", Double.parseDouble(params.get("minRating"))); } catch (NumberFormatException ignored) {}
        }
        if (params.containsKey("minDiscount") && !params.get("minDiscount").isEmpty()) {
            try { filters.put("minDiscount", Double.parseDouble(params.get("minDiscount"))); } catch (NumberFormatException ignored) {}
        }
        if (params.containsKey("keyword") && !params.get("keyword").isEmpty()) {
            filters.put("keyword", params.get("keyword"));
        }
        return filters;
    }
}