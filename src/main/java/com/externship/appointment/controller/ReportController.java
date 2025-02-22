package com.externship.appointment.controller;

import com.externship.appointment.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("")
    public String showReportsPage(Model model) {
        model.addAttribute("specialties", reportService.getAllSpecialties());
        return "admin/reports";
    }

    @GetMapping("/appointments")
    public ResponseEntity<InputStreamResource> generateAppointmentsReport(
            @RequestParam String range,
            @RequestParam String format) {
        
        String filename = "appointments_report_" + LocalDateTime.now().toString().replace(":", "-");
        String contentType;
        byte[] report;

        if ("excel".equals(format)) {
            report = reportService.generateAppointmentsExcelReport(range);
            filename += ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            report = reportService.generateAppointmentsPdfReport(range);
            filename += ".pdf";
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(new ByteArrayInputStream(report)));
    }

    @GetMapping("/doctors")
    public ResponseEntity<InputStreamResource> generateDoctorsReport(
            @RequestParam String specialty,
            @RequestParam String format) {
        
        String filename = "doctors_report_" + LocalDateTime.now().toString().replace(":", "-");
        String contentType;
        byte[] report;

        if ("excel".equals(format)) {
            report = reportService.generateDoctorsExcelReport(specialty);
            filename += ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            report = reportService.generateDoctorsPdfReport(specialty);
            filename += ".pdf";
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(new ByteArrayInputStream(report)));
    }

    @GetMapping("/patients")
    public ResponseEntity<InputStreamResource> generatePatientsReport(
            @RequestParam String range,
            @RequestParam String format) {
        
        String filename = "patients_report_" + LocalDateTime.now().toString().replace(":", "-");
        String contentType;
        byte[] report;

        if ("excel".equals(format)) {
            report = reportService.generatePatientsExcelReport(range);
            filename += ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            report = reportService.generatePatientsPdfReport(range);
            filename += ".pdf";
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(new ByteArrayInputStream(report)));
    }
}
