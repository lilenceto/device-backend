package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.DeviceCreateVO;
import bg.tuvarna.devicebackend.models.dtos.DeviceUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.PassportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class DeviceServiceTests {

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private PassportService passportService;

    @Autowired
    private DeviceService deviceService;

    private Passport passport;
    private User user;

    @BeforeEach
    void setUp() {
        passport = new Passport();
        passport.setWarrantyMonths(24);
        user = new User();
    }

    @Test
    void registerDeviceShouldSaveSuccessfully() {
        when(passportService.findPassportBySerialId("SN123")).thenReturn(passport);
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        Device result = deviceService.registerDevice("SN123", LocalDate.now(), user);

        assertNotNull(result);
        assertEquals("SN123", result.getSerialNumber());
        assertNotNull(result.getWarrantyExpirationDate());
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void registerDeviceShouldThrowInvalidSerialNumberException() {
        when(passportService.findPassportBySerialId("BAD")).thenThrow(new RuntimeException("Invalid"));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> deviceService.registerDevice("BAD", LocalDate.now(), user)
        );

        assertEquals("Invalid serial number", ex.getMessage());
    }

    @Test
    void findDeviceShouldReturnDeviceWhenExists() {
        Device d = new Device();
        d.setSerialNumber("SN123");
        when(deviceRepository.findById("SN123")).thenReturn(Optional.of(d));

        Device found = deviceService.findDevice("SN123");

        assertNotNull(found);
        assertEquals("SN123", found.getSerialNumber());
    }

    @Test
    void findDeviceShouldReturnNullWhenNotExists() {
        when(deviceRepository.findById("SN404")).thenReturn(Optional.empty());
        Device found = deviceService.findDevice("SN404");
        assertNull(found);
    }

    @Test
    void alreadyExistShouldThrowIfExists() {
        Device existing = new Device();
        when(deviceRepository.findById("SN123")).thenReturn(Optional.of(existing));

        assertThrows(CustomException.class, () -> deviceService.alreadyExist("SN123"));
    }

    @Test
    void updateDeviceShouldSaveUpdatedInfo() {
        Device d = new Device();
        d.setSerialNumber("SN123");
        Passport p = new Passport();
        p.setWarrantyMonths(24);
        d.setPassport(p);
        d.setUser(user);

        DeviceUpdateVO updateVO = new DeviceUpdateVO(LocalDate.now(), "updated comment");

        when(deviceRepository.findById("SN123")).thenReturn(Optional.of(d));
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        Device updated = deviceService.updateDevice("SN123", updateVO);

        assertNotNull(updated);
        assertEquals("updated comment", updated.getComment());
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void deleteDeviceShouldCallRepositoryDelete() {
        doNothing().when(deviceRepository).deleteBySerialNumber("SN123");
        assertDoesNotThrow(() -> deviceService.deleteDevice("SN123"));
        verify(deviceRepository, times(1)).deleteBySerialNumber("SN123");
    }

    @Test
    void deleteDeviceShouldThrowWhenRepositoryFails() {
        doThrow(new RuntimeException("fail")).when(deviceRepository).deleteBySerialNumber("SN123");
        assertThrows(CustomException.class, () -> deviceService.deleteDevice("SN123"));
    }
}
