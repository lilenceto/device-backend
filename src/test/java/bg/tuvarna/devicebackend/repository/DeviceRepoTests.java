package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.Device;
import bg.tuvarna.devicebackend.repositories.DeviceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DeviceRepoTests {

    @Autowired
    private DeviceRepository deviceRepository;

    private Device device;

    @BeforeEach
    void setUp() {
        device = new Device();
        device.setSerialNumber("SN123");
        device.setPurchaseDate(LocalDate.now());
        device.setWarrantyExpirationDate(LocalDate.now().plusMonths(24));
        deviceRepository.save(device);
    }

    @AfterEach
    void tearDown() {
        deviceRepository.deleteAll();
    }

    @Test
    void findAllDevicesShouldReturnResults() {
        Page<Device> devices = deviceRepository.getAllDevices(PageRequest.of(0, 5));
        assertEquals(1, devices.getTotalElements());
    }



    @Test
    void findDeviceByIdShouldReturnCorrectDevice() {
        assertTrue(deviceRepository.findById("SN123").isPresent());
        assertEquals("SN123", deviceRepository.findById("SN123").get().getSerialNumber());
    }
}
