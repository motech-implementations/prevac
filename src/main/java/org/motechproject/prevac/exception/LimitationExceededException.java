package org.motechproject.prevac.exception;

public class LimitationExceededException extends IllegalArgumentException {

    public LimitationExceededException(String message) {
        super(message);
    }

    public LimitationExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
