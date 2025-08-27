// =============================================================================
// Constants - الثوابت
// =============================================================================

package com.nakqeeb.amancare.util;

/**
 * ثوابت التطبيق
 */
public class Constants {

    // ثوابت المرضى
    public static final class Patients {
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_PHONE_LENGTH = 50;
        public static final int MAX_EMAIL_LENGTH = 255;
        public static final int MAX_NOTES_LENGTH = 1000;

        // بادئات أرقام المرضى
        public static final String PATIENT_NUMBER_PREFIX = "P";
    }

    // ثوابت المواعيد
    public static final class Appointments {
        public static final int DEFAULT_DURATION_MINUTES = 30;
        public static final int MAX_DAILY_APPOINTMENTS = 50;
    }

    // ثوابت النظام
    public static final class System {
        public static final int DEFAULT_PAGE_SIZE = 10;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_DIRECTION = "asc";
    }

    // ثوابت JWT
    public static final class Jwt {
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String AUTH_HEADER = "Authorization";
    }

    // رسائل الأخطاء
    public static final class ErrorMessages {
        public static final String CLINIC_NOT_FOUND = "العيادة غير موجودة";
        public static final String PATIENT_NOT_FOUND = "المريض غير موجود";
        public static final String USER_NOT_FOUND = "المستخدم غير موجود";
        public static final String DUPLICATE_PHONE = "رقم الهاتف مستخدم بالفعل";
        public static final String INVALID_PHONE = "رقم الهاتف غير صحيح";
        public static final String ACCESS_DENIED = "ليس لديك صلاحية للوصول إلى هذا المورد";
    }

    // رسائل النجاح
    public static final class SuccessMessages {
        public static final String PATIENT_CREATED = "تم إنشاء المريض بنجاح";
        public static final String PATIENT_UPDATED = "تم تحديث بيانات المريض بنجاح";
        public static final String PATIENT_DELETED = "تم حذف المريض بنجاح";
        public static final String PATIENT_REACTIVATED = "تم إعادة تفعيل المريض بنجاح";
    }
}