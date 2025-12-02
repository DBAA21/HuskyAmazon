package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.ReviewDAO;
import com.csye6220.huskyamazon.entity.Review;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewDAOImpl implements ReviewDAO {

    private final EntityManager entityManager;

    @Autowired
    public ReviewDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Review review) {
        getCurrentSession().persist(review);
    }

    @Override
    public List<Review> findByProductId(Long productId) {
        // "fetch join r.user" 强制 Hibernate 在一条 SQL 里把 User 信息也查出来，避免 N+1 查询
        Query<Review> query = getCurrentSession().createQuery(
                "from Review r join fetch r.user where r.product.id = :pid order by r.createdAt desc", Review.class);
        query.setParameter("pid", productId);
        return query.list();
    }
}