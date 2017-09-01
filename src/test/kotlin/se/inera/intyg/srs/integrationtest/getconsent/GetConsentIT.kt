package se.inera.intyg.srs.integrationtest.getconsent

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

const val SOAP_ROOT = "Envelope.Body.GetConsentResponse"

class GetConsentIT : BaseIntegrationTest() {

    @Test
    fun testPositiveConsentForAPatientShouldBeReturned() {
        addConsent("191212121212", true, "abc")

        given()
            .contentType(ContentType.JSON)
            .body(getClasspathResourceAsString("getconsent/getConsentRequest.xml"))
        .whenever()
            .post("/services/get-consent")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.samtycke", Matchers.equalTo("true"))
    }
}