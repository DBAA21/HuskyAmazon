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

    // This method will run before each Controller is executed
    // Its function is to synchronize the login status from Spring Security to the original session.currentUser.
    @ModelAttribute
    public void populateUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user is logged in and is not an anonymous user.
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // If there is no `currentUser` in the session, we will retrieve it and put it in the session.
            if (session.getAttribute("currentUser") == null) {
                String username = auth.getName();
                User user = userService.findByUsername(username);
                if (user != null) {
                    session.setAttribute("currentUser", user);
                }
            }
        } else {
            // If you are not logged in, make sure the session is cleared.
            if (session.getAttribute("currentUser") != null) {
                session.removeAttribute("currentUser");
            }
        }
    }
}