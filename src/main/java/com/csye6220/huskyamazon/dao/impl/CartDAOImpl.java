package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.CartDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CartDAOImpl implements CartDAO {

    private final EntityManager entityManager;

    @Autowired
    public CartDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Cart cart) {
        getCurrentSession().persist(cart);
    }

    @Override
    public void update(Cart cart) {
        getCurrentSession().merge(cart);
    }

    @Override
    public void delete(Cart cart) {
        getCurrentSession().remove(cart);
    }

    @Override
    public Cart findById(Long id) {
        return getCurrentSession().get(Cart.class, id);
    }

    @Override
    public Cart findByUser(User user) {
        // HQL: 根据userIDFindcart
        Query<Cart> query = getCurrentSession().createQuery("from Cart where user.id = :uid", Cart.class);
        query.setParameter("uid", user.getId());
        return query.uniqueResult();
    }
}