package com.externship.appointment.initializers;

import com.externship.appointment.Admin_storage.Admin;
import com.externship.appointment.Admin_storage.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class AdminInitializer extends BaseInitializer {

    @Autowired
    private AdminRepository adminRepository;


    @Override
    protected String getInitializerName() {
        return "AdminInitializer";
    }

    @Override
    protected void initialize() {
        if (!initializationTracker.isInitialized(getInitializerName())) {
            List<Admin> admins = List.of(
                    new Admin("admin1@example.com", "password1"),
                    new Admin("admin2@example.com", "password2")
            );
            adminRepository.saveAll(admins);
            initializationTracker.markAsInitialized(getInitializerName());
        }
    }
}
