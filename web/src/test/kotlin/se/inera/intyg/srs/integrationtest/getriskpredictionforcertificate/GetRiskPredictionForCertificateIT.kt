package se.inera.intyg.srs.integrationtest.getsrsinformation

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import se.inera.intyg.srs.controller.TestController
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class GetRiskPredictionForCertificateIT : BaseIntegrationTest() {

    private val PREDIKTION_ROOT = "Envelope.Body.GetSRSInformationResponse.bedomningsunderlag.prediktion.diagnosprediktion"
    private val RISKPREDIKTIONER_ROOT = "Envelope.Body.GetRiskPredictionForCertificateResponse.riskPrediktioner"

    @Test
    fun testGetRiskPredictionForCertificateOnlyReturnsLatest() {
        setModels("x99v0")
        addDiagnosis(TestController.DiagnosisRequest("X99", 1.0, false, emptyList(), "3.0"))
        addConsent("200005292396", true, "root")

        val response = sendPrediktionRequest("getPrediktion_Model1Request_output_0.44.xml", "X99", "intyg1")
        response.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.44"))
                .body("$PREDIKTION_ROOT.risksignal.riskkategori", equalTo("2"))
                .body("$PREDIKTION_ROOT.risksignal.beskrivning", equalTo("Hög risk att sjukfallet varar i mer än 90 dagar"))
                .body("$PREDIKTION_ROOT.diagnosprediktionstatus", equalTo("OK"))

        setModels("x99v0")

        val response2 = sendPrediktionRequest("getPrediktion_Model1Request_output_0.78.xml", "X99", "intyg1")
        response2.assertThat()
                .body("$PREDIKTION_ROOT.sannolikhet-overgransvarde", equalTo("0.78"))


        val response3 = sendGetRiskPredictionForCertificateRequest("getRiskPredictionForCertificate.xml",
                "intyg1")
        response3.assertThat()
                .body("$RISKPREDIKTIONER_ROOT.size()", equalTo(1))
                .body("$RISKPREDIKTIONER_ROOT[0].intygs-id", equalTo("intyg1"))
                .body("$RISKPREDIKTIONER_ROOT[0].risksignal.riskkategori", equalTo("3"))
                .body("$RISKPREDIKTIONER_ROOT[0].risksignal.beskrivning", startsWith("Mycket hög"))

    }

    private fun sendGetRiskPredictionForCertificateRequest(requestFile: String, intygsId: String = "intygsId") =
            given()
                    .contentType(ContentType.XML)
                    .body(getClasspathResourceAsString("prediktion/$requestFile")
                            .replace("intygsid_placeholder", intygsId))
                    .whenever()
                    .post("/services/get-risk-prediction-for-certificate/v1.0")
                    .then()
                    .statusCode(200)

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
