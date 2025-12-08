package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Coupon;
import java.util.List;

public interface CouponService {
    List<Coupon> getAllCoupons();
    void saveCoupon(Coupon coupon);
    void deleteCoupon(Long id);

    /**
     * 验证并获取优惠券
     * @param code 优惠码
     * @param orderTotal 订单总金额 (用于检查最低消费)
     * @return 有效的 Coupon 对象，如果无效抛出异常
     */
    Coupon getValidCoupon(String code, double orderTotal);
}