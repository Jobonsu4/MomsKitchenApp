package com.example.momskitchen.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AdminApiKeyFilter> adminKeyFilter(AdminApiKeyFilter filter) {
        FilterRegistrationBean<AdminApiKeyFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);
        reg.addUrlPatterns("/api/admin/*");
        reg.setOrder(1);
        return reg;
    }
}
