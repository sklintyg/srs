package se.inera.intyg.srs.integrationtest

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import com.jayway.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import se.inera.intyg.srs.integrationtest.util.When

const val SOAP_ROOT = "Envelope.Body.GetSRSInformationResponse"

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = arrayOf("classpath:test.properties"))
class GetSRSInformationResponderIT {
    @LocalServerPort
    private val serverPort: Int = 0

    @Before
    fun setup() {
        RestAssured.port = serverPort
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun testServicePageIsReachable() {
        given()
            .contentType(ContentType.JSON)
        .When()
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
        .When()
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

