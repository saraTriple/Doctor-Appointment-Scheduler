package com.externship.appointment.Admin_storage;


import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Appointment_storage.AppointmentService;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private AppointmentService appointmentService;

//    @Autowired
//    private DoctorService doctorService;
//
//    @Autowired
//    private PatientService patientService;

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/appointments")
    public String getAllAppointments(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Appointment> appointmentPage = appointmentService.findAllPaginated(PageRequest.of(page, size));
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Add page size options
        int[] pageSizes = {5, 10, 20, 50};
        model.addAttribute("pageSizes", pageSizes);

        // Add doctors and patients for the modal
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("patients", patientRepository.findAll());

        return "admin/appointments";
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        Optional<Appointment> appointment = appointmentService.findById(id);
        if (appointment.isPresent()) {
            model.addAttribute("appointment", appointment.get());
            return "admin/appointment-details";
        }
        return "redirect:/admin/appointments";
    }

    @GetMapping("/appointment/{id}/edit")
    public String editAppointmentForm(@PathVariable Long id, Model model) {
        Optional<Appointment> appointment = appointmentService.findById(id);
        if (appointment.isPresent()) {
            model.addAttribute("appointment", appointment.get());
            model.addAttribute("doctors", doctorRepository.findAll());
            model.addAttribute("patients", patientRepository.findAll());
            return "admin/edit-appointment";
        }
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointment/{id}/edit")
    public String updateAppointment(@PathVariable Long id, @ModelAttribute Appointment appointment) {
        appointment.setId(id);
        appointmentService.updateAppointment(appointment);
        return "redirect:/admin/appointments";
    }

    @GetMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointment/add")
    public String addAppointment(@ModelAttribute Appointment appointment) {
        appointmentService.saveAppointment(appointment);
        return "redirect:/admin/appointments";
    }
}