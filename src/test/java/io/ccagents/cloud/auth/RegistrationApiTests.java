package io.ccagents.cloud.auth;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationApiTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM identities");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void registersAUserAndStoresOnlyAPasswordHash() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "  Victor@Example.COM ",
                                  "password": "correct-horse-battery-staple",
                                  "displayName": "Victor"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", matchesPattern(
                        "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")))
                .andExpect(jsonPath("$.email").value("victor@example.com"))
                .andExpect(jsonPath("$.displayName").value("Victor"));

        String storedEmail = jdbc.queryForObject("SELECT email FROM users", String.class);
        String storedHash = jdbc.queryForObject(
                "SELECT password_hash FROM identities WHERE provider = 'PASSWORD'",
                String.class);

        org.assertj.core.api.Assertions.assertThat(storedEmail).isEqualTo("victor@example.com");
        org.assertj.core.api.Assertions.assertThat(storedHash)
                .startsWith("$argon2")
                .doesNotContain("correct-horse-battery-staple");
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegistered() throws Exception {
        String body = """
                {
                  "email": "victor@example.com",
                  "password": "correct-horse-battery-staple",
                  "displayName": "Victor"
                }
                """;

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());

        Integer userCount = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer identityCount = jdbc.queryForObject("SELECT COUNT(*) FROM identities", Integer.class);
        org.assertj.core.api.Assertions.assertThat(userCount).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(identityCount).isEqualTo(1);
    }

    @Test
    void rejectsInvalidRegistrationInput() throws Exception {
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short",
                                  "displayName": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.password").exists())
                .andExpect(jsonPath("$.details.displayName").exists());

        Integer userCount = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        org.assertj.core.api.Assertions.assertThat(userCount).isZero();
    }
}
