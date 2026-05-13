package com.uems_backend.uems.dto;

import com.uems_backend.uems.model.Role;

public record AuthResponse(String token, String username, Role role) {
}
