package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.CartDAO;
import com.csye6220.huskyamazon.entity.Cart;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
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
    public void update(Cart cart) {
        getCurrentSession().merge(cart);
    }

    @Override
    public Cart findById(Long id) {
        return getCurrentSession().get(Cart.class, id);
    }
}