package com.externship.appointment.Admin_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByEmail(username);
    }

    public void saveAdmin(Admin admin) {
        adminRepository.save(admin);
    }
}