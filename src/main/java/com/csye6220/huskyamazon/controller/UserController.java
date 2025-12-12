package com.csye6220.huskyamazon.controller;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // Core: trigger validation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Core: receive error results
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- Registration page ---
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        // Key: must put an empty User object, otherwise frontend th:object will error
        model.addAttribute("user", new User());
        return "register";
    }

    // --- Handle registration submission ---
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result, // ⚠️Note: BindingResult must immediately follow the @Valid object
                               Model model) {

        // 1. Check @Pattern defined format errors
        if (result.hasErrors()) {
            // If errors exist, Spring will automatically bring error messages back to register.html for display
            return "register";
        }

        // 2. Check business logic errors (database duplicate check)
        if (userService.findByUsername(user.getUsername()) != null) {
            // Manually add an error to the "username" field
            result.rejectValue("username", "error.user", "Username already exists!");
            return "register";
        }

        // 3. Everything is normal, execute registration
        userService.registerUser(user);
        return "redirect:/login?success";
    }

    // --- Login page ---
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // --- Handle login submission ---
    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            @RequestParam(name = "remember-me", required = false) boolean rememberMe, // Keep consistent with frontend name
                            HttpSession session,
                            HttpServletResponse response, // Key: used to send Cookie
                            Model model) {

        User user = userService.login(username, password);

        if (user != null) {
            session.setAttribute("currentUser", user);

            // --- Auto-login logic (Remember Me) start ---
            if (rememberMe) {
                // Generate a random Token (UUID)
                String token = UUID.randomUUID().toString();

                // 1. Store in database
                userService.updateLoginToken(user, token);

                // 2. Store in browser Cookie
                Cookie cookie = new Cookie("HUSKY_AUTH_TOKEN", token);
                cookie.setMaxAge(24 * 60 * 60); // Valid for 24 hours (in seconds)
                cookie.setPath("/"); // Valid for entire website
                cookie.setHttpOnly(true); // Security setting: prevent script from stealing Cookie
                response.addCookie(cookie);
            }
            // --- Auto-login logic (Remember Me) end ---

            return "redirect:/";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    // --- Logout ---
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // 1. Destroy Session
        session.invalidate();

        // 2. Clear Cookie (set same-name Cookie with max age 0)
        Cookie cookie = new Cookie("HUSKY_AUTH_TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/";
    }
}
