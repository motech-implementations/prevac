package org.motechproject.prevac.exception;

public class PrevacExportException extends RuntimeException {

    public PrevacExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrevacExportException(String message) {
        super(message);
    }
}
