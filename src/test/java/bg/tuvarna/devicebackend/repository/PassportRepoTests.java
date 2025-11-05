package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.Passport;
import bg.tuvarna.devicebackend.repositories.PassportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PassportRepoTests {

    @Autowired
    private PassportRepository passportRepository;

    private Passport passport;

    @BeforeEach
    void setUp() {
        passport = Passport.builder()
                .name("Device A")
                .model("Model X")
                .serialPrefix("ABC")
                .fromSerialNumber(100)
                .toSerialNumber(200)
                .warrantyMonths(12)
                .build();
        passportRepository.save(passport);
    }

    @AfterEach
    void tearDown() {
        passportRepository.deleteAll();
    }

    @Test
    void findByFromSerialNumberBetweenShouldReturnResults() {
        List<Passport> result = passportRepository.findByFromSerialNumberBetween("ABC", 50, 150);
        assertFalse(result.isEmpty());
        assertEquals("ABC", result.get(0).getSerialPrefix());
    }

    @Test
    void findByFromSerialShouldReturnPassports() {
        List<Passport> result = passportRepository.findByFromSerial("ABC101");
        assertFalse(result.isEmpty());
    }
}
