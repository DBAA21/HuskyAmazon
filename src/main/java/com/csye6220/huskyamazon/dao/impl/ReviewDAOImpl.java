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
        // "fetch join r.user" 强制 Hibernate 在一item SQL 里把 User info也查出来，avoid N+1 Query
        Query<Review> query = getCurrentSession().createQuery(
                "from Review r join fetch r.user where r.product.id = :pid order by r.createdAt desc", Review.class);
        query.setParameter("pid", productId);
        return query.list();
    }
}