package com.example.walletservice.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;


public final class TestSecurity {

    private TestSecurity() {
    }

    public static void loginAs(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    }

    public static void logout() {
        SecurityContextHolder.clearContext();
    }
}
