package se.inera.intyg.srs.integrationtest

import com.jayway.restassured.RestAssured
import org.json.JSONObject
import org.junit.Before
import org.junit.BeforeClass
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.web.client.RestTemplate
import se.inera.intyg.srs.controllers.TestController
import se.inera.intyg.srs.persistence.Consent
import se.inera.intyg.srs.persistence.Measure
import se.inera.intyg.srs.persistence.PredictionDiagnosis
import java.time.LocalDateTime
import java.time.Month

open class BaseIntegrationTest {

    @Before
    fun clearAllTables() {
        restTemplate.delete("/measures")
        restTemplate.delete("/consents")
        restTemplate.delete("/statistics")
        restTemplate.delete("/diagnosis")
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
            Measure::class.java)

    protected fun getConsent(personnummer: String, vardenhet: String): Consent? {
        val jsonString = restTemplate.getForObject(
                "/consents?personnummer=$personnummer&vardenhet=$vardenhet",
                String::class.java) ?: return null
        val jsonObject = JSONObject(jsonString)
        val timeObject = jsonObject.getJSONObject("skapatTid")

        return Consent(
                jsonObject.getString("personnummer"),
                jsonObject.getString("vardgivareId"),
                LocalDateTime.of(timeObject.getInt("year"),
                        Month.of(timeObject.getInt("monthValue")),
                        timeObject.getInt("dayOfMonth"),
                        timeObject.getInt("hour"),
                        timeObject.getInt("minute")))
    }

    protected fun addStatistics(diagnosId: String, bildUrl: String): String =
        restTemplate.postForObject(
            "/statistics",
            TestController.StatisticsRequest(diagnosId, bildUrl),
            String::class.java)

    protected fun addDiagnosis(request: TestController.DiagnosisRequest): PredictionDiagnosis =
        restTemplate.postForObject(
                "/diagnosis",
                request,
                PredictionDiagnosis::class.java)

    protected fun setModels(vararg models: String) {
        restTemplate.postForObject(
                "/set-models",
                createModelRequest(models),
                Void::class.java)
        restTemplate.postForObject(
                "/await-model-update",
                null,
                Void::class.java)
    }

    private fun createModelRequest(models: Array<out String>) =
            TestController.ModelRequest(
                models.contains("x99v0"),
                models.contains("x9900v0"),
                models.contains("x99v1"))

    @Suppress("UNCHECKED_CAST")
    protected fun getIntyg(intygsId: String) =
        restTemplate.getForObject(
                "/intyg/$intygsId",
                List::class.java
        ) as List<LinkedHashMap<String, Any>>

    protected fun getClasspathResourceAsString(fileName: String): String =
        ClassPathResource("integrationtest/$fileName").file.readText()

    companion object SetUp {

        private val baseURI = System.getProperty("integration.tests.baseUrl") ?: "http://localhost:8080"

        val restTemplate: RestTemplate = RestTemplateBuilder().rootUri(baseURI).build()

        @BeforeClass
        @JvmStatic
        fun setupRestAssured() {
            RestAssured.baseURI = baseURI
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        }
    }

}
