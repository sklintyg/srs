package se.inera.intyg.srs.integrationtest.util

import io.restassured.specification.RequestSpecification

/**
 * Since "when" is reserved in kotlin, we can import this and use "whenever" instead
 */
fun RequestSpecification.whenever(): RequestSpecification {
    return this.`when`()
}
