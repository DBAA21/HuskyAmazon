package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Product;
import java.util.List;
import java.util.Map;

public interface ProductDAO {
    // ... (其他methodremain unchanged) ...
    void save(Product product);
    Product findById(Long id);
    List<Product> findAll();
    void update(Product product);
    void delete(Product product);
    List<Product> searchProducts(String keyword);

    // ⭐ Modifiedsignature：supportpagination和sort
    List<Product> findWithFilters(Map<String, Object> filters, int page, int size, String sortBy);

    // ⭐ New：calculatetotalitem数 (used forfrontendcalculatepage number)
    long countWithFilters(Map<String, Object> filters);

    // --- ⭐ New：Find经常一起购买的product ---
    List<Product> findFrequentlyBoughtTogether(Long productId, int limit);
}