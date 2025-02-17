package com.externship.appointment.Doctor_storage;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/doctor")
public class DoctorScheduleController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/schedule")
    public String showSchedulePage(Model model) {
        // Generate next 30 days
        List<LocalDate> availableDates = IntStream.range(0, 30)
                .mapToObj(i -> LocalDate.now().plusDays(i))
                .collect(Collectors.toList());
        
        // Generate time slots from 8 AM to 8 PM
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(20, 0);
        
        while (!startTime.isAfter(endTime)) {
            timeSlots.add(startTime);
            startTime = startTime.plusMinutes(30);
        }

        model.addAttribute("availableDates", availableDates);
        model.addAttribute("timeSlots", timeSlots);
        return "doctor/schedule";
    }

    @PostMapping("/create-appointments")
    @ResponseBody
    public String createAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam int durationMinutes,
            @RequestParam String doctorEmail) {
        
        Doctor doctor = new Doctor(); // You should get the actual doctor from session or service
        doctor.setEmail(doctorEmail);

        LocalTime currentTime = startTime;
        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) || 
               currentTime.plusMinutes(durationMinutes).equals(endTime)) {
            
            Appointment appointment = new Appointment();
            appointment.setDate(date);
            appointment.setTime(currentTime);
            appointment.setDoctor(doctor);
            appointment.setStatus("AVAILABLE");
            appointment.setPrice(0.0); // Set default price or get from doctor's settings
            
            appointmentRepository.save(appointment);
            currentTime = currentTime.plusMinutes(durationMinutes);
        }

        return "Appointments created successfully";
    }

    @GetMapping("/check-availability")
    @ResponseBody
    public boolean checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam String doctorEmail) {
        
        List<Appointment> existingAppointments = appointmentRepository
            .findByDateAndTimeAndDoctor_Email(date, time, doctorEmail);
        
        return existingAppointments.isEmpty();
    }
}
