package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CouponDAO;
import com.csye6220.huskyamazon.entity.Coupon;
import com.csye6220.huskyamazon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * couponserviceimplementation class
 * <p>
 * responsible forHandlecoupon相关的Corebusiness logic，包括coupon管理、
 * couponValidate（valid期、最低消费门槛等）
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
     * Getallcouponcolumntable（administrator功能）
     *
     * @return allcoupon的columntable
     */
    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponDAO.findAll();
    }

    /**
     * savecoupon（New或Update）
     * <p>
     * Corebusiness logic：
     * automatic将coupon代码convert为大写（统一format，avoidsize写问题）
     * </p>
     *
     * @param coupon couponobject
     */
    @Override
    @Transactional
    public void saveCoupon(Coupon coupon) {
        // 业务规则：coupon代码统一convert为大写（便于Query和使用）
        if (coupon.getCode() != null) {
            coupon.setCode(coupon.getCode().toUpperCase());
        }
        couponDAO.save(coupon);
    }

    /**
     * Deletecoupon（administrator功能）
     *
     * @param id couponID
     */
    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        couponDAO.deleteById(id);
    }

    /**
     * Validate并Getvalid的coupon（Corebusiness logic）
     * <p>
     * complete的couponValidate流程：
     * 1. 根据coupon代码Findcoupon（automatic转大写）
     * 2. Checkcoupon是否存在
     * 3. Checkcoupon是否expired或已disable
     * 4. Checkorderamount是否满足最低消费门槛
     * 5. allValidatepass后returncouponobject
     * </p>
     *
     * @param code       coupon代码
     * @param orderTotal ordertotalamount
     * @return valid的couponobject
     * @throws RuntimeException coupondoesn't exist、已expired或不满足使用item件时throw exception
     * @apiNote 该method会在结账时调用，used forValidateuserinput的coupon是否available
     */
    @Override
    @Transactional(readOnly = true)
    public Coupon getValidCoupon(String code, double orderTotal) {
        // ========== 第一步：Findcoupon（统一转大写） ==========
        Coupon coupon = couponDAO.findByCode(code.toUpperCase());

        // ========== 第二步：基础Check（是否存在） ==========
        if (coupon == null) {
            throw new RuntimeException("Invalid coupon code.");
        }

        // ========== 第三步：valid性Check（是否expired或disable） ==========
        // 调用entity自身的isValid()method，Checkvalid期和是否enable
        if (!coupon.isValid()) {
            throw new RuntimeException("Coupon has expired.");
        }

        // ========== 第四步：最低消费门槛Check ==========
        if (coupon.getMinSpend() != null && orderTotal < coupon.getMinSpend()) {
            throw new RuntimeException("Order total must be at least $" + coupon.getMinSpend());
        }

        // allValidatepass，returnvalid的coupon
        return coupon;
    }
}
