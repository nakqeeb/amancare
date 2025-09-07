// UserSummaryResponse.java (للاستخدام في القوائم المختصرة)
        package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import lombok.Data;

/**
 * ملخص المستخدم للقوائم
 */
@Data
public class UserSummaryResponse {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String specialization;
    private Boolean isActive;

    /**
     * تحويل من كائن المستخدم إلى ملخص
     */
    public static UserSummaryResponse fromEntity(User user) {
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setSpecialization(user.getSpecialization());
        response.setIsActive(user.getIsActive());
        return response;
    }
}