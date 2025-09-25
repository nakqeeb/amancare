package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * استجابة البحث في المواعيد مع ترقيم الصفحات
 */
@Schema(description = "نتائج البحث في المواعيد مع ترقيم الصفحات")
public class AppointmentPageResponse {

    @Schema(description = "قائمة المواعيد")
    private java.util.List<AppointmentResponse> appointments;

    @Schema(description = "إجمالي عدد المواعيد")
    private long totalElements;

    @Schema(description = "إجمالي عدد الصفحات")
    private int totalPages;

    @Schema(description = "الصفحة الحالية")
    private int currentPage;

    @Schema(description = "حجم الصفحة")
    private int pageSize;

    @Schema(description = "هل يوجد صفحة سابقة")
    private boolean hasPrevious;

    @Schema(description = "هل يوجد صفحة تالية")
    private boolean hasNext;

    // Constructors
    public AppointmentPageResponse() {}

    public AppointmentPageResponse(java.util.List<AppointmentResponse> appointments,
                                   long totalElements, int totalPages, int currentPage,
                                   int pageSize, boolean hasPrevious, boolean hasNext) {
        this.appointments = appointments;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    // Getters and Setters
    public java.util.List<AppointmentResponse> getAppointments() { return appointments; }
    public void setAppointments(java.util.List<AppointmentResponse> appointments) { this.appointments = appointments; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}