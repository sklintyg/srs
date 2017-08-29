package se.inera.intyg.srs.integrationtest.getsrsinformation

import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest

class StatistikIT : BaseIntegrationTest() {

    @Test
    fun test_existing_image_should_be_returned_for_a_diagnosis() {
        // Om statistik finns för en diagnos ska statistik-bilden för denna diagnos returneras med svaret.
    }

    @Test
    fun test_missing_image_should_yield_error_message() {
        // Om statistik saknas för en diagnos ska felmeddelandet "STATISTIK SAKNAS" returneras med svaret.
    }

    @Test
    fun test_existing_image_on_higher_diagnosis_id_level() {
        // Om M751 inte finns men M75 finns så ska statistik för denna returneras,
        // dessutom ska flaggan för högre diagnoskodnivå vara satt.
    }

    @Test
    fun test_statistics_should_not_be_returned_when_filter_is_false() {
        // Kontrollera att inga statistikbilder returneras om filtret är satt till false.
    }
}