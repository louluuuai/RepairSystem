package com.residence.repair.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Réponse API standard pour toutes les interfaces REST.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;
    private Instant timestamp;

    /**
     * Succès avec données.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code("OK")
                .message("success")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Succès sans données (pour Create/Update/Delete).
     */
    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder()
                .code("OK")
                .message("success")
                .data(null)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Erreur standard.
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();
    }
}
