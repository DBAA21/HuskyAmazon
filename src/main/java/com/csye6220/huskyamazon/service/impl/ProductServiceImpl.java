package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CategoryDAO;
import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.dao.ViewHistoryDAO;
import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.ViewHistory;
import com.csye6220.huskyamazon.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * <p>
 * 负责处理商品相关的核心业务逻辑，包括商品查询、搜索、分类管理、
 * 浏览历史记录、商品推荐、分页查询等功能
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final ViewHistoryDAO viewHistoryDAO;

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO, CategoryDAO categoryDAO, ViewHistoryDAO viewHistoryDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.viewHistoryDAO = viewHistoryDAO;
    }

    /**
     * 记录用户浏览历史（核心业务逻辑）
     * <p>
     * 用于实现"最近浏览"功能，核心逻辑：
     * 1. 未登录用户不记录浏览历史
     * 2. 检查用户是否已浏览过该商品
     * 3. 如果已浏览过，更新浏览时间（保证该商品在浏览历史中排到最前）
     * 4. 如果未浏览过，新增浏览记录
     * </p>
     *
     * @param user      当前用户（可为null）
     * @param productId 商品ID
     * @apiNote 使用"更新时间"而非"插入新记录"的方式，避免同一商品重复出现在浏览历史中
     */
    @Override
    @Transactional
    public void recordViewHistory(User user, Long productId) {
        if (user == null) return; // 未登录不记录

        Product product = productDAO.findById(productId);
        if (product == null) return;

        // 检查数据库中是否已存在该用户对该商品的浏览记录
        ViewHistory history = viewHistoryDAO.findByUserAndProductId(user, productId);

        if (history != null) {
            // 已存在记录：只更新浏览时间为当前时间（排序时会排到最前面）
            history.setViewedAt(LocalDateTime.now());
            viewHistoryDAO.save(history); // update操作
        } else {
            // 不存在记录：插入新的浏览历史
            ViewHistory newHistory = new ViewHistory(user, product);
            viewHistoryDAO.save(newHistory); // insert操作
        }
    }

    /**
     * 获取用户最近浏览的商品列表
     * <p>
     * 核心业务逻辑：
     * 1. 按浏览时间倒序获取前6条记录
     * 2. 过滤掉3天以前的浏览记录（只显示近期浏览）
     * 3. 提取商品对象返回
     * </p>
     *
     * @param user 当前用户
     * @return 最近浏览的商品列表（最多6个，且在3天内）
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getViewHistory(User user) {
        if (user == null) return new ArrayList<>();

        // 使用分页查询，只取前6条记录（性能优化）
        List<ViewHistory> histories = viewHistoryDAO.findByUserOrderByViewedAtDesc(user, PageRequest.of(0, 6));

        // 时间截止点：只显示最近3天内的浏览记录
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);

        // 从ViewHistory对象中提取Product对象并过滤
        return histories.stream()
                .filter(h -> h.getViewedAt() != null && !h.getViewedAt().isBefore(cutoff))
                .map(ViewHistory::getProduct)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productDAO.findWithFilters(new HashMap<>(), 1, 100, "newest");
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productDAO.findById(id);
    }

    /**
     * 根据关键词搜索商品
     *
     * @param keyword 搜索关键词（在商品名称和描述中搜索）
     * @return 匹配的商品列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productDAO.searchProducts(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    @Override
    @Transactional
    public void addCategory(Category category) {
        categoryDAO.save(category);
    }

    /**
     * 添加新商品
     *
     * @param product    商品对象
     * @param categoryId 所属分类ID
     * @throws RuntimeException 分类不存在时抛出异常
     */
    @Override
    @Transactional
    public void addProduct(Product product, Long categoryId) {
        Category category = categoryDAO.findById(categoryId);
        if (category == null) throw new RuntimeException("Category not found");
        product.setCategory(category);
        productDAO.save(product);
    }

    /**
     * 更新商品信息
     *
     * @param product    更新后的商品对象
     * @param categoryId 所属分类ID
     * @throws RuntimeException 分类不存在时抛出异常
     */
    @Override
    @Transactional
    public void updateProduct(Product product, Long categoryId) {
        Category category = categoryDAO.findById(categoryId);
        if (category == null) throw new RuntimeException("Category not found");
        product.setCategory(category);
        productDAO.update(product);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productDAO.findById(id);
        if(product != null) productDAO.delete(product);
    }

    /**
     * 根据筛选条件获取商品列表
     *
     * @param filters 筛选条件（如分类、价格区间等）
     * @return 符合条件的商品列表
     */
    @Override
    public List<Product> getProductsWithFilters(Map<String, Object> filters) {
        return productDAO.findWithFilters(filters, 1, 100, "newest");
    }

    /**
     * 获取带分页的商品列表（核心业务逻辑）
     * <p>
     * 用于商品列表页的分页展示，包含完整的分页信息
     * </p>
     *
     * @param filters 筛选条件（分类、价格区间、关键词等）
     * @param page    当前页码（从1开始）
     * @param size    每页显示数量
     * @param sortBy  排序方式（newest-最新, price-价格等）
     * @return 包含商品列表、当前页、总页数、总数量的Map对象
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProductsWithPagination(Map<String, Object> filters, int page, int size, String sortBy) {
        // 获取当前页的商品列表
        List<Product> products = productDAO.findWithFilters(filters, page, size, sortBy);
        
        // 获取符合条件的商品总数
        long totalItems = productDAO.countWithFilters(filters);
        
        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalItems", totalItems);
        return result;
    }

    /**
     * 获取商品推荐列表（核心业务逻辑）
     * <p>
     * 基于"购买关联分析"，推荐经常一起购买的商品
     * 用于商品详情页的"买了此商品的人还买了"功能
     * </p>
     *
     * @param productId 当前商品ID
     * @return 推荐的商品列表（最多4个）
     * @apiNote 推荐算法基于历史订单数据分析，计算商品共同购买频率
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts(Long productId) {
        // 获取前4个推荐商品（基于购买关联度排序）
        return productDAO.findFrequentlyBoughtTogether(productId, 4);
    }
}
