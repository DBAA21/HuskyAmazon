package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.entity.Product;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductDAOImpl implements ProductDAO {

    private final EntityManager entityManager;

    @Autowired
    public ProductDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    // ... (save, findById, findAll, update, delete 保持不变) ...
    @Override
    public void save(Product product) { getCurrentSession().persist(product); }
    @Override
    public Product findById(Long id) { return getCurrentSession().get(Product.class, id); }
    @Override
    public List<Product> findAll() { return getCurrentSession().createQuery("from Product", Product.class).list(); }
    @Override
    public void update(Product product) { getCurrentSession().merge(product); }
    @Override
    public void delete(Product product) { getCurrentSession().remove(product); }

    // 这个旧的 searchProducts 方法虽然留着，但我们不再用它了
    @Override
    public List<Product> searchProducts(String keyword) {
        String hql = "from Product p where lower(p.name) like :keyword or lower(p.description) like :keyword";
        Query<Product> query = getCurrentSession().createQuery(hql, Product.class);
        query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        return query.list();
    }

    // --- ⭐ 核心修改：统一的构建 Where 子句 ---
    private void applyFilters(StringBuilder hql, List<String> conditions, Map<String, Object> parameters, Map<String, Object> filters) {
        // 1. 类别
        if (filters.containsKey("categoryId")) {
            conditions.add("p.category.id = :catId");
            parameters.put("catId", filters.get("categoryId"));
        }
        // 2. 价格
        if (filters.containsKey("maxPrice")) {
            conditions.add("p.price <= :maxPrice");
            parameters.put("maxPrice", filters.get("maxPrice"));
        }
        // 3. 评分
        if (filters.containsKey("minRating")) {
            conditions.add("p.id in (select r.product.id from Review r group by r.product.id having avg(r.rating) >= :minRating)");
            parameters.put("minRating", filters.get("minRating"));
        }
        // 4. 折扣
        if (filters.containsKey("minDiscount")) {
            conditions.add("p.originalPrice is not null and (1 - (p.price / p.originalPrice)) >= :minDiscount");
            parameters.put("minDiscount", filters.get("minDiscount"));
        }
        // ⭐ 5. 新增：关键词搜索
        if (filters.containsKey("keyword")) {
            conditions.add("(lower(p.name) like :keyword or lower(p.description) like :keyword)");
            parameters.put("keyword", "%" + filters.get("keyword").toString().toLowerCase() + "%");
        }

        if (!conditions.isEmpty()) {
            hql.append(" where ").append(String.join(" and ", conditions));
        }
    }

    // --- 核心实现：带分页的查询 (保持不变，因为 applyFilters 已经升级了) ---
    @Override
    public List<Product> findWithFilters(Map<String, Object> filters, int page, int size, String sortBy) {
        StringBuilder hql = new StringBuilder("from Product p");
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();

        applyFilters(hql, conditions, parameters, filters);

        // 排序逻辑
        if ("price_asc".equals(sortBy)) hql.append(" order by p.price asc");
        else if ("price_desc".equals(sortBy)) hql.append(" order by p.price desc");
        else if ("newest".equals(sortBy)) hql.append(" order by p.id desc");
        else hql.append(" order by p.id desc");

        Query<Product> query = getCurrentSession().createQuery(hql.toString(), Product.class);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.list();
    }

    // --- 计算总条数 (保持不变) ---
    @Override
    public long countWithFilters(Map<String, Object> filters) {
        StringBuilder hql = new StringBuilder("select count(p) from Product p");
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();

        applyFilters(hql, conditions, parameters, filters);

        Query<Long> query = getCurrentSession().createQuery(hql.toString(), Long.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.uniqueResult();
    }
}