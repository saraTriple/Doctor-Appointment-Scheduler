package com.externship.appointment.initializers;

import com.externship.appointment.Appointment_storage.AppointmentStatus;
import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppointmentStatusInitializer implements CommandLineRunner {

    private final AppointmentStatusRepository appointmentStatusRepository;

    public AppointmentStatusInitializer(AppointmentStatusRepository appointmentStatusRepository) {
        this.appointmentStatusRepository = appointmentStatusRepository;
    }

    @Override
    public void run(String... args) {
        // Only initialize if the table is empty
        if (appointmentStatusRepository.count() == 0) {
            // Create default status values
            appointmentStatusRepository.save(new AppointmentStatus("AVAILABLE", "Appointment slot is available for booking"));
            appointmentStatusRepository.save(new AppointmentStatus("SCHEDULED", "Appointment has been booked by a patient"));
            appointmentStatusRepository.save(new AppointmentStatus("COMPLETED", "Appointment has been completed"));
            appointmentStatusRepository.save(new AppointmentStatus("CANCELLED", "Appointment has been cancelled"));
            appointmentStatusRepository.save(new AppointmentStatus("NO_SHOW", "Patient did not show up for the appointment"));
        }
    }
}
