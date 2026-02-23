package com.residence.repair.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception métier standard avec code + status HTTP.
 */
public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ApiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}