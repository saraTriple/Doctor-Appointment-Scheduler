package com.externship.appointment.Admin_storage;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentService;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping("/admin/login")
    public String showLoginPage() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        Admin admin = adminService.findByUsername(username);
        if (admin != null && admin.getPassword().equals(password)) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid username or password");
        return "admin-login";
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model) {
        List<Doctor> doctors = doctorRepository.findAll(); // Fetch all doctors
        List<Appointment> appointments = appointmentService.getAllAppointments(); // Fetch all appointments

        model.addAttribute("doctors", doctors);
        model.addAttribute("appointments", appointments);

        return "admin-dashboard";
    }


    @GetMapping("/admin/appointments")
    public String showAppointmentForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        return "admin-appointment";
    }

    @PostMapping("/admin/appointments")
    public String createAppointment(@ModelAttribute Appointment appointment) {
        appointmentService.createAppointment(appointment);
        return "redirect:/admin/dashboard";
    }
}
