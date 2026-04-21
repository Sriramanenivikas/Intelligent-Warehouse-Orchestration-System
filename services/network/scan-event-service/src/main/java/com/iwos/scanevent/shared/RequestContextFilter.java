package com.iwos.scanevent.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component("scanEventRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .filter(value -> !value.isBlank())
                .orElse(UUID.randomUUID().toString());
        String traceId = Optional.ofNullable(request.getHeader("X-Trace-Id"))
                .filter(value -> !value.isBlank())
                .orElse(requestId);
        MDC.put(REQUEST_ID, requestId);
        MDC.put(TRACE_ID, traceId);
        request.setAttribute(REQUEST_ID, requestId);
        response.setHeader("X-Request-Id", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
            MDC.remove(TRACE_ID);
        }
    }
}
