package se.inera.intyg.srs.integrationtest.util

import com.jayway.restassured.specification.RequestSpecification

/**
 * Created by stillor on 2/17/17.
 */
fun RequestSpecification.When(): RequestSpecification {
    return this.`when`()
}
