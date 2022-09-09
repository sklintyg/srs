package se.inera.intyg.srs.integrationtest.getsrsinformation

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Ignore
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import se.inera.intyg.srs.controller.TestController
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class PrediktionIT : BaseIntegrationTest() {

    private val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"
    private val PREDIKTION_ROOT = "Envelope.Body.GetSRSInformationResponse.bedomningsunderlag.prediktion.diagnosprediktion"

    @Test
    fun testPredictionModel() {
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        addConsent("200005292396", true, "root")

        val response = sendPrediktionRequest("getPrediktion_Model1Request_output_0.44.xml", "X99")
        response.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.44"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("2"))
                .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Hög risk att sjukfallet varar i mer än 90 dagar"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
    }

//    @Test
    fun testPredictionModelv3() {
        setModels("m797v3")
        val questions = listOf(
            TestController.PredictionQuestion("Question", "SA_1_gross", "Help text", false,
                listOf(
                    TestController.PredictionResponse("SA_1_gross", "(0,90]", false),
                    TestController.PredictionResponse("SA_1_gross", "(180,366]", false),
                    TestController.PredictionResponse("SA_1_gross", "(90,180]", false),
                    TestController.PredictionResponse("SA_1_gross", "0", true)
                )),
            TestController.PredictionQuestion("Question", "SA_SyssStart_fct", "Help text", false,
                listOf(
                    TestController.PredictionResponse("SA_SyssStart_fct", "not_unemp", true),
                    TestController.PredictionResponse("SA_SyssStart_fct", "unemp", false)
                )),
            TestController.PredictionQuestion("Question", "SA_ExtentFirst", "Help text", false,
                listOf(
                    TestController.PredictionResponse("SA_ExtentFirst", "0.25", false),
                    TestController.PredictionResponse("SA_ExtentFirst", "0.5", false),
                    TestController.PredictionResponse("SA_ExtentFirst", "0.75", false),
                    TestController.PredictionResponse("SA_ExtentFirst", "1", true)
                )),
            TestController.PredictionQuestion("Question", "comorbidity", "Help text", false,
                listOf(
                    TestController.PredictionResponse("comorbidity", "no", true),
                    TestController.PredictionResponse("comorbidity", "yes", false)
                )),
            TestController.PredictionQuestion("Question", "DP_atStart", "Help text", false,
                listOf(
                    TestController.PredictionResponse("DP_atStart", "true", false),
                    TestController.PredictionResponse("DP_atStart", "false", true)
                )),
            TestController.PredictionQuestion("Question", "Visits_yearBefore_all_r1_median", "Help text", false,
                listOf(
                    TestController.PredictionResponse("Visits_yearBefore_all_r1_median", "aboveMedian", false),
                    TestController.PredictionResponse("Visits_yearBefore_all_r1_median", "LessT2V", true)
                )

            ));
        addDiagnosis(TestController.DiagnosisRequest("M79", 0.37, false, listOf(), "3.0"))
        addDiagnosis(TestController.DiagnosisRequest("M797", 0.0, false, questions, "3.0"))
        val response = sendPrediktionRequest("getPrediktion_Model1Request_output_M797_0.65.xml", "M797")
        response.assertThat()
            .body("$PREDIKTION_ROOT.prevalens", equalTo("0.37"))
            .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.65"))
            .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("3"))
            .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Mycket hög risk att sjukfallet varar i mer än 90 dagar"))
            .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
    }

    @Test
    fun testHighestVersionOfPredictionShouldBeUsed() {
        // Om två prediktionsfiler för samma diagnos finns ska den med högst
        // versionsnummer användas
        setModels("x99v0", "x99v1")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        addConsent("200005292396", true, "root")

        val response = sendPrediktionRequest("getPrediktion_Model2Request_output_0.6.xml", "X99")
        response.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.06"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("1"))
    }

    @Test
    fun testShouldPickUpChangedModelFiles() {
        setModels() // Removes all models
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        addConsent("195801080214", true, "root")

        val response1 = sendPrediktionRequest("getPrediktion_Model1Request_output_0.89.xml", "X99")
        response1.assertThat()
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("PREDIKTIONSMODELL_SAKNAS"))

        setModels("x99v0")

        val response2 = sendPrediktionRequest("getPrediktion_Model1Request_output_0.89.xml", "X99")
        response2.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.89"))
    }

    @Test
    fun testGetHistoricPrediction() {
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        // TODO: Check how to/fix addition of questions and answers to the test suite without breaking referential integrity of the test
        // TODO: We probably need to clean up questions and answers in BaseIntegrationTest between each test
//        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, listOf(
//                TestController.PredictionQuestion("SA_1_gross", "SA_1_gross", "", listOf(TestController.PredictionResponse("0", "0",false))),
//                TestController.PredictionQuestion("DP_atStart", "DP_atStart", "", listOf(TestController.PredictionResponse("false", "false",false))),
//                TestController.PredictionQuestion("Visits_yearBefore_all_r1_median", "Visits_yearBefore_all_r1_median", "",
//                        listOf(TestController.PredictionResponse("LessT2V", "LessT2V",false))),
//                TestController.PredictionQuestion("SA_SyssStart_fct", "SA_SyssStart_fct", "",
//                        listOf(TestController.PredictionResponse("work", "work",false),TestController.PredictionResponse("unemp", "unemp",false))),
//                TestController.PredictionQuestion("fam_cat_4_cat_fct", "fam_cat_4_cat_fct", "",
//                        listOf(TestController.PredictionResponse("Married", "Married",false))),
//                TestController.PredictionQuestion("edu_cat_fct", "edu_cat_fct", "",
//                        listOf(TestController.PredictionResponse(">12 years", ">12 years",false)))
//        )))
        addConsent("200005292396", true, "root")

        val response = sendPrediktionRequest("getPrediktion_Model1Request_output_0.44.xml",
                "X99", "intyg2")
        response.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.44"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("2"))
                .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Hög risk att sjukfallet varar i mer än 90 dagar"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
                .body("$PREDIKTION_ROOT.prediktionsfaktorer.fragasvar.size()", equalTo(0))

        val response2 = sendPrediktionRequest("getHistoricPrediktion_Model1Request_output_0.44.xml",
                "X99", "intyg2")
        response2.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.44"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("2"))
                .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Hög risk att sjukfallet varar i mer än 90 dagar"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
                .body("$PREDIKTION_ROOT.prediktionsfaktorer.fragasvar.size()", equalTo(0)) // No questions in integration tests yet


    }

    @Test
    fun testExistingPredictionOnHigherDiagnosisIdLevel() {
        // T.ex. När prediktion efterfrågas på M751 men bara finns på M75
        // så ska prediktion för M75 returneras.
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        addConsent("195801080214", true, "root")

        val response2 = sendPrediktionRequest("getPrediktion_Model1Request_output_0.89.xml", "X991")
        response2.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.89"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("DIAGNOSKOD_PA_HOGRE_NIVA"))
    }

    @Test
    fun testExistingPredictionOn5CharDiagnosisId() {
        // Om prediktion finns för M75 och M7512 och M7512 efterfrågas
        // så är det prediktion för M7512 som ska returneras

        setModels("x99v0", "x9900v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        addDiagnosis(TestController.DiagnosisRequest("X9900", 1.0, true, emptyList()))
        addConsent("195801080214", true, "root")

        sendPrediktionRequest("getPrediktion_Model2Request_output_0.77.xml", "X9900")
                .assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.77"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
    }

    @Test
    fun testTooLongDiagnosisCodeRequestShouldBeRejected() {
        addConsent("200005292396", true, "root")
        // Anropa med 6-ställig kod och verifiera fel
        sendPrediktionRequest("getPrediktion_Model2Request_output_0.77.xml", "X99001")
                .assertThat()
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("NOT_OK"))
    }

    val log = LoggerFactory.getLogger("se.inera.intyg.srs.integration-test")


    @Test
