package com.externship.appointment.Admin_storage;


import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class AdminController {

    private final DoctorRepository docRepo;
    private final AppointmentRepository appRepo;
    private final PatientRepository personRepository;

    public AdminController(DoctorRepository docRepo, AppointmentRepository appRepo, PatientRepository personRepository) {
        this.docRepo = docRepo;
        this.appRepo = appRepo;
        this.personRepository = personRepository;
    }

    // Main Admin Dashboard
    @GetMapping("/admin/dashboard")
    public ModelAndView adminDashboard(HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }

        ModelAndView modelAndView = new ModelAndView("admin/admin-dashboard");
        return modelAndView;
    }

    @GetMapping("/admin/home")
    public ModelAndView adminDashboardHome(HttpSession session) {

        ModelAndView modelAndView = new ModelAndView("admin/home");
        return modelAndView;
    }

    // Doctors List Page
    @GetMapping("/admin/doctors")
    public ModelAndView doctorsList(HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }

        List<Doctor> doctors = docRepo.findAll();
        ModelAndView modelAndView = new ModelAndView("admin/doctors");
        modelAndView.addObject("doctors", doctors);
        return modelAndView;
    }

    @GetMapping("/admin/patients")
    public ModelAndView patientsList(HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }

        List<Patient> patients = personRepository.findAll();
        ModelAndView modelAndView = new ModelAndView("admin/patients");
        modelAndView.addObject("patients", patients);
        return modelAndView;
    }

    // Appointments List Page
    @GetMapping("/admin/appointments")
    public ModelAndView appointmentsList(HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }

        List<Appointment> appointments = appRepo.findAll();
        ModelAndView modelAndView = new ModelAndView("admin/appointments");
        modelAndView.addObject("appointments", appointments);
        return modelAndView;
    }
}