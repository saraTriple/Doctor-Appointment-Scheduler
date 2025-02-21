package com.externship.appointment.initializers;


import com.externship.appointment.Admin_storage.Admin;
import com.externship.appointment.Admin_storage.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Configuration
@Order(1)
public class AdminInitializer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initAdmins(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepository.count() == 0) {
                List<Admin> admins = List.of(
                        new Admin("admin1@example.com", "password1"),
                        new Admin("admin2@example.com", "password2")
                );
                adminRepository.saveAll(admins);
                System.out.println("Admin accounts initialized.");
            }
        };
    }
}

