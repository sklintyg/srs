package se.inera.intyg.srs.vo

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestPredictionAdapter: PredictionAdapter {

    override fun getPrediction(person: Person, diagnose: Diagnose): Double {
        return 0.0;
    }

}
