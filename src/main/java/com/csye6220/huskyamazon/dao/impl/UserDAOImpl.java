package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 标记这是一个 DAO 组件
public class UserDAOImpl implements UserDAO {

    // 注入 JPA 的 EntityManager，它是 Spring Boot 管理 Hibernate 的入口
    private final EntityManager entityManager;

    @Autowired
    public UserDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // 辅助方法：获取 Hibernate 原生 Session
    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(User user) {
        // 使用 Hibernate 原生 API 保存
        getCurrentSession().persist(user);
    }

    @Override
    public User findById(Long id) {
        return getCurrentSession().get(User.class, id);
    }

    @Override
    public User findByUsername(String username) {
        // HQL (Hibernate Query Language) 查询
        Query<User> query = getCurrentSession().createQuery("from User where username = :username", User.class);
        query.setParameter("username", username);
        // uniqueResult() 如果找不到会返回 null
        return query.uniqueResult();
    }

    @Override
    public List<User> findAll() {
        Query<User> query = getCurrentSession().createQuery("from User", User.class);
        return query.list();
    }

    @Override
    public void update(User user) {
        getCurrentSession().merge(user);
    }

    @Override
    public void delete(User user) {
        getCurrentSession().remove(user);
    }

    @Override
    public User findByLoginToken(String token) {
        Query<User> query = getCurrentSession().createQuery("from User where loginToken = :token", User.class);
        query.setParameter("token", token);
        return query.uniqueResult();
    }
}