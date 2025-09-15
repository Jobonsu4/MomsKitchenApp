package com.example.momskitchen.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdminApiKeyFilter implements Filter {

    @Value("${admin.apiKey:changeme}")
    private String configuredKey;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        // Always let CORS preflight pass through so the browser can send real requests
        if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        if (r.getRequestURI().startsWith("/api/admin/")) {
            String key = r.getHeader("X-Admin-Key");
            if (key == null || !key.equals(configuredKey)) {
                w.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                w.getWriter().write("Unauthorized");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}

