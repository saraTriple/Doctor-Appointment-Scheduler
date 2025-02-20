package com.externship.appointment.Appointment_storage;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "appointment_status")
public class AppointmentStatus {
    @Id
    private String status;
    private String description;

    // Default constructor
    public AppointmentStatus() {}

    public AppointmentStatus(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
