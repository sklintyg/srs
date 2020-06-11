package se.inera.intyg.srs.integrationtest.predictionquestions

import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import se.inera.intyg.srs.controller.TestController.*
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

open class PredictionQuestionsIT : BaseIntegrationTest() {

    private val SOAP_ROOT = "Envelope.Body.GetPredictionQuestionsResponse"

    @Test
    fun testGetPredictionQuestions() {
        addDiagnosis(DiagnosisRequest("M24", 0.54, false,
                listOf(PredictionQuestion("Stressad?", "abc", "Hjälptext", false,
                        listOf(PredictionResponse("Lite kanske", "def", true),
                                PredictionResponse("Bara på fredagar", "ghi", false))
                    ), PredictionQuestion("Chill?", "jkl", "Hjälp mig då", false,
                        listOf(PredictionResponse("Lätt mannen", "mno", false),
                                PredictionResponse("Bara på måndagar", "pqr", true))
                ))
        ))

        RestAssured
        .given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("predictionquestions/getPredictionQuestionsRequest.xml")
                    .replace("diagnosis_placeholder", "M24"))
        .whenever()
            .post("/services/predictionquestions")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.prediktionsfraga[0].frageid-srs", equalTo("abc"))
                .body("$SOAP_ROOT.prediktionsfraga[0].fragetext", equalTo("Stressad?"))
                .body("$SOAP_ROOT.prediktionsfraga[0].hjalptext", equalTo("Hjälptext"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[0].svarsid-srs", equalTo("def"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[0].svarstext", equalTo("Lite kanske"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[0].default", equalTo("true"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[1].svarsid-srs", equalTo("ghi"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[1].svarstext", equalTo("Bara på fredagar"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[1].default", equalTo("false"))
                .body("$SOAP_ROOT.prediktionsfraga[1].frageid-srs", equalTo("jkl"))
                .body("$SOAP_ROOT.prediktionsfraga[1].fragetext", equalTo("Chill?"))
                .body("$SOAP_ROOT.prediktionsfraga[1].hjalptext", equalTo("Hjälp mig då"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[0].svarsid-srs", equalTo("mno"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[0].svarstext", equalTo("Lätt mannen"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[0].default", equalTo("false"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[1].svarsid-srs", equalTo("pqr"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[1].svarstext", equalTo("Bara på måndagar"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[1].default", equalTo("true"))
    }

    @Test
    fun testGetPredictionQuestionsForHigherDiagnosis() {
        addDiagnosis(DiagnosisRequest("M24", 0.54, false,
                listOf(PredictionQuestion("Stressad?", "abc", "Hjälptext", false,
                        listOf(PredictionResponse("Lite kanske", "def", true),
                                PredictionResponse("Bara på fredagar", "ghi", false))
                ), PredictionQuestion("Chill?", "jkl", "Hjälp mig då", false,
                        listOf(PredictionResponse("Lätt mannen", "mno", false),
                                PredictionResponse("Bara på måndagar", "pqr", true))
                ))
        ))

        RestAssured.given()
                .contentType(ContentType.XML)
                .body(getClasspathResourceAsString("predictionquestions/getPredictionQuestionsRequest.xml")
                        .replace("diagnosis_placeholder", "M2400"))
            .whenever()
                .post("/services/predictionquestions")
            .then()
                .statusCode(200)
                .assertThat()
                .body("$SOAP_ROOT.prediktionsfraga[0].frageid-srs", equalTo("abc"))
                .body("$SOAP_ROOT.prediktionsfraga[0].fragetext", equalTo("Stressad?"))
                .body("$SOAP_ROOT.prediktionsfraga[0].hjalptext", equalTo("Hjälptext"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[0].svarsid-srs", equalTo("def"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[0].svarstext", equalTo("Lite kanske"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[0].default", equalTo("true"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[1].svarsid-srs", equalTo("ghi"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[1].svarstext", equalTo("Bara på fredagar"))
                .body("$SOAP_ROOT.prediktionsfraga[0].svarsalternativ[1].default", equalTo("false"))
                .body("$SOAP_ROOT.prediktionsfraga[1].frageid-srs", equalTo("jkl"))
                .body("$SOAP_ROOT.prediktionsfraga[1].fragetext", equalTo("Chill?"))
                .body("$SOAP_ROOT.prediktionsfraga[1].hjalptext", equalTo("Hjälp mig då"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[0].svarsid-srs", equalTo("mno"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[0].svarstext", equalTo("Lätt mannen"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[0].default", equalTo("false"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[1].svarsid-srs", equalTo("pqr"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[1].svarstext", equalTo("Bara på måndagar"))
                .body("$SOAP_ROOT.prediktionsfraga[1].svarsalternativ[1].default", equalTo("true"))
    }

}
