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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
            if (password.equals(admin.getPassword())) {
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

    @GetMapping("/doctors")
    public String listDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String state,
            Model model) {

        List<Doctor> doctors;
        if (name != null || specialization != null || state != null) {
            doctors = docRepo.findAll().stream()
                    .filter(d -> name == null || d.getName().toLowerCase().contains(name.toLowerCase()))
                    .filter(d -> specialization == null || d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .filter(d -> state == null || Objects.nonNull(d.getState()) && d.getState().toLowerCase().contains(state.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            doctors = docRepo.findAll();
        }

        model.addAttribute("doctors", doctors);
        return "public/doctors";
    }

    @GetMapping("/doctors/{email}")
    public String showDoctorDetails(
            @PathVariable String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            Model model) {

        Doctor doctor = docRepo.findById(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));
        Page<Appointment> appointments;

        if (date != null && time == null) {
            appointments = appRepo.findByDateAndDoctor_EmailAndPerson_EmailIsNull(
                    date, email, pageable);
        } else if (time != null && date == null) {
            appointments = appRepo.findByTimeAndDoctor_EmailAndPerson_EmailIsNull(
                    time, email, pageable);
        } else if (time != null && date != null) {
            appointments = appRepo.findByDateAndTimeAndDoctor_EmailAndPerson_EmailIsNull(
                    date, time, email, pageable);
        } else {
            appointments = appRepo.findByDoctor_EmailAndDateGreaterThanEqualAndPerson_EmailIsNull(
                    email, LocalDate.now(), pageable);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "public/doctor-details";
    }

    @GetMapping("/appointments")
    public String listAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorName,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));

        Page<Appointment> appointments = appRepo.findByDateGreaterThanEqualAndPerson_EmailIsNull(LocalDate.now(), pageable);

        if (specialty != null || doctorName != null || date != null) {
            appointments = appRepo.findByFilters(
                    LocalDate.now(),
                    date,
                    specialty,
                    doctorName,
                    pageable);
        }

        model.addAttribute("appointments", appointments);
        return "public/available-appointments";
    }

    @GetMapping("/appointments/available")
    public String listAvailableAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorName,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));

        Page<Appointment> appointments = appRepo.findByDoctor_EmailAndDateGreaterThanEqualAndPerson_EmailIsNull(
                null, LocalDate.now(), pageable);

        if (specialty != null || doctorName != null || date != null) {
            appointments = appRepo.findByFilters(
                    LocalDate.now(),
                    date,
                    specialty,
                    doctorName,
                    pageable);
        }

        model.addAttribute("appointments", appointments);
        return "public/available-appointments";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/admin/login")
    public String showAdminLogin() {
        return "redirect:/adminlog";
    }

    @GetMapping("/doctor/login")
    public String showDoctorLogin() {
        return "redirect:/doclog";
    }

    @GetMapping("/patient/login")
    public String showPatientLogin() {
        return "redirect:/patlog";
    }
}
