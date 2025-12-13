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
 * orderserviceimplementation class
 * <p>
 * responsible forHandleorder相关的Corebusiness logic，包括orderCreate、couponValidate、
 * stock扣减、orderQuery和stateUpdate等功能
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
     * Createorder（Corebusiness logic）
     * <p>
     * complete的下单流程包括：
     * 1. Validatecart不为空
     * 2. calculateoriginaltotal价
     * 3. 应用coupon（二次Validatevalid性、最低消费amount）
     * 4. Createorderobject，settings折后price
     * 5. 扣减productstock（atomicOperation，stock不足则抛exceptionrollback）
     * 6. generateorder明细（record购买时的price）
     * 7. saveorder并Clearcart
     * </p>
     *
     * @param user   下单user
     * @param coupon 使用的coupon（可为null）
     * @return Createsuccessful的orderobject
     * @throws RuntimeException cart为空、couponinvalid、stock不足时throw exception
     * @apiNote entire流程在一个transaction中Execute，任何步骤fail都会rollback，guarantee数据consistent性
     */
    @Override
    @Transactional // Key：entire下单流程must在transaction中，guaranteeatomic性（要么allsuccessful，要么allrollback）
    public Order placeOrder(User user, Coupon coupon) {
        Cart cart = cartService.getCartByUser(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // ========== 第一步：calculateoriginaltotal价 ==========
        double originalTotal = cart.getTotalAmount();
        double finalTotal = originalTotal;
        String statusMessage = "PLACED";

        // ========== 第二步：应用coupondiscount（二次Validate） ==========
        if (coupon != null) {
            // 再次Validatevalid性（preventSession中store的coupon已expired）
            if (!coupon.isValid()) {
                throw new RuntimeException("Coupon expired.");
            }
            // Validate是否满足最低消费amount
            if (coupon.getMinSpend() != null && originalTotal < coupon.getMinSpend()) {
                throw new RuntimeException("Did not meet minimum spend for coupon.");
            }

            // calculatediscountamount和finalprice
            double discountAmount = originalTotal * coupon.getDiscountPercent();
            finalTotal = originalTotal - discountAmount;

            // 在orderstate中record使用的coupon（后续可optimization为associationfield）
            statusMessage = "PLACED (Coupon: " + coupon.getCode() + ")";
        }

        // ========== 第三步：Createorderobject ==========
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(statusMessage);
        order.setTotalAmount(finalTotal); // Important：store的是discount后的actual支付amount
        order.setOrderItems(new ArrayList<>());

        // ========== 第四步：扣减stock并generateorder明细 ==========
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            // stockchecksum（prevent超卖）
            if (product.getStock() < qty) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock.");
            }

            // 扣减stock（Keybusiness logic）
            product.setStock(product.getStock() - qty);
            productDAO.update(product);

            // Createorder明细（record购买时的price，prevent后续product调价影响历史order）
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            orderItem.setPriceAtPurchase(product.getPrice()); // save购买时的unit price
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // ========== 第五步：saveorder并Clearcart ==========
        orderDAO.save(order);
        cartService.clearCart(user); // 下单successful后Clearcart

        return order;
    }

    /**
     * Getuser的order历史
     *
     * @param user Query的user
     * @return 该user的allordercolumntable
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(User user) {
        return orderDAO.findByUser(user);
    }

    /**
     * Getorder详情
     *
     * @param orderId orderID
     * @return order详情object（includeorder明细）
     */
    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId) {
        return orderDAO.findById(orderId);
    }

    /**
     * Getallorder（administrator功能）
     *
     * @return 系统中的allordercolumntable
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    /**
     * Updateorderstate（administrator功能）
     * <p>
     * used fororder流转管理，如：PLACED -> SHIPPED -> DELIVERED
     * </p>
     *
     * @param orderId orderID
     * @param status  新的orderstate
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
