package com.csye6220.huskyamazon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating; // 1-5 星

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 关系：一个评论 属于 一个用户
    @ManyToOne(fetch = FetchType.EAGER) // 查评论时总想知道是谁写的
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 关系：一个评论 属于 一个商品
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}