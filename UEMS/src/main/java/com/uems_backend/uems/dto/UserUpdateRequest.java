package com.uems_backend.uems.dto;

import com.uems_backend.uems.model.Role;

public record UserUpdateRequest(String email, Role role, Boolean enabled) {
}
