package se.inera.intyg.srs.integrationtest.getsrsinformation

import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest

class PrediktionIT : BaseIntegrationTest() {

    @Test
    fun test_should_return_maximum_risk_from_model() {
        // Givet en dummymodell som alltid returnerar max risk vill vi
        // säkerställa att korrekt prediktion blir returnerad i svaret
    }

    @Test
    fun test_should_pick_up_changed_models() {
        // När modellen byts till en dummymodell som returnerar min risk
        // kan vi säkerställa att man kan byta modell-fil on the fly.
    }

    @Test
    fun test_existing_prediction_on_higher_diagnosis_id_level() {
        // T.ex. När prediktion efterfrågas på M751 men bara finns på M75
        // så ska prediktion för M75 returneras.
    }

    @Test
    fun test_missing_prediction_should_yield_error_message() {
        // Om prediktion saknas för en diagnos ska "PREDIKTION SAKNAS" returneras för den diagnosen.
    }

}