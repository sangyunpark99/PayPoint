package org.sangyunpark.pointpay.config.web;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.common.idempotency.CachingResponseFilter;
import org.sangyunpark.pointpay.common.idempotency.IdempotencyInterceptor;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final IdempotencyService idempotencyService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IdempotencyInterceptor(idempotencyService));
    }

    @Bean
    public FilterRegistrationBean<CachingResponseFilter> cachingResponseFilter() {
        FilterRegistrationBean<CachingResponseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CachingResponseFilter());
        registration.setOrder(0);
        return registration;
    }
}
