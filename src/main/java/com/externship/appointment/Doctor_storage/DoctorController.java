package com.externship.appointment.Doctor_storage;

import com.externship.appointment.Appointment_storage.*;
import com.externship.appointment.Patient_history.PatientHistory;
import com.externship.appointment.Patient_history.PatientHistoryRepository;
import com.externship.appointment.Patient_history.PatientHistoryService;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import com.externship.appointment.Prescription_storage.Prescription;
import com.externship.appointment.Prescription_storage.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    private PatientHistoryService patientHistoryService;

    @Autowired
    private PatientHistoryRepository patientHistoryRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @GetMapping("/patients")
    public String showPatients(Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        List<Appointment> doctorAppointments = appointmentService.findByDoctorId(doctorEmail);

        // Get unique patients with their last appointment
        Map<String, Appointment> latestAppointments = doctorAppointments.stream()
                .filter(apm -> Objects.nonNull(apm.getPerson()))
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getPerson().getEmail(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy((a1, a2) -> {
                                    int dateCompare = a1.getDate().compareTo(a2.getDate());
                                    return dateCompare != 0 ? dateCompare : a1.getTime().compareTo(a2.getTime());
                                }),
                                optional -> optional.orElse(null)
                        )
                ));

        List<Patient> patients = new ArrayList<>();
        for (Appointment appointment : latestAppointments.values()) {
            Patient patient = appointment.getPerson();
            patient.setLastAppointment(appointment);
            patients.add(patient);
        }
        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        model.addAttribute("doctor", doctor.get());
        model.addAttribute("patients", patients);
        return "doctor/patients";
    }

    @GetMapping("/appointments")
    public String showAppointments(Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        List<Appointment> appointments = appointmentService.findByDoctorId(doctorEmail);
        model.addAttribute("appointments", appointments);
        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        model.addAttribute("doctor", doctor.get());
        return "doctor/appointments";
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointmentDetails(@PathVariable Long id, Model model, Authentication authentication) {
        String doctorEmail = authentication.getName();
        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        Optional<Appointment> appointment = appointmentRepository.findById(id);

        if (appointment.isEmpty()) {
            return "redirect:/doctor/appointments";
        }

        // Verify the appointment belongs to the logged-in doctor
        if (!appointment.get().getDoctor().getEmail().equals(doctor.get().getEmail())) {
            throw new RuntimeException("Unauthorized access to appointment");
        }

        if (appointment.get().getPerson() != null) {
            List<PatientHistory> patientHistory = patientHistoryRepository.findByPatientOrderByRecordDateDesc(appointment.get().getPerson());
            model.addAttribute("patientHistory", patientHistory);

            List<Prescription> prescriptions = prescriptionRepository.findByPatientOrderByDateDesc(appointment.get().getPerson().getId());
            model.addAttribute("prescriptions", prescriptions);
        }

        model.addAttribute("appointment", appointment.get());
        model.addAttribute("doctor", doctor.get());
        return "doctor/appointment-details";
    }

    @PostMapping("/appointment/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeAppointment(@PathVariable Long id, HttpSession session) {
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

    @PostMapping("/appointment/{id}/prescribe")
    public String prescribePatient(@PathVariable Long id,
                                   @RequestParam String symptoms,
                                   @RequestParam String diagnosis,
                                   @RequestParam List<String> medications,
                                   @RequestParam String dosageInstructions,
                                   @RequestParam(required = false) String labTests,
                                   @RequestParam(required = false) String doctorNotes,
                                   @RequestParam(required = false) String followUpInstructions,
                                   @RequestParam(required = false) String bloodType,
                                   @RequestParam(required = false) Double weight,
                                   @RequestParam(required = false) Double height,
                                   @RequestParam(required = false) String allergies,
                                   @RequestParam(required = false) String chronicConditions,
                                   @RequestParam(required = false) String familyHistory,
                                   HttpSession session) {

        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) {
            return "redirect:/doctor/appointments";
        }

        Appointment appointment = appointmentOpt.get();

        // Create new prescription
        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setPatient(appointment.getPerson());
//        prescription.setA(appointment.getDoctor());
        prescription.setSymptoms(symptoms);
        prescription.setDiagnosis(diagnosis);
        prescription.setMedications(medications);
        prescription.setDosageInstructions(dosageInstructions);
        prescription.setLabTests(labTests);
        prescription.setDoctorNotes(doctorNotes);
        prescription.setFollowUpInstructions(followUpInstructions);

        prescriptionRepository.save(prescription);

        // Update or create patient history
        PatientHistory patientHistory = new PatientHistory();
        patientHistory.setPatient(appointment.getPerson());
        if (bloodType != null) patientHistory.setBloodType(bloodType);
        if (weight != null) patientHistory.setWeight(weight);
        if (height != null) patientHistory.setHeight(height);
        if (allergies != null) patientHistory.setAllergies(allergies);
        if (chronicConditions != null) patientHistory.setChronicConditions(chronicConditions);
        if (familyHistory != null) patientHistory.setFamilyHistory(familyHistory);
        patientHistory.setRecordDate(LocalDate.now());

        patientHistoryService.save(patientHistory);

        return "redirect:/doctor/appointment/" + id;
    }

    @GetMapping("/patient/{email}")
    public String showPatientDetails(@PathVariable String email, Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        Optional<Patient> patientOpt = patientRepository.findById(email);
        if (patientOpt.isEmpty()) {
            return "redirect:/doctor/patients";
        }

        Patient patient = patientOpt.get();
        List<Appointment> appointments = appointmentService.findByDoctorId(doctorEmail);

        // Filter appointments for this patient
        List<Appointment> patientAppointments = appointments.stream()
                .filter(a -> a.getPerson() != null && a.getPerson().getEmail().equals(email))
                .collect(Collectors.toList());

        List<PatientHistory> patientHistory = patientHistoryRepository.findByPatientOrderByRecordDateDesc(patient);
        List<Prescription> prescriptions = prescriptionRepository.findByPatientOrderByDateDesc(patient.getId());

        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        model.addAttribute("doctor", doctor.get());
        model.addAttribute("patient", patient);
        model.addAttribute("appointments", patientAppointments);
        model.addAttribute("patientHistory", patientHistory);
        model.addAttribute("prescriptions", prescriptions);

        return "doctor/patient-details";
    }
}
