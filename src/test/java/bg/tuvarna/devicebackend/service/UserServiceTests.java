package bg.tuvarna.devicebackend.service;

import bg.tuvarna.devicebackend.controllers.exceptions.CustomException;
import bg.tuvarna.devicebackend.models.dtos.UserCreateVO;
import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import bg.tuvarna.devicebackend.services.DeviceService;
import bg.tuvarna.devicebackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private DeviceService deviceService;

    @Autowired
    private UserService userService;

    private UserCreateVO validUserVO;

    @BeforeEach
    void setUp() {
        validUserVO = new UserCreateVO(
                "Ivan",
                "Ivanov",
                "ivan@mail.com",
                "+359888888",
                "Varna",
                LocalDate.of(1995, 3, 15),
                "pass123"
        );
    }

    @Test
    void registerUserShouldThrowPhoneExistsException() {
        when(userRepository.getByPhone("+359888888")).thenReturn(new User());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.register(validUserVO)
        );

        assertEquals("Phone already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserShouldThrowEmailExistsException() {
        when(userRepository.getByEmail("ivan@mail.com")).thenReturn(new User());

        CustomException ex = assertThrows(
                CustomException.class,
                () -> userService.register(validUserVO)
        );

        assertEquals("Email already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserShouldEncodePasswordAndSave() {
        when(userRepository.getByPhone("+359888888")).thenReturn(null);
        when(userRepository.getByEmail("ivan@mail.com")).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded123");

        doAnswer(invocation -> null).when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.register(validUserVO));

        verify(passwordEncoder, atLeastOnce()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
