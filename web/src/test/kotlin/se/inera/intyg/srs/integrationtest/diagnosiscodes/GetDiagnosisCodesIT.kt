package se.inera.intyg.srs.integrationtest.diagnosiscodes

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import se.inera.intyg.srs.controller.TestController
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class GetDiagnosisCodesIT : BaseIntegrationTest() {

    @Test
    fun testDiagnosisCodes() {
        addDiagnosis(TestController.DiagnosisRequest("M24", 0.54, emptyList()))
        addDiagnosis(TestController.DiagnosisRequest("M14", 0.54, emptyList()))
        addDiagnosis(TestController.DiagnosisRequest("M6", 0.54, emptyList()))

        RestAssured.given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("getdiagnosiscodes/getDiagnosisCodesRequest.xml"))
        .whenever()
            .post("/services/diagnosiscodes")
        .then()
            .statusCode(200)
            .assertThat()
                .body("Envelope.Body.GetDiagnosisCodesResponse.diagnos[0].code", equalTo("M24"))
                .body("Envelope.Body.GetDiagnosisCodesResponse.diagnos[1].code", equalTo("M14"))
                .body("Envelope.Body.GetDiagnosisCodesResponse.diagnos[2].code", equalTo("M6"))
    }

}