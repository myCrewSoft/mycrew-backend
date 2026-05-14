// package com.mycrewsoft.app.config;

// import io.swagger.v3.oas.models.Components;
// import io.swagger.v3.oas.models.OpenAPI;
// import io.swagger.v3.oas.models.info.Info;
// import io.swagger.v3.oas.models.security.SecurityRequirement;
// import io.swagger.v3.oas.models.security.SecurityScheme;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// /**
//  * Swagger(Springdoc OpenAPI) 설정 클래스.
//  * JWT Bearer 토큰 인증 방식을 Swagger UI 에 연동한다.
//  * 설정 후 Swagger UI 에서 Authorize 버튼으로 토큰을 입력하고 인증 API 를 테스트할 수 있다.
//  *
//  * 접속 URL: <http://localhost:8080/swagger-ui/index.html>
//  */
// @Configuration
// public class SwaggerConfig {

//     /**
//      * OpenAPI 빈을 생성한다.
//      * JWT Bearer 토큰 인증 스킴을 전역으로 등록하여
//      * 모든 API 엔드포인트에 자물쇠 아이콘을 표시한다.
//      *
//      * @return 설정이 완료된 OpenAPI 빈
//      */
//     @Bean
//     public OpenAPI openAPI() {
//         // JWT Bearer 토큰 인증 방식 정의
//         SecurityScheme securityScheme = new SecurityScheme()
//                 .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
//                 .in(SecurityScheme.In.HEADER).name("Authorization");

//         // 전역 보안 요구사항 (모든 API 에 자물쇠 아이콘 표시)
//         SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

//         return new OpenAPI()
//                 .info(new Info().title("프로젝트명 API").description("API 명세서").version("v1.0.0"))
//                 .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
//                 .components(new Components().addSecuritySchemes("bearerAuth", securityScheme));
//     }
// }