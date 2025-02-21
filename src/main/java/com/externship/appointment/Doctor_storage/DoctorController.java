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
    public String cancelAppointment(@PathVariable Long id, HttpSession session) {
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
}
