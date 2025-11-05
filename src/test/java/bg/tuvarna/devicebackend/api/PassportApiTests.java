package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.models.dtos.PassportCreateVO;
import bg.tuvarna.devicebackend.models.dtos.PassportUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.services.PassportService;
import bg.tuvarna.devicebackend.utils.CustomPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PassportApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PassportService passportService;

    private Passport passport;

    @BeforeEach
    void setUp() {
        passport = new Passport();
        passport.setId(1L);
        passport.setName("Smart Device");
        passport.setModel("X1000");
        passport.setSerialPrefix("SN");
        passport.setWarrantyMonths(24);
        passport.setFromSerialNumber(1);
        passport.setToSerialNumber(100);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createPassportShouldReturnCreated() throws Exception {
        PassportCreateVO createVO = new PassportCreateVO("Smart Device", "X1000", "SN", 24, 1, 100);
        when(passportService.create(any(PassportCreateVO.class))).thenReturn(passport);

        mvc.perform(post("/api/v1/passports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createVO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Smart Device"))

                .andExpect(jsonPath("$.model").value("X1000"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updatePassportShouldReturnOk() throws Exception {
        PassportUpdateVO updateVO = new PassportUpdateVO("Updated Device", "ModelZ", "PR", 12, 5, 50);
        when(passportService.update(eq(1L), any(PassportUpdateVO.class))).thenReturn(passport);

        mvc.perform(put("/api/v1/passports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smart Device"))
                .andExpect(jsonPath("$.model").value("X1000"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getPassportsShouldReturnOk() throws Exception {
        CustomPage<Passport> page = new CustomPage<>();
        page.setContent(List.of(passport));
        when(passportService.getPassports(1, 10)).thenReturn(page);

        mvc.perform(get("/api/v1/passports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Smart Device"));

    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deletePassportShouldReturnOk() throws Exception {
        doNothing().when(passportService).delete(1L);

        mvc.perform(delete("/api/v1/passports/1"))
                .andExpect(status().isOk());

        verify(passportService, times(1)).delete(1L);
    }

    @Test
    void getPassportBySerialIdShouldReturnOkWithoutAuth() throws Exception {
        when(passportService.findPassportBySerialId("SN123")).thenReturn(passport);

        mvc.perform(get("/api/v1/passports/getBySerialId/SN123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smart Device"))
                .andExpect(jsonPath("$.model").value("X1000"));
    }
}
