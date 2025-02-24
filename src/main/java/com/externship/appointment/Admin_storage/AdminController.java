package com.externship.appointment.Admin_storage;

import com.externship.appointment.Appointment_storage.*;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Doctor_storage.DoctorService;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import com.externship.appointment.Patient_storage.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    // Main Admin Dashboard
    @GetMapping("/dashboard")
    public ModelAndView adminDashboard(HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }

        ModelAndView modelAndView = new ModelAndView("admin/admin-dashboard");
        modelAndView.addObject("doctorCount", doctorService.count());
        modelAndView.addObject("patientCount", patientService.count());
        modelAndView.addObject("appointmentCount", appointmentService.count());
        modelAndView.addObject("totalRevenue", appointmentService.calculateTotalRevenue());
        
        // Add required data for report filters
        modelAndView.addObject("doctors", doctorService.findAll());
        modelAndView.addObject("specialties", doctorService.getAllSpecialties());
        
        return modelAndView;
    }

    // Appointments List Page with Pagination
    @GetMapping("/appointments")
    public String getAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String doctorEmail,
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session) {
        
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").ascending().and(Sort.by("time")));
        Page<Appointment> appointments;

        if (date != null && !date.isEmpty()) {
            LocalDate filterDate = LocalDate.parse(date);
            if (time != null && !time.isEmpty()) {
                LocalTime filterTime = LocalTime.parse(time);
                appointments = appointmentRepository.findByDateAndTime(filterDate, filterTime, pageable);
            } else {
                appointments = appointmentRepository.findByDate(filterDate, pageable);
            }
        } else if (time != null && !time.isEmpty()) {
            LocalTime filterTime = LocalTime.parse(time);
            appointments = appointmentRepository.findByTime(filterTime, pageable);
        } else if (status != null && !status.isEmpty()) {
            appointments = appointmentRepository.findByAppointmentStatus_Status(status, pageable);
        } else if (doctorEmail != null && !doctorEmail.isEmpty()) {
            appointments = appointmentRepository.findByDoctor_Email(doctorEmail, pageable);
        } else {
            appointments = appointmentRepository.findAll(pageable);
        }

        // Add doctors list to the model
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("appointments", appointments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointments.getTotalPages());
        return "admin/appointments";
    }

    // Doctors List Page with Pagination
    @GetMapping("/doctors")
    public String getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String degree,
            Model model,
            HttpSession session) {
        
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("name").ascending());
        Page<Doctor> doctors = doctorRepository.findByFilters(name, email, specialty, degree, pageable);

        model.addAttribute("doctors", doctors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", doctors.getTotalPages());
        return "admin/doctors";
    }

    // View Appointment Details
    @GetMapping("/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            model.addAttribute("appointment", appointment.get());
            Optional<Doctor> doctor = doctorRepository.findById(appointment.get().getDoctor().getEmail());
            model.addAttribute("doctor", doctor.get());
            return "admin/appointment-details";
        }
        return "redirect:/admin/appointments";
    }

    // Edit Appointment
    @PostMapping("/appointment/{id}/edit")
    public String editAppointment(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session) {

        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setAppointmentStatus(appointmentStatusRepository.findByStatus(status.toUpperCase()).get());
            appointmentRepository.save(appointment);
        }

        return "redirect:/admin/appointments";
    }

    // Cancel Appointment
    @PostMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setAppointmentStatus(appointmentStatusRepository.findByStatus("CANCELLED").get());
            appointmentRepository.save(appointment);
        }

        return "redirect:/admin/appointments";
    }

    // Schedule New Appointment Page
    @GetMapping("/schedule")
    public String scheduleAppointment(Model model, HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("patients", patientRepository.findAll());

        return "admin/schedule-appointment";
    }

    // Create New Appointment
    @PostMapping("/schedule")
    public String createAppointment(
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String doctorEmail,
            @RequestParam String patientEmail,
            HttpSession session) {

        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        Appointment appointment = new Appointment();
        try {
            appointment.setDate(LocalDate.parse(date));
            appointment.setTime(LocalTime.parse(time));
            
            Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
            Optional<Patient> patient = patientRepository.findByEmail(patientEmail);
            
            if (doctor.isPresent() && patient.isPresent()) {
                appointment.setDoctor(doctor.get());
                appointment.setPerson(patient.get());
                appointment.setAppointmentStatus(appointmentStatusRepository.findByStatus("SCHEDULED").get());
                appointmentService.createAppointment(appointment);
                return "redirect:/admin/appointments";
            }
        } catch (DateTimeParseException e) {
            return "redirect:/admin/schedule?error=invalid_date";
        }

        return "redirect:/admin/schedule?error=invalid_users";
    }

    @GetMapping("/patients")
    public String getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            Model model,
            HttpSession session) {
        
        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("firstName").ascending());
        Page<Patient> patients = patientRepository.findByFilters(email, firstName, lastName, phoneNumber, pageable);

        model.addAttribute("patients", patients);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", patients.getTotalPages());
        return "admin/patients";
    }
}