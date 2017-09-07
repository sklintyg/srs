package se.inera.intyg.srs.integrationtest.setconsent

import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.exparity.hamcrest.date.LocalDateTimeMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import se.inera.intyg.srs.integrationtest.BaseIntegrationTest
import se.inera.intyg.srs.integrationtest.util.whenever
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SetConsentIT : BaseIntegrationTest() {
    @Test
    fun testSetConsentFalse() {
        postStandardSetConsentRequest(false)

        val consent = getConsent("191212121212", "abc")
        assertThat(consent, nullValue())
    }

    @Test
    fun testSetConsentTrue() {
        postStandardSetConsentRequest(true)

        val consent = getConsent("191212121212", "abc")
        assertThat(consent, notNullValue())
    }

    @Test
    fun testMultipleConsentsOnSamePersonShouldOverwrite() {
        postStandardSetConsentRequest(true)
        postStandardSetConsentRequest(true)

        val consent = getConsent("191212121212", "abc")
        assertThat(consent, notNullValue())
        assertThat(consent?.personnummer, equalTo("191212121212"))
        assertThat(consent?.vardgivareId, equalTo("abc"))
    }

    @Test
    fun testRevokeConsent() {

    }

    @Test
    fun testTimeOnConsentShouldBeCorrect() {
        postStandardSetConsentRequest(true)

        val consent = getConsent("191212121212", "abc")

        assertThat(consent?.skapatTid, LocalDateTimeMatchers.within(5, ChronoUnit.MINUTES, LocalDateTime.now()))
    }



    private fun postStandardSetConsentRequest(samtycke: Boolean) =
        given()
            .contentType(ContentType.XML)
            .body(getClasspathResourceAsString("setconsent/setConsentRequest.xml")
                    .replace("samtycke_placeholder", samtycke.toString()))
        .whenever()
            .post("/services/set-consent")
        .then()
            .statusCode(200)
            .assertThat()
                .body("Envelope.Body.SetConsentResponse.resultCode", equalTo("OK"))

}