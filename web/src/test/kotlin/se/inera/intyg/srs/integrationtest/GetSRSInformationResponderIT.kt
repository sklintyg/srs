package se.inera.intyg.srs.integrationtest

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import se.inera.intyg.srs.integrationtest.util.whenever

const val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"

class GetSRSInformationResponderIT : BaseIntegrationTest() {

    @Test
    fun testServicePageIsReachable() {
        given()
            .contentType(ContentType.JSON)
        .whenever()
            .get("/services")
        .then()
            .statusCode(200)
            .assertThat().body(containsString("Available SOAP services:"))
    }

    @Test
    fun testGetSrs() {
        given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("getSRSInformationRequest.xml"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", equalTo("OK"))
    }

    @Test
    fun testWhenAllFilterFlagsAreDisabledNothingShouldBeReturned() {
        given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("emptyUtdataFilterRequest.xml"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", equalTo("OK"))
                .body(SOAP_ROOT, not(hasItem("atgardsrekommendationer")))
                .body(SOAP_ROOT, not(hasItem("prediktion")))
                .body(SOAP_ROOT, not(hasItem("statistik")))
    }

}

