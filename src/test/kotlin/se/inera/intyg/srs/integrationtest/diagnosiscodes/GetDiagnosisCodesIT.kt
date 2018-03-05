package se.inera.intyg.srs.integrationtest.diagnosiscodes

import com.jayway.restassured.RestAssured
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class GetDiagnosisCodesIT : BaseIntegrationTest() {

    @Test
    fun testDiagnosisCodes() {
        addDiagnosis(TestController.DiagnosisRequest("M24", 0.54, 0.54, 0.64, emptyList()))
        addDiagnosis(TestController.DiagnosisRequest("M14", 0.54, 0.54, 0.64, emptyList()))
        addDiagnosis(TestController.DiagnosisRequest("M6", 0.54, 0.54, 0.64, emptyList()))

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