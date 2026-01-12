package com.example.shortmovie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SwaggerConfig {

    private final Environment environment;

    public OpenAPI openAPI()
    {
        String appversion = environment.getProperty("project.version", "1.0.0");

        return new OpenAPI().info(new Info().title("系统接口文档").version(appversion));
    }
}
