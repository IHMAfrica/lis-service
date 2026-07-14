package moh.gov.zm.lis.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, "CONFLICT", 409);
    }
}
