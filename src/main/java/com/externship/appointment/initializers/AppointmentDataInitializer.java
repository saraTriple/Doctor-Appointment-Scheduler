package com.externship.appointment.initializers;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Appointment_storage.AppointmentStatus;
import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(7) // Run after patient and doctor initializers
public class AppointmentDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentDataInitializer.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        logger.info("Initializing appointment data...");

        // Get all doctors and patients
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = patientRepository.findAll();

        // Get or create appointment statuses
        AppointmentStatus upcomingStatus = appointmentStatusRepository.findByStatus("SCHEDULED")
                .orElseGet(() -> {
                    AppointmentStatus status = new AppointmentStatus();
                    status.setStatus("SCHEDULED");
                    return appointmentStatusRepository.save(status);
                });

        // Create available appointments for each doctor
        for (Doctor doctor : doctors) {
            createAvailableAppointments(doctor);
        }

        // Create booked appointments for each patient
        for (Patient patient : patients) {
            createBookedAppointments(patient, upcomingStatus);
        }

        logger.info("Appointment data initialization completed.");
    }

    private void createAvailableAppointments(Doctor doctor) {
        // Create 3 available appointments for each doctor
        LocalDate startDate = LocalDate.now().plusDays(1);
        List<LocalTime> timeSlots = generateTimeSlots();

        int i =0;
        while (i < 3) {
            LocalDate appointmentDate = startDate.plusDays(random.nextInt(14)); // Random day within next 2 weeks
            LocalTime appointmentTime = timeSlots.get(random.nextInt(timeSlots.size()));

            // Check if appointment already exists
            if (appointmentRepository.findByDateAndTimeAndDoctor_Email(
                    appointmentDate, appointmentTime, doctor.getEmail()).isEmpty()) {
                
                Appointment appointment = new Appointment();
                appointment.setDoctor(doctor);
                appointment.setDate(appointmentDate);
                appointment.setTime(appointmentTime);
                appointment.setAppointmentStatus(appointmentStatusRepository.findByStatus("AVAILABLE").get());
                appointmentRepository.save(appointment);
                i++;
            }
        }
    }

    private void createBookedAppointments(Patient patient, AppointmentStatus upcomingStatus) {
        // Create 2 booked appointments for each patient
        List<Doctor> doctors = doctorRepository.findAll();
        LocalDate startDate = LocalDate.now().plusDays(1);
        List<LocalTime> timeSlots = generateTimeSlots();

        for (int i = 0; i < 2; i++) {
            Doctor randomDoctor = doctors.get(random.nextInt(doctors.size()));
            LocalDate appointmentDate = startDate.plusDays(random.nextInt(14)); // Random day within next 2 weeks
            LocalTime appointmentTime = timeSlots.get(random.nextInt(timeSlots.size()));

            // Check if appointment already exists
            if (appointmentRepository.findByDateAndTimeAndDoctor_Email(
                    appointmentDate, appointmentTime, randomDoctor.getEmail()).isEmpty()) {
                
                Appointment appointment = new Appointment();
                appointment.setDoctor(randomDoctor);
                appointment.setPerson(patient);
                appointment.setDate(appointmentDate);
                appointment.setTime(appointmentTime);
                appointment.setAppointmentStatus(upcomingStatus);
                appointmentRepository.save(appointment);
            }
        }
    }

    private List<LocalTime> generateTimeSlots() {
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0); // 9:00 AM
        LocalTime endTime = LocalTime.of(17, 0);  // 5:00 PM

        while (startTime.isBefore(endTime)) {
            timeSlots.add(startTime);
            startTime = startTime.plusMinutes(30); // 30-minute intervals
        }

        return timeSlots;
    }
}
