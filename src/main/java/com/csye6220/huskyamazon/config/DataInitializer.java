package com.csye6220.huskyamazon.config;

// ... (imports)
import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.ProductService;
import com.csye6220.huskyamazon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public DataInitializer(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========== Starting to initialize test data ===========");

        // 1. Initialize普通testuser
        if (userService.findByUsername("testuser") == null) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword("Password123");
            testUser.setEmail("student@northeastern.edu");
            testUser.setRole("USER"); // Explicitly set as a regular user.
            userService.registerUser(testUser);
            System.out.println("✅ Test user created successfully: testuser");
        }

        // --- Initialize the administrator user. ---
        if (userService.findByUsername("admin") == null) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("AdminPass123");
            adminUser.setEmail("admin@northeastern.edu");
            adminUser.setRole("ADMIN");
            userService.registerUser(adminUser);
            System.out.println("✅ administratoruserCreatesuccessful: admin (password: AdminPass123)");
        }
    }
}