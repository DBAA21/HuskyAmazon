package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CouponDAO;
import com.csye6220.huskyamazon.entity.Coupon;
import com.csye6220.huskyamazon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponDAO couponDAO;

    @Autowired
    public CouponServiceImpl(CouponDAO couponDAO) {
        this.couponDAO = couponDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponDAO.findAll();
    }

    @Override
    @Transactional
    public void saveCoupon(Coupon coupon) {
        // 这里可以加一些逻辑，比如把 code 转大写
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase());
        }
        couponDAO.save(coupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        couponDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getValidCoupon(String code, double orderTotal) {
        // 1. 查找优惠券
        Coupon coupon = couponDAO.findByCode(code.toUpperCase());

        // 2. 基础检查
        if (coupon == null) {
            throw new RuntimeException("Invalid coupon code.");
        }

        // 3. 检查是否过期或停用 (调用实体自身的 isValid 方法)
        if (!coupon.isValid()) {
            throw new RuntimeException("Coupon has expired.");
        }

        // 4. 检查最低消费门槛
        if (coupon.getMinSpend() != null && orderTotal < coupon.getMinSpend()) {
            throw new RuntimeException("Order total must be at least $" + coupon.getMinSpend());
        }

        return coupon;
    }
}