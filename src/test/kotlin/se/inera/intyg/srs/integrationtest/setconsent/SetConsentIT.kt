package se.inera.intyg.srs.integrationtest.setconsent

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever

class SetConsentIT : BaseIntegrationTest() {

    @Test
    fun testMultipleConsentsOnSamePersonShouldOverwrite() {
        postStandardSetConsentRequest()
        postStandardSetConsentRequest()

        val consent = getConsent("191212121212", "abc")
        assertThat(consent.personnummer, equalTo("191212121212"))
        assertThat(consent.vardgivareId, equalTo("abc"))
        assertThat(consent.samtycke, equalTo(true))
    }



    private fun postStandardSetConsentRequest() =
        given()
            .contentType(ContentType.JSON)
            .body(getClasspathResourceAsString("setconsent/setConsentRequest.xml"))
        .whenever()
            .post("/services/set-consent")
        .then()
            .statusCode(200)



}