package org.motechproject.prevac.exception;

public class PrevacLookupException extends RuntimeException {

    public PrevacLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrevacLookupException(String message) {
        super(message);
    }
}
