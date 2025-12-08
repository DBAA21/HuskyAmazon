package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.OrderDAO;
import com.csye6220.huskyamazon.dao.ProductDAO;
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
    private final ProductDAO productDAO;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO, CartService cartService, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartService = cartService;
        this.productDAO = productDAO;
    }

    // ⭐ 完美实现：在 Service 层处理折扣逻辑
    @Override
    @Transactional
    public Order placeOrder(User user, Coupon coupon) {
        Cart cart = cartService.getCartByUser(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 1. 计算原始总价
        double originalTotal = cart.getTotalAmount();
        double finalTotal = originalTotal;
        String statusMessage = "PLACED";

        // 2. 应用优惠券逻辑 (Double Check)
        if (coupon != null) {
            // 再次验证有效性 (防止 Session 里存的是过期的)
            if (!coupon.isValid()) {
                throw new RuntimeException("Coupon expired.");
            }
            if (coupon.getMinSpend() != null && originalTotal < coupon.getMinSpend()) {
                throw new RuntimeException("Did not meet minimum spend for coupon.");
            }

            // 计算折扣
            double discountAmount = originalTotal * coupon.getDiscountPercent();
            finalTotal = originalTotal - discountAmount;

            // 记录一下用了什么券 (追加在状态里，或者以后 Order 表加个 coupon_id 字段)
            statusMessage = "PLACED (Coupon: " + coupon.getCode() + ")";
        }

        // 3. 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(statusMessage);
        order.setTotalAmount(finalTotal); // ⭐ 存入的是打折后的价格
        order.setOrderItems(new ArrayList<>());

        // 4. 扣减库存并保存明细
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            if (product.getStock() < qty) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock.");
            }

            // 扣库存
            product.setStock(product.getStock() - qty);
            productDAO.update(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // 5. 保存并清空购物车
        orderDAO.save(order);
        cartService.clearCart(user);

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(User user) { return orderDAO.findByUser(user); }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId) { return orderDAO.findById(orderId); }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() { return orderDAO.findAll(); }

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