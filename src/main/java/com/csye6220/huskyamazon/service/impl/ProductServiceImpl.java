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
 * productserviceimplementation class
 * <p>
 * responsible forHandleproduct相关的Corebusiness logic，包括productQuery、search、category管理、
 * view historyrecord、product推荐、paginationQuery等功能
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
     * recorduserview history（Corebusiness logic）
     * <p>
     * used forimplement"最近浏览"功能，Core逻辑：
     * 1. 未loginuser不recordview history
     * 2. Checkuser是否已浏览过该product
     * 3. If已浏览过，Update浏览time（guarantee该product在view history中排到最前）
     * 4. If未浏览过，New浏览record
     * </p>
     *
     * @param user      currentuser（可为null）
     * @param productId productID
     * @apiNote 使用"Updatetime"而非"insert新record"的方式，avoid同一product重复出现在view history中
     */
    @Override
    @Transactional
    public void recordViewHistory(User user, Long productId) {
        if (user == null) return; // 未login不record

        Product product = productDAO.findById(productId);
        if (product == null) return;

        // Checkdatabase中是否already exists该user对该product的浏览record
        ViewHistory history = viewHistoryDAO.findByUserAndProductId(user, productId);

        if (history != null) {
            // already existsrecord：只Update浏览time为currenttime（sort时会排到最前面）
            history.setViewedAt(LocalDateTime.now());
            viewHistoryDAO.save(history); // updateOperation
        } else {
            // doesn't existrecord：insert新的view history
            ViewHistory newHistory = new ViewHistory(user, product);
            viewHistoryDAO.save(newHistory); // insertOperation
        }
    }

    /**
     * Getuser最近浏览的productcolumntable
     * <p>
     * Corebusiness logic：
     * 1. 按浏览time倒序Get前6itemrecord
     * 2. 过滤掉3天以前的浏览record（只display近期浏览）
     * 3. extractproductobjectreturn
     * </p>
     *
     * @param user currentuser
     * @return 最近浏览的productcolumntable（最多6个，且在3天内）
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getViewHistory(User user) {
        if (user == null) return new ArrayList<>();

        // 使用paginationQuery，只取前6itemrecord（performanceoptimization）
        List<ViewHistory> histories = viewHistoryDAO.findByUserOrderByViewedAtDesc(user, PageRequest.of(0, 6));

        // time截止点：只display最近3天内的浏览record
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);

        // 从ViewHistoryobject中extractProductobject并过滤
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
     * 根据Key词searchproduct
     *
     * @param keyword searchKey词（在productname和description中search）
     * @return match的productcolumntable
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
     * Add新product
     *
     * @param product    productobject
     * @param categoryId 所属categoryID
     * @throws RuntimeException categorydoesn't exist时throw exception
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
     * Updateproductinfo
     *
     * @param product    Update后的productobject
     * @param categoryId 所属categoryID
     * @throws RuntimeException categorydoesn't exist时throw exception
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
     * Deleteproduct
     *
     * @param id productID
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productDAO.findById(id);
        if(product != null) productDAO.delete(product);
    }

    /**
     * 根据filteritem件Getproductcolumntable
     *
     * @param filters filteritem件（如category、price区间等）
     * @return 符合item件的productcolumntable
     */
    @Override
    public List<Product> getProductsWithFilters(Map<String, Object> filters) {
        return productDAO.findWithFilters(filters, 1, 100, "newest");
    }

    /**
     * Get带pagination的productcolumntable（Corebusiness logic）
     * <p>
     * used forproductcolumntablepage的pagination展示，includecomplete的paginationinfo
     * </p>
     *
     * @param filters filteritem件（category、price区间、Key词等）
     * @param page    currentpage number（从1start）
     * @param size    每pagedisplayquantity
     * @param sortBy  sort方式（newest-latest, price-price等）
     * @return includeproductcolumntable、currentpage、totalpage数、totalquantity的Mapobject
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getProductsWithPagination(Map<String, Object> filters, int page, int size, String sortBy) {
        // Getcurrentpage的productcolumntable
        List<Product> products = productDAO.findWithFilters(filters, page, size, sortBy);
        
        // Get符合item件的producttotal数
        long totalItems = productDAO.countWithFilters(filters);
        
        // calculatetotalpage数
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // assemblereturn结果
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("totalItems", totalItems);
        return result;
    }

    /**
     * Getproduct推荐columntable（Corebusiness logic）
     * <p>
     * based on"购买associationanalysis"，推荐经常一起购买的product
     * used forproduct详情page的"买了此product的人还买了"功能
     * </p>
     *
     * @param productId currentproductID
     * @return 推荐的productcolumntable（最多4个）
     * @apiNote 推荐算法based on历史order数据analysis，calculateproduct共同购买频率
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts(Long productId) {
        // Get前4个推荐product（based on购买association度sort）
        return productDAO.findFrequentlyBoughtTogether(productId, 4);
    }
}
