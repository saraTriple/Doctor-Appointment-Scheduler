package com.externship.appointment.Doctor_storage;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Appointment_storage.AppointmentStatus;
import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/doctor")
public class DoctorScheduleController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    @GetMapping("/schedule")
    public ModelAndView showSchedulePage(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        String doctorEmail = (String) session.getAttribute("doctor");
        
        if (doctorEmail == null) {
            modelAndView.setViewName("redirect:/doclog");
            return modelAndView;
        }

        // Generate available dates (next 7 days)
        List<LocalDate> availableDates = IntStream.range(0, 7)
                .mapToObj(i -> LocalDate.now().plusDays(i))
                .collect(Collectors.toList());

        // Generate time slots (9 AM to 5 PM)
        List<LocalTime> timeSlots = IntStream.range(9, 17)
                .mapToObj(hour -> LocalTime.of(hour, 0))
                .collect(Collectors.toList());

        modelAndView.addObject("availableDates", availableDates);
        modelAndView.addObject("timeSlots", timeSlots);
        modelAndView.setViewName("doctor/schedule");
        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        modelAndView.addObject("doctor", doctor.get());
        return modelAndView;
    }

    @PostMapping("/create-appointments")
    public ResponseEntity<Map<String, Object>> createAppointments(@RequestBody Map<String, String> request, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            String dateStr = request.get("date");
            String startTimeStr = request.get("startTime");
            String endTimeStr = request.get("endTime");
            String durationMinutes = request.get("duration");

            if (dateStr == null || startTimeStr == null || endTimeStr == null || durationMinutes == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Missing required fields"));
            }

            LocalDate date = LocalDate.parse(dateStr);
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);

            if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "End time must be after start time"));
            }

            Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
            if (doctor.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Doctor not found"));
            }

            // Get the AVAILABLE status
            AppointmentStatus availableStatus = appointmentStatusRepository.findById("AVAILABLE")
                    .orElseThrow(() -> new RuntimeException("AVAILABLE status not found"));

            LocalTime currentTime = startTime;
            while (currentTime.plus(Duration.ofMinutes(Long.parseLong(durationMinutes))).isBefore(endTime) || 
                   currentTime.plus(Duration.ofMinutes(Long.parseLong(durationMinutes))).equals(endTime)) {
                
                // Check if appointment already exists
                if (!appointmentRepository.findByDateAndTimeAndDoctor_Email(date, currentTime, doctorEmail).isEmpty()) {
                    currentTime = currentTime.plusMinutes(Integer.parseInt(durationMinutes));
                    continue;
                }

                Appointment appointment = new Appointment();
                appointment.setDate(date);
                appointment.setTime(currentTime);
                appointment.setDoctor(doctor.get());
                appointment.setAppointmentStatus(availableStatus);
                
                appointmentRepository.save(appointment);
                currentTime = currentTime.plusMinutes(Integer.parseInt(durationMinutes));
            }

            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Appointments created successfully"));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid date or time format"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error creating appointments: " + e.getMessage()));
        }
    }

    @GetMapping("/check-availability")
    @ResponseBody
    public boolean checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam String doctorEmail) {
        
        Optional<Appointment> existingAppointments = appointmentRepository
            .findByDateAndTimeAndDoctor_Email(date, time, doctorEmail);
        
        return existingAppointments.isEmpty();
    }
}
