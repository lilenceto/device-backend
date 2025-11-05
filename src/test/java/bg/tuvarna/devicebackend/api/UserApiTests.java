package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.controllers.exceptions.ErrorResponse;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User user = User.builder()
                .fullName("gosho")
                .email("gosho@abv.bg")
                .password(passwordEncoder.encode("Az$um_GOSHO123"))
                .phone("0888123456")
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void userRegistrationShouldFailWhenEmailExists() throws Exception {
        MvcResult registration = mvc.perform(
                        post("/api/v1/users/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "Georgi Ivanov",
                                          "email": "gosho@abv.bg",
                                          "phone": "123456789",
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
    void userLoginShouldSucceed() throws Exception {
        mvc.perform(
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
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void userLoginShouldFailWithInvalidPassword() throws Exception {
        mvc.perform(
                        post("/api/v1/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "gosho@abv.bg",
                                          "password": "wrongpassword"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsersWithoutAuthShouldBeUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserWithoutTokenShouldReturnUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/users/getUser"))
                .andExpect(status().isUnauthorized());
    }
}
