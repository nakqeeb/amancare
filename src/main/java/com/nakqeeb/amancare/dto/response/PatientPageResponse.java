package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * استجابة البحث في المرضى مع ترقيم الصفحات
 */
@Schema(description = "نتائج البحث في المرضى مع ترقيم الصفحات")
public class PatientPageResponse {

    @Schema(description = "قائمة المرضى")
    private java.util.List<PatientSummaryResponse> patients;

    @Schema(description = "إجمالي عدد المرضى")
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
    public PatientPageResponse() {}

    public PatientPageResponse(java.util.List<PatientSummaryResponse> patients,
                               long totalElements, int totalPages, int currentPage,
                               int pageSize, boolean hasPrevious, boolean hasNext) {
        this.patients = patients;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    // Getters and Setters
    public java.util.List<PatientSummaryResponse> getPatients() { return patients; }
    public void setPatients(java.util.List<PatientSummaryResponse> patients) { this.patients = patients; }

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