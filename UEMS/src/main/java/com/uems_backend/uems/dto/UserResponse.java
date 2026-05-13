package com.uems_backend.uems.dto;

import com.uems_backend.uems.model.AppUser;
import com.uems_backend.uems.model.Role;

public record UserResponse(Long id, String username, String email, Role role, boolean enabled) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isEnabled());
    }
}
