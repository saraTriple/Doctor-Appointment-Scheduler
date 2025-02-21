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
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        List<Appointment> doctorAppointments = appointmentService.findByDocId(doctorEmail);

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
            patient.setLastAppointment(appointment);  // Add this field to Patient class
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

        List<Appointment> appointments = appointmentService.findByDocId(doctorEmail);
        model.addAttribute("appointments", appointments);
        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        model.addAttribute("doctor", doctor.get());
        return "doctor/appointments";
    }

    @GetMapping("/appointment/{id}")
    public String showAppointmentDetails(@PathVariable Long id, Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isEmpty()) {
            return "redirect:/doctor/appointments";
        }

        Appointment appointment = appointmentOpt.get();
        model.addAttribute("appointment", appointment);

        if (appointment.getPerson() != null) {
            List<PatientHistory> patientHistory = patientHistoryRepository.findByPatientOrderByRecordDateDesc(appointment.getPerson());
            model.addAttribute("patientHistory", patientHistory);

            List<Prescription> prescriptions = prescriptionRepository.findByPatientOrderByDateDesc(appointment.getPerson().getId());
            model.addAttribute("prescriptions", prescriptions);
        }

        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        model.addAttribute("doctor", doctor.get());

        return "doctor/appointment-details";
    }

    @PostMapping("/appointment/{id}/cancel")
    public String cancelAppointmentById(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("doctor") == null) {
            return "redirect:/fail_login";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setAppointmentStatus(new AppointmentStatus("CANCELLED", "Cancelled by doctor"));
            appointmentRepository.save(appointment);
        }

        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointment/{id}/complete")
    public String completeAppointment(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("doctor") == null) {
            return "redirect:/fail_login";
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setAppointmentStatus(new AppointmentStatus("COMPLETED", "Completed by doctor"));
            appointmentRepository.save(appointment);
        }

        return "redirect:/doctor/appointments";
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
        prescription.setSymptoms(symptoms);
        prescription.setDiagnosis(diagnosis);
        prescription.setMedications(medications);
        prescription.setDosageInstructions(dosageInstructions);
        prescription.setLabTests(labTests);
        prescription.setDoctorNotes(doctorNotes);
        prescription.setFollowUpInstructions(followUpInstructions);
        prescription.setAppointment(appointment);
        prescription.setPatient(appointment.getPerson());
        prescriptionRepository.save(prescription);

        // Create new patient history record
        PatientHistory history = new PatientHistory();
        history.setPatient(appointment.getPerson());
        history.setRecordDate(LocalDate.now());
        history.setSymptoms(symptoms);
        history.setDiagnosis(diagnosis);
        history.setMedications(String.join(", ", medications));

        // Add optional medical history fields
        if (bloodType != null && !bloodType.isEmpty()) history.setBloodType(bloodType);
        if (weight != null) history.setWeight(weight);
        if (height != null) history.setHeight(height);
        if (allergies != null && !allergies.isEmpty()) history.setAllergies(allergies);
        if (chronicConditions != null && !chronicConditions.isEmpty()) history.setChronicConditions(chronicConditions);
        if (familyHistory != null && !familyHistory.isEmpty()) history.setFamilyHistory(familyHistory);

        patientHistoryRepository.save(history);

        // Mark appointment as complete
        AppointmentStatus completedStatus = appointmentStatusRepository.findById("COMPLETED")
                .orElseThrow(() -> new RuntimeException("COMPLETED status not found"));
        appointment.setAppointmentStatus(completedStatus);
        appointmentRepository.save(appointment);
        return "redirect:/doctor/appointment/" + id;
    }

    @GetMapping("/patient/{email}")
    public String showPatientDetails(@PathVariable String email, Model model, HttpSession session) {
        String doctorEmail = (String) session.getAttribute("doctor");
        if (doctorEmail == null) {
            return "redirect:/doclog";
        }

        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isEmpty()) {
            return "redirect:/doctor/patients";
        }

        // Get patient's history
        List<PatientHistory> patientHistory = patientHistoryRepository.findByPatientOrderByRecordDateDesc(patient.get());

        // Get patient's prescriptions
        List<Prescription> prescriptions = prescriptionRepository.findByPatientOrderByDateDesc(patient.get().getId());

        // Get patient's appointments
        List<Appointment> appointments = appointmentRepository.findByPerson_Email(patient.get().getEmail());

        model.addAttribute("patient", patient.get());
        model.addAttribute("patientHistory", patientHistory);
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("appointments", appointments);
        Optional<Doctor> doctor = doctorRepository.findById(doctorEmail);
        model.addAttribute("doctor", doctor.get());
        return "doctor/patient-details";
    }

    @GetMapping("/doctors")
    public String listDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String state,
            Model model) {

        List<Doctor> doctors;
        if (name != null || specialization != null || state != null) {
            // Filter doctors based on search criteria
            doctors = doctorRepository.findAll().stream()
                    .filter(d -> name == null || d.getName().toLowerCase().contains(name.toLowerCase()))
                    .filter(d -> specialization == null || d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .filter(d -> state == null || d.getState().toLowerCase().contains(state.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            doctors = doctorRepository.findAll();
        }

        model.addAttribute("doctors", doctors);
        return "doctorlist";
    }

    @GetMapping("/doctor/{email}")
    public String showDoctorDetails(
            @PathVariable String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String time,
            Model model) {

        Doctor doctor = doctorRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Page<Appointment> appointments;
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));

        if (date != null) {
            appointments = appointmentRepository.findByDateAndDoctor_Email(date, email, pageable);
        } else {
            appointments = appointmentRepository.findByDoctor_EmailAndDateGreaterThanEqual(
                    email, LocalDate.now(), pageable);
        }

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        return "doctor-details";
    }

    @GetMapping("/appointments/available")
    public String showAvailableAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorName,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").ascending().and(Sort.by("time").ascending()));

        // Base query for appointments
        Page<Appointment> appointments = appointmentRepository.findByDateGreaterThanEqual(LocalDate.now(), pageable);

        // Apply filters if provided
        if (date != null) {
            appointments = appointmentRepository.findByDate(date, pageable);
        }

        if (specialty != null || doctorName != null) {
            List<Appointment> filteredAppointments = appointments.stream()
                    .filter(appointment -> {
                        Doctor doctor = appointment.getDoctor();
                        boolean matchesSpecialty = specialty == null ||
                                doctor.getSpecialization().toLowerCase().contains(specialty.toLowerCase());
                        boolean matchesName = doctorName == null ||
                                doctor.getName().toLowerCase().contains(doctorName.toLowerCase());
                        return matchesSpecialty && matchesName;
                    })
                    .collect(Collectors.toList());
            appointments = new PageImpl<>(filteredAppointments, pageable, filteredAppointments.size());
        }

        model.addAttribute("appointments", appointments);
        return "available-appointments";
    }

    @GetMapping("/appointments/my")
    public String showMyAppointments(HttpSession session, Model model) {
        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return "redirect:/patlog";
        }

        List<Appointment> appointments = appointmentRepository.findByPerson_Email(patientEmail);
        model.addAttribute("appointments", appointments);
        return "my-appointments";
    }

    @PostMapping("/appointments/book")
    @ResponseBody
    public ResponseEntity<?> bookAppointment(
            @RequestParam Long appointmentId,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to book appointments");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Patient patient = patientRepository.findById(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Check if appointment is already booked
        if (appointment.getPerson() != null) {
            return ResponseEntity.badRequest()
                    .body("This appointment is already booked");
        }

        // Book the appointment
        appointment.setPerson(patient);
        AppointmentStatus status = appointmentStatusRepository.findByStatus("UPCOMING")
                .orElseThrow(() -> new RuntimeException("Status not found"));
        appointment.setAppointmentStatus(status);

        appointmentRepository.save(appointment);

        return ResponseEntity.ok()
                .body("Appointment booked successfully");
    }

    @PostMapping("/appointments/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAppointment(
            @RequestParam Long appointmentId,
            HttpSession session) {

        String patientEmail = (String) session.getAttribute("patient");
        if (patientEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to cancel appointments");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify this is the patient's appointment
        if (!appointment.getPerson().getEmail().equals(patientEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only cancel your own appointments");
        }

        // Cancel the appointment
        AppointmentStatus status = appointmentStatusRepository.findByStatus("CANCELLED")
                .orElseThrow(() -> new RuntimeException("Status not found"));
        appointment.setAppointmentStatus(status);
        appointment.setPerson(null);

        appointmentRepository.save(appointment);

        return ResponseEntity.ok()
                .body("Appointment cancelled successfully");
    }
}
