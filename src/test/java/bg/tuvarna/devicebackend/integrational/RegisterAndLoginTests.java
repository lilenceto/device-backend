package bg.tuvarna.devicebackend.integrational;

import bg.tuvarna.devicebackend.controllers.exceptions.ErrorResponse;
import bg.tuvarna.devicebackend.models.dtos.AuthResponseDTO;
import bg.tuvarna.devicebackend.models.enums.UserRole;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *  Интеграционен тест, който:
 *  Стартира реален PostgreSQL контейнер с Testcontainers.
 * -Регистрира нов потребител.
 *  Проверява логин и достъп с JWT токен.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegisterAndLoginTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    private static String token;


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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @BeforeEach
    void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Order(1)
    void userLoginFailed() throws Exception {
        mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "petar123",
                                          "password": "Az$um_PET@R123"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Wrong credentials!"));
    }

    @Test
    @Order(2)
    void userRegistrationSuccess() throws Exception {
        MvcResult registration = mvc.perform(
                        post("/api/v1/users/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "Georgi Ivanov",
                                          "email": "gosho@abv.bg",
                                          "phone": "123456789",
                                          "username": "gosho123",
                                          "password": "Az$um_GOSHO123"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, registration.getResponse().getStatus());
    }

    @Test
    @Order(3)
    void userRegistrationFailed() throws Exception {
        MvcResult registration = mvc.perform(
                        post("/api/v1/users/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "Georgi Ivanov",
                                          "email": "gosho@abv.bg",
                                          "phone": "123456789",
                                          "username": "gosho123",
                                          "password": "Az$um_GOSHO123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = mapper.readValue(
                registration.getResponse().getContentAsString(),
                ErrorResponse.class
        );

        assertEquals("Email already taken", errorResponse.getError());
    }

    @Test
    @Order(4)
    void userLoginSuccess() throws Exception {
        MvcResult login = mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "gosho@abv.bg",
                                          "password": "Az$um_GOSHO123"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDTO authResponseDTO = mapper.readValue(
                login.getResponse().getContentAsString(),
                AuthResponseDTO.class
        );

        token = authResponseDTO.getToken();
        Assertions.assertNotNull(token);
    }

    @Test
    @Order(5)
    void accessProtectedEndpointWithToken() throws Exception {
        mvc.perform(
                        get("/api/v1/users/getUser")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(UserRole.USER.toString()));
    }
}
