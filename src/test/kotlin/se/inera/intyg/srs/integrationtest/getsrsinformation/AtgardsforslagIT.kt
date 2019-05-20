package se.inera.intyg.srs.integrationtest.getsrsinformation

import com.jayway.restassured.RestAssured
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class AtgardsforslagIT : BaseIntegrationTest() {
    private val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"
    private val ATGARD_ROOT = "$SOAP_ROOT.bedomningsunderlag.atgardsrekommendationer"

    @Test
    fun testRequestAtgardsforslag() {
        // En request som frågar efter åtgärdsförslag för en diagnos
        // som har åtgärdsförslag ska returnera dessa. Diagnos som inte
        // har åtgärdsförslag ska indikera detta.
        addMeasure("M75", "Riktigt stressad",
                listOf(Pair("Relaxa","Ta det lugnt"), Pair("Chilla","Hetsa inte upp dig")))

        RestAssured.given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("atgardsforslag/getAtgardsforslagRequest.xml")
                .replace("diagnosis_placeholder", "M75"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", equalTo("OK"))
                .body("$ATGARD_ROOT.rekommendation[0].atgardsrekommendationstatus", equalTo("INFORMATION_SAKNAS"))
                .body("$ATGARD_ROOT.rekommendation[1].atgardsrekommendationstatus", equalTo("OK"))
                .body("$ATGARD_ROOT.rekommendation[1].diagnos.displayName", equalTo("Riktigt stressad"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[0].atgardsforslag", equalTo("Ta det lugnt"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[0].prioritet", equalTo("1"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[1].atgardsforslag", equalTo("Hetsa inte upp dig"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[1].prioritet", equalTo("2"))

    }

    @Test
    fun testRequestAtgardsforslagHigherDiagnoseLevel() {
        // En request som frågar efter åtgärdsförslag för en diagnos
        // som har åtgärdsförslag fast på högre nivå ska returnera dessa.
        // Status ska indikera att åtgärdsförslagen fanns på högre nivå.
        addMeasure("M75", "Riktigt stressad",
                listOf(Pair("Relaxa","Ta det lugnt"), Pair("Chilla","Hetsa inte upp dig")))

        RestAssured.given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("atgardsforslag/getAtgardsforslagRequest.xml")
                .replace("diagnosis_placeholder", "M751"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", equalTo("OK"))
                .body("$ATGARD_ROOT.rekommendation[1].atgardsrekommendationstatus", equalTo("DIAGNOSKOD_PA_HOGRE_NIVA"))
                .body("$ATGARD_ROOT.rekommendation[1].diagnos.displayName", equalTo("Riktigt stressad"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[0].atgardsforslag", equalTo("Ta det lugnt"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[0].prioritet", equalTo("1"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[0].version", notNullValue())
                .body("$ATGARD_ROOT.rekommendation[1].atgard[1].atgardsforslag", equalTo("Hetsa inte upp dig"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[1].prioritet", equalTo("2"))
                .body("$ATGARD_ROOT.rekommendation[1].atgard[1].version", notNullValue())

    }
}