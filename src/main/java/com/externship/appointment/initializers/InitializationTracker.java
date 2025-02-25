package com.externship.appointment.initializers;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InitializationTracker {
    private final ConcurrentHashMap<String, Boolean> initializationStatus = new ConcurrentHashMap<>();

    public boolean isInitialized(String initializerName) {
        return initializationStatus.getOrDefault(initializerName, false);
    }

    public void markAsInitialized(String initializerName) {
        initializationStatus.put(initializerName, true);
    }
}
