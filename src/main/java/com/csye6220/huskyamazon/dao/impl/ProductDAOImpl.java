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

    // ... (保留原有的 save, findById, findAll, update, delete, searchProducts 等方法) ...
    @Override public void save(Product product) { getCurrentSession().persist(product); }
    @Override public Product findById(Long id) { return getCurrentSession().get(Product.class, id); }
    @Override public List<Product> findAll() { return getCurrentSession().createQuery("from Product", Product.class).list(); }
    @Override public void update(Product product) { getCurrentSession().merge(product); }
    @Override public void delete(Product product) { getCurrentSession().remove(product); }
    @Override
    public List<Product> searchProducts(String keyword) {
        String hql = "from Product p where lower(p.name) like :keyword or lower(p.description) like :keyword";
        Query<Product> query = getCurrentSession().createQuery(hql, Product.class);
        query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        return query.list();
    }

    // ... (保留 findWithFilters 和 countWithFilters 方法) ...
    private void applyFilters(StringBuilder hql, List<String> conditions, Map<String, Object> parameters, Map<String, Object> filters) {
        if (filters.containsKey("categoryId")) { conditions.add("p.category.id = :catId"); parameters.put("catId", filters.get("categoryId")); }
        if (filters.containsKey("maxPrice")) { conditions.add("p.price <= :maxPrice"); parameters.put("maxPrice", filters.get("maxPrice")); }
        if (filters.containsKey("minRating")) { conditions.add("p.id in (select r.product.id from Review r group by r.product.id having avg(r.rating) >= :minRating)"); parameters.put("minRating", filters.get("minRating")); }
        if (filters.containsKey("minDiscount")) { conditions.add("p.originalPrice is not null and (1 - (p.price / p.originalPrice)) >= :minDiscount"); parameters.put("minDiscount", filters.get("minDiscount")); }
        if (filters.containsKey("keyword")) { conditions.add("(lower(p.name) like :keyword or lower(p.description) like :keyword)"); parameters.put("keyword", "%" + filters.get("keyword").toString().toLowerCase() + "%"); }
        if (!conditions.isEmpty()) hql.append(" where ").append(String.join(" and ", conditions));
    }

    @Override
    public List<Product> findWithFilters(Map<String, Object> filters, int page, int size, String sortBy) {
        StringBuilder hql = new StringBuilder("from Product p");
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        applyFilters(hql, conditions, parameters, filters);
        if ("price_asc".equals(sortBy)) hql.append(" order by p.price asc");
        else if ("price_desc".equals(sortBy)) hql.append(" order by p.price desc");
        else hql.append(" order by p.id desc");
        Query<Product> query = getCurrentSession().createQuery(hql.toString(), Product.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) query.setParameter(entry.getKey(), entry.getValue());
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.list();
    }

    @Override
    public long countWithFilters(Map<String, Object> filters) {
        StringBuilder hql = new StringBuilder("select count(p) from Product p");
        List<String> conditions = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        applyFilters(hql, conditions, parameters, filters);
        Query<Long> query = getCurrentSession().createQuery(hql.toString(), Long.class);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) query.setParameter(entry.getKey(), entry.getValue());
        return query.uniqueResult();
    }

    // --- ⭐ 实现推荐算法 ---
    @Override
    public List<Product> findFrequentlyBoughtTogether(Long productId, int limit) {
        // SQL 逻辑：
        // 1. SELECT sub_oi.order.id ... WHERE sub_oi.product.id = :pid -> 找到包含当前商品的所有订单
        // 2. SELECT p FROM OrderItem oi JOIN oi.product p -> 找到这些订单里的商品
        // 3. WHERE oi.order.id IN (...) AND p.id != :pid -> 排除当前商品自己
        // 4. GROUP BY p.id ORDER BY count(...) DESC -> 按出现频率排序

        String hql = "SELECT p FROM OrderItem oi " +
                "JOIN oi.product p " +
                "WHERE oi.order.id IN (" +
                "    SELECT sub_oi.order.id " +
                "    FROM OrderItem sub_oi " +
                "    WHERE sub_oi.product.id = :productId" +
                ") " +
                "AND p.id != :productId " +
                "GROUP BY p.id " +
                "ORDER BY count(oi.order.id) DESC";

        Query<Product> query = getCurrentSession().createQuery(hql, Product.class);
        query.setParameter("productId", productId);
        query.setMaxResults(limit); // 只取前 N 个

        return query.list();
    }
}