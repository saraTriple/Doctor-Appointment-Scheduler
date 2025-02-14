package com.externship.appointment.Admin_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public void saveAdmin(Admin admin) {
        adminRepository.save(admin);
    }
}