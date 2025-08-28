package com.nakqeeb.amancare.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityCheckResponse {
    private Long doctorId;
    private LocalDate date;
    private LocalTime time;
    private boolean available;
    private String message;

    // Getters
    public Long getDoctorId() {
        return doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}