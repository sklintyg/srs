package se.inera.intyg.srs.integrationtest

import com.jayway.restassured.RestAssured
import org.json.JSONObject
import org.junit.Before
import org.junit.BeforeClass
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.persistence.Consent
import se.inera.intyg.srs.persistence.Measure
import java.time.LocalDateTime
import java.time.Month

open class BaseIntegrationTest {

    @Before
    fun clearAllTables() {
        restTemplate.delete("/measures")
        restTemplate.delete("/consents")
        restTemplate.delete("/statistics")
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

    protected fun getConsent(personnummer: String, vardenhet: String): Consent {
        val jsonString = restTemplate.getForObject(
                "/consents?personnummer=$personnummer&vardenhet=$vardenhet",
                String::class.java
        )
        val jsonObject = JSONObject(jsonString)
        val timeObject = jsonObject.getJSONObject("skapatTid")

        return Consent(
                jsonObject.getString("personnummer"),
                jsonObject.getString("vardgivareId"),
                LocalDateTime.of(timeObject.getInt("year"),
                        Month.of(timeObject.getInt("monthValue")),
                        timeObject.getInt("dayOfMonth"),
                        timeObject.getInt("hour"),
                        timeObject.getInt("minute"))
        )
    }


    protected fun addStatistics(diagnosId: String, bildUrl: String): String =
        restTemplate.postForObject(
            "/statistics",
            TestController.StatisticsRequest(diagnosId, bildUrl),
            String::class.java
        )

    protected fun getClasspathResourceAsString(fileName: String): String =
        ClassPathResource("integrationtest/$fileName").file.readText()

    companion object SetUp {

        private val baseURI = System.getProperty("integration.tests.baseUrl") ?: "http://localhost:8080"

        private val restTemplate = RestTemplateBuilder().rootUri(baseURI).build()

        @BeforeClass
        @JvmStatic
        fun setupRestAssured() {
            RestAssured.baseURI = baseURI
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        }
    }

}
