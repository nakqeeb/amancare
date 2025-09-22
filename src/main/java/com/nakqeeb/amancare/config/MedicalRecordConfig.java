package com.nakqeeb.amancare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * إعدادات السجلات الطبية
 * Medical Records Configuration Properties
 */
@Configuration
@ConfigurationProperties(prefix = "amancare.medical-records")
@Validated
public class MedicalRecordConfig {

    /**
     * الحد الأقصى لعدد السجلات الطبية لكل مريض في اليوم الواحد
     */
    @Min(1)
    @Max(10)
    private int maxRecordsPerPatientPerDay = 3;

    /**
     * الحد الأقصى لعدد المرفقات لكل سجل طبي
     */
    @Min(0)
    @Max(20)
    private int maxAttachmentsPerRecord = 5;

    /**
     * الحد الأقصى لحجم المرفق بالميجابايت
     */
    @Min(1)
    @Max(100)
    private int maxAttachmentSizeMB = 10;

    /**
     * مدة تعديل السجل الطبي بالساعات بعد الإنشاء
     */
    @Min(1)
    @Max(72)
    private int editingTimeoutHours = 24;

    /**
     * الفترة الافتراضية للمتابعة بالأيام
     */
    @Min(1)
    @Max(365)
    private int defaultFollowUpDays = 7;

    /**
     * تمكين التصديق على السجلات الطبية
     */
    @NotNull
    private boolean enableDigitalSignature = false;

    /**
     * تمكين النسخ الاحتياطي التلقائي
     */
    @NotNull
    private boolean enableAutoBackup = true;

    /**
     * فترة الاحتفاظ بالسجلات المحذوفة بالأيام
     */
    @Min(30)
    @Max(3650)
    private int deletedRecordsRetentionDays = 365;

    /**
     * أنواع الملفات المسموح بها للمرفقات
     */
    private List<String> allowedAttachmentTypes = List.of("pdf", "jpg", "jpeg", "png", "doc", "docx");

    /**
     * تمكين إشعارات المتابعة
     */
    @NotNull
    private boolean enableFollowUpNotifications = true;

    /**
     * عدد أيام الإنذار المبكر للمتابعة
     */
    @Min(1)
    @Max(30)
    private int followUpReminderDays = 1;

    // Getters and Setters
    public int getMaxRecordsPerPatientPerDay() {
        return maxRecordsPerPatientPerDay;
    }

    public void setMaxRecordsPerPatientPerDay(int maxRecordsPerPatientPerDay) {
        this.maxRecordsPerPatientPerDay = maxRecordsPerPatientPerDay;
    }

    public int getMaxAttachmentsPerRecord() {
        return maxAttachmentsPerRecord;
    }

    public void setMaxAttachmentsPerRecord(int maxAttachmentsPerRecord) {
        this.maxAttachmentsPerRecord = maxAttachmentsPerRecord;
    }

    public int getMaxAttachmentSizeMB() {
        return maxAttachmentSizeMB;
    }

    public void setMaxAttachmentSizeMB(int maxAttachmentSizeMB) {
        this.maxAttachmentSizeMB = maxAttachmentSizeMB;
    }

    public int getEditingTimeoutHours() {
        return editingTimeoutHours;
    }

    public void setEditingTimeoutHours(int editingTimeoutHours) {
        this.editingTimeoutHours = editingTimeoutHours;
    }

    public int getDefaultFollowUpDays() {
        return defaultFollowUpDays;
    }

    public void setDefaultFollowUpDays(int defaultFollowUpDays) {
        this.defaultFollowUpDays = defaultFollowUpDays;
    }

    public boolean isEnableDigitalSignature() {
        return enableDigitalSignature;
    }

    public void setEnableDigitalSignature(boolean enableDigitalSignature) {
        this.enableDigitalSignature = enableDigitalSignature;
    }

    public boolean isEnableAutoBackup() {
        return enableAutoBackup;
    }

    public void setEnableAutoBackup(boolean enableAutoBackup) {
        this.enableAutoBackup = enableAutoBackup;
    }

    public int getDeletedRecordsRetentionDays() {
        return deletedRecordsRetentionDays;
    }

    public void setDeletedRecordsRetentionDays(int deletedRecordsRetentionDays) {
        this.deletedRecordsRetentionDays = deletedRecordsRetentionDays;
    }

    public List<String> getAllowedAttachmentTypes() {
        return allowedAttachmentTypes;
    }

    public void setAllowedAttachmentTypes(List<String> allowedAttachmentTypes) {
        this.allowedAttachmentTypes = allowedAttachmentTypes;
    }

    public boolean isEnableFollowUpNotifications() {
        return enableFollowUpNotifications;
    }

    public void setEnableFollowUpNotifications(boolean enableFollowUpNotifications) {
        this.enableFollowUpNotifications = enableFollowUpNotifications;
    }

    public int getFollowUpReminderDays() {
        return followUpReminderDays;
    }

    public void setFollowUpReminderDays(int followUpReminderDays) {
        this.followUpReminderDays = followUpReminderDays;
    }
}