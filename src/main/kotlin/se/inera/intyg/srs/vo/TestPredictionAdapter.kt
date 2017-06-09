package se.inera.intyg.srs.vo

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestPredictionAdapter: PredictionAdapter {

    override fun doStuff() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
