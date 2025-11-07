package com.codewithvy.quanlydatsan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("QuanLyDatSan API")
                        .version("v1.0")
                        .description("""
                                API quản lý đặt sân thể thao - Hệ thống cho phép quản lý venue, court, booking và users
                                
                                ⏰ **QUAN TRỌNG - THỜI GIAN:**
                                - Tất cả thời gian sử dụng múi giờ **Việt Nam (Asia/Ho_Chi_Minh, UTC+7)**
                                - Format: `yyyy-MM-dd'T'HH:mm:ss` (VD: `2025-11-07T14:00:00`)
                                - **KHÔNG CẦN** thêm `Z` hoặc `+07:00` ở cuối
                                - Backend tự động xử lý timezone
                                """)
                        .contact(new Contact()
                                .name("CodeWithVy Team")
                                .email("vyp8269@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://your-production-url.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token để xác thực. Token có thể lấy từ endpoint /api/auth/login")
                        )
                );
    }
}
