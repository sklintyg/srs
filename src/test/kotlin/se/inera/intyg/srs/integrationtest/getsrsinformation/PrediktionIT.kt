package se.inera.intyg.srs.integrationtest.getsrsinformation

import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest

class PrediktionIT : BaseIntegrationTest() {

    @Test
    fun testShouldReturnMaximumRiskFromModel() {
        // Givet en dummymodell som alltid returnerar max risk vill vi
        // säkerställa att korrekt prediktion blir returnerad i svaret
    }

    @Test
    fun testShouldPickUpChangedModels() {
        // När modellen byts till en dummymodell som returnerar min risk
        // kan vi säkerställa att man kan byta modell-fil on the fly.
    }

    @Test
    fun testExistingPredictionOnHigherDiagnosisIdLevel() {
        // T.ex. När prediktion efterfrågas på M751 men bara finns på M75
        // så ska prediktion för M75 returneras.
    }

    @Test
    fun testMissingPredictionShouldYieldErrorMessage() {
        // Om prediktion saknas för en diagnos ska "PREDIKTION SAKNAS" returneras för den diagnosen.
    }

}