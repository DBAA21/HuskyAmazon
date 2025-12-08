package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User; // 记得导入 User

import java.util.List;
import java.util.Map;

public interface ProductService {
    // ... (保留你原有的所有方法) ...
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> searchProducts(String keyword);
    List<Category> getAllCategories();

    void addCategory(Category category);
    void addProduct(Product product, Long categoryId);
    void updateProduct(Product product, Long categoryId);
    void deleteProduct(Long id);

    List<Product> getProductsWithFilters(Map<String, Object> filters);
    Map<String, Object> getProductsWithPagination(Map<String, Object> filters, int page, int size, String sortBy);

    // --- ⭐ 新增的两个浏览记录方法 ---
    void recordViewHistory(User user, Long productId);
    List<Product> getViewHistory(User user);

    // ⭐ 新增
    List<Product> getRecommendedProducts(Long productId);
}