package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "استجابة الحذف النهائي للمريض")
public class PermanentDeleteResponse {

    @Schema(description = "معرف المريض المحذوف", example = "123")
    private Long patientId;

    @Schema(description = "رقم المريض", example = "PAT-2024-001")
    private String patientNumber;

    @Schema(description = "اسم المريض المحذوف", example = "أحمد محمد علي")
    private String patientName;

    @Schema(description = "وقت الحذف", example = "2024-01-15T10:30:00")
    private LocalDateTime deletedAt;

    @Schema(description = "معرف المستخدم الذي قام بالحذف", example = "1")
    private Long deletedByUserId;

    @Schema(description = "عدد السجلات المحذوفة", example = "{\"appointments\": 5, \"medicalRecords\": 10, \"invoices\": 3}")
    private Map<String, Integer> recordsDeleted;

    @Schema(description = "رسالة تأكيد", example = "تم حذف المريض وجميع البيانات المرتبطة نهائياً")
    private String confirmationMessage;

    // Constructors
    public PermanentDeleteResponse() {
        this.confirmationMessage = "تم حذف المريض وجميع البيانات المرتبطة نهائياً. لا يمكن التراجع عن هذا الإجراء.";
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getDeletedByUserId() {
        return deletedByUserId;
    }

    public void setDeletedByUserId(Long deletedByUserId) {
        this.deletedByUserId = deletedByUserId;
    }

    public Map<String, Integer> getRecordsDeleted() {
        return recordsDeleted;
    }

    public void setRecordsDeleted(Map<String, Integer> recordsDeleted) {
        this.recordsDeleted = recordsDeleted;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }
}