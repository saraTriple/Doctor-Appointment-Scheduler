package com.externship.appointment.Patient_storage;

import com.externship.appointment.Appointment_storage.*;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    @GetMapping("/patlog")
    public String showLoginForm() {
        return "patlog";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<Patient> patient = patientRepository.findByEmail(email);

        if (patient.isPresent() && patient.get().getPassword().equals(password)) {
            session.setAttribute("patient", email);
            return "redirect:/patient/appointments/my";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "patlog";
        }
    }

    // My Appointments Page
    @GetMapping("/appointments/my")
    public String showMyAppointments(HttpSession session, Model model) {
        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return "redirect:/patlog";
        }

        List<Appointment> appointments = appointmentRepository.findByPerson_Email(patientEmail);
        model.addAttribute("appointments", appointments);
        return "patient/my-appointments";
    }

    // Available Appointments Page
    @GetMapping("/appointments/book")
    public String showAvailableAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorName,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return "redirect:/patlog";
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));
        Page<Appointment> appointments = appointmentRepository.findByDateGreaterThanEqualAndPerson_EmailIsNull(
                LocalDate.now(), pageable);

        // Apply filters
//        if (date != null) {
//            appointments = appointmentRepository.findByDateAndPerson_EmailIsNull(date, pageable);
//        }

        if (specialty != null || doctorName != null || date != null) {
            appointments = appointmentRepository.findByFilters(LocalDate.now(),
                    date,
                    specialty,
                    doctorName,
                    pageable);
        }

        model.addAttribute("appointments", appointments);
        return "patient/available-appointments";
    }

    // Doctors List Page
    @GetMapping("/doctors")
    public String listDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String state,
            Model model,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return "redirect:/patlog";
        }

        List<Doctor> doctors;
        if (name != null || specialization != null || state != null) {
            doctors = doctorRepository.findAll().stream()
                    .filter(d -> name == null || d.getName().toLowerCase().contains(name.toLowerCase()))
                    .filter(d -> specialization == null || d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .filter(d -> state == null || Objects.nonNull(d.getState()) && d.getState().toLowerCase().contains(state.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            doctors = doctorRepository.findAll();
        }

        model.addAttribute("doctors", doctors);
        return "patient/doctors";
    }

    // Doctor Details Page
    @GetMapping("/doctors/{email}")
    public String showDoctorDetails(
            @PathVariable String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            Model model,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return "redirect:/patlog";
        }

        Doctor doctor = doctorRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));
        Page<Appointment> appointments;

        if (date != null && time == null) {
            appointments = appointmentRepository.findByDateAndDoctor_EmailAndPerson_EmailIsNull(
                    date, email, pageable);
        } else if (time != null && date == null) {
            appointments = appointmentRepository.findByTimeAndDoctor_EmailAndPerson_EmailIsNull(
                    time, email, pageable);
        } else if (time != null && date != null) {
            appointments = appointmentRepository.findByDateAndTimeAndDoctor_EmailAndPerson_EmailIsNull(
                    date, time, email, pageable);
        } else {
            appointments = appointmentRepository.findByDoctor_EmailAndDateGreaterThanEqualAndPerson_EmailIsNull(
                    email, LocalDate.now(), pageable);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "patient/doctor-details";
    }

    // Book Appointment
    @PostMapping("/appointments/book")
    @ResponseBody
    public ResponseEntity<?> bookAppointment(
            @RequestParam Long appointmentId,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to book appointments");
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        if (appointment.getPerson() != null) {
            return ResponseEntity.badRequest()
                    .body("This appointment is already booked");
        }

        Optional<Patient> patientOpt = patientRepository.findByEmail(patientEmail);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Patient not found");
        }

        appointment.setPerson(patientOpt.get());
        AppointmentStatus status = appointmentStatusRepository.findByStatus("SCHEDULED")
                .orElseThrow(() -> new RuntimeException("Status not found"));
        appointment.setAppointmentStatus(status);

        appointmentRepository.save(appointment);

        return ResponseEntity.ok()
                .body("Appointment booked successfully");
    }

    // Cancel Appointment
    @PostMapping("/appointments/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(
            @RequestParam Long appointmentId,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to cancel appointments");
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();

        // Check if this appointment belongs to the logged-in patient
        if (appointment.getPerson() == null ||
                !appointment.getPerson().getEmail().equals(patientEmail)) {
            return ResponseEntity.badRequest()
                    .body("You can only cancel your own appointments");
        }

        // Reset the appointment
        appointment.setPerson(null);
        appointment.setAppointmentStatus(
                appointmentStatusRepository.findByStatus("AVAILABLE")
                        .orElseThrow(() -> new RuntimeException("Status not found"))
        );

        appointmentRepository.save(appointment);

        return ResponseEntity.ok()
                .body("Appointment cancelled successfully");
    }
}
