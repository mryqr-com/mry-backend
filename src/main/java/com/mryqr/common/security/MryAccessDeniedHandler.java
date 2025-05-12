package com.mryqr.common.security;

import com.mryqr.common.exception.Error;
import com.mryqr.common.tracing.MryTracingService;
import com.mryqr.common.utils.MryObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import static com.mryqr.common.exception.ErrorCode.ACCESS_DENIED;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class MryAccessDeniedHandler implements AccessDeniedHandler {
    private final MryObjectMapper objectMapper;
    private final MryTracingService mryTracingService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(403);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8);

        String traceId = mryTracingService.currentTraceId();
        Error error = new Error(ACCESS_DENIED, 403, "Access denied.", request.getRequestURI(), traceId, null);
        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(error.toErrorResponse()));
        writer.flush();
    }
}
