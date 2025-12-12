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
    private int rating; // 1-5 stars

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Relationship: one review belongs to one user
    @ManyToOne(fetch = FetchType.EAGER) // When querying reviews always want to know who wrote it
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relationship: one review belongs to one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}
