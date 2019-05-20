package se.inera.srs
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class GetRiskPredictionForCertificate extends Simulation {

  val numberOfUsers = 100

  val scn = scenario("GetRiskPredictionForCertificate")
    .exec(http("GetRiskPredictionForCertificate")
      .post("/get-risk-prediction-for-certificate/v1.0")
      .body(ELFileBody("request-bodies/GetRiskPredictionForCertificate.xml"))
      .check(
        status.is(200),
        substring("<GetRiskPredictionForCertificateResponse")))
    .pause(50 milliseconds)


  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

