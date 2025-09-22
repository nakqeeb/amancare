package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء حالة السجل الطبي غير صالحة
 * Exception thrown when trying to transition to an invalid medical record status
 */
public class InvalidRecordStatusException extends BusinessLogicException {

    public InvalidRecordStatusException(String message) {
        super(message);
    }

    public static InvalidRecordStatusException invalidStatusTransition(String fromStatus, String toStatus) {
        return new InvalidRecordStatusException(
                "لا يمكن تغيير حالة السجل من " + fromStatus + " إلى " + toStatus);
    }

    public static InvalidRecordStatusException cannotModifyInStatus(String status) {
        return new InvalidRecordStatusException("لا يمكن تعديل السجل في حالة " + status);
    }

    public static InvalidRecordStatusException statusLocked() {
        return new InvalidRecordStatusException("السجل مقفل ولا يمكن تعديل حالته");
    }
}
