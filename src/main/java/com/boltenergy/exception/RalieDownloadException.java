package com.boltenergy.exception;

public class RalieDownloadException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RalieDownloadException(String message) {
        super(message);
    }

    public RalieDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
