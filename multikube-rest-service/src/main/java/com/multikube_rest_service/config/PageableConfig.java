package com.multikube_rest_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class PageableConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new PageableHandlerMethodArgumentResolver() {
            {
                PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
                pageableResolver.setOneIndexedParameters(true); // Start page numbering from 1
                resolvers.add(pageableResolver);
                setFallbackPageable(PageRequest.of(1, 10)); // Default to page=0, size=10
            }
        });
    }
}