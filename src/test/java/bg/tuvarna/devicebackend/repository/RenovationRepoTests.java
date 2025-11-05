package bg.tuvarna.devicebackend.repository;

import bg.tuvarna.devicebackend.models.entities.Renovation;
import bg.tuvarna.devicebackend.repositories.RenovationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RenovationRepoTests {

    @Autowired
    private RenovationRepository renovationRepository;

    private Renovation renovation;

    @BeforeEach
    void setUp() {
        renovation = new Renovation();
        renovation.setDescription("Battery replaced");
        renovation.setRenovationDate(LocalDate.now());
        renovationRepository.save(renovation);
    }

    @AfterEach
    void tearDown() {
        renovationRepository.deleteAll();
    }

    @Test
    void saveRenovationShouldPersistData() {
        List<Renovation> list = renovationRepository.findAll();
        assertEquals(1, list.size());
        assertEquals("Battery replaced", list.get(0).getDescription());
    }

    @Test
    void findByIdShouldReturnRenovation() {
        Renovation found = renovationRepository.findById(renovation.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Battery replaced", found.getDescription());
    }

    @Test
    void deleteRenovationShouldRemoveIt() {
        renovationRepository.deleteById(renovation.getId());
        assertTrue(renovationRepository.findAll().isEmpty());
    }
}
