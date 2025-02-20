package com.externship.appointment.Appointment_storage;

public enum AppointmentStatusEnum {
    SCHEDULED("Scheduled", "Appointment is scheduled"),
    COMPLETED("Completed", "Appointment has been completed"),
    CANCELLED("Cancelled", "Appointment was cancelled"),
    NO_SHOW("No Show", "Patient did not show up");

    private final String status;
    private final String description;

    AppointmentStatusEnum(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
}
