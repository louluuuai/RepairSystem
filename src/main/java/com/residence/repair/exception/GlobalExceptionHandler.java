package com.residence.repair.exception;

import com.residence.repair.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestion globale des erreurs (REST).
 * 全局异常处理：把各种异常统一转换为 ApiResponse，并给出正确 HTTP 状态码.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Erreurs de validation (@Valid).
     * DTO 参数校验失败（比如@NotBlank）.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", errors.toString()));
    }

    /**
     * Erreurs de validation (ConstraintViolation).
     * 路径参数/请求参数校验失败.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", ex.getMessage()));
    }

    /**
     * Exceptions métier (ApiException).
     * Service 层抛出的统一业务异常.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }
    /**
     * Erreurs de login（mot de passe incorrect)
     * 登录失败（密码错误）
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials() {
        return ResponseEntity.status(401)
                .body(ApiResponse.error("INVALID_CREDENTIALS", "Invalid email or password"));
    }

    /**
     * AccessDeniedException Spring Security.
     * Spring Security 的拒绝访问（例如 /admin/** 被 tenant 访问).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403)
                .body(ApiResponse.error("FORBIDDEN", "Access denied"));
    }

    /**
     * Fallback: erreur inattendue.
     * 兜底异常，避免把堆栈暴露给前端.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(500)
                .body(ApiResponse.error("INTERNAL_ERROR", "Unexpected error"));
    }
}