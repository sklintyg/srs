package se.inera.intyg.srs.integrationtest.getsrsinformation

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.Ignore
import org.junit.Test
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v1.GetSRSInformationResponseType
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class PrediktionIT : BaseIntegrationTest() {

    private val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"
    private val PREDIKTION_ROOT = "Envelope.Body.GetSRSInformationResponse.bedomningsunderlag.prediktion.diagnosprediktion"

    @Test
    fun testPredictionModel() {
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, emptyList()))

        val response = sendPrediktionRequest("getPrediktion_Model1Request_output_0.44.xml", "X99")
        response.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.44"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("2"))
                .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Ingen förhöjd risk detekterad."))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
    }

    @Test
    fun testHighestVersionOfPredictionShouldBeUsed() {
        // Om två prediktionsfiler för samma diagnos finns ska den med högst
        // versionsnummer användas
        setModels("x99v0", "x99v1")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, emptyList()))

        val response = sendPrediktionRequest("getPrediktion_Model2Request_output_0.6.xml", "X99")
        response.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.06"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("2"))
    }

    @Test
    fun testShouldPickUpChangedModelFiles() {
        setModels() // Removes all models
        val response1 = sendPrediktionRequest("getPrediktion_Model1Request_output_0.89.xml", "X99")
        response1.assertThat()
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("PREDIKTIONSMODELL_SAKNAS"))

        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, emptyList()))

        val response2 = sendPrediktionRequest("getPrediktion_Model1Request_output_0.89.xml", "X99")
        response2.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.89"))
    }


    @Test
    fun testExistingPredictionOnHigherDiagnosisIdLevel() {
        // T.ex. När prediktion efterfrågas på M751 men bara finns på M75
        // så ska prediktion för M75 returneras.
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, emptyList()))

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
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, emptyList()))
        addDiagnosis(TestController.DiagnosisRequest("X9900", 1.0, emptyList()))

        val response2 = sendPrediktionRequest("getPrediktion_Model2Request_output_0.77.xml", "X9900")
        response2.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.77"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))
    }

    @Test
    fun testTooLongDiagnosisCodeRequestShouldBeRejected() {
        // Anropa med 6-ställig kod och verifiera fel
        sendPrediktionRequest("getPrediktion_Model2Request_output_0.77.xml", "X99001")
                .statusCode(400)
    }

    @Test
    fun testMissingInputParameters() {
        // Om inparametrar till modellen saknas i anropet ska felmeddelande
        // anges.
        // TODO: Ett mer beskrivande felmeddelande hade varit bättre än bara "NOT_OK".
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, emptyList()))

        sendPrediktionRequest("getPrediktion_Model1Request_missingParams.xml", "X99")
                .assertThat()
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("NOT_OK"))
    }

    @Test
    @Ignore("Funktionalitet inte implementerat än (INTYG-4463)")
    fun testResultShouldBeSavedToDatabase() {
        // Kontrollera att Prediktionsresultat ska sparas i databasen tillsammans med
        // Intygs-ID

        //val response =
            given()
                .contentType(ContentType.XML)
                .body(getClasspathResourceAsString("prediktion/getPrediktionRequest.xml")
                        .replace("diagnosis_placeholder", "FINNSINTE")
                        .replace("intygsid_placeholder", "testid"))
            .whenever()
                .post("/services/getsrs")
            .then()
                .extract().response().`as`(GetSRSInformationResponseType::class.java)

        /*val intyg = getIntyg("testid")
        val riskkategori = response.bedomningsunderlag[0].prediktion.diagnosprediktion[0].risksignal.riskkategori
        assertThat(riskkategori, equalTo(intyg.riskkategori))*/
    }

    @Test
    fun testMissingPredictionShouldYieldErrorMessage() {
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