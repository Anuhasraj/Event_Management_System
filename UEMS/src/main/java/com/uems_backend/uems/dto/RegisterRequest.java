package com.uems_backend.uems.dto;

import com.uems_backend.uems.model.Role;

public record RegisterRequest(String username, String email, String password, Role role) {
}
