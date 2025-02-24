package com.externship.appointment.admin;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/reports")
public class ReportController {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ReportService reportService;

    // 1. Income Report by Doctor
    @GetMapping("/income")
    public ResponseEntity<byte[]> generateIncomeReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorId,
            @RequestParam(defaultValue = "xlsx") String format
    ) {
        try {
            // Handle old date range format if present
            if (dateRange != null && !dateRange.isEmpty()) {
                String[] dates = dateRange.split(" to ");
                if (dates.length != 2) {
                    throw new IllegalArgumentException("Invalid date range format. Expected 'startDate to endDate'");
                }
                startDate = LocalDate.parse(dates[0].trim());
                endDate = LocalDate.parse(dates[1].trim());
            }
            
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Either dateRange or both startDate and endDate must be provided");
            }

            List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

            // Filter by specialty if provided
            if (specialty != null && !specialty.isEmpty()) {
                appointments = appointments.stream()
                        .filter(a -> a.getDoctor().getSpecialization().equals(specialty))
                        .collect(Collectors.toList());
            }

            // Filter by doctor if provided
            if (doctorId != null && !doctorId.isEmpty()) {
                appointments = appointments.stream()
                        .filter(a -> a.getDoctor().getEmail().equals(doctorId))
                        .collect(Collectors.toList());
            }

            String normalizedFormat = format.toLowerCase();
            // Support both xlsx and excel values
            if ("excel".equals(normalizedFormat)) {
                normalizedFormat = "xlsx";
            }
            byte[] report = reportService.generateIncomeReport(appointments, startDate, endDate, normalizedFormat);
            String filename = String.format("income_report_%s_to_%s.%s", startDate, endDate, normalizedFormat);
            MediaType mediaType;
            if ("xlsx".equals(normalizedFormat)) {
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                mediaType = MediaType.APPLICATION_PDF;
                normalizedFormat = "pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .body(report);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating report: " + e.getMessage(), e);
        }
    }

    // 2. Appointment Status Report
    @GetMapping("/appointment-status")
    public ResponseEntity<byte[]> generateStatusReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "xlsx") String format
    ) {
        try {
            // Handle old date range format if present
            if (dateRange != null && !dateRange.isEmpty()) {
                String[] dates = dateRange.split(" to ");
                if (dates.length != 2) {
                    throw new IllegalArgumentException("Invalid date range format. Expected 'startDate to endDate'");
                }
                startDate = LocalDate.parse(dates[0].trim());
                endDate = LocalDate.parse(dates[1].trim());
            }
            
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Either dateRange or both startDate and endDate must be provided");
            }

            List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                appointments = appointments.stream()
                        .filter(a -> a.getAppointmentStatus().getStatus().equalsIgnoreCase(status))
                        .collect(Collectors.toList());
            }

            String normalizedFormat = format.toLowerCase();
            // Support both xlsx and excel values
            if ("excel".equals(normalizedFormat)) {
                normalizedFormat = "xlsx";
            }
            byte[] report = reportService.generateStatusReport(appointments, startDate, endDate, normalizedFormat);
            String filename = String.format("status_report_%s_to_%s.%s", startDate, endDate, normalizedFormat);
            MediaType mediaType;
            if ("xlsx".equals(normalizedFormat)) {
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                mediaType = MediaType.APPLICATION_PDF;
                normalizedFormat = "pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .body(report);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating report: " + e.getMessage(), e);
        }
    }

    // 3. Doctor Performance Report
    @GetMapping("/doctor-performance")
    public ResponseEntity<byte[]> generateDoctorPerformanceReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String specialty,
            @RequestParam(defaultValue = "xlsx") String format
    ) {
        try {
            // Handle old date range format if present
            if (dateRange != null && !dateRange.isEmpty()) {
                String[] dates = dateRange.split(" to ");
                if (dates.length != 2) {
                    throw new IllegalArgumentException("Invalid date range format. Expected 'startDate to endDate'");
                }
                startDate = LocalDate.parse(dates[0].trim());
                endDate = LocalDate.parse(dates[1].trim());
            }
            
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Either dateRange or both startDate and endDate must be provided");
            }

            List<Doctor> doctors = doctorRepository.findAll();
            Map<Doctor, List<Appointment>> doctorAppointments = new HashMap<>();

            // Filter doctors by specialty if provided
            if (specialty != null && !specialty.isEmpty()) {
                doctors = doctors.stream()
                        .filter(d -> d.getSpecialization().equals(specialty))
                        .collect(Collectors.toList());
            }

            for (Doctor doctor : doctors) {
                List<Appointment> appointments = appointmentRepository.findByDoctorAndDateBetween(
                        doctor, startDate, endDate);
                doctorAppointments.put(doctor, appointments);
            }

            String normalizedFormat = format.toLowerCase();
            // Support both xlsx and excel values
            if ("excel".equals(normalizedFormat)) {
                normalizedFormat = "xlsx";
            }
            byte[] report = reportService.generateDoctorPerformanceReport(doctorAppointments, startDate, endDate, normalizedFormat);
            String filename = String.format("doctor_performance_report_%s_to_%s.%s", startDate, endDate, normalizedFormat);
            MediaType mediaType;
            if ("xlsx".equals(normalizedFormat)) {
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                mediaType = MediaType.APPLICATION_PDF;
                normalizedFormat = "pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .body(report);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating report: " + e.getMessage(), e);
        }
    }

    // 4. Specialty Analysis Report
    @GetMapping("/specialty-analysis")
    public ResponseEntity<byte[]> generateSpecialtyReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String dateRange,
            @RequestParam(defaultValue = "xlsx") String format
    ) {
        try {
            // Handle old date range format if present
            if (dateRange != null && !dateRange.isEmpty()) {
                String[] dates = dateRange.split(" to ");
                if (dates.length != 2) {
                    throw new IllegalArgumentException("Invalid date range format. Expected 'startDate to endDate'");
                }
                startDate = LocalDate.parse(dates[0].trim());
                endDate = LocalDate.parse(dates[1].trim());
            }
            
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Either dateRange or both startDate and endDate must be provided");
            }

            List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

            Map<String, Long> specialtyCounts = appointments.stream()
                    .map(appointment -> appointment.getDoctor().getSpecialization())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            String normalizedFormat = format.toLowerCase();
            // Support both xlsx and excel values
            if ("excel".equals(normalizedFormat)) {
                normalizedFormat = "xlsx";
            }
            byte[] report = reportService.generateSpecialtyReport(specialtyCounts, startDate, endDate, normalizedFormat);
            String filename = String.format("specialty_report_%s_to_%s.%s", startDate, endDate, normalizedFormat);
            MediaType mediaType;
            if ("xlsx".equals(normalizedFormat)) {
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                mediaType = MediaType.APPLICATION_PDF;
                normalizedFormat = "pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .body(report);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating report: " + e.getMessage(), e);
        }
    }

    // 5. Time Slot Utilization Report
    @GetMapping("/timeslot-utilization")
    public ResponseEntity<byte[]> generateTimeSlotReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String dateRange,
            @RequestParam(defaultValue = "xlsx") String format
    ) {
        try {
            // Handle old date range format if present
            if (dateRange != null && !dateRange.isEmpty()) {
                String[] dates = dateRange.split(" to ");
                if (dates.length != 2) {
                    throw new IllegalArgumentException("Invalid date range format. Expected 'startDate to endDate'");
                }
                startDate = LocalDate.parse(dates[0].trim());
                endDate = LocalDate.parse(dates[1].trim());
            }
            
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Either dateRange or both startDate and endDate must be provided");
            }

            List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

            Map<LocalTime, Long> timeSlotCounts = appointments.stream()
                    .map(Appointment::getTime)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            String normalizedFormat = format.toLowerCase();
            // Support both xlsx and excel values
            if ("excel".equals(normalizedFormat)) {
                normalizedFormat = "xlsx";
            }
            byte[] report = reportService.generateTimeSlotReport(timeSlotCounts, startDate, endDate, normalizedFormat);
            String filename = String.format("timeslot_report_%s_to_%s.%s", startDate, endDate, normalizedFormat);
            MediaType mediaType;
            if ("xlsx".equals(normalizedFormat)) {
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                mediaType = MediaType.APPLICATION_PDF;
                normalizedFormat = "pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(mediaType)
                    .body(report);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd format.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error generating report: " + e.getMessage(), e);
    }
    }
}
