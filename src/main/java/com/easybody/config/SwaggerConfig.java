package com.easybody.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI easyBodyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EasyBody API")
                        .version("v1")
                        .description("EasyBody backend powered by Spring Boot, PostgreSQL/PostGIS, and AWS integrations"));
    }
}
