package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Coupon;
import java.util.List;

public interface CouponService {
    List<Coupon> getAllCoupons();
    void saveCoupon(Coupon coupon);
    void deleteCoupon(Long id);

    /**
     * Validate and get coupon
     * @param code Coupon code
     * @param orderTotal Order total amount (used to check minimum spend)
     * @return Valid Coupon object, throws exception if invalid
     */
    Coupon getValidCoupon(String code, double orderTotal);
}
