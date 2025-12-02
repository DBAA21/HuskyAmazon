package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.AddressDAO;
import com.csye6220.huskyamazon.entity.Address;
import com.csye6220.huskyamazon.entity.User;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AddressDAOImpl implements AddressDAO {

    private final EntityManager entityManager;

    @Autowired
    public AddressDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Address address) {
        getCurrentSession().persist(address);
    }

    @Override
    public void delete(Address address) {
        getCurrentSession().remove(address);
    }

    @Override
    public Address findById(Long id) {
        return getCurrentSession().get(Address.class, id);
    }

    @Override
    public List<Address> findByUser(User user) {
        Query<Address> query = getCurrentSession().createQuery("from Address where user.id = :uid", Address.class);
        query.setParameter("uid", user.getId());
        return query.list();
    }
}