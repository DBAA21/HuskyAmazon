package com.csye6220.huskyamazon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

// 这是一个 "Join Table" 实体，用于 N:M 关系
@Entity
@Table(name = "wishlist", uniqueConstraints = {
        // 确保一个用户对一个商品只能收藏一次
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
@Data
@NoArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关系：属于哪个用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 关系：收藏了哪个商品
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Wishlist(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}