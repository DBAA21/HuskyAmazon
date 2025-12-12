package com.csye6220.huskyamazon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // ... (username, password remain unchanged) ...
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{2,19}$", message = "Username format invalid")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password too weak")
    private String password;

    // ... (email remain unchanged) ...
    @Column(nullable = false)
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email format invalid")
    private String email;

    @Column(name = "login_token")
    private String loginToken;

    // --- ⭐ New: Role field ---
    // Default is "USER", only specific accounts are "ADMIN"
    @Column(nullable = false)
    private String role = "USER";

    // ... (Relationships: cart, reviews, wishlist, addresses remain unchanged) ...
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Wishlist> wishlistItems = new ArrayList<>();

    // (If you previously added Address relationship, keep it, don't delete)
}
