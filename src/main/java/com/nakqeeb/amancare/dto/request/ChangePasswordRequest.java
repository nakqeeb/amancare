/**
 * طلب تغيير كلمة المرور
 */
package com.nakqeeb.amancare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "كلمة المرور القديمة مطلوبة")
    private String oldPassword;

    @NotBlank(message = "كلمة المرور الجديدة مطلوبة")
    @Size(min = 6, message = "كلمة المرور الجديدة يجب أن تكون على الأقل 6 أحرف")
    private String newPassword;

    // Constructors
    public ChangePasswordRequest() {}

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
