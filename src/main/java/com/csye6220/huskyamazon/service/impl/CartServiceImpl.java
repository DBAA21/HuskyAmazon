package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CartDAO;
import com.csye6220.huskyamazon.dao.CartItemDAO;
import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.CartItem;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CartService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;

    @Autowired
    public CartServiceImpl(CartDAO cartDAO, CartItemDAO cartItemDAO, ProductDAO productDAO, UserDAO userDAO) {
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
    }

    @Override
    @Transactional
    public Cart getCartByUser(User user) {
        User managedUser = userDAO.findById(user.getId());
        Cart cart = managedUser.getCart();
        Hibernate.initialize(cart.getItems());
        return cart;
    }

    @Override
    @Transactional
    public void addToCart(User user, Long productId, int quantity) {
        User managedUser = userDAO.findById(user.getId());
        Cart cart = managedUser.getCart();
        Product product = productDAO.findById(productId);

        // ⭐ 库存检查逻辑开始
        int currentInCart = 0;
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            currentInCart = existingItem.get().getQuantity();
        }

        // 如果 (购物车已有的 + 新加的) > 库存，抛出异常
        if (currentInCart + quantity > product.getStock()) {
            throw new RuntimeException("Insufficient stock! Only " + product.getStock() + " left.");
        }
        // ⭐ 库存检查逻辑结束

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setCart(cart);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        updateCartTotal(cart);
        cartDAO.update(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        User managedUser = userDAO.findById(user.getId());
        Cart cart = managedUser.getCart();

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElse(null);

        if (itemToRemove != null) {
            cart.getItems().remove(itemToRemove);
            itemToRemove.setCart(null);
            updateCartTotal(cart);
            cartDAO.update(cart);
            cartItemDAO.delete(itemToRemove);
        }
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        User managedUser = userDAO.findById(user.getId());
        Cart cart = managedUser.getCart();
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cartDAO.update(cart);
    }

    private void updateCartTotal(Cart cart) {
        double total = 0.0;
        for (CartItem item : cart.getItems()) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        cart.setTotalAmount(total);
    }
}