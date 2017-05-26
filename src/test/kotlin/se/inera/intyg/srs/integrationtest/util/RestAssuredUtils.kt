package se.inera.intyg.srs.integrationtest.util

import com.jayway.restassured.specification.RequestSpecification

fun RequestSpecification.whenever(): RequestSpecification {
    return this.`when`()
}
