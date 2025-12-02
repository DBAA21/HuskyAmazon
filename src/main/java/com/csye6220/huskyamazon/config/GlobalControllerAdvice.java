package com.csye6220.huskyamazon.config;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;

    // 这个方法会在每个 Controller 执行前运行
    // 它的作用是把 Spring Security 的登录状态同步给原来的 session.currentUser
    @ModelAttribute
    public void populateUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 检查是否已登录且不是匿名用户
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // 如果 Session 里没有 currentUser，我们就查出来放进去
            if (session.getAttribute("currentUser") == null) {
                String username = auth.getName();
                User user = userService.findByUsername(username);
                if (user != null) {
                    session.setAttribute("currentUser", user);
                }
            }
        } else {
            // 如果没登录，确保 Session 清空
            if (session.getAttribute("currentUser") != null) {
                session.removeAttribute("currentUser");
            }
        }
    }
}