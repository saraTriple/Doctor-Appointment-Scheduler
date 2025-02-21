package com.externship.appointment;

import com.externship.appointment.Admin_storage.Admin;
import com.externship.appointment.Admin_storage.AdminRepository;
import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentDelete;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Appointment_storage.AppointmentService;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ControllerClass {

    int count = 0;

    @Autowired
    PatientRepository personRepo;
    @Autowired
    DoctorRepository docRepo;
    @Autowired
    AppointmentRepository appRepo;
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    AdminRepository adminRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/registerdoc")
    public String registerdoc() {
        return "registerdoc";
    }

    @GetMapping("/")
    public String home() {
        return "start";
    }

    @GetMapping("/patlog")
    public String patlog() {
        return "index";
    }

    @GetMapping("/doclog")
    public String doclog() {
        return "doclog";
    }

    @GetMapping("/adminlog")
    public String adminlog() {
        return "admin-log";
    }

    @PostMapping("/registered")
    public String registered(Patient person) {
        personRepo.save(person);
        return "redirect:/";
    }

    @PostMapping("/registereddoc")
    public String registereddoc(Doctor doctor) {
        docRepo.save(doctor);
        return "redirect:/";
    }

    @GetMapping("/fail_login")
    public String fail_login() {
        return "fail_login";
    }

    @PostMapping("/authenticate")
    public String authenticate(Patient person, HttpSession session) {
        if (personRepo.findByEmail(person.getEmail()).get().getPassword().equals(person.getPassword())) {
            session.setAttribute("patient", person.getEmail());
            return "redirect:/patient/appointments/my";
        }
        return "redirect:/fail_login";
    }

    @PostMapping("/authenticatedoc")
    public String authenticatedoc(Doctor doctor, HttpSession session) {
        if (docRepo.existsById(doctor.getEmail()) && docRepo.findById(doctor.getEmail()).get().getPassword().equals(doctor.getPassword())) {
            session.setAttribute("doctor", doctor.getEmail());
            return "redirect:/doctor/patients";
        }
        return "redirect:/fail_login";
    }

    @PostMapping("/authenticateAdmin")
    public String authenticateAdmin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        Optional<Admin> optionalAdmin = adminRepository.findByEmail(email);

        if (optionalAdmin.isPresent()) {
            Admin admin = optionalAdmin.get();
            if (passwordEncoder.matches(password, admin.getPassword())) {
                session.setAttribute("admin", email);
                return "redirect:/adminDashboard"; // Successful login
            }
        }
        return "redirect:/fail_login"; // Authentication failed
    }

    @GetMapping("/adminDashboard")
    public ModelAndView adminDashboard(HttpSession session) {
        // Check if admin is logged in
        if (session.getAttribute("admin") == null) {
            return new ModelAndView("redirect:/fail_login");
        }

        List<Doctor> doctors = docRepo.findAll();
        List<Appointment> appointments = appRepo.findAll();

        ModelAndView modelAndView = new ModelAndView("admin/admin-dashboard");
        modelAndView.addObject("doctors", doctors);
        modelAndView.addObject("appointments", appointments);

        return modelAndView;
    }

    @PostMapping("/cancel")
    public String cancel(AppointmentDelete dApp) {
        appRepo.deleteById(dApp.getAppId());
        return "redirect:/userdetails";
    }

    @GetMapping("/home")
    public ModelAndView display(HttpSession session) {
        ModelAndView mav = new ModelAndView("fail_login");
        String email = null;


        if (session.getAttribute("person") != null) {
            mav = new ModelAndView("home");
            email = (String) session.getAttribute("person");
        }

        mav.addObject("email", email);

        return mav;


    }



    @GetMapping("/docdetails")
    public ModelAndView DocDetails(HttpSession session) {

        List<Doctor> doctors = new ArrayList<Doctor>();
        docRepo.findAll().forEach(doctors::add);
        Map<String, Object> params = new HashMap<>();

        params.put("doctor", doctors);
        params.put("email", session.getAttribute("person"));

        return new ModelAndView("doctorlist", params);
    }

    @GetMapping("/userdetails")
    public ModelAndView UserDetails(HttpSession session) {
        List<Appointment> apps = appointmentService.findByPesonId(session.getAttribute("person").toString());
        Map<String, Object> params = new HashMap<>();

        params.put("appointments", apps);
        params.put("email", session.getAttribute("person"));

        return new ModelAndView("appointed", params);


    }

    @GetMapping("/patientlist")
    public ModelAndView PatientList(HttpSession session) {
        List<Appointment> apps = appRepo.findByDoctor_Email(session.getAttribute("doctor").toString());
        Map<String, Object> params = new HashMap<>();

        params.put("appointments", apps);
        params.put("email", session.getAttribute("doctor"));

        return new ModelAndView("appointedDoc", params);


    }


}
