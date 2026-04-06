package com.ezticket.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@EnableJpaRepositories

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/areport/ListAll").setViewName("/areport/ListAll.html");
    }

    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**")
                // Explicitly list allowed origins so allowCredentials(true) works correctly.
                // Add your Vue SPA origin(s) here. The wildcard "*" cannot be used with credentials.
                .allowedOrigins(
                    "http://localhost:5173",   // Vite dev server
                    "http://localhost:80",     // Nginx (local Docker)
                    "http://vue-frontend:80"   // Docker inter-container
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
