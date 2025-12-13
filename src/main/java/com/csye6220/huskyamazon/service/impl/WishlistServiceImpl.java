package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.dao.WishlistDAO;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.Wishlist;
import com.csye6220.huskyamazon.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;

    @Autowired
    public WishlistServiceImpl(WishlistDAO wishlistDAO, ProductDAO productDAO) {
        this.wishlistDAO = wishlistDAO;
        this.productDAO = productDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(User user, Long productId) {
        if (user == null) return false;
        return wishlistDAO.findByUserAndProduct(user.getId(), productId) != null;
    }

    @Override
    @Transactional
    public void toggleFavorite(User user, Long productId) {
        Wishlist item = wishlistDAO.findByUserAndProduct(user.getId(), productId);

        if (item == null) {
            // doesn't exist -> Addfavorite
            Product product = productDAO.findById(productId);
            if (product == null) throw new RuntimeException("Product not found");
            wishlistDAO.save(new Wishlist(user, product));
        } else {
            // already exists -> cancelfavorite
            wishlistDAO.delete(item);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistForUser(User user) {
        return wishlistDAO.findByUser(user);
    }

    /**
     * ⭐ Checkproduct是否在user的心愿单中
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(User user, Product product) {
        if (user == null || product == null) {
            return false;
        }

        List<Wishlist> userWishlist = wishlistDAO.findByUser(user);

        return userWishlist.stream()
                .anyMatch(w -> w.getProduct().getId().equals(product.getId()));
    }
}