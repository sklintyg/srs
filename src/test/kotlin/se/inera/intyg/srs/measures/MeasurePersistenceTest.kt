package se.inera.intyg.srs.measures

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.Atgardstyp
import se.inera.intyg.srs.persistence.MeasureRepository
import se.inera.intyg.srs.persistence.Recommendation
import se.inera.intyg.srs.persistence.RecommendationRepository


@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class MeasurePersistenceTest {

    @Autowired
    lateinit var recommendationRepo: RecommendationRepository

    @Before
    fun setUp() {
    }

    @Test
    fun testCreateWithGeneratedId() {
        var rec: Recommendation = Recommendation(Atgardstyp.fromValue("OBS"), "title", "text")
        recommendationRepo.save(rec)
    }

}
