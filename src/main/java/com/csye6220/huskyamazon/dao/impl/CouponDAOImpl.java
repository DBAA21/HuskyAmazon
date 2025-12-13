package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.CouponDAO;
import com.csye6220.huskyamazon.entity.Coupon;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CouponDAOImpl implements CouponDAO {

    private final EntityManager entityManager;

    @Autowired
    public CouponDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Coupon coupon) {
        // merge canHandle新建(insert)和Update(update)
        getCurrentSession().merge(coupon);
    }

    @Override
    public void deleteById(Long id) {
        Coupon coupon = findById(id);
        if (coupon != null) {
            getCurrentSession().remove(coupon);
        }
    }

    @Override
    public Coupon findById(Long id) {
        return getCurrentSession().get(Coupon.class, id);
    }

    @Override
    public Coupon findByCode(String code) {
        Query<Coupon> query = getCurrentSession().createQuery("from Coupon where code = :code", Coupon.class);
        query.setParameter("code", code);
        return query.uniqueResult();
    }

    @Override
    public List<Coupon> findAll() {
        return getCurrentSession().createQuery("from Coupon", Coupon.class).list();
    }
}