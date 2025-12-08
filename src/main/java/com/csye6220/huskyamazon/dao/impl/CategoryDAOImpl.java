package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.CategoryDAO;
import com.csye6220.huskyamazon.entity.Category;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CategoryDAOImpl implements CategoryDAO {

    private final EntityManager entityManager;

    @Autowired
    public CategoryDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Category category) {
        getCurrentSession().persist(category);
    }

    @Override
    public Category findById(Long id) {
        return getCurrentSession().get(Category.class, id);
    }

    @Override
    public List<Category> findAll() {
        return getCurrentSession().createQuery("from Category", Category.class).list();
    }

    @Override
    public void delete(Long id) {
        Category category = entityManager.find(Category.class, id);
        if (category != null) {
            entityManager.remove(category);
        }
    }
}