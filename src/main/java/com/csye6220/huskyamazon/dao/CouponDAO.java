package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Coupon;
import java.util.List;

public interface CouponDAO {
    void save(Coupon coupon);
    void deleteById(Long id);
    Coupon findById(Long id);
    Coupon findByCode(String code);
    List<Coupon> findAll();
}