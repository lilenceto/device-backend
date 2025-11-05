package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.models.dtos.RenovationCreateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Renovation;
import bg.tuvarna.devicebackend.repositories.RenovationRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.RenovationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RenovationServiceTests {

    @MockBean
    private RenovationRepository renovationRepository;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private RenovationService renovationService;

    private RenovationCreateVO renovationCreateVO;
    private Device device;
    private Renovation renovation;

    @BeforeEach
    void setUp() {
        device = new Device();
        device.setSerialNumber("SN001");

        renovation = new Renovation();
        renovation.setId(1L);
        renovation.setDescription("Changed display");
        renovation.setRenovationDate(LocalDate.of(2024, 5, 1));
        renovation.setDevice(device);

        renovationCreateVO = new RenovationCreateVO(
                "SN001",
                "Changed display",
                LocalDate.of(2024, 5, 1)
        );
    }

    @Test
    void saveShouldCreateRenovationSuccessfully() {
        when(deviceService.isDeviceExists("SN001")).thenReturn(device);
        when(renovationRepository.save(any(Renovation.class))).thenReturn(renovation);

        Renovation result = renovationService.save(renovationCreateVO);

        assertNotNull(result);
        assertEquals("Changed display", result.getDescription());
        assertEquals(LocalDate.of(2024, 5, 1), result.getRenovationDate());
        assertEquals("SN001", result.getDevice().getSerialNumber());

        verify(deviceService, times(1)).isDeviceExists("SN001");
        verify(renovationRepository, times(1)).save(any(Renovation.class));
    }

    @Test
    void saveShouldThrowWhenDeviceNotFound() {
        when(deviceService.isDeviceExists("SN001"))
                .thenThrow(new RuntimeException("Device not registered"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> renovationService.save(renovationCreateVO)
        );

        assertEquals("Device not registered", ex.getMessage());
        verify(renovationRepository, never()).save(any());
    }

    @Test
    void saveShouldCallRepositoryWithCorrectData() {
        when(deviceService.isDeviceExists(anyString())).thenReturn(device);
        when(renovationRepository.save(any(Renovation.class))).thenAnswer(invocation -> {
            Renovation saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        Renovation result = renovationService.save(renovationCreateVO);

        assertNotNull(result.getId());
        assertEquals("SN001", result.getDevice().getSerialNumber());
        verify(renovationRepository, times(1)).save(any(Renovation.class));
    }
}
