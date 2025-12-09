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
        System.out.println("=========== 开始初始化测试数据 ===========");

        // 1. 初始化普通测试用户
        if (userService.findByUsername("testuser") == null) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword("Password123");
            testUser.setEmail("student@northeastern.edu");
            testUser.setRole("USER"); // 明确设置为普通用户
            userService.registerUser(testUser);
            System.out.println("✅ 测试用户创建成功: testuser");
        }

        // --- ⭐ 新增：初始化管理员用户 ---
        if (userService.findByUsername("admin") == null) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("AdminPass123"); // 强密码
            adminUser.setEmail("admin@northeastern.edu");
            adminUser.setRole("ADMIN"); // ⭐ 关键：设置为管理员
            userService.registerUser(adminUser);
            System.out.println("✅ 管理员用户创建成功: admin (密码: AdminPass123)");
        }
    }
}