package com.csye6220.huskyamazon.service;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Custom user details service interface that directly extends Spring Security's UserDetailsService,
 * to enable proper injection and usage in places that require UserDetailsService
 * (such as RememberMe, DaoAuthenticationProvider).
 */
public interface CustomUserDetailsService extends UserDetailsService {
}
