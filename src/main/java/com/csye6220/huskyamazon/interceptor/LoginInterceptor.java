package com.csye6220.huskyamazon.interceptor;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Collections;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final UserService userService;


    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // 1. 尝试从 Cookie 自动登录 (Remember Me 逻辑)
        if (currentUser == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("HUSKY_AUTH_TOKEN".equals(cookie.getName())) {
                        User user = userService.findByLoginToken(cookie.getValue());
                        if (user != null) {
                            session.setAttribute("currentUser", user);
                            currentUser = user; // 更新当前用户变量

                            // 同步到 Spring Security，构造认证信息并放入上下文
                            String role = "ROLE_" + (user.getRole() != null ? user.getRole() : "USER");
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getUsername(),
                                            null,
                                            Collections.singletonList(new SimpleGrantedAuthority(role))
                                    );

                            SecurityContext context = new SecurityContextImpl(authentication);
                            SecurityContextHolder.setContext(context);
                            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
                        }
                        break;
                    }
                }
            }
        }

        // --- ⭐ 新增：后台权限检查 ---
        String requestURI = request.getRequestURI();

        // 如果请求的是后台页面 (/admin/...)
        if (requestURI.startsWith("/admin")) {
            // 1. 没登录 -> 踢回登录页
            if (currentUser == null) {
                response.sendRedirect("/login");
                return false;
            }
            // 2. 登录了但不是管理员 -> 踢回首页 (或者显示403页面)
            if (!"ADMIN".equals(currentUser.getRole())) {
                System.out.println("⚠️ 警告: 用户 " + currentUser.getUsername() + " 试图非法访问后台!");
                response.sendRedirect("/"); // 踢回首页
                return false;
            }
        }

        return true; // 放行其他请求
    }
}