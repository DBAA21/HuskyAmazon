package com.csye6220.huskyamazon.service;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 自定义的用户详情服务接口，直接继承 Spring Security 的 UserDetailsService，
 * 以便在需要 UserDetailsService 的地方（如 RememberMe、DaoAuthenticationProvider）
 * 能够正确注入与使用。
 */
public interface CustomUserDetailsService extends UserDetailsService {
}
