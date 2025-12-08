package com.csye6220.huskyamazon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // 优惠码 (例如: SAVE20)

    @Column(nullable = false)
    private Double discountPercent; // 折扣力度 (例如: 0.20 代表打8折)

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate; // 过期时间

    private Double minSpend; // 最低消费门槛 (可选)

    private boolean isActive = true; // 开关

    // 判断是否有效
    public boolean isValid() {
        return isActive && (expiryDate == null || !LocalDate.now().isAfter(expiryDate));
    }
}