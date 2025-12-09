package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CouponDAO;
import com.csye6220.huskyamazon.entity.Coupon;
import com.csye6220.huskyamazon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 优惠券服务实现类
 * <p>
 * 负责处理优惠券相关的核心业务逻辑，包括优惠券管理、
 * 优惠券验证（有效期、最低消费门槛等）
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
@Service
public class CouponServiceImpl implements CouponService {

    private final CouponDAO couponDAO;

    @Autowired
    public CouponServiceImpl(CouponDAO couponDAO) {
        this.couponDAO = couponDAO;
    }

    /**
     * 获取所有优惠券列表（管理员功能）
     *
     * @return 所有优惠券的列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponDAO.findAll();
    }

    /**
     * 保存优惠券（新增或更新）
     * <p>
     * 核心业务逻辑：
     * 自动将优惠券代码转换为大写（统一格式，避免大小写问题）
     * </p>
     *
     * @param coupon 优惠券对象
     */
    @Override
    @Transactional
    public void saveCoupon(Coupon coupon) {
        // 业务规则：优惠券代码统一转换为大写（便于查询和使用）
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase());
        }
        couponDAO.save(coupon);
    }

    /**
     * 删除优惠券（管理员功能）
     *
     * @param id 优惠券ID
     */
    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        couponDAO.deleteById(id);
    }

    /**
     * 验证并获取有效的优惠券（核心业务逻辑）
     * <p>
     * 完整的优惠券验证流程：
     * 1. 根据优惠券代码查找优惠券（自动转大写）
     * 2. 检查优惠券是否存在
     * 3. 检查优惠券是否过期或已停用
     * 4. 检查订单金额是否满足最低消费门槛
     * 5. 所有验证通过后返回优惠券对象
     * </p>
     *
     * @param code       优惠券代码
     * @param orderTotal 订单总金额
     * @return 有效的优惠券对象
     * @throws RuntimeException 优惠券不存在、已过期或不满足使用条件时抛出异常
     * @apiNote 该方法会在结账时调用，用于验证用户输入的优惠券是否可用
     */
    @Override
    @Transactional(readOnly = true)
    public Coupon getValidCoupon(String code, double orderTotal) {
        // ========== 第一步：查找优惠券（统一转大写） ==========
        Coupon coupon = couponDAO.findByCode(code.toUpperCase());

        // ========== 第二步：基础检查（是否存在） ==========
        if (coupon == null) {
            throw new RuntimeException("Invalid coupon code.");
        }

        // ========== 第三步：有效性检查（是否过期或停用） ==========
        // 调用实体自身的isValid()方法，检查有效期和是否启用
        if (!coupon.isValid()) {
            throw new RuntimeException("Coupon has expired.");
        }

        // ========== 第四步：最低消费门槛检查 ==========
        if (coupon.getMinSpend() != null && orderTotal < coupon.getMinSpend()) {
            throw new RuntimeException("Order total must be at least $" + coupon.getMinSpend());
        }

        // 所有验证通过，返回有效的优惠券
        return coupon;
    }
}
