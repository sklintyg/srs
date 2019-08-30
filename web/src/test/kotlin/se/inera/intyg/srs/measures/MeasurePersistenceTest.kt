package se.inera.intyg.srs.measures

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.entity.Recommendation
import se.inera.intyg.srs.persistence.repository.RecommendationRepository


@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
open class MeasurePersistenceTest {

    @Autowired
    lateinit var recommendationRepo: RecommendationRepository

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun testCreateWithGeneratedId() {
        var rec: Recommendation = Recommendation(Atgardstyp.fromValue("OBS"), "title", "text", 45)
        recommendationRepo.save(rec)
    }

}
