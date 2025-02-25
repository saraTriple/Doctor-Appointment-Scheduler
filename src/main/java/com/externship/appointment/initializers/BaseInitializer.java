package com.externship.appointment.initializers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public abstract class BaseInitializer implements CommandLineRunner {
    
    @Autowired
    protected InitializationTracker initializationTracker;

    protected abstract String getInitializerName();
    protected abstract void initialize();

    @Override
    public void run(String... args) {
        if (!initializationTracker.isInitialized(getInitializerName())) {
            initialize();
            initializationTracker.markAsInitialized(getInitializerName());
        }
    }
}
