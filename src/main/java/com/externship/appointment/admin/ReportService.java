package com.externship.appointment.admin;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Doctor_storage.Doctor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    public byte[] generateIncomeReport(List<Appointment> appointments, LocalDate startDate, LocalDate endDate, String format) {
        try {
            if (!"xlsx".equalsIgnoreCase(format)) {
                Document document = new Document();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, out);
                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Income Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("Period: " + startDate + " to " + endDate));
                document.add(new Paragraph("\n"));

                // Create table
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.addCell("Doctor");
                table.addCell("Specialty");
                table.addCell("Appointments");
                table.addCell("Total Income");

                // Group by doctor
                Map<Doctor, List<Appointment>> doctorAppointments = appointments.stream()
                        .collect(java.util.stream.Collectors.groupingBy(Appointment::getDoctor));

                for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                    Doctor doctor = entry.getKey();
                    List<Appointment> doctorAppts = entry.getValue();
                    double totalIncome = doctorAppts.stream().mapToDouble(Appointment::getPrice).sum();

                    table.addCell(doctor.getName());
                    table.addCell(doctor.getSpecialization());
                    table.addCell(String.valueOf(doctorAppts.size()));
                    table.addCell(String.format("$%.2f", totalIncome));
                }

                document.add(table);
                document.close();
                return out.toByteArray();
            } else {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Income Report");
                
                // Create cell styles
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                
                CellStyle currencyStyle = workbook.createCellStyle();
                currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

                // Create header row
                Row headerRow = sheet.createRow(0);
                Cell cell0 = headerRow.createCell(0);
                cell0.setCellValue("Doctor");
                cell0.setCellStyle(headerStyle);
                
                Cell cell1 = headerRow.createCell(1);
                cell1.setCellValue("Specialty");
                cell1.setCellStyle(headerStyle);
                
                Cell cell2 = headerRow.createCell(2);
                cell2.setCellValue("Appointments");
                cell2.setCellStyle(headerStyle);
                
                Cell cell3 = headerRow.createCell(3);
                cell3.setCellValue("Total Income");
                cell3.setCellStyle(headerStyle);

                // Group by doctor
                Map<Doctor, List<Appointment>> doctorAppointments = appointments.stream()
                        .collect(java.util.stream.Collectors.groupingBy(Appointment::getDoctor));

                int rowNum = 1;
                for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                    Doctor doctor = entry.getKey();
                    List<Appointment> doctorAppts = entry.getValue();
                    double totalIncome = doctorAppts.stream().mapToDouble(Appointment::getPrice).sum();

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(doctor.getName());
                    row.createCell(1).setCellValue(doctor.getSpecialization());
                    row.createCell(2).setCellValue(doctorAppts.size());
                    Cell incomeCell = row.createCell(3);
                    incomeCell.setCellValue(totalIncome);
                    incomeCell.setCellStyle(currencyStyle);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating income report: " + e.getMessage(), e);
        }
    }

    public byte[] generateStatusReport(List<Appointment> appointments, LocalDate startDate, LocalDate endDate, String format) {
        try {
            if (!"xlsx".equalsIgnoreCase(format)) {
                Document document = new Document();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, out);
                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Appointment Status Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("Period: " + startDate + " to " + endDate));
                document.add(new Paragraph("\n"));

                // Create table
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.addCell("Status");
                table.addCell("Count");
                table.addCell("Percentage");

                // Group by status
                Map<String, Long> statusCounts = appointments.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                a -> a.getAppointmentStatus().getStatus(),
                                java.util.stream.Collectors.counting()));

                long total = appointments.size();
                for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
                    table.addCell(entry.getKey());
                    table.addCell(String.valueOf(entry.getValue()));
                    table.addCell(String.format("%.1f%%", (entry.getValue() * 100.0) / total));
                }

                document.add(table);
                document.close();
                return out.toByteArray();
            } else {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Status Report");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Status");
                headerRow.createCell(1).setCellValue("Count");
                headerRow.createCell(2).setCellValue("Percentage");

                // Group by status
                Map<String, Long> statusCounts = appointments.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                a -> a.getAppointmentStatus().getStatus(),
                                java.util.stream.Collectors.counting()));

                long total = appointments.size();
                int rowNum = 1;
                for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                    Cell percentCell = row.createCell(2);
                    percentCell.setCellValue((entry.getValue() * 100.0) / total);
                    CellStyle percentStyle = workbook.createCellStyle();
                    percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
                    percentCell.setCellStyle(percentStyle);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating status report: " + e.getMessage(), e);
        }
    }

    public byte[] generateDoctorPerformanceReport(Map<Doctor, List<Appointment>> doctorAppointments, LocalDate startDate, LocalDate endDate, String format) {
        try {
            if (!"xlsx".equalsIgnoreCase(format)) {
                Document document = new Document();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, out);
                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Doctor Performance Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("Period: " + startDate + " to " + endDate));
                document.add(new Paragraph("\n"));

                // Create table
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.addCell("Doctor");
                table.addCell("Specialty");
                table.addCell("Total Appointments");
                table.addCell("Completed");
                table.addCell("Completion Rate");

                for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                    Doctor doctor = entry.getKey();
                    List<Appointment> appointments = entry.getValue();
                    long completed = appointments.stream()
                            .filter(a -> "COMPLETED".equalsIgnoreCase(a.getAppointmentStatus().getStatus()))
                            .count();
                    double completionRate = appointments.isEmpty() ? 0 : (completed * 100.0) / appointments.size();

                    table.addCell(doctor.getName());
                    table.addCell(doctor.getSpecialization());
                    table.addCell(String.valueOf(appointments.size()));
                    table.addCell(String.valueOf(completed));
                    table.addCell(String.format("%.1f%%", completionRate));
                }

                document.add(table);
                document.close();
                return out.toByteArray();
            } else {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Doctor Performance");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Doctor");
                headerRow.createCell(1).setCellValue("Specialty");
                headerRow.createCell(2).setCellValue("Total Appointments");
                headerRow.createCell(3).setCellValue("Completed");
                headerRow.createCell(4).setCellValue("Completion Rate");

                int rowNum = 1;
                for (Map.Entry<Doctor, List<Appointment>> entry : doctorAppointments.entrySet()) {
                    Doctor doctor = entry.getKey();
                    List<Appointment> appointments = entry.getValue();
                    long completed = appointments.stream()
                            .filter(a -> "COMPLETED".equalsIgnoreCase(a.getAppointmentStatus().getStatus()))
                            .count();
                    double completionRate = appointments.isEmpty() ? 0 : (completed * 100.0) / appointments.size();

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(doctor.getName());
                    row.createCell(1).setCellValue(doctor.getSpecialization());
                    row.createCell(2).setCellValue(appointments.size());
                    row.createCell(3).setCellValue(completed);
                    row.createCell(4).setCellValue(completionRate);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating doctor performance report: " + e.getMessage(), e);
        }
    }

    public byte[] generateSpecialtyReport(Map<String, Long> specialtyCounts, LocalDate startDate, LocalDate endDate, String format) {
        try {
            if (!"xlsx".equalsIgnoreCase(format)) {
                Document document = new Document();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, out);
                document.open();

                // Add title
                com.itextpdf.text.Font titleFont= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Specialty Analysis Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("Period: " + startDate + " to " + endDate));
                document.add(new Paragraph("\n"));

                // Create table
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.addCell("Specialty");
                table.addCell("Appointments");
                table.addCell("Percentage");

                long total = specialtyCounts.values().stream().mapToLong(Long::longValue).sum();
                for (Map.Entry<String, Long> entry : specialtyCounts.entrySet()) {
                    table.addCell(entry.getKey());
                    table.addCell(String.valueOf(entry.getValue()));
                    table.addCell(String.format("%.1f%%", (entry.getValue() * 100.0) / total));
                }

                document.add(table);
                document.close();
                return out.toByteArray();
            } else {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Specialty Analysis");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Specialty");
                headerRow.createCell(1).setCellValue("Appointments");
                headerRow.createCell(2).setCellValue("Percentage");

                long total = specialtyCounts.values().stream().mapToLong(Long::longValue).sum();
                int rowNum = 1;
                for (Map.Entry<String, Long> entry : specialtyCounts.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                    Cell percentCell = row.createCell(2);
                    percentCell.setCellValue((entry.getValue() * 100.0) / total);
                    CellStyle percentStyle = workbook.createCellStyle();
                    percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
                    percentCell.setCellStyle(percentStyle);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating specialty report: " + e.getMessage(), e);
        }
    }

    public byte[] generateTimeSlotReport(Map<LocalTime, Long> timeSlotCounts, LocalDate startDate, LocalDate endDate, String format) {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            if (!"xlsx".equalsIgnoreCase(format)) {
                Document document = new Document();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfWriter.getInstance(document, out);
                document.open();

                // Add title
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Time Slot Utilization Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("Period: " + startDate + " to " + endDate));
                document.add(new Paragraph("\n"));

                // Create table
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.addCell("Time Slot");
                table.addCell("Appointments");
                table.addCell("Percentage");

                long total = timeSlotCounts.values().stream().mapToLong(Long::longValue).sum();
                // Sort time slots
                List<Map.Entry<LocalTime, Long>> sortedEntries = new ArrayList<>(timeSlotCounts.entrySet());
                sortedEntries.sort(Map.Entry.comparingByKey());
                
                for (Map.Entry<LocalTime, Long> entry : sortedEntries) {
                    table.addCell(entry.getKey().format(timeFormatter));
                    table.addCell(String.valueOf(entry.getValue()));
                    table.addCell(String.format("%.1f%%", (entry.getValue() * 100.0) / total));
                }

                document.add(table);
                document.close();
                return out.toByteArray();
            } else {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Time Slot Utilization");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Time Slot");
                headerRow.createCell(1).setCellValue("Appointments");
                headerRow.createCell(2).setCellValue("Percentage");

                long total = timeSlotCounts.values().stream().mapToLong(Long::longValue).sum();
                int rowNum = 1;
                // Sort time slots
                List<Map.Entry<LocalTime, Long>> sortedEntries = new ArrayList<>(timeSlotCounts.entrySet());
                sortedEntries.sort(Map.Entry.comparingByKey());
                
                for (Map.Entry<LocalTime, Long> entry : sortedEntries) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey().format(timeFormatter));
                    row.createCell(1).setCellValue(entry.getValue());
                    Cell percentCell = row.createCell(2);
                    percentCell.setCellValue((entry.getValue() * 100.0) / total);
                    CellStyle percentStyle = workbook.createCellStyle();
                    percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
                    percentCell.setCellStyle(percentStyle);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                workbook.close();
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating time slot report: " + e.getMessage(), e);
        }
    }
}
