package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User; // 导入
import com.csye6220.huskyamazon.service.ProductService;
import com.csye6220.huskyamazon.service.UserService; // 导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal; // 导入
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final ProductService productService;
    private final UserService userService; // 注入 UserService

    @Autowired
    public HomeController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    private void loadCategories(Model model) {
        List<Category> categories = productService.getAllCategories();
        model.addAttribute("categories", categories);
    }

    @GetMapping(value = {"/", "/filter", "/search"})
    public String home(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double minDiscount,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "newest") String sortBy,
            Model model,
            Principal principal // ⭐ 接收当前登录用户
    ) {
        Map<String, Object> filters = new HashMap<>();
        if (categoryId != null) filters.put("categoryId", categoryId);
        if (maxPrice != null) filters.put("maxPrice", maxPrice);
        if (minRating != null) filters.put("minRating", minRating);
        if (minDiscount != null) filters.put("minDiscount", minDiscount);
        if (keyword != null && !keyword.trim().isEmpty()) filters.put("keyword", keyword);

        Map<String, Object> result = productService.getProductsWithPagination(filters, page, 6, sortBy);

        model.addAttribute("products", result.get("products"));
        model.addAttribute("currentPage", result.get("currentPage"));
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("totalItems", result.get("totalItems"));
        model.addAttribute("filters", filters);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("searchKeyword", keyword);

        String title = "Products";
        if (keyword != null && !keyword.isEmpty()) title = "Search: " + keyword;
        else if (categoryId != null) title = "Filtered Results";
        model.addAttribute("pageTitle", title);

        loadCategories(model);

        // ⭐ 核心修改：加载浏览历史
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            List<Product> historyProducts = productService.getViewHistory(user);
            model.addAttribute("historyProducts", historyProducts);
        }

        return "home";
    }
}