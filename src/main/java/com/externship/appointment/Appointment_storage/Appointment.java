package com.externship.appointment.Appointment_storage;

import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Prescription_storage.Prescription;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
public class Appointment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "date")
	private LocalDate date;

	@Column(name = "time")
	private LocalTime time;

	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name = "patient_email", referencedColumnName = "email", nullable = true)  // Foreign key to Patient's email
	private Patient person;

	@ManyToOne
	@JoinColumn(name = "doctor_email", referencedColumnName = "email", nullable = false)  // Foreign key to Doctor's email
	private Doctor doctor;

	@OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Prescription> prescriptions;

	@ManyToOne
	@JoinColumn(name = "status", nullable = false)  // Foreign key to AppointmentStatus
	private AppointmentStatus appointmentStatus;

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Patient getPerson() {
		return person;
	}

	public void setPerson(Patient person) {
		this.person = person;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	public List<Prescription> getPrescriptions() {
		return prescriptions;
	}

	public void setPrescriptions(List<Prescription> prescription) {
		this.prescriptions = prescription;
	}

	public AppointmentStatus getAppointmentStatus() {
		return appointmentStatus;
	}

	public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
		this.appointmentStatus = appointmentStatus;
	}
}
