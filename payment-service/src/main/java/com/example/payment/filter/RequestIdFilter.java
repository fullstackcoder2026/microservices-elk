package com.example.payment.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String SPAN_ID_MDC_KEY = "spanId";

    private final Tracer tracer;

    public RequestIdFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Get or generate requestId
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            // Add to MDC
            MDC.put(REQUEST_ID_MDC_KEY, requestId);

            // Add trace information to MDC
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                String spanId = currentSpan.context().spanId();
                
                MDC.put(TRACE_ID_MDC_KEY, traceId);
                MDC.put(SPAN_ID_MDC_KEY, spanId);
            }

            // Add requestId to response header
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

            chain.doFilter(request, response);
        } finally {
            // Clean up MDC
            MDC.remove(REQUEST_ID_MDC_KEY);
            MDC.remove(TRACE_ID_MDC_KEY);
            MDC.remove(SPAN_ID_MDC_KEY);
        }
    }
}
