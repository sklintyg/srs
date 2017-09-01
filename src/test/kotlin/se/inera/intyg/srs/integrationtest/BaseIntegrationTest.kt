package se.inera.intyg.srs.integrationtest

import com.jayway.restassured.RestAssured
import org.junit.Before
import org.junit.BeforeClass
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.persistence.Consent
import se.inera.intyg.srs.persistence.Measure

open class BaseIntegrationTest {

    @Before
    fun clearAllTables() {
        restTemplate.delete("/measures")
        restTemplate.delete("/consents")
    }

    protected fun addConsent(personnummer: String, samtycke: Boolean, vardenhet: String): String =
        restTemplate.postForObject(
            "/consents",
            TestController.ConsentRequest(personnummer, samtycke, vardenhet),
            String::class.java)

    protected fun addMeasure(diagnosId: String, diagnosText: String, rekommendationer: List<String>): Measure =
        restTemplate.postForObject(
            "/measures",
            TestController.MeasureRequest(diagnosId, diagnosText, rekommendationer),
            Measure::class.java
        )

    protected fun getConsent(personnummer: String, vardenhet: String): Consent =
        restTemplate.getForObject(
            "/consents?personnummer=$personnummer&vardenhet=$vardenhet",
            Consent::class.java
        )

    protected fun getClasspathResourceAsString(fileName: String): String {
        return ClassPathResource("integrationtest/$fileName").file.readText()
    }
    companion object SetUp {

        private val baseURI = "http://localhost"
        private val basePort = 8080
        private val restTemplate = RestTemplateBuilder().rootUri(baseURI + ":" + basePort).build()

        @BeforeClass
        @JvmStatic
        fun setupRestAssured() {
            // TODO: Bör vara paremetriserat så vi kan välja miljö
            RestAssured.baseURI = baseURI
            RestAssured.port = basePort
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        }
    }

}
