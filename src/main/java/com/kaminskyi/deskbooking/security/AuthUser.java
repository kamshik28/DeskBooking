package com.kaminskyi.deskbooking.security;

import com.kaminskyi.deskbooking.entity.Role;

// what we keep in SecurityContext after the token is parsed
public record AuthUser(Long id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
