package com.externship.appointment.controller;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentService;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("//admin/doctor")
public class DoctorAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping("/appointments")
    public String getDoctorAppointments(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Optional<Doctor> doctor = doctorRepository.findById(authentication.getName());
        Page<Appointment> appointmentPage = appointmentService.findByDoctorId(doctor.get().getEmail(), PageRequest.of(page, size));
        
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        // Add page size options
        int[] pageSizes = {5, 10, 20, 50};
        model.addAttribute("pageSizes", pageSizes);

        return "doctor/appointments";
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Doctor> doctor = doctorRepository.findById(authentication.getName());
        Optional<Appointment> appointment = appointmentService.findById(id);
        
        if (appointment.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        
        // Verify the appointment belongs to the logged-in doctor
        if (!appointment.get().getDoctor().getEmail().equals(doctor.get().getEmail())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }
        
        model.addAttribute("appointment", appointment.get());
        return "doctor/appointment-details";
    }

    @GetMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, Authentication authentication) {
        Optional<Doctor> doctor = doctorRepository.findById(authentication.getName());
        Optional<Appointment> appointment = appointmentService.findById(id);
        
        if (appointment.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        
        // Verify the appointment belongs to the logged-in doctor
        if (!appointment.get().getDoctor().getEmail().equals(doctor.get().getEmail())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }
        
        appointmentService.cancelAppointment(id);
        return "redirect:/doctor/appointments";
    }
}
