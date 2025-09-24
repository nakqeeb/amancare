package com.nakqeeb.amancare.dto.response;

/**
 * Statistics DTO for clinic users
 */
public class ClinicUserStats {
    private long totalUsers;
    private long activeUsers;
    private long doctorsCount;
    private long nursesCount;
    private long receptionistsCount;
    private long activeDoctorsCount;
    private long activeNursesCount;
    private long activeReceptionistsCount;

    // Default constructor
    public ClinicUserStats() {
        this.totalUsers = 0;
        this.activeUsers = 0;
        this.doctorsCount = 0;
        this.nursesCount = 0;
        this.receptionistsCount = 0;
        this.activeDoctorsCount = 0;
        this.activeNursesCount = 0;
        this.activeReceptionistsCount = 0;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getDoctorsCount() {
        return doctorsCount;
    }

    public void setDoctorsCount(long doctorsCount) {
        this.doctorsCount = doctorsCount;
    }

    public long getNursesCount() {
        return nursesCount;
    }

    public void setNursesCount(long nursesCount) {
        this.nursesCount = nursesCount;
    }

    public long getReceptionistsCount() {
        return receptionistsCount;
    }

    public void setReceptionistsCount(long receptionistsCount) {
        this.receptionistsCount = receptionistsCount;
    }

    public long getActiveDoctorsCount() {
        return activeDoctorsCount;
    }

    public void setActiveDoctorsCount(long activeDoctorsCount) {
        this.activeDoctorsCount = activeDoctorsCount;
    }

    public long getActiveNursesCount() {
        return activeNursesCount;
    }

    public void setActiveNursesCount(long activeNursesCount) {
        this.activeNursesCount = activeNursesCount;
    }

    public long getActiveReceptionistsCount() {
        return activeReceptionistsCount;
    }

    public void setActiveReceptionistsCount(long activeReceptionistsCount) {
        this.activeReceptionistsCount = activeReceptionistsCount;
    }
}
