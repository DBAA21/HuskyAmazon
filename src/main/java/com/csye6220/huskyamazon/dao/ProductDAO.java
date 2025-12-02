package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Product;
import java.util.List;
import java.util.Map;

public interface ProductDAO {
    // ... (其他方法保持不变) ...
    void save(Product product);
    Product findById(Long id);
    List<Product> findAll();
    void update(Product product);
    void delete(Product product);
    List<Product> searchProducts(String keyword);

    // ⭐ 修改签名：支持分页和排序
    List<Product> findWithFilters(Map<String, Object> filters, int page, int size, String sortBy);

    // ⭐ 新增：计算总条数 (用于前端计算页码)
    long countWithFilters(Map<String, Object> filters);
}