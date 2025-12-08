package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CategoryDAO;
import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.dao.ViewHistoryDAO; // 新增
import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User; // 新增
import com.csye6220.huskyamazon.entity.ViewHistory; // 新增
import com.csye6220.huskyamazon.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest; // 新增
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final ViewHistoryDAO viewHistoryDAO; // 注入新 DAO

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO, CategoryDAO categoryDAO, ViewHistoryDAO viewHistoryDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.viewHistoryDAO = viewHistoryDAO;
    }

    // --- ⭐ 新增实现：记录浏览历史 ---
    @Override
    @Transactional
    public void recordViewHistory(User user, Long productId) {
        if (user == null) return; // 未登录不记录

        Product product = productDAO.findById(productId);
        if (product == null) return;

        // 1. 检查数据库里是否已经有这条记录
        ViewHistory history = viewHistoryDAO.findByUserAndProductId(user, productId);

        if (history != null) {
            // 2. 如果有，只更新时间为“现在” (这样排序时它会排到最前面)
            history.setViewedAt(LocalDateTime.now());
            viewHistoryDAO.save(history); // update
        } else {
            // 3. 如果没有，插入一条新记录
            ViewHistory newHistory = new ViewHistory(user, product);
            viewHistoryDAO.save(newHistory); // insert
        }
    }

    // --- ⭐ 新增实现：获取最近浏览的商品 ---
    @Override
    @Transactional(readOnly = true)
    public List<Product> getViewHistory(User user) {
        if (user == null) return new ArrayList<>();

        // 使用 PageRequest.of(0, 6) 只取前 6 条记录
        List<ViewHistory> histories = viewHistoryDAO.findByUserOrderByViewedAtDesc(user, PageRequest.of(0, 6));

        // 仅展示最近3天内的记录
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);

        // 从 ViewHistory 对象中提取出 Product 对象返回
        return histories.stream()
                .filter(h -> h.getViewedAt() != null && !h.getViewedAt().isBefore(cutoff))
                .map(ViewHistory::getProduct)
                .collect(Collectors.toList());
    }

    // ... (保留你原来的所有方法，不要删！) ...
    // 为了完整性，这里列出关键的几个，确保你没删错

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() { return productDAO.findWithFilters(new HashMap<>(), 1, 100, "newest"); }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) { return productDAO.findById(id); }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) { return productDAO.searchProducts(keyword); }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() { return categoryDAO.findAll(); }

    @Override
    @Transactional public void addCategory(Category category) { categoryDAO.save(category); }

    @Override
    @Transactional
    public void addProduct(Product product, Long categoryId) {
        Category category = categoryDAO.findById(categoryId);
        if (category == null) throw new RuntimeException("Category not found");
        product.setCategory(category);
        productDAO.save(product);
    }

    @Override
    @Transactional
    public void updateProduct(Product product, Long categoryId) {
        Category category = categoryDAO.findById(categoryId);
        if (category == null) throw new RuntimeException("Category not found");
        product.setCategory(category);
        productDAO.update(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productDAO.findById(id);
        if(product != null) productDAO.delete(product);
    }

    @Override
    public List<Product> getProductsWithFilters(Map<String, Object> filters) {
        return productDAO.findWithFilters(filters, 1, 100, "newest");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProductsWithPagination(Map<String, Object> filters, int page, int size, String sortBy) {
        List<Product> products = productDAO.findWithFilters(filters, page, size, sortBy);
        long totalItems = productDAO.countWithFilters(filters);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalItems", totalItems);
        return result;
    }

    // ⭐ 实现
    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts(Long productId) {
        // 获取前 4 个推荐商品
        return productDAO.findFrequentlyBoughtTogether(productId, 4);
    }
}