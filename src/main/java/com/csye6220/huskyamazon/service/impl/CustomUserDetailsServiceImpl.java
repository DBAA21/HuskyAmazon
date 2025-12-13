package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserDAO userDAO;

    @Autowired
    public CustomUserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // 将我们的 User convert成 Spring Security 的 UserDetails
        // Key点：Add ROLE_ 前缀，这是 Spring Security 的规范
        String role = "ROLE_" + (user.getRole() != null ? user.getRole() : "USER");

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}