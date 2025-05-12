package com.mryqr.core.common.exception;

import com.mryqr.common.tracing.MryTracingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.core.common.exception.MryException.accessDeniedException;
import static com.mryqr.core.common.exception.MryException.authenticationException;
import static com.mryqr.core.common.exception.MryException.requestValidationException;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.valueOf;


@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final List<Integer> WARN_CODES = List.of(400, 401, 403, 426, 429);
    private final MryTracingService mryTracingService;


    @ResponseBody
    @ExceptionHandler(MryException.class)
    public ResponseEntity<?> handleMryException(MryException ex, HttpServletRequest request) {
        if (WARN_CODES.contains(ex.getCode().getStatus())) {
            log.warn("Mry warning: {}", ex.getMessage());
        } else {
            log.error("Mry error: {}", ex.getMessage());
        }

        return createErrorResponse(ex, request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<QErrorResponse> handleAccessDinedException(HttpServletRequest request) {
        return createErrorResponse(accessDeniedException(), request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<QErrorResponse> handleAuthenticationFailedException(HttpServletRequest request) {
        return createErrorResponse(authenticationException(), request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<QErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> error = ex.getBindingResult().getFieldErrors().stream()
                .collect(toImmutableMap(FieldError::getField, fieldError -> {
                    String message = fieldError.getDefaultMessage();
                    return isBlank(message) ? "无错误提示。" : message;
                }, (field1, field2) -> field1 + "|" + field2));

        log.error("Method argument validation error[{}]: {}", ex.getParameter().getParameterType().getName(), error);
        MryException exception = requestValidationException(error);
        return createErrorResponse(exception, request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({ServletRequestBindingException.class, HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<QErrorResponse> handleServletRequestBindingException(Exception ex, HttpServletRequest request) {
        MryException exception = requestValidationException("message", "请求验证失败。");
        log.error("Request processing error: {}", ex.getMessage());
        return createErrorResponse(exception, request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleGeneralException(Throwable ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String traceId = mryTracingService.currentTraceId();

        log.error("Error access[{}]:", path, ex);
        Error error = new Error(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getStatus(), "系统错误。", path, traceId, null);
        return new ResponseEntity<>(error.toErrorResponse(), new HttpHeaders(), HttpStatus.valueOf(ErrorCode.SYSTEM_ERROR.getStatus()));
    }

    private ResponseEntity<QErrorResponse> createErrorResponse(MryException exception, String path) {
        String traceId = mryTracingService.currentTraceId();
        Error error = new Error(exception, path, traceId);
        QErrorResponse representation = error.toErrorResponse();
        return new ResponseEntity<>(representation, new HttpHeaders(), valueOf(representation.getError().getStatus()));
    }

}
