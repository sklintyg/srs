package se.inera.intyg.srs.integrationtest.getsrsinformation

import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest

class StatistikIT : BaseIntegrationTest() {

    @Test
    fun testExistingImageShouldBeReturnedForADiagnosis() {
        // Om statistik finns för en diagnos ska statistik-bilden för denna diagnos returneras med svaret.
    }

    @Test
    fun testMissingImageShouldYieldErrorMessage() {
        // Om statistik saknas för en diagnos ska felmeddelandet "STATISTIK SAKNAS" returneras med svaret.
    }

    @Test
    fun testExistingImageOnHigherDiagnosisIdLevel() {
        // Om M751 inte finns men M75 finns så ska statistik för denna returneras,
        // dessutom ska flaggan för högre diagnoskodnivå vara satt.
    }
}