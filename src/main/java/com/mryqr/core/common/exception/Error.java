package com.mryqr.core.common.exception;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

import static java.time.Instant.now;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class Error {
    private ErrorCode code;
    private String message;
    private String userMessage;
    private int status;
    private String path;
    private Instant timestamp;
    private String traceId;
    private Map<String, Object> data;

    public Error(MryException ex, String path, String traceId) {
        ErrorCode errorCode = ex.getCode();
        this.code = errorCode;
        this.message = ex.getMessage();
        this.userMessage = ex.getUserMessage();
        this.status = errorCode.getStatus();
        this.path = path;
        this.timestamp = now();
        this.traceId = traceId;
        this.data = ex.getData();
    }

    public Error(ErrorCode code, int status, String message, String path, String traceId, Map<String, Object> data) {
        this.code = code;
        this.message = message;
        this.userMessage = message;
        this.status = status;
        this.path = path;
        this.timestamp = now();
        this.traceId = traceId;
        this.data = data;
    }

    public QErrorResponse toErrorResponse() {
        return QErrorResponse.builder().error(this).build();
    }

}
