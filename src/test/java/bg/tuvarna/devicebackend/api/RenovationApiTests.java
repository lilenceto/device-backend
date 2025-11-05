package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.models.dtos.RenovationCreateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.Renovation;
import bg.tuvarna.devicebackend.services.RenovationService;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RenovationApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RenovationService renovationService;

    private Renovation renovation;

    @BeforeEach
    void setUp() {
        Device device = new Device();
        device.setSerialNumber("SN1001");

        // 🩹 Добавяме фалшив паспорт, за да не е null
        Passport passport = new Passport();
        passport.setId(10L);
        passport.setName("Demo passport");
        passport.setModel("Model X");
        passport.setSerialPrefix("SN");
        passport.setFromSerialNumber(1000);
        passport.setToSerialNumber(2000);
        passport.setWarrantyMonths(24);
        device.setPassport(passport);

        renovation = new Renovation();
        renovation.setId(1L);
        renovation.setDescription("Changed main board");
        renovation.setRenovationDate(LocalDate.of(2025, 1, 15));
        renovation.setDevice(device);
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void saveRenovationShouldReturnCreated() throws Exception {
        RenovationCreateVO vo = new RenovationCreateVO(
                "SN1001",
                "Changed main board",
                LocalDate.of(2025, 1, 15)
        );

        when(renovationService.save(any(RenovationCreateVO.class))).thenReturn(renovation);

        mvc.perform(post("/api/v1/renovations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(vo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Changed main board"))
                .andExpect(jsonPath("$.device.serialNumber").value("SN1001"));
    }
}