//    @Disabled// TODO: Fixa så att det här testet fungerar igen, det är i förberedelsen det går fel, när diagnos läggs till
    fun testMissingInputParameters() {
        // Om inparametrar till modellen saknas i anropet ska felmeddelande
        // anges.
        // TODO: Ett mer beskrivande felmeddelande hade varit bättre än bara "NOT_OK".
        setModels("x99v0")
        log.debug("before adding diagnosis")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false,
                listOf(
                        TestController.PredictionQuestion("Question", "SA_1_gross", "Help text", false,
                                listOf(
                                        TestController.PredictionResponse("SA_1_gross", "0", true),
                                        TestController.PredictionResponse("SA_1_gross", "1", true)
                                        )),
                        TestController.PredictionQuestion("Question", "DP_atStart", "Help text", false,
                                listOf(
                                        TestController.PredictionResponse("DP_atStart", "true", true),
                                        TestController.PredictionResponse("DP_atStart", "false", true)
                                )
                ))))
        addConsent("200005292396", true, "root")
        log.debug("after adding diagnosis, sending test")
        sendPrediktionRequest("getPrediktion_Model1Request_missingParams.xml", "X99")
                .assertThat()
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("NOT_OK"))
    }

    @Test
    fun testResultShouldBeSavedToDatabase() {
        // Kontrollera att Prediktionsresultat ska sparas i databasen tillsammans med
        // Intygs-ID
        restTemplate.delete("/intyg")
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList()))
        addConsent("200005292396", true, "root")

        sendPrediktionRequest("getPrediktion_Model1Request_output_0.44.xml", "X999", "TestId")

        getIntyg("TestId").first().let {
            assertThat(it["diagnosis"] as String, equalTo("X99"))
            assertThat(it["incomingDiagnosis"] as String, equalTo("X999"))
            assertThat(it["probability"] as Double, equalTo(0.44))
            assertThat(it["riskCategory"] as Int, equalTo(2))
        }
    }

    @Test
    fun testMissingPredictionShouldYieldErrorMessage() {
        addConsent("197308051234", true, "root")
        // Om prediktion saknas för en diagnos ska detta indikeras för den diagnosen.
        given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("prediktion/getPrediktionRequest.xml")
                    .replace("diagnos_placeholder", "FINNSINTE"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", equalTo("OK"))
                .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Prediktion saknas."))
    }

    private fun sendPrediktionRequest(requestFile: String, diagnosisId: String, intygsId: String = "intygsId") =
            given()
                .contentType(ContentType.XML)
                .body(getClasspathResourceAsString("prediktion/$requestFile")
                        .replace("diagnos_placeholder", diagnosisId)
                        .replace("intygsid_placeholder", intygsId))
            .whenever()
                .post("/services/getsrs")
            .then()
                .statusCode(200)
}
