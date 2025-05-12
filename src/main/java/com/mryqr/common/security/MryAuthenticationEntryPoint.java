package com.mryqr.common.security;

import com.mryqr.common.tracing.MryTracingService;
import com.mryqr.core.common.exception.Error;
import com.mryqr.core.common.utils.MryObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import static com.mryqr.core.common.exception.ErrorCode.AUTHENTICATION_FAILED;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class MryAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final MryObjectMapper objectMapper;
    private final MryTracingService mryTracingService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8);
        String traceId = mryTracingService.currentTraceId();
        Error error = new Error(AUTHENTICATION_FAILED, 401, "Authentication failed.", request.getRequestURI(), traceId, null);

        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(error.toErrorResponse()));
        writer.flush();
    }
}
