package com.csye6220.huskyamazon.config;

// ⭐ 修正引用：根据之前的文件上下文，类名应该是 CustomUserDetailsService
import com.csye6220.huskyamazon.service.CustomUserDetailsService;
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

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 静态资源 (全部放行)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/error").permitAll()

                        // 2. 公开页面 (首页, 搜索, 注册, 登录, 商品详情) -> 全部放行
                        // ⭐ 关键：增加了 /filter 和 /category/** 确保游客可以筛选
                        .requestMatchers("/", "/home", "/index", "/register", "/login").permitAll()
                        .requestMatchers("/search", "/filter", "/product/**", "/category/**").permitAll()

                        // 3. 后台管理 -> 只有 ADMIN 能进
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 4. 其他页面 (购物车, 订单) -> 必须登录
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // 处理前端 POST /login 请求
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // 5. 记住我功能 (Cookie)
                .rememberMe(remember -> remember
                        .key("mySecretKey")
                        .tokenValiditySeconds(86400) // 24小时
                        .userDetailsService(userDetailsService)
                );

        return http.build();
    }

    // 6. 密码编码器
    // 为了兼容你现有的明文密码数据，我们使用 NoOpPasswordEncoder
    // (注意：生产环境建议换成 BCryptPasswordEncoder)
    @SuppressWarnings("deprecation")
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