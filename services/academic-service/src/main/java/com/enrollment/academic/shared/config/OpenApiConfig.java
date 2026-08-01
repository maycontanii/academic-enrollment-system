package com.enrollment.academic.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI academicOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Academic Enrollment — academic-service")
                .description("Catalog and enrollment REST API")
                .version("v1"));
    }
}
