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

/**
 * 购物车服务实现类
 * <p>
 * 负责处理购物车相关的核心业务逻辑，包括购物车查询、商品添加/删除/修改、
 * 总价自动计算等功能
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    @Autowired
    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    /**
     * 获取用户的购物车
     * <p>
     * 核心业务逻辑：
     * 1. 如果用户没有购物车，自动创建一个空购物车
     * 2. 强制初始化购物车商品集合（解决Hibernate懒加载问题）
     * 3. 每次查询时重新计算总价（确保数据准确性）
     * </p>
     *
     * @param user 查询的用户
     * @return 用户的购物车对象（包含商品列表和总价）
     * @apiNote 即使数据库中的总价不准确，也会自动重新计算并修正
     */
    @Override
    @Transactional
    public Cart getCartByUser(User user) {
        Cart cart = cartDAO.findByUser(user);
        
        // 如果用户还没有购物车，自动创建一个
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(0.0);
            cartDAO.save(cart);
        }

        // 强制初始化items集合（触发Hibernate加载，避免懒加载异常）
        if (cart.getItems() != null) {
            cart.getItems().size();
        }

        // 关键修复：每次查询购物车时，强制重新计算总价
        // 这样即使数据库中的total_amount不准确，也会自动修正
        recalculateTotal(cart);

        return cart;
    }

    /**
     * 添加商品到购物车
     * <p>
     * 核心业务逻辑：
     * 1. 验证商品是否存在
     * 2. 检查购物车中是否已有该商品
     * 3. 如果已存在，增加数量；否则新建购物车项
     * 4. 重新计算购物车总价
     * </p>
     *
     * @param user      当前用户
     * @param productId 要添加的商品ID
     * @param quantity  添加的数量
     * @throws RuntimeException 商品不存在时抛出异常
     */
    @Override
    @Transactional
    public void addItemToCart(User user, Long productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productDAO.findById(productId);

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // 检查购物车中是否已经有这个商品
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // 商品已存在，增加数量
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // 商品不存在，创建新的购物车项
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // 重新计算总价
        recalculateTotal(cart);
        cartDAO.update(cart);
    }

    /**
     * 从购物车中移除商品
     *
     * @param user      当前用户
     * @param productId 要移除的商品ID
     */
    @Override
    @Transactional
    public void removeItemFromCart(User user, Long productId) {
        Cart cart = getCartByUser(user);
        
        // 从购物车列表中删除指定商品
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        if (removed) {
            // 删除成功后重新计算总价
            recalculateTotal(cart);
            cartDAO.update(cart);
        }
    }

    /**
     * 更新购物车中商品的数量
     * <p>
     * 核心业务逻辑：
     * 1. 如果数量小于等于0，直接删除该商品
     * 2. 否则更新商品数量并重新计算总价
     * </p>
     *
     * @param user      当前用户
     * @param productId 要更新的商品ID
     * @param quantity  新的数量
     */
    @Override
    @Transactional
    public void updateItemQuantity(User user, Long productId, int quantity) {
        // 数量小于等于0时，直接删除该商品
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
            // 重新计算总价
            recalculateTotal(cart);
            cartDAO.update(cart);
        }
    }

    /**
     * 清空购物车
     * <p>
     * 删除购物车中的所有商品，并将总价重置为0
     * </p>
     *
     * @param user 当前用户
     */
    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cartDAO.update(cart);
    }

    /**
     * 重新计算购物车总金额（私有辅助方法）
     * <p>
     * 遍历购物车中的所有商品，计算 单价 × 数量 的总和
     * 并更新购物车的totalAmount字段
     * </p>
     *
     * @param cart 要计算的购物车对象
     */
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
