package com.uems_backend.uems.config;

import com.uems_backend.uems.model.AppUser;
import com.uems_backend.uems.model.Role;
import com.uems_backend.uems.model.Venue;
import com.uems_backend.uems.repository.UserRepository;
import com.uems_backend.uems.repository.VenueRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(VenueRepository venueRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            List.of("Raja Veediya", "Auditorium", "Canteen Area", "Playground")
                    .forEach(name -> venueRepository.findByName(name)
                            .orElseGet(() -> venueRepository.save(new Venue(name))));

            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new AppUser(
                        "admin",
                        "admin@uems.local",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                ));
            } else {
                userRepository.findByUsername("admin").ifPresent(admin -> {
                    admin.updateProfile("admin@uems.local", Role.ADMIN);
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setEnabled(true);
                    userRepository.save(admin);
                });
            }
        };
    }
}
