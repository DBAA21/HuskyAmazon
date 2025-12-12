package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Coupon;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface OrderService {
    // ⭐ Modified signature: Added Coupon parameter (can be null)
    Order placeOrder(User user, Coupon coupon);

    List<Order> getOrderHistory(User user);
    Order getOrderDetails(Long orderId);
    List<Order> getAllOrders();
    void updateOrderStatus(Long orderId, String status);
}
