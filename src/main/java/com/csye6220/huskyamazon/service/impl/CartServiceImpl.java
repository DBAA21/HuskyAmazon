package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CartDAO;
import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.CartItem;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    @Autowired
    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    @Transactional
    public Cart getCartByUser(User user) {
        Cart cart = cartDAO.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(0.0);
            cartDAO.save(cart);
        }

        // 强制初始化 items 集合
        if (cart.getItems() != null) {
            cart.getItems().size();
        }

        // ⭐⭐ 关键修复：每次查看购物车时，强制重新计算总价 ⭐⭐
        // 这样即使数据库里的 total_amount 是错的，页面显示也会自动变对，并且会自动修正数据库
        recalculateTotal(cart);

        return cart;
    }

    @Override
    @Transactional
    public void addItemToCart(User user, Long productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productDAO.findById(productId);

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        recalculateTotal(cart);
        cartDAO.update(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(User user, Long productId) {
        Cart cart = getCartByUser(user);
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        if (removed) {
            recalculateTotal(cart);
            cartDAO.update(cart);
        }
    }

    @Override
    @Transactional
    public void updateItemQuantity(User user, Long productId, int quantity) {
        if (quantity <= 0) {
            removeItemFromCart(user, productId);
            return;
        }

        Cart cart = getCartByUser(user);
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(quantity);
            recalculateTotal(cart);
            cartDAO.update(cart);
        }
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cartDAO.update(cart);
    }

    // 辅助方法：计算购物车总金额
    private void recalculateTotal(Cart cart) {
        double total = 0.0;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        cart.setTotalAmount(total);
    }
}