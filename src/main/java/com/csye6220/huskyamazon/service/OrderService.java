package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface OrderService {
    Order placeOrder(User user); // 结账核心方法
    List<Order> getOrderHistory(User user);
    Order getOrderDetails(Long orderId);
    List<Order> getAllOrders(); // 新增
    void updateOrderStatus(Long orderId, String status);
}