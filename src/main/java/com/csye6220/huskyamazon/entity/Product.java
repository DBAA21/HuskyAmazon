package com.csye6220.huskyamazon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT") // 允许存储长文本
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "original_price")
    private Double originalPrice; // 注意是 Double (nullable)

    @Column(nullable = false)
    private Integer stock; // 库存

    private String imageUrl; // 存储图片链接

    // 多个商品属于一个分类
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    // 一个商品下的所有评论
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Review> reviews = new ArrayList<>();
}