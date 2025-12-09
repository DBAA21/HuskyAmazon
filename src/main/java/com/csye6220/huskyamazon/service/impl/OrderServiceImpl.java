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

/**
 * 订单服务实现类
 * <p>
 * 负责处理订单相关的核心业务逻辑，包括订单创建、优惠券验证、
 * 库存扣减、订单查询和状态更新等功能
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
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

    /**
     * 创建订单（核心业务逻辑）
     * <p>
     * 完整的下单流程包括：
     * 1. 验证购物车不为空
     * 2. 计算原始总价
     * 3. 应用优惠券（二次验证有效性、最低消费金额）
     * 4. 创建订单对象，设置折后价格
     * 5. 扣减商品库存（原子操作，库存不足则抛异常回滚）
     * 6. 生成订单明细（记录购买时的价格）
     * 7. 保存订单并清空购物车
     * </p>
     *
     * @param user   下单用户
     * @param coupon 使用的优惠券（可为null）
     * @return 创建成功的订单对象
     * @throws RuntimeException 购物车为空、优惠券无效、库存不足时抛出异常
     * @apiNote 整个流程在一个事务中执行，任何步骤失败都会回滚，保证数据一致性
     */
    @Override
    @Transactional // 关键：整个下单流程必须在事务中，保证原子性（要么全部成功，要么全部回滚）
    public Order placeOrder(User user, Coupon coupon) {
        Cart cart = cartService.getCartByUser(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // ========== 第一步：计算原始总价 ==========
        double originalTotal = cart.getTotalAmount();
        double finalTotal = originalTotal;
        String statusMessage = "PLACED";

        // ========== 第二步：应用优惠券折扣（二次验证） ==========
        if (coupon != null) {
            // 再次验证有效性（防止Session中存储的优惠券已过期）
            if (!coupon.isValid()) {
                throw new RuntimeException("Coupon expired.");
            }
            // 验证是否满足最低消费金额
            if (coupon.getMinSpend() != null && originalTotal < coupon.getMinSpend()) {
                throw new RuntimeException("Did not meet minimum spend for coupon.");
            }

            // 计算折扣金额和最终价格
            double discountAmount = originalTotal * coupon.getDiscountPercent();
            finalTotal = originalTotal - discountAmount;

            // 在订单状态中记录使用的优惠券（后续可优化为关联字段）
            statusMessage = "PLACED (Coupon: " + coupon.getCode() + ")";
        }

        // ========== 第三步：创建订单对象 ==========
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(statusMessage);
        order.setTotalAmount(finalTotal); // 重要：存储的是优惠后的实际支付金额
        order.setOrderItems(new ArrayList<>());

        // ========== 第四步：扣减库存并生成订单明细 ==========
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            // 库存校验（防止超卖）
            if (product.getStock() < qty) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock.");
            }

            // 扣减库存（关键业务逻辑）
            product.setStock(product.getStock() - qty);
            productDAO.update(product);

            // 创建订单明细（记录购买时的价格，防止后续商品调价影响历史订单）
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            orderItem.setPriceAtPurchase(product.getPrice()); // 保存购买时的单价
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // ========== 第五步：保存订单并清空购物车 ==========
        orderDAO.save(order);
        cartService.clearCart(user); // 下单成功后清空购物车

        return order;
    }

    /**
     * 获取用户的订单历史
     *
     * @param user 查询的用户
     * @return 该用户的所有订单列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(User user) {
        return orderDAO.findByUser(user);
    }

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情对象（包含订单明细）
     */
    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId) {
        return orderDAO.findById(orderId);
    }

    /**
     * 获取所有订单（管理员功能）
     *
     * @return 系统中的所有订单列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    /**
     * 更新订单状态（管理员功能）
     * <p>
     * 用于订单流转管理，如：PLACED -> SHIPPED -> DELIVERED
     * </p>
     *
     * @param orderId 订单ID
     * @param status  新的订单状态
     */
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
