package se.inera.intyg.srs.integrationtest.getsrsinformation

import com.jayway.restassured.RestAssured
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class PrediktionIT : BaseIntegrationTest() {

    private val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"

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
        // Om prediktion saknas för en diagnos ska detta indikeras för den diagnosen.
        RestAssured.given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("prediktion/getPrediktionRequest.xml")
                    .replace("diagnosis_placeholder", "FINNSINTE"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", equalTo("OK"))
                .body("$SOAP_ROOT.bedomningsunderlag.prediktion.diagnosprediktion.risksignal.beskrivning", equalTo("Prediktion saknas."))
    }
}