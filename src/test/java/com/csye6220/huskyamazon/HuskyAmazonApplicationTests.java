package com.csye6220.huskyamazon;

import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.Category;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.ProductService;
import com.csye6220.huskyamazon.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 启动完整的 Spring 上下文，以便注入 Service 和 DAO
class HuskyAmazonApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 测试核心流程：用户注册 -> 自动分配购物车 -> 创建分类 -> 创建商品
     * 使用 @Transactional 后，测试结束时数据会自动回滚（Rollback），
     * 这样不会弄脏你的数据库，你可以无限次运行这个测试！
     */
    @Test
    void testBackendLogic() {
        // --- 1. 测试用户注册与购物车级联 ---
        System.out.println(">>> 开始测试用户注册...");
        String username = "test_junit_user";

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword("pass123");
        newUser.setEmail("junit@neu.edu");

        // 执行注册
        userService.registerUser(newUser);

        // 验证：从数据库查出来
        User retrievedUser = userService.findByUsername(username);

        // 断言：用户不能为空
        assertNotNull(retrievedUser, "用户应该被保存到数据库");
        // 断言：购物车不能为空 (级联创建验证)
        assertNotNull(retrievedUser.getCart(), "注册用户时应该自动创建一个购物车");
        // 断言：购物车的所有者应该是当前用户 (双向关联验证)
        assertEquals(retrievedUser.getId(), retrievedUser.getCart().getUser().getId(), "购物车应该关联到正确的用户");

        System.out.println("✅ 用户与购物车测试通过！");

        // --- 2. 测试商品分类与商品创建 ---
        System.out.println(">>> 开始测试商品逻辑...");

        // 创建分类
        Category category = new Category();
        category.setName("JUnit Electronics");
        category.setDescription("Electronics for testing");
        productService.addCategory(category);

        assertNotNull(category.getId(), "保存后分类ID不应为空");

        // 创建商品
        Product product = new Product();
        product.setName("Test Laptop");
        product.setDescription("High performance");
        product.setPrice(1200.00);
        product.setStock(5);

        // 将商品加入分类
        productService.addProduct(product, category.getId());

        // 验证：查出该分类下的商品
        List<Product> products = Collections.singletonList(productService.getProductById(category.getId()));

        // 断言：应该能查到刚才添加的商品
        assertFalse(products.isEmpty(), "该分类下应该有商品");
        assertEquals("Test Laptop", products.get(0).getName(), "商品名称应该匹配");

        System.out.println("✅ 商品与分类测试通过！");
    }
}