package moh.gov.zm.lis.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends BaseException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, "VALIDATION_ERROR", 400);
        this.fieldErrors = fieldErrors;
    }
}
