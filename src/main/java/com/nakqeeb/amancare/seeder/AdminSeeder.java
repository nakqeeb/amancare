package com.nakqeeb.amancare.seeder;

import com.nakqeeb.amancare.dto.request.UserCreationRequest;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.repository.ClinicRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// @Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;

    private final PasswordEncoder passwordEncoder;


    public AdminSeeder(
            UserRepository userRepository,
            ClinicRepository clinicRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.clinicRepository = clinicRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        // First, check if a default clinic exists, if not create one
        Clinic defaultClinic = clinicRepository.findById(1L)
                .orElseGet(() -> {
                    Clinic newClinic = new Clinic();
                    newClinic.setName("Default Clinic");
                    newClinic.setAddress("System Default");
                    return clinicRepository.save(newClinic);
                });


        UserCreationRequest userDto = new UserCreationRequest();
        userDto.setUsername("admin");
        userDto.setEmail("admin@email.com");
        userDto.setPassword("123456");
        userDto.setFirstName("Khaled");
        userDto.setLastName("Gamal");
        userDto.setPhone("780004781");
        userDto.setRole(UserRole.SYSTEM_ADMIN);
        userDto.setSpecialization("Admin");

        var user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole());
        user.setSpecialization(userDto.getSpecialization());
        user.setClinic(defaultClinic);

        userRepository.save(user);
    }
}