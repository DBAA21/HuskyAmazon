package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.entity.Order;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // 从配置文件读取发送者邮箱
    @Value("${spring.mail.username:noreply@huskyamazon.com}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async // 异步执行，防止发邮件卡住主线程
    public void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Welcome to Husky Amazon!");
        message.setText("Dear " + user.getUsername() + ",\n\n" +
                "Thank you for registering with Husky Amazon. We are excited to have you on board!\n\n" +
                "Best Regards,\nThe Husky Team");

        try {
            mailSender.send(message);
            System.out.println("✅ 欢迎邮件已发送给: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ 邮件发送失败: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOrderConfirmation(User user, Order order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Order Confirmation - Order #" + order.getId());
        message.setText("Dear " + user.getUsername() + ",\n\n" +
                "Your order has been placed successfully!\n" +
                "Order ID: " + order.getId() + "\n" +
                "Total Amount: $" + order.getTotalAmount() + "\n\n" +
                "We will notify you when your items are shipped.\n\n" +
                "Thanks for shopping with us!");

        try {
            mailSender.send(message);
            System.out.println("✅ 订单确认邮件已发送给: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ 邮件发送失败: " + e.getMessage());
        }
    }
}