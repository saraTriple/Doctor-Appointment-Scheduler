package com.externship.appointment.Admin_storage;

import com.externship.appointment.Appointment_storage.*;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // Main Admin Dashboard
    @GetMapping("/dashboard")
    public ModelAndView adminDashboard(HttpSession session) {
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }
        return new ModelAndView("admin/admin-dashboard");
    }

    // Appointments List Page with Pagination
    @GetMapping("/appointments")
    public String appointmentsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String doctorEmail,
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session) {

        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        Page<Appointment> appointmentsPage;
        if (doctorEmail != null && !doctorEmail.isEmpty()) {
            appointmentsPage = appointmentService.getAppointmentsByDoctor(doctorEmail, PageRequest.of(page, size, Sort.by("date").descending()));
        } else if (status != null && !status.isEmpty()) {
            appointmentsPage = appointmentService.getAppointmentsByStatus(status, PageRequest.of(page, size, Sort.by("date").descending()));
        } else {
            appointmentsPage = appointmentService.getAllAppointmentsPaginated(PageRequest.of(page, size, Sort.by("date").descending()));
        }

        model.addAttribute("appointments", appointmentsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointmentsPage.getTotalPages());
        model.addAttribute("doctors", doctorRepository.findAll());

        return "admin/appointments";
    }

    // Doctors List Page with Pagination
    @GetMapping("/doctors")
    public String doctorsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        if (session.getAttribute("admin") == null) {
            return "redirect:/fail_login";
        }

        Page<Doctor> doctorsPage = doctorRepository.findAll(PageRequest.of(page, size, Sort.by("email")));

        model.addAttribute("doctors", doctorsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", doctorsPage.getTotalPages());

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
}