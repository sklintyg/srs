package se.inera.intyg.srs.integrationtest

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.Before

import org.junit.Test
import org.springframework.core.io.ClassPathResource

import se.inera.intyg.srs.integrationtest.util.whenever

const val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"

class GetSRSInformationResponderIT {

    @Before
    fun setup() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = 8080
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun testServicePageIsReachable() {
        given()
            .contentType(ContentType.JSON)
        .whenever()
            .get("/services")
        .then()
            .statusCode(200)
            .assertThat().body(Matchers.containsString("Available SOAP services:"))
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
                .body("$SOAP_ROOT.resultCode", Matchers.equalTo("OK"))
    }

    private fun getClasspathResourceAsString(fileName: String): String {
        return ClassPathResource("integrationtest/$fileName").file.readText()
    }
}

