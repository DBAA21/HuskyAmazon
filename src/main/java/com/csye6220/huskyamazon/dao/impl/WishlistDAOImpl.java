package com.csye6220.huskyamazon.dao.impl;

import com.csye6220.huskyamazon.dao.WishlistDAO;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.Wishlist;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WishlistDAOImpl implements WishlistDAO {

    private final EntityManager entityManager;

    @Autowired
    public WishlistDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Override
    public void save(Wishlist item) {
        getCurrentSession().persist(item);
    }

    @Override
    public void delete(Wishlist item) {
        getCurrentSession().remove(item);
    }

    @Override
    public Wishlist findByUserAndProduct(Long userId, Long productId) {
        Query<Wishlist> query = getCurrentSession().createQuery(
                "from Wishlist where user.id = :uid and product.id = :pid", Wishlist.class);
        query.setParameter("uid", userId);
        query.setParameter("pid", productId);
        return query.uniqueResult(); // 找不到就返回 null
    }

    @Override
    public List<Wishlist> findByUser(User user) {
        // 使用 "fetch join" 一次性查出商品信息，防止N+1查询
        Query<Wishlist> query = getCurrentSession().createQuery(
                "from Wishlist w join fetch w.product where w.user.id = :uid", Wishlist.class);
        query.setParameter("uid", user.getId());
        return query.list();
    }
}