package com.externship.appointment.service;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    public Set<String> getAllSpecialties() {
        return doctorRepository.findAll().stream()
                .map(Doctor::getSpecialty)
                .collect(Collectors.toSet());
    }

    public byte[] generateAppointmentsExcelReport(String range) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Appointments Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Time");
            headerRow.createCell(2).setCellValue("Doctor");
            headerRow.createCell(3).setCellValue("Patient");
            headerRow.createCell(4).setCellValue("Status");

            // Add data rows
            List<Appointment> appointments = getAppointmentsByRange(range);
            int rowNum = 1;
            for (Appointment appointment : appointments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(appointment.getDate().toString());
                row.createCell(1).setCellValue(appointment.getTime().toString());
                row.createCell(2).setCellValue(appointment.getDoctor().getName());
                row.createCell(3).setCellValue(appointment.getPatient().getName());
                row.createCell(4).setCellValue(appointment.getStatus());
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    public byte[] generateAppointmentsPdfReport(String range) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            document.addTitle("Appointments Report");

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Appointments Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Add header cells
            addTableHeader(table, new String[]{"Date", "Time", "Doctor", "Patient", "Status"});

            // Add data rows
            List<Appointment> appointments = getAppointmentsByRange(range);
            for (Appointment appointment : appointments) {
                table.addCell(appointment.getDate().toString());
                table.addCell(appointment.getTime().toString());
                table.addCell(appointment.getDoctor().getName());
                table.addCell(appointment.getPatient().getName());
                table.addCell(appointment.getStatus());
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public byte[] generateDoctorsExcelReport(String specialty) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doctors Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Specialty");
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Phone");
            headerRow.createCell(4).setCellValue("Total Appointments");

            // Add data rows
            List<Doctor> doctors = "all".equals(specialty) 
                ? doctorRepository.findAll()
                : doctorRepository.findBySpecialty(specialty);

            int rowNum = 1;
            for (Doctor doctor : doctors) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(doctor.getName());
                row.createCell(1).setCellValue(doctor.getSpecialty());
                row.createCell(2).setCellValue(doctor.getEmail());
                row.createCell(3).setCellValue(doctor.getPhone());
                row.createCell(4).setCellValue(doctor.getAppointments().size());
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    public byte[] generateDoctorsPdfReport(String specialty) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            document.addTitle("Doctors Report");

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Doctors Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Add header cells
            addTableHeader(table, new String[]{"Name", "Specialty", "Email", "Phone", "Total Appointments"});

            // Add data rows
            List<Doctor> doctors = "all".equals(specialty) 
                ? doctorRepository.findAll()
                : doctorRepository.findBySpecialty(specialty);

            for (Doctor doctor : doctors) {
                table.addCell(doctor.getName());
                table.addCell(doctor.getSpecialty());
                table.addCell(doctor.getEmail());
                table.addCell(doctor.getPhone());
                table.addCell(String.valueOf(doctor.getAppointments().size()));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public byte[] generatePatientsExcelReport(String range) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Patients Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Email");
            headerRow.createCell(2).setCellValue("Phone");
            headerRow.createCell(3).setCellValue("Registration Date");
            headerRow.createCell(4).setCellValue("Total Appointments");

            // Add data rows
            List<Patient> patients = getPatientsByRange(range);
            int rowNum = 1;
            for (Patient patient : patients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(patient.getName());
                row.createCell(1).setCellValue(patient.getEmail());
                row.createCell(2).setCellValue(patient.getPhone());
                row.createCell(3).setCellValue(patient.getRegistrationDate().toString());
                row.createCell(4).setCellValue(patient.getAppointments().size());
            }

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    public byte[] generatePatientsPdfReport(String range) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();
            document.addTitle("Patients Report");

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Patients Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            // Add header cells
            addTableHeader(table, new String[]{"Name", "Email", "Phone", "Registration Date", "Total Appointments"});

            // Add data rows
            List<Patient> patients = getPatientsByRange(range);
            for (Patient patient : patients) {
                table.addCell(patient.getName());
                table.addCell(patient.getEmail());
                table.addCell(patient.getPhone());
                table.addCell(patient.getRegistrationDate().toString());
                table.addCell(String.valueOf(patient.getAppointments().size()));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private List<Appointment> getAppointmentsByRange(String range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        switch (range) {
            case "today":
                startDate = now.toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = now.minusWeeks(1);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            case "year":
                startDate = now.minusYears(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid date range: " + range);
        }

        // Filter appointments in memory since we don't have a specific repository method
        return appointmentRepository.findAll().stream()
                .filter(appointment -> {
                    LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getDate(), appointment.getTime());
                    return appointmentDateTime.isAfter(startDate);
                })
                .collect(Collectors.toList());
    }

    private List<Patient> getPatientsByRange(String range) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;

        switch (range) {
            case "month":
                startDate = now.minusMonths(1);
                break;
            case "quarter":
                startDate = now.minusMonths(3);
                break;
            case "year":
                startDate = now.minusYears(1);
                break;
            case "all":
                return patientRepository.findAll();
            default:
                throw new IllegalArgumentException("Invalid date range: " + range);
        }

        // Filter patients in memory since we don't have a specific repository method
        return patientRepository.findAll().stream()
                .filter(patient -> patient.getRegistrationDate().isAfter(startDate))
                .collect(Collectors.toList());
    }
}
