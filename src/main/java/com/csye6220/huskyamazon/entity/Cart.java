package com.csye6220.huskyamazon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    // 购物车总金额 (简单起见先放这里，实际可能由 CartItem 计算得出)
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @jakarta.persistence.OneToMany(mappedBy = "cart", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private java.util.List<CartItem> items = new java.util.ArrayList<>();

    // --- 关系映射 ---

    // @JoinColumn 表示 carts 表里会多一列 "user_id" 作为外键
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude // 防止 toString() 死循环
    private User user;
}