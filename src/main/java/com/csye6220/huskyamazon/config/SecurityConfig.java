package com.csye6220.huskyamazon.config;

import com.csye6220.huskyamazon.service.impl.CustomUserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsServiceImpl userDetailsService;

    public SecurityConfig(CustomUserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行静态资源和公开页面
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/", "/home", "/search", "/product/**", "/register", "/login").permitAll()
                        // 2. 后台管理限制 ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 3. 其他所有页面需要登录 (USER 或 ADMIN)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // 指定自定义登录页
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // 开启 Remember-Me (Cookie)
                .rememberMe(remember -> remember
                        .key("mySecretKey")
                        .tokenValiditySeconds(86400)
                        .userDetailsService(userDetailsService)
                );

        return http.build();
    }

    // 开发环境使用明文密码 (生产环境请换成 BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
}