package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // 核心：触发验证
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // 核心：接收错误结果
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- 注册页面 ---
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        // 关键：必须放入一个空的 User 对象，否则前端 th:object 会报错
        model.addAttribute("user", new User());
        return "register";
    }

    // --- 处理注册提交 ---
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result, // ⚠️注意：BindingResult 必须紧跟在 @Valid 对象后面
                               Model model) {

        // 1. 检查 @Pattern 定义的格式错误
        if (result.hasErrors()) {
            // 如果有错，Spring 会自动把错误信息带回 register.html 显示
            return "register";
        }

        // 2. 检查业务逻辑错误 (数据库查重)
        if (userService.findByUsername(user.getUsername()) != null) {
            // 手动向 "username" 字段添加一个错误
            result.rejectValue("username", "error.user", "Username already exists!");
            return "register";
        }

        // 3. 一切正常，执行注册
        userService.registerUser(user);
        return "redirect:/login?success";
    }

    // --- 登录页面 ---
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // --- 处理登录提交 ---
    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam(name = "remember-me", required = false) boolean rememberMe, // 与前端name保持一致
                            HttpSession session,
                            HttpServletResponse response, // 关键：用来发 Cookie
                            Model model) {

        User user = userService.login(username, password);

        if (user != null) {
            session.setAttribute("currentUser", user);

            // --- 自动登录逻辑（记住我）开始 ---
            if (rememberMe) {
                // 生成一个随机 Token (UUID)
                String token = UUID.randomUUID().toString();

                // 1. 存入数据库
                userService.updateLoginToken(user, token);

                // 2. 存入浏览器 Cookie
                Cookie cookie = new Cookie("HUSKY_AUTH_TOKEN", token);
                cookie.setMaxAge(24 * 60 * 60); // 有效期 24 小时 (秒为单位)
                cookie.setPath("/"); // 整个网站有效
                cookie.setHttpOnly(true); // 安全设置：防止脚本偷取 Cookie
                response.addCookie(cookie);
            }
            // --- 自动登录逻辑（记住我）结束 ---

            return "redirect:/";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    // --- 注销 ---
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // 1. 销毁 Session
        session.invalidate();

        // 2. 清除 Cookie (设置同名 Cookie，有效期为 0)
        Cookie cookie = new Cookie("HUSKY_AUTH_TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/";
    }
}