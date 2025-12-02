package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.CartItemDAO;
import com.csye6220.huskyamazon.entity.CartItem;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CartItemDAOImpl implements CartItemDAO {

    private final EntityManager entityManager;

    @Autowired
    public CartItemDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(CartItem item) {
        getCurrentSession().persist(item);
    }

    @Override
    public void delete(CartItem item) {
        getCurrentSession().remove(item);
    }

    @Override
    public CartItem findById(Long id) {
        return getCurrentSession().get(CartItem.class, id);
    }
}