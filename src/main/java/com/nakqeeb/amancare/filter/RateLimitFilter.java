// src/main/java/com/nakqeeb/amancare/filter/RateLimitFilter.java

package com.nakqeeb.amancare.filter;

import com.nakqeeb.amancare.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Apply rate limiting only to guest booking endpoints
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/api/v1/guest/book-appointment")) {
            String key = getClientIP(httpRequest);
            Bucket bucket = rateLimitConfig.resolveBucket(key);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                httpResponse.addHeader("X-Rate-Limit-Remaining",
                        String.valueOf(probe.getRemainingTokens()));
                chain.doFilter(request, response);
            } else {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.getWriter().write(
                        "{\"success\": false, \"message\": \"تم تجاوز حد الطلبات. يرجى المحاولة لاحقاً\"}"
                );
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}