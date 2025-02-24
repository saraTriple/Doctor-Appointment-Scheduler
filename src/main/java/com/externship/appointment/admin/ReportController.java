package com.externship.appointment.admin;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Doctor_storage.Doctor;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private com.externship.appointment.Doctor_storage.DoctorRepository doctorRepository;

    // 1. Income Report by Doctor
    @GetMapping("/income")
    public ResponseEntity<byte[]> generateIncomeReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String doctorId,
            @RequestParam String format
    ) {
        List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

        // Apply filters
        if (specialty != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getSpecialization().equals(specialty))
                    .collect(Collectors.toList());
        }

        if (doctorId != null) {
            appointments = appointments.stream()
                    .filter(a -> a.getDoctor().getEmail().equals(doctorId))
                    .collect(Collectors.toList());
        }

        if ("excel".equals(format)) {
            return generateExcelIncomeReport(appointments);
        } else {
            return generatePdfIncomeReport(appointments);
        }
    }

    // 2. Appointment Status Report
    @GetMapping("/appointment-status")
    public ResponseEntity<byte[]> generateStatusReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam String format
    ) {
        List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

        Map<String, Long> statusCounts = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentStatus().getStatus(),
                        Collectors.counting()
                ));

        if ("excel".equals(format)) {
            return generateExcelStatusReport(statusCounts, appointments.size());
        } else {
            return generatePdfStatusReport(statusCounts, appointments.size());
        }
    }

    // 3. Doctor Performance Report
    @GetMapping("/doctor-performance")
    public ResponseEntity<byte[]> generateDoctorPerformanceReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String specialty,
            @RequestParam String format
    ) {
        List<Doctor> doctors = doctorRepository.findAll();
        Map<Doctor, List<Appointment>> doctorAppointments = new HashMap<>();

        for (Doctor doctor : doctors) {
            if (specialty == null || doctor.getSpecialization().equals(specialty)) {
                List<Appointment> appointments = appointmentRepository.findByDoctorAndDateBetween(
                        doctor, startDate, endDate);
                doctorAppointments.put(doctor, appointments);
            }
        }

        if ("excel".equals(format)) {
            return generateExcelPerformanceReport(doctorAppointments);
        } else {
            return generatePdfPerformanceReport(doctorAppointments);
        }
    }

    // 4. Specialty Analysis Report
    @GetMapping("/specialty-analysis")
    public ResponseEntity<byte[]> generateSpecialtyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam String format
    ) {
        List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

        Map<String, Long> specialtyCounts = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getSpecialization(),
                        Collectors.counting()
                ));

        if ("excel".equals(format)) {
            return generateExcelSpecialtyReport(specialtyCounts);
        } else {
            return generatePdfSpecialtyReport(specialtyCounts);
        }
    }

    // 5. Time Slot Utilization Report
    @GetMapping("/timeslot-utilization")
    public ResponseEntity<byte[]> generateTimeSlotReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam String format
    ) {
        List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);

        Map<LocalTime, Long> timeSlotCounts = appointments.stream()
                .collect(Collectors.groupingBy(
                        Appointment::getTime,
                        Collectors.counting()
                ));

        if ("excel".equals(format)) {
            return generateExcelTimeSlotReport(timeSlotCounts);
        } else {
            return generatePdfTimeSlotReport(timeSlotCounts);
        }
    }

    private ResponseEntity<byte[]> generateExcelIncomeReport(List<Appointment> appointments) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Doctor");
            headerRow.createCell(1).setCellValue("Specialty");
            headerRow.createCell(2).setCellValue("Total Appointments");
            headerRow.createCell(3).setCellValue("Total Income");

            // Group appointments by doctor
            Map<Doctor, List<Appointment>> doctorAppointments = appointments.stream()
                    .collect(Collectors.groupingBy(Appointment::getDoctor));

            int rowNum = 1;
            for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                Doctor doctor = entry.getKey();
                List<Appointment> doctorAppts = entry.getValue();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(doctor.getName());
                row.createCell(1).setCellValue(doctor.getSpecialization());
                row.createCell(2).setCellValue(doctorAppts.size());
                row.createCell(3).setCellValue(doctorAppts.stream()
                        .mapToDouble(Appointment::getPrice)
                        .sum());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "income_report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generatePdfPerformanceReport(Map<Doctor, List<Appointment>> doctorAppointments) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Doctor Performance Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Doctor Name", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Specialty", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total Appointments", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Completed Appointments", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Completion Rate", headerFont)));

            // Add data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                Doctor doctor = entry.getKey();
                List<Appointment> appointments = entry.getValue();
                long completedAppointments = appointments.stream()
                        .filter(a -> a.getAppointmentStatus().getStatus().equals("COMPLETED"))
                        .count();
                double completionRate = appointments.isEmpty() ? 0 :
                        (double) completedAppointments / appointments.size() * 100;

                table.addCell(new PdfPCell(new Phrase(doctor.getName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(doctor.getSpecialization(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(appointments.size()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(completedAppointments), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", completionRate), cellFont)));
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "performance_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generatePdfIncomeReport(List<Appointment> appointments) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Income Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Create table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Doctor", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Specialty", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total Appointments", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Total Income", headerFont)));

            // Add data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            Map<Doctor, List<Appointment>> doctorAppointments = appointments.stream()
                    .collect(Collectors.groupingBy(Appointment::getDoctor));
            for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                Doctor doctor = entry.getKey();
                List<Appointment> doctorAppts = entry.getValue();

                table.addCell(new PdfPCell(new Phrase(doctor.getName(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(doctor.getSpecialization(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(doctorAppts.size()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(doctorAppts.stream()
                        .mapToDouble(Appointment::getPrice)
                        .sum()), cellFont)));
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "income_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generateExcelStatusReport(Map<String, Long> statusCounts, long totalAppointments) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Appointment Status Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Status");
            headerRow.createCell(1).setCellValue("Count");
            headerRow.createCell(2).setCellValue("Percentage");

            int rowNum = 1;
            for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
                row.createCell(2).setCellValue(String.format("%.2f%%", (double) entry.getValue() / totalAppointments * 100));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "status_report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generatePdfStatusReport(Map<String, Long> statusCounts, long totalAppointments) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Appointment Status Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Create table
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Status", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Count", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Percentage", headerFont)));

            // Add data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
                table.addCell(new PdfPCell(new Phrase(entry.getKey(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(entry.getValue()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", (double) entry.getValue() / totalAppointments * 100), cellFont)));
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "status_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generateExcelSpecialtyReport(Map<String, Long> specialtyCounts) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Specialty Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Specialty");
            headerRow.createCell(1).setCellValue("Count");

            int rowNum = 1;
            for (Map.Entry<String, Long> entry : specialtyCounts.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "specialty_report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generatePdfSpecialtyReport(Map<String, Long> specialtyCounts) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Specialty Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Create table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Specialty", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Count", headerFont)));

            // Add data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            for (Map.Entry<String, Long> entry : specialtyCounts.entrySet()) {
                table.addCell(new PdfPCell(new Phrase(entry.getKey(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(entry.getValue()), cellFont)));
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "specialty_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generateExcelTimeSlotReport(Map<LocalTime, Long> timeSlotCounts) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Time Slot Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Time Slot");
            headerRow.createCell(1).setCellValue("Count");

            int rowNum = 1;
            for (Map.Entry<LocalTime, Long> entry : timeSlotCounts.entrySet()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey().toString());
                row.createCell(1).setCellValue(entry.getValue());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "time_slot_report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generatePdfTimeSlotReport(Map<LocalTime, Long> timeSlotCounts) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Time Slot Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Create table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Add table headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            table.addCell(new PdfPCell(new Phrase("Time Slot", headerFont)));
            table.addCell(new PdfPCell(new Phrase("Count", headerFont)));

            // Add data rows
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
            for (Map.Entry<LocalTime, Long> entry : timeSlotCounts.entrySet()) {
                table.addCell(new PdfPCell(new Phrase(entry.getKey().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(entry.getValue()), cellFont)));
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "time_slot_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> generateExcelPerformanceReport(Map<Doctor, List<Appointment>> doctorAppointments) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doctor Performance Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Doctor Name");
            headerRow.createCell(1).setCellValue("Specialization");
            headerRow.createCell(2).setCellValue("Total Appointments");
            headerRow.createCell(3).setCellValue("Completed");
            headerRow.createCell(4).setCellValue("Cancelled");
            headerRow.createCell(5).setCellValue("Revenue");

            int rowNum = 1;
            for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                Doctor doctor = entry.getKey();
                List<Appointment> appointments = entry.getValue();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(doctor.getName());
                row.createCell(1).setCellValue(doctor.getSpecialization());
                row.createCell(2).setCellValue(appointments.size());

                long completed = appointments.stream()
                        .filter(a -> "COMPLETED".equals(a.getAppointmentStatus().getStatus()))
                        .count();
                long cancelled = appointments.stream()
                        .filter(a -> "CANCELLED".equals(a.getAppointmentStatus().getStatus()))
                        .count();
                double revenue = appointments.stream()
                        .filter(a -> "COMPLETED".equals(a.getAppointmentStatus().getStatus()))
                        .mapToDouble(a -> a.getPrice())
                        .sum();

                row.createCell(3).setCellValue(completed);
                row.createCell(4).setCellValue(cancelled);
                row.createCell(5).setCellValue(revenue);
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "doctor_performance_report.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
