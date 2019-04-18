package se.inera.intyg.srs.vo

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v2.Diagnosprediktionstatus
import java.time.LocalDateTime

@Configuration
@Profile("test")
class TestPredictionAdapter : PredictionAdapter {

    override fun getPrediction(person: Person, diagnosis: Diagnosis, extraParams: Map<String, Map<String, String>>): Prediction {
        return Prediction("F43", 0.0, Diagnosprediktionstatus.OK, LocalDateTime.now())
    }

}
