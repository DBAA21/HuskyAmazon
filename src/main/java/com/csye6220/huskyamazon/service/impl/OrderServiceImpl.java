package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.OrderDAO;
import com.csye6220.huskyamazon.entity.*;
import com.csye6220.huskyamazon.service.CartService;
import com.csye6220.huskyamazon.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartService cartService;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO, CartService cartService) {
        this.orderDAO = orderDAO;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public Order placeOrder(User user) {
        // 1. 获取用户当前的购物车
        Cart cart = cartService.getCartByUser(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. 创建新订单对象
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setTotalAmount(cart.getTotalAmount());
        order.setOrderItems(new ArrayList<>()); // 初始化列表

        // 3. 核心转换：CartItem -> OrderItem
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();

            // 复制商品信息
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            // ⚠️关键：锁定当前价格 (Snapshot Price)
            // 防止以后商品涨价影响历史订单金额
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());

            // 建立双向关联
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // 4. 保存订单
        // 因为 Order 设了 CascadeType.ALL，所以 OrderItems 会被自动保存
        orderDAO.save(order);

        // 5. 清空购物车
        cartService.clearCart(user);

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(User user) {
        return orderDAO.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId) {
        return orderDAO.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderDAO.findById(orderId);
        if (order != null) {
            order.setStatus(status);
            orderDAO.update(order);
        }
    }
}