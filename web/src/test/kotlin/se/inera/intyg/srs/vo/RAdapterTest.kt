package se.inera.intyg.srs.vo

import org.junit.Before
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable


import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Diagnosprediktionstatus
import se.inera.intyg.srs.service.LOCATION_KEY
import se.inera.intyg.srs.service.ModelFileUpdateService
import se.inera.intyg.srs.service.QUESTIONS_AND_ANSWERS_KEY
import se.inera.intyg.srs.service.REGION_KEY
import se.inera.intyg.srs.service.ZIP_CODE_KEY

class RAdapterTest () {

    val resourceLoader:ResourceLoader = DefaultResourceLoader()
    val modelFileService:ModelFileUpdateService = ModelFileUpdateService(resourceLoader, "classpath:/model/*")

    @Before
    fun setup() {
        modelFileService.update()
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "R_HOME", matches = "^.*$" ) // only use when a local R environment is available
    fun testRAdapter() {
        val rAdapter:RAdapter = RAdapter(modelFileService, "/tmp/r-log", 1)
        val person:Person = Person("19121212-1212", "57-63", Sex.MAN, listOf(Diagnosis("F438A")),
                "cert-id-1")

        val extraParams = mapOf(
                LOCATION_KEY to mapOf(
                        REGION_KEY to "VAST",
                        ZIP_CODE_KEY to "44235"
                ),
                QUESTIONS_AND_ANSWERS_KEY to mapOf(
                        "SA_1_gross" to "(0,90]",
                        "SA_ExtentFirst" to "1",
                        "SA_SyssStart_fct" to "not_unemp",
                        "birth_cat_fct" to "SW",
                        "any_visits_-365_+6_Mental_notF43" to "0",
                        "any_visits_-365_+6_R53" to "0",
                        "any_visits_-6_+15_F43_subdiag_group" to "Annat/Vet ej"
                )
        )

        val prediction = rAdapter.getPrediction(person, Diagnosis("F438A"), extraParams,10)

        assertEquals(Diagnosprediktionstatus.DIAGNOSKOD_PA_HOGRE_NIVA, prediction.status)
        assertEquals("F43", prediction.diagnosis)
        assertEquals(0.53, prediction.prediction)

    }
}