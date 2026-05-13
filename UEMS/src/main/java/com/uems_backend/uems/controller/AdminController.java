package com.uems_backend.uems.controller;

import com.uems_backend.uems.dto.UserUpdateRequest;
import com.uems_backend.uems.dto.UserResponse;
import com.uems_backend.uems.service.UserService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return UserResponse.from(userService.update(id, request));
    }

    @PatchMapping("/users/{id}/enable")
    public UserResponse enableUser(@PathVariable Long id) {
        return UserResponse.from(userService.setEnabled(id, true, null));
    }

    @PatchMapping("/users/{id}/disable")
    public UserResponse disableUser(@PathVariable Long id, Principal principal) {
        return UserResponse.from(userService.setEnabled(id, false, principal.getName()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        userService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
