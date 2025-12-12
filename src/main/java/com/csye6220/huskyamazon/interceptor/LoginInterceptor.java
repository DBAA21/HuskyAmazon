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

        // 1. Try to auto-login from Cookie (Remember Me logic)
        if (currentUser == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("HUSKY_AUTH_TOKEN".equals(cookie.getName())) {
                        User user = userService.findByLoginToken(cookie.getValue());
                        if (user != null) {
                            session.setAttribute("currentUser", user);
                            currentUser = user; // Update current user variable

                            // Sync to Spring Security, construct authentication info and put into context
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

        // --- ⭐ New: Admin permission check ---
        String requestURI = request.getRequestURI();

        // If requesting admin pages (/admin/...)
        if (requestURI.startsWith("/admin")) {
            // 1. Not logged in -> redirect to login page
            if (currentUser == null) {
                response.sendRedirect("/login");
                return false;
            }
            // 2. Logged in but not admin -> redirect to home page (or show 403 page)
            if (!"ADMIN".equals(currentUser.getRole())) {
                System.out.println("⚠️ Warning: User " + currentUser.getUsername() + " attempted unauthorized admin access!");
                response.sendRedirect("/"); // Redirect to home page
                return false;
            }
        }

        return true; // Allow other requests
    }
}
