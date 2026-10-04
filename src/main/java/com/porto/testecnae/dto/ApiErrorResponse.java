package com.porto.testecnae.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}
