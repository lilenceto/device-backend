package bg.tuvarna.devicebackend.integrational;

import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EndToEndFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void fullUserFlow() throws Exception {

        // 0️⃣ Създай тестов паспорт (в test профил е permitAll)
        String passportJson = """
{
  "name": "Lenovo IdeaPad",
  "model": "IdeaPad",
  "serialPrefix": "SN",
  "warrantyMonths": 24,
  "fromSerialNumber": 10000000,
  "toSerialNumber": 99999999
}
""";

        mockMvc.perform(post("/api/v1/passports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passportJson))
                .andExpect(status().isCreated());

        // 1️⃣ Регистрация
        var newUser = new UserCreateVO(
                "Test User",
                "Password123!",
                "test@example.com",
                "0888123456",
                "Sofia, Bulgaria",
                java.time.LocalDate.now(),
                "SN12345678"
        );
        mockMvc.perform(post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // 2️⃣ Логин и токен
        String loginBody = """
                {
                    "username": "test@example.com",
                    "password": "Password123!"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // 3️⃣ Достъп до защитен endpoint
        mockMvc.perform(get("/api/v1/users/getUser")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4️⃣ Опит за достъп до devices с USER токен
        mockMvc.perform(get("/api/v1/devices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());


        mockMvc.perform(put("/api/v1/users/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"phone": "0888999999"}
                """))
                .andExpect(status().isOk());

    }
}

