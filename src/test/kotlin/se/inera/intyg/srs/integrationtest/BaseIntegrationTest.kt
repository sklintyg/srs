package se.inera.intyg.srs.integrationtest

import com.jayway.restassured.RestAssured
import org.junit.Before

open class BaseIntegrationTest {
    @Before
    fun setup() {
        // TODO: Bör vara paremetriserat så vi kan välja miljö
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = 8080
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }
}