package com.uems_backend.uems.service;

import com.uems_backend.uems.dto.UserUpdateRequest;
import com.uems_backend.uems.exception.BadRequestException;
import com.uems_backend.uems.exception.NotFoundException;
import com.uems_backend.uems.model.AppUser;
import com.uems_backend.uems.repository.EventRepository;
import com.uems_backend.uems.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public UserService(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public AppUser update(Long id, UserUpdateRequest request) {
        AppUser user = findUser(id);
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BadRequestException("Email already exists");
        }
        if (request.role() == null) {
            throw new BadRequestException("Role is required");
        }

        user.updateProfile(request.email(), request.role());
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        return user;
    }

    @Transactional
    public AppUser setEnabled(Long id, boolean enabled, String currentUsername) {
        AppUser user = findUser(id);
        if (user.getUsername().equals(currentUsername) && !enabled) {
            throw new BadRequestException("You cannot disable your own account");
        }
        user.setEnabled(enabled);
        return user;
    }

    @Transactional
    public void delete(Long id, String currentUsername) {
        AppUser user = findUser(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new BadRequestException("You cannot delete your own account");
        }
        if (eventRepository.existsByOrganizerId(id)) {
            throw new BadRequestException("Users with event history cannot be deleted; disable the account instead");
        }
        userRepository.delete(user);
    }

    private AppUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
