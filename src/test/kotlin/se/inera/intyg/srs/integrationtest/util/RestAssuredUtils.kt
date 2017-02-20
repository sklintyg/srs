package se.inera.intyg.srs.integrationtest.util

import com.jayway.restassured.specification.RequestSpecification

fun RequestSpecification.When(): RequestSpecification {
    return this.`when`()
}
