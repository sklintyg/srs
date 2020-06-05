package se.inera.intyg.srs.vo

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus
import java.time.LocalDateTime

@Configuration
@Profile("test")
open class TestPredictionAdapter : PredictionAdapter {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getPrediction(person: Person, diagnosis: CertDiagnosis, extraParams: Map<String, Map<String, String>>, daysInto:Int): Prediction {
        log.debug("Test prediction adapter got getPrediction(person: $person, diagnosis: $diagnosis, extraParams: $extraParams)")
        return Prediction("F43", 0.52, Diagnosprediktionstatus.OK, LocalDateTime.now())
    }

}
