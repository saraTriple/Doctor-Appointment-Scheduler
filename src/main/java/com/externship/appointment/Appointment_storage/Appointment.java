package com.externship.appointment.Appointment_storage;

import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Person_storage.Patient;
import com.externship.appointment.Appointment_storage.AppointmentStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Appointment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate date;
	private LocalTime time;
	private double price;

	@ManyToOne
	@JoinColumn(name = "patient_id", nullable = true)  // Foreign key to Person
	private Patient person;

	@ManyToOne
	@JoinColumn(name = "doctor_id", nullable = false)  // Foreign key to Doctor
	private Doctor doctor;

	@ManyToOne
	@JoinColumn(name = "status_id", nullable = false)  // Foreign key to AppointmentStatus
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
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

	public AppointmentStatus getAppointmentStatus() {
		return appointmentStatus;
	}

	public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
		this.appointmentStatus = appointmentStatus;
	}
}
