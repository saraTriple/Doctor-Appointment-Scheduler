package com.externship.appointment.initializers;

import com.externship.appointment.Appointment_storage.AppointmentStatus;
import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AppointmentStatusInitializer extends BaseInitializer {

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    @Override
    protected String getInitializerName() {
        return "AppointmentStatusInitializer";
    }

    @Override
    protected void initialize() {
        if (!initializationTracker.isInitialized(getInitializerName())) {
            // Create default status values
            appointmentStatusRepository.save(new AppointmentStatus("AVAILABLE", "Appointment slot is available for booking"));
            appointmentStatusRepository.save(new AppointmentStatus("SCHEDULED", "Appointment has been booked by a patient"));
            appointmentStatusRepository.save(new AppointmentStatus("COMPLETED", "Appointment has been completed"));
            appointmentStatusRepository.save(new AppointmentStatus("CANCELLED", "Appointment has been cancelled"));
            appointmentStatusRepository.save(new AppointmentStatus("NO_SHOW", "Patient did not show up for the appointment"));
            initializationTracker.markAsInitialized(getInitializerName());
        }
    }
}
