package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Coupon;
import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface OrderService {
    // ⭐ 修改签名：增加 Coupon 参数 (可以为 null)
    Order placeOrder(User user, Coupon coupon);

    List<Order> getOrderHistory(User user);
    Order getOrderDetails(Long orderId);
    List<Order> getAllOrders();
    void updateOrderStatus(Long orderId, String status);
}