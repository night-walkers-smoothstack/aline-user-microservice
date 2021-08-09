package com.aline.usermicroservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value= "classpath:openapi-config.properties")
public class OpenApiConfig {

    @Value("${openapi.info.title}")
    private String title;
    @Value("${openapi.info.description}")
    private String description;
    @Value("${openapi.info.version}")
    private String version;

    @Bean
    public OpenAPI api() {
        val info = new Info()
                .title(title)
                .description(description)
                .version(version);
        return new OpenAPI()
                .info(info);
    }
}
