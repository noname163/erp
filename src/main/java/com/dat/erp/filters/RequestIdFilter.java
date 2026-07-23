package com.dat.erp.filters;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-Request-Id";
    public static final String TRACE_HEADER_NAME = "X-Trace-Id";
    public static final String MDC_KEY = "requestId";
    public static final String TRACE_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(HEADER_NAME);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        String traceId = request.getHeader(TRACE_HEADER_NAME);
        if (traceId == null || traceId.isBlank()) {
            traceId = requestId;
        }

        MDC.put(MDC_KEY, requestId);
        MDC.put(TRACE_MDC_KEY, traceId);
        response.setHeader(HEADER_NAME, requestId);
        response.setHeader(TRACE_HEADER_NAME, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_MDC_KEY);
            MDC.remove(MDC_KEY);
        }
    }
}

