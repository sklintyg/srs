package se.inera.intyg.srs.integrationtest.getsrsinformation

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever
import java.math.BigInteger

class StatistikIT : BaseIntegrationTest() {

    private val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"
    private val STATISTIK_ROOT = "$SOAP_ROOT.bedomningsunderlag.statistik"

    @Test
    fun testExistingImageShouldBeReturnedAndNonExistingShouldYieldErrorMessage() {
        addStatistics("M75", 30, 90, 100, 100)

        given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("statistik/getStatistikRequest.xml"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", Matchers.equalTo("OK"))
                .body("$STATISTIK_ROOT.diagnosstatistik[0].statistikstatus", Matchers.equalTo("STATISTIK_SAKNAS"))
                .body("$STATISTIK_ROOT.diagnosstatistik[1].statistikstatus", Matchers.equalTo("OK"))
                .body("$STATISTIK_ROOT.diagnosstatistik[1].data[0].dagintervall_min", Matchers.equalTo("30"))
                .body("$STATISTIK_ROOT.diagnosstatistik[1].data[0].individer", Matchers.equalTo("100"))
    }

    @Test
    fun testExistingImageOnHigherDiagnosisIdLevel() {
        // Om M751 inte finns men M75 finns så ska statistik för denna returneras,
        // dessutom ska flaggan för högre diagnoskodnivå vara satt.

        addStatistics("M75", 30, 90, 150, 150)

        given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("statistik/getStatistikHigherDiagnoseRequest.xml"))
        .whenever()
            .post("/services/getsrs")
        .then()
            .statusCode(200)
            .assertThat()
                .body("$SOAP_ROOT.resultCode", Matchers.equalTo("OK"))
                .body("$STATISTIK_ROOT.diagnosstatistik[0].statistikstatus", Matchers.equalTo("DIAGNOSKOD_PA_HOGRE_NIVA"))
                .body("$STATISTIK_ROOT.diagnosstatistik[0].data[0].dagintervall_min", Matchers.equalTo("30"))
                .body("$STATISTIK_ROOT.diagnosstatistik[0].data[0].individer", Matchers.equalTo("150"))
    }
}