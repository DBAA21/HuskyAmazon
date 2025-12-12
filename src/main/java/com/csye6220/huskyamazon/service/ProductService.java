package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User; // Remember to import User

import java.util.List;
import java.util.Map;

public interface ProductService {
    // ... (Keep all your original methods) ...
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

    // --- ⭐ New: Two view history methods ---
    void recordViewHistory(User user, Long productId);
    List<Product> getViewHistory(User user);

    // ⭐ New
    List<Product> getRecommendedProducts(Long productId);
}
