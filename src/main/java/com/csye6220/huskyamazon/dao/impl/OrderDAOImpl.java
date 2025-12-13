package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.OrderDAO;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDAOImpl implements OrderDAO {

    private final EntityManager entityManager;

    @Autowired
    public OrderDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Order order) {
        getCurrentSession().persist(order);
    }

    @Override
    public Order findById(Long id) {
        return getCurrentSession().get(Order.class, id);
    }

    @Override
    public List<Order> findByUser(User user) {
        // HQL: Query属于该user的allorder，按date倒序排column
        Query<Order> query = getCurrentSession().createQuery(
                "from Order where user.id = :uid order by orderDate desc", Order.class);
        query.setParameter("uid", user.getId());
        return query.list();
    }

    @Override
    public List<Order> findAll() {
        return getCurrentSession().createQuery("from Order order by orderDate desc", Order.class).list();
    }

    @Override
    public void update(Order order) {
        getCurrentSession().merge(order);
    }
}