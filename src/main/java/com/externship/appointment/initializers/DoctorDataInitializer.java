package com.externship.appointment.initializers;


import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Order(3)
public class DoctorDataInitializer implements CommandLineRunner {

    private final DoctorRepository doctorRepository;

    public DoctorDataInitializer(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() <  100) { // Insert only if empty
            List<Doctor> doctors = List.of(
                    new Doctor("dr.john@example.com", "Dr. John Doe", "Cardiologist", "MD", "California", "Los Angeles", "pass123"),
                    new Doctor("dr.smith@example.com", "Dr. Alice Smith", "Neurologist", "PhD", "Texas", "Houston", "pass123"),
                    new Doctor("dr.miller@example.com", "Dr. Robert Miller", "Dermatologist", "MD", "New York", "New York City", "pass123"),
                    new Doctor("dr.lee@example.com", "Dr. Kevin Lee", "Orthopedic", "MS", "Florida", "Miami", "pass123"),
                    new Doctor("dr.wilson@example.com", "Dr. Emma Wilson", "Pediatrician", "MD", "Illinois", "Chicago", "pass123"),
                    new Doctor("dr.anderson@example.com", "Dr. James Anderson", "Oncologist", "MD", "Arizona", "Phoenix", "pass123"),
                    new Doctor("dr.thomas@example.com", "Dr. Olivia Thomas", "ENT Specialist", "MD", "Nevada", "Las Vegas", "pass123"),
                    new Doctor("dr.taylor@example.com", "Dr. Sophia Taylor", "Radiologist", "PhD", "Georgia", "Atlanta", "pass123"),
                    new Doctor("dr.white@example.com", "Dr. William White", "Psychiatrist", "MD", "Washington", "Seattle", "pass123"),
                    new Doctor("dr.moore@example.com", "Dr. Daniel Moore", "Urologist", "MD", "Michigan", "Detroit", "pass123"),
                    new Doctor("dr.harris@example.com", "Dr. Emily Harris", "Gynecologist", "MD", "Colorado", "Denver", "pass123"),
                    new Doctor("dr.clark@example.com", "Dr. Ethan Clark", "General Surgeon", "MS", "Oregon", "Portland", "pass123"),
                    new Doctor("dr.hall@example.com", "Dr. Charlotte Hall", "Nephrologist", "MD", "Ohio", "Columbus", "pass123"),
                    new Doctor("dr.young@example.com", "Dr. Benjamin Young", "Hematologist", "PhD", "Minnesota", "Minneapolis", "pass123"),
                    new Doctor("dr.king@example.com", "Dr. Isabella King", "Pulmonologist", "MD", "Pennsylvania", "Philadelphia", "pass123"),
                    new Doctor("dr.scott@example.com", "Dr. Henry Scott", "Endocrinologist", "MD", "Tennessee", "Nashville", "pass123"),
                    new Doctor("dr.green@example.com", "Dr. Victoria Green", "Rheumatologist", "MD", "Missouri", "St. Louis", "pass123"),
                    new Doctor("dr.adams@example.com", "Dr. Samuel Adams", "Gastroenterologist", "MD", "Massachusetts", "Boston", "pass123"),
                    new Doctor("dr.baker@example.com", "Dr. Ava Baker", "Ophthalmologist", "MD", "Indiana", "Indianapolis", "pass123"),
                    new Doctor("dr.carter@example.com", "Dr. Lucas Carter", "Neurologist", "PhD", "North Carolina", "Charlotte", "pass123")
            );

            doctorRepository.saveAll(doctors);
            System.out.println("Inserted 20 doctors into the database.");
        }
    }
}
