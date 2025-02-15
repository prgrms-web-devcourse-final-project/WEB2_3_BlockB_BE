package com.example.earthtalk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI earthtalkOpenAPI() {
        String jwtScheme = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtScheme);

        Components components =
            new Components()
                .addSecuritySchemes(
                    jwtScheme,
                    new SecurityScheme()
                        .name(jwtScheme)
                        .type(Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));
        return new OpenAPI()
            .info(getinfo())
            .addSecurityItem(securityRequirement)
            .components(components);
    }

    private Info getinfo() {
        return new Info()
            .title("어스톡 API")
            .description("TEAM5_어스톡 API 명세")
            .version("0.0.1");
    }

    @Bean
    public OpenApiCustomizer globalResponseCustomizer() {
        return openApi -> openApi.getPaths()
            .forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                // ApiResponse 전역적으로 추가하려면 여기에 추가.
                operation.getResponses().addApiResponse("400",
                    new ApiResponse().description("잘못된 요청").content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .schema(new Schema<>()
                                .type("object")
                                .addProperty("status",
                                    new Schema<>().type("integer").example(400))
                                .addProperty("message",
                                    new Schema<>().type("string").example("Validation failed"))
                            )
                        )
                    ));
                operation.getResponses().addApiResponse("500",
                    new ApiResponse().description("서버 오류").content(new Content()
                        .addMediaType("application/json", new MediaType()
                            .schema(new Schema<>()
                                .type("object")
                                .addProperty("status",
                                    new Schema<>().type("integer").example(500))
                                .addProperty("message", new Schema<>().type("string")
                                    .example("Internal server error"))
                            )
                        )
                    ));
            }));
    }
}

