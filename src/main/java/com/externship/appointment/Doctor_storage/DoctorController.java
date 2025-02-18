package com.externship.appointment.Doctor_storage;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Appointment_storage.AppointmentService;
import com.externship.appointment.Appointment_storage.AppointmentStatus;
import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import com.externship.appointment.Person_storage.Patient;
import com.externship.appointment.Person_storage.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    @GetMapping("/patients")
    public String showPatients(Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        List<Appointment> doctorAppointments = appointmentService.findByDocId(doctorEmail);
        List<String> patientEmails = doctorAppointments.stream()
                .map(appointment -> appointment.getPerson().getEmail())
                .distinct()
                .collect(Collectors.toList());

        List<Patient> patients = new ArrayList<>();
        for (String email : patientEmails) {
            Patient patient = patientRepository.findByEmail(email);
            if (patient != null) {
                patients.add(patient);
            }
        }
        
        model.addAttribute("patients", patients);
        return "doctor/patients";
    }

    @GetMapping("/appointments")
    public String showAppointments(Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        List<Appointment> appointments = appointmentService.findByDocId(doctorEmail);
        model.addAttribute("appointments", appointments);
        
        return "doctor/appointments";
    }

    @PostMapping("/appointment/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeAppointment(@PathVariable String id, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            if (!appointment.getDoctor().getEmail().equals(doctorEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Not authorized to complete this appointment"));
            }

            AppointmentStatus completedStatus = appointmentStatusRepository.findById("COMPLETED")
                    .orElseThrow(() -> new RuntimeException("COMPLETED status not found"));

            appointment.setAppointmentStatus(completedStatus);
            appointmentRepository.save(appointment);

            return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Appointment marked as completed"));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid appointment ID"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error completing appointment: " + e.getMessage()));
        }
    }
}
