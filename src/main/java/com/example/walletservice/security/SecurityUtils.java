package com.example.walletservice.security;

import com.example.walletservice.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }


    public static Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static void checkOwner(User owner) {
        if (!owner.getId().equals(currentUserId())) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
