package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface OrderDAO {
    void save(Order order);
    Order findById(Long id);
    List<Order> findByUser(User user);
    List<Order> findAll(); // 新增
    void update(Order order);
}