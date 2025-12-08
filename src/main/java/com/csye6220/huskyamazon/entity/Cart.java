package com.csye6220.huskyamazon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

    // 购物车总金额
    // 注意：这个字段的值由 CartServiceImpl.recalculateTotal() 负责更新
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    // --- 关系映射 ---

    // 关联用户 (一对一)
    // @JoinColumn 表示 carts 表里有一列 "user_id"
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude // 防止 Lombok toString() 死循环
    private User user;

    // 关联购物车条目 (一对多)
    // mappedBy = "cart" 表示 CartItem 类里有一个叫 "cart" 的字段负责维护关系
    // cascade = ALL: 删除购物车时，里面的条目也一起删
    // orphanRemoval = true: 从列表里移除条目时，数据库里也删掉
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CartItem> items = new ArrayList<>();
}