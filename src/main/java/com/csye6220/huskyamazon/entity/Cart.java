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

    // Cart total amount
    // Note: This field's value is updated by CartServiceImpl.recalculateTotal()
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    // --- Relationship Mappings ---

    // Associate with user (one-to-one)
    // @JoinColumn indicates there's a "user_id" column in the carts table
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude // Prevent Lombok toString() infinite loop
    private User user;

    // Associate with cart items (one-to-many)
    // mappedBy = "cart" indicates there's a field called "cart" in CartItem class that maintains the relationship
    // cascade = ALL: When cart is deleted, items inside are also deleted
    // orphanRemoval = true: When item is removed from list, it's also deleted from database
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CartItem> items = new ArrayList<>();
}
