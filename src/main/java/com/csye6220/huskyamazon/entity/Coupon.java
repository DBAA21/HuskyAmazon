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
    private String code; // Coupon code (e.g., SAVE20)

    @Column(nullable = false)
    private Double discountPercent; // Discount rate (e.g., 0.20 represents 20% off)

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate; // Expiry date

    private Double minSpend; // Minimum spend threshold (optional)

    private boolean isActive = true; // Active toggle

    // Check if valid
    public boolean isValid() {
        return isActive && (expiryDate == null || !LocalDate.now().isAfter(expiryDate));
    }
}
