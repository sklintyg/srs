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
    val modelFileService:ModelFileUpdateService = ModelFileUpdateService(resourceLoader, "classpath:/models_3_0/*")

    @Before
    fun setup() {
        modelFileService.update()
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "R_HOME", matches = "^.*$" ) // only use when a local R environment is available
    fun testRAdapter() {
        val rAdapter:RAdapter = RAdapter(modelFileService, "/tmp/r-log", 1)
        val person:Person = Person("19121212-1212", "57-63", Sex.MAN, listOf(CertDiagnosis("cert-id-1", "F438A")))

        val extraParams = mapOf(
                LOCATION_KEY to mapOf(
                        REGION_KEY to "Vast",
                        ZIP_CODE_KEY to "44235"
                ),
                QUESTIONS_AND_ANSWERS_KEY to mapOf(
                    "SA_ExtentFirst" to "1",
                    "SA_SyssStart_fct" to "not_unemp",
                    "SA_1_gross" to "(0,90]",
                    "comorbidity" to "no",
                    "birth_cat_fct" to "SW",
                    "edu_cat_fct" to "No university"
                )
        )

        val prediction = rAdapter.getPrediction(person, CertDiagnosis("cert-id-1","F438A"), extraParams,10)

        assertEquals(Diagnosprediktionstatus.OK, prediction.status)
        assertEquals("F438A", prediction.diagnosis)
        assertEquals(0.65, prediction.prediction)

    }
}