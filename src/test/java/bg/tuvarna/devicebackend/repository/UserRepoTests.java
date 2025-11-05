package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.User;
import bg.tuvarna.devicebackend.models.enums.UserRole;
import bg.tuvarna.devicebackend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepoTests {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .fullName("Gosho Petrov")
                .email("gosho@abv.bg")
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
    void userFindBySearchName() {
        Page<User> page = userRepository.searchBy("gosho", Pageable.ofSize(1));
        assertEquals(1, page.getTotalElements());
        assertEquals("Gosho Petrov", page.getContent().getFirst().getFullName());
    }

    @Test
    void userFindBySearchPhone() {
        Page<User> page = userRepository.searchBy("0888123456", Pageable.ofSize(1));
        assertEquals(1, page.getTotalElements());
        assertEquals("gosho@abv.bg", page.getContent().getFirst().getEmail());
    }

    @Test
    void getByEmailShouldReturnCorrectUser() {
        User found = userRepository.getByEmail("gosho@abv.bg");
        assertNotNull(found);
        assertEquals("Gosho Petrov", found.getFullName());
    }

    @Test
    void findByEmailOrPhoneShouldReturnOptional() {
        Optional<User> found = userRepository.findByEmailOrPhone("0888123456");
        assertTrue(found.isPresent());
        assertEquals("gosho@abv.bg", found.get().getEmail());
    }

    @Test
    void getAllUsersShouldReturnNonEmptyList() {
        Page<User> result = userRepository.getAllUsers(Pageable.ofSize(2));
        assertFalse(result.isEmpty());
    }
}
