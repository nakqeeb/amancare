package com.nakqeeb.amancare.dto.response;

public class DoctorSummaryResponse {
    private Long id;
    private String fullName;
    private String specialization;

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}