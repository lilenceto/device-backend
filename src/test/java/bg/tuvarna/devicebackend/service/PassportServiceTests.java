package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.PassportCreateVO;
import bg.tuvarna.devicebackend.models.dtos.PassportUpdateVO;
import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import bg.tuvarna.devicebackend.services.PassportService;
import bg.tuvarna.devicebackend.utils.CustomPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PassportServiceTests {

    @MockBean
    private PassportRepository passportRepository;

    @Autowired
    private PassportService passportService;

    private Passport passport;

    @BeforeEach
    void setUp() {
        passport = Passport.builder()
                .id(1L)
                .name("Device A")
                .model("Model X")
                .serialPrefix("ABC")
                .fromSerialNumber(100)
                .toSerialNumber(200)
                .warrantyMonths(24)
                .build();
    }

    @Test
    void createPassportShouldSaveWhenNoConflict() {
        PassportCreateVO createVO = new PassportCreateVO("Device A", "Model X", "ABC", 100, 200, 24);
        when(passportRepository.findByFromSerialNumberBetween(anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(passportRepository.save(any(Passport.class))).thenReturn(passport);

        Passport result = passportService.create(createVO);

        assertNotNull(result);
        assertEquals("ABC", result.getSerialPrefix());
        verify(passportRepository, times(1)).save(any(Passport.class));
    }

    @Test
    void createPassportShouldThrowWhenSerialRangeExists() {
        PassportCreateVO createVO = new PassportCreateVO("Device A", "Model X", "ABC", 100, 200, 24);
        when(passportRepository.findByFromSerialNumberBetween(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(passport));

        CustomException ex = assertThrows(CustomException.class, () -> passportService.create(createVO));

        assertEquals("Serial number already exists", ex.getMessage());
        verify(passportRepository, never()).save(any());
    }

    @Test
    void findPassportByIdShouldReturnWhenExists() {
        when(passportRepository.findById(1L)).thenReturn(Optional.of(passport));

        Passport found = passportService.findPassportById(1L);

        assertNotNull(found);
        assertEquals("Device A", found.getName());
    }

    @Test
    void findPassportBySerialIdShouldReturnCorrectPassport() {
        Passport other = Passport.builder()
                .id(2L)
                .serialPrefix("XYZ")
                .fromSerialNumber(100)
                .toSerialNumber(300)
                .build();

        when(passportRepository.findByFromSerial(anyString())).thenReturn(List.of(passport, other));

        Passport found = passportService.findPassportBySerialId("ABC150");

        assertNotNull(found);
        assertEquals("ABC", found.getSerialPrefix());
    }




    @Test
    void updatePassportShouldApplyChanges() {
        PassportUpdateVO updateVO = new PassportUpdateVO(
                "Updated Device",
                "Model Y",
                "NEW",
                36,
                50,
                150
        );

        when(passportRepository.findById(1L)).thenReturn(Optional.of(passport));
        when(passportRepository.findByFromSerialNumberBetween(anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(passportRepository.save(any(Passport.class))).thenAnswer(inv -> inv.getArgument(0));

        Passport updated = passportService.update(1L, updateVO);

        assertNotNull(updated);
        assertEquals("NEW", updated.getSerialPrefix());
        verify(passportRepository, times(1)).save(any(Passport.class));
    }

    @Test
    void updatePassportShouldThrowWhenNotFound() {
        when(passportRepository.findById(1L)).thenReturn(Optional.empty());

        PassportUpdateVO updateVO = new PassportUpdateVO(
                "Device Updated",
                "Model Y",
                "NEW",
                12,
                1,
                10
        );

        assertThrows(CustomException.class, () -> passportService.update(1L, updateVO));
    }

    @Test
    void getPassportsShouldReturnPagedResult() {
        Page<Passport> mockPage = new PageImpl<>(List.of(passport), PageRequest.of(0, 10), 1);
        when(passportRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        CustomPage<Passport> result = passportService.getPassports(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getCurrentPage());
    }

    @Test
    void deletePassportShouldCallRepository() {
        doNothing().when(passportRepository).deleteById(1L);

        assertDoesNotThrow(() -> passportService.delete(1L));
        verify(passportRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePassportShouldThrowWhenRepositoryFails() {
        doThrow(new RuntimeException("fail")).when(passportRepository).deleteById(1L);

        assertThrows(CustomException.class, () -> passportService.delete(1L));
    }
}
