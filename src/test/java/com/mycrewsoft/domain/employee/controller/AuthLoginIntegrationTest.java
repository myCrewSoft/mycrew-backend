package com.mycrewsoft.domain.employee.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.security.jwt.JwtTokenProvider;
import com.mycrewsoft.security.service.RefreshTokenService;
import com.mycrewsoft.security.users.AuthSession;
import com.mycrewsoft.testsupport.TestLoginAccountSeeder;

@SpringBootTest
@AutoConfigureMockMvc
class AuthLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private TestLoginAccountSeeder accountSeeder;

    @BeforeEach
    void setUp() {
        assumeRedisIsAvailable();
        accountSeeder = new TestLoginAccountSeeder(jdbcTemplate, passwordEncoder);

        try {
            accountSeeder.resetTestAccount();
        } catch (DataAccessException e) {
            Assumptions.abort("Oracle test schema is not ready for auth login integration test: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanUp() {
        try {
            refreshTokenService.deleteSessionsByEmpId(TestLoginAccountSeeder.EMP_ID);
        } catch (RuntimeException ignored) {
            // Redis may be unavailable when the test is aborted.
        }

        if (accountSeeder != null) {
            try {
                accountSeeder.deleteTestAccount();
            } catch (DataAccessException ignored) {
                // Keep cleanup best-effort so an unavailable DB does not hide the original result.
            }
        }
    }

    @Test
    void loginSucceedsWithSeededEmployeeAndCreatesRedisSession() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(
                TestLoginAccountSeeder.EMP_ID,
                TestLoginAccountSeeder.PASSWORD));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.empId").value(TestLoginAccountSeeder.EMP_ID))
                .andExpect(jsonPath("$.data.authVersion").value(0))
                .andExpect(jsonPath("$.data.firstLoginRequired").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        String accessToken = data.get("accessToken").asText();
        String sessionId = jwtTokenProvider.getSessionId(accessToken);

        AuthSession session = refreshTokenService.findSession(sessionId).orElseThrow();

        assertThat(session.getEmpId()).isEqualTo(TestLoginAccountSeeder.EMP_ID);
        assertThat(session.getUsername()).isEqualTo(String.valueOf(TestLoginAccountSeeder.EMP_ID));
        assertThat(session.getAuthVersion()).isZero();
        assertThat(session.getAuthorities()).contains("ROLE_USER");
        assertThat(session.getRefreshTokenHash()).isNotBlank();
    }

    private void assumeRedisIsAvailable() {
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            connection.ping();
        } catch (RuntimeException e) {
            Assumptions.abort("localhost:6379 Redis is not available. Start Redis before running this integration test.");
        }
    }

    private record LoginRequest(Long empId, String password) {
    }
}
