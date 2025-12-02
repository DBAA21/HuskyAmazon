package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;

public interface EmailService {
    void sendWelcomeEmail(User user);
    void sendOrderConfirmation(User user, Order order);
}