package bg.tuvarna.devicebackend.integrational;

import bg.tuvarna.devicebackend.models.dtos.DeviceCreateVO;
import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Disabled;


@Disabled("Disabled temporarily — anonymous device requires passport")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeviceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void config(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    private static final String TEST_SERIAL = "ABC123456";

    @Test
    @Order(1)
    void createAnonymousDevice() throws Exception {
        DeviceCreateVO device = new DeviceCreateVO(
                TEST_SERIAL,
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/devices/anonymousDevice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void registerUser() throws Exception {
        UserCreateVO newUser = new UserCreateVO(
                "Test User",
                "Password123!",
                "test@example.com",
                "0888123456",
                "Sofia, Bulgaria",
                LocalDate.now(),
                TEST_SERIAL // вече съществува
        );

        mockMvc.perform(post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void loginUser() throws Exception {
        var result = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "test@example.com",
                                    "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        var jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = "Bearer " + jsonNode.get("token").asText();

        assertThat(jwtToken).isNotBlank();
    }

    @Test
    @Order(4)
    void getAllDevicesShouldReturnList() throws Exception {
        assumeTrue(jwtToken != null && !jwtToken.isBlank(), "JWT token must not be null");

        var result = mockMvc.perform(get("/api/v1/devices")
                        .header("Authorization", jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("items");
    }
}
