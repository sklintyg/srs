package se.inera.intyg.srs.integrationtest.getsrsinformation

import com.jayway.restassured.RestAssured
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever


const val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"
const val STATISTIK_ROOT = "$SOAP_ROOT.bedomningsunderlag.statistik"

class StatistikIT : BaseIntegrationTest() {

    @Test
    fun testExistingImageShouldBeReturnedAndNonExistingShouldYieldErrorMessage() {
        addStatistics("M75", "http://i.imgur.com/q0qXPgz.gif")

        RestAssured.given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("statistik/getStatistikRequest.xml"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", Matchers.equalTo("OK"))
                .body("$STATISTIK_ROOT.statistikbild[0].statistikstatus", Matchers.equalTo("STATISTIK_SAKNAS"))
                .body("$STATISTIK_ROOT.statistikbild[1].statistikstatus", Matchers.equalTo("OK"))
                .body("$STATISTIK_ROOT.statistikbild[1].bildadress", Matchers.equalTo("http://i.imgur.com/q0qXPgz.gif"))
    }

    @Test
    fun testExistingImageOnHigherDiagnosisIdLevel() {
        // Om M751 inte finns men M75 finns så ska statistik för denna returneras,
        // dessutom ska flaggan för högre diagnoskodnivå vara satt.

        addStatistics("M75", "http://i.imgur.com/q0qXPgz.gif")

        RestAssured.given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("statistik/getStatistikHigherDiagnoseRequest.xml"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", Matchers.equalTo("OK"))
                .body("$STATISTIK_ROOT.statistikbild[0].statistikstatus", Matchers.equalTo("DIAGNOSKOD_PA_HOGRE_NIVA"))
                .body("$STATISTIK_ROOT.statistikbild[0].bildadress", Matchers.equalTo("http://i.imgur.com/q0qXPgz.gif"))
    }
}