package moh.gov.zm.lis.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException{
    private final String errorCode;
    private final int httpStatus;

    protected BaseException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
