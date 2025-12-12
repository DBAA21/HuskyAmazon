package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User service implementation class
 * <p>
 * Responsible for handling core business logic related to users, including user registration,
 * login validation, auto-login (remember me feature), password modification, etc.
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * User registration
     * <p>
     * Core business logic:
     * 1. Automatically create an empty shopping cart for new users
     * 2. Establish bidirectional association between user and cart
     * 3. Use JPA cascade save feature to save user and cart in one operation
     * </p>
     *
     * @param user User object to be registered (containing username, password, email, etc.)
     * @throws org.springframework.dao.DataIntegrityViolationException If username already exists
     */
    @Override
    @Transactional // Transaction guarantee: user and cart are either both saved successfully or both rolled back
    public void registerUser(User user) {
        // Business logic: Each new user is automatically assigned an empty shopping cart
        Cart cart = new Cart();
        cart.setUser(user); // Cart associated with user
        user.setCart(cart); // User associated with cart (bidirectional association)

        // Because User entity is configured with CascadeType.ALL, only need to save User, Cart will be cascaded automatically
        userDAO.save(user);
    }

    /**
     * User login validation
     * <p>
     * Validate if username and password match
     * </p>
     *
     * @param username Username
     * @param password Password (plain text)
     * @return Returns user object if validation succeeds, null if fails
     * @apiNote Production environment should use BCrypt or similar encryption algorithms for password hash comparison, simplified to plain text comparison here
     */
    @Override
    @Transactional(readOnly = true)
    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // Note: In actual projects, passwords should use BCrypt hash comparison, simplified to plain text comparison here
            return user;
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    /**
     * Update user's auto-login token
     * <p>
     * Used to implement "remember me" feature, saves generated token to database,
     * and the token is also stored in browser Cookie for auto-login validation
     * </p>
     *
     * @param user  User object
     * @param token Generated unique token (usually UUID)
     */
    @Override
    @Transactional
    public void updateLoginToken(User user, String token) {
        // First query the persisted User object (ensure within Hibernate Session management)
        User managedUser = userDAO.findById(user.getId());
        if (managedUser != null) {
            managedUser.setLoginToken(token);
            userDAO.update(managedUser);
        }
    }

    /**
     * Find user by auto-login token
     * <p>
     * Used for "remember me" feature's auto-login, validates user identity through token in Cookie
     * </p>
     *
     * @param token Auto-login token
     * @return Matching user object, returns null if not found
     */
    @Override
    @Transactional(readOnly = true)
    public User findByLoginToken(String token) {
        return userDAO.findByLoginToken(token);
    }

    /**
     * Find user by user ID
     *
     * @param id User ID
     * @return User object, returns null if not found
     */
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userDAO.findById(id);
    }

    /**
     * Get all users list
     * <p>
     * Mainly used for admin backend to view all users
     * </p>
     *
     * @return List of all users
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * Update user information (admin function)
     * <p>
     * Core business logic:
     * 1. First query latest user data from database (avoid overwriting other fields)
     * 2. Only update allowed fields (email, role)
     * 3. Sensitive fields like password, relationships remain unchanged
     * </p>
     *
     * @param user User object containing information to be updated
     */
    @Override
    @Transactional
    public void updateUser(User user) {
        // Key logic: First query persisted object from database, avoid direct update causing data loss
        User existingUser = userDAO.findById(user.getId());
        if (existingUser != null) {
            // Only update allowed fields (whitelist strategy)
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());

            // Note: Password, cart, orders and other relationship info remain unchanged, prevent accidental modification

            userDAO.update(existingUser);
        }
    }

    /**
     * Change user password
     * <p>
     * Core business logic:
     * 1. Get latest user info from database (prevent using stale data)
     * 2. Validate if old password is correct (security check)
     * 3. Only allow update to new password if old password is correct
     * </p>
     *
     * @param user        Current user object
     * @param oldPassword Old password (for validation)
     * @param newPassword New password
     * @return true-modification successful, false-old password incorrect or user doesn't exist
     */
    @Override
    @Transactional
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        // 1. Get latest user info from database (ensure data consistency)
        User currentUser = userDAO.findById(user.getId());

        if (currentUser != null) {
            // 2. Validate if old password is correct (security check, prevent unauthorized modification)
            if (currentUser.getPassword().equals(oldPassword)) {
                // 3. Update to new password
                currentUser.setPassword(newPassword);
                userDAO.update(currentUser);
                return true; // Modification successful
            }
        }
        return false; // Old password incorrect or user doesn't exist
    }
}
