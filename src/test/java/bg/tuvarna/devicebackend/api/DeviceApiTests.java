package bg.tuvarna.devicebackend.api;

import bg.tuvarna.devicebackend.models.dtos.DeviceCreateVO;
import bg.tuvarna.devicebackend.models.dtos.DeviceUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.services.DeviceService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeviceApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private DeviceService deviceService;

    private Device device;

    @BeforeEach
    void setUp() {
        Passport passport = new Passport();
        passport.setId(1L);
        passport.setName("Test Passport");

        device = new Device();
        device.setSerialNumber("SN12345");
        device.setPurchaseDate(LocalDate.now());
        device.setWarrantyExpirationDate(LocalDate.now().plusMonths(24));
        device.setComment("Test device");
        device.setPassport(passport);
        device.setRenovations(Collections.emptyList());
    }

    // 🔹 Всички тестове вече ще имат mock ADMIN user
    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")

    void findDeviceShouldReturnOk() throws Exception {
        when(deviceService.findDevice("SN12345")).thenReturn(device);

        mvc.perform(get("/api/v1/devices/SN12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("SN12345"))
                .andExpect(jsonPath("$.comment").value("Test device"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")

    void isDeviceExistsShouldReturnOk() throws Exception {
        when(deviceService.isDeviceExists("SN12345")).thenReturn(device);

        mvc.perform(get("/api/v1/devices/exists/SN12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("SN12345"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")

    void addDeviceShouldReturnCreated() throws Exception {
        DeviceCreateVO createVO = new DeviceCreateVO("SN6789", LocalDate.now());
        when(deviceService.registerNewDevice(any(DeviceCreateVO.class), any())).thenReturn(device);

        mvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createVO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SN12345"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")

    void addAnonymousDeviceShouldReturnCreated() throws Exception {
        DeviceCreateVO createVO = new DeviceCreateVO("SN9876", LocalDate.now());
        when(deviceService.addAnonymousDevice(any(DeviceCreateVO.class))).thenReturn(device);

        mvc.perform(post("/api/v1/devices/anonymousDevice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createVO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SN12345"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")

    void updateDeviceShouldReturnOk() throws Exception {
        DeviceUpdateVO updateVO = new DeviceUpdateVO(LocalDate.now().plusDays(1), "Updated comment");
        when(deviceService.updateDevice(eq("SN12345"), any(DeviceUpdateVO.class))).thenReturn(device);

        mvc.perform(put("/api/v1/devices/SN12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("SN12345"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")

    void deleteDeviceShouldReturnOk() throws Exception {
        doNothing().when(deviceService).deleteDevice("SN12345");

        mvc.perform(delete("/api/v1/devices/SN12345"))
                .andExpect(status().isOk());

        verify(deviceService, times(1)).deleteDevice("SN12345");
    }
}
