package com.codelogium.ticketing.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfing {
    @Bean
    public OpenAPI defineOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("本地开发环境");

        Info info = new Info()
                .title("IT Service Ticket System API")
                .version("v1.0")
                .description("企业内部IT服务工单管理系统接口文档");

        // 全局 API 响应文档
        Components components = new Components()
                .addResponses("401", new ApiResponse()
                .description("Unauthorized - Invalid or missing JWT Token"))
                .addResponses("403", new ApiResponse().description("Forbidden - Insufficient permissions"))
                .addResponses("500", new ApiResponse().description("Internal Server Error"));

        return new OpenAPI().components(components).info(info).servers(List.of(localServer));

    }
}
