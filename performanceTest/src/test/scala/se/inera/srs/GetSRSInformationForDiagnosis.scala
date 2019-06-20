package se.inera.srs
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class GetSRSInformationForDiagnosis extends Simulation {

  val numberOfUsers = 100

  val scn = scenario("GetSRSInformationForDiagnosis")
    .exec(http("GetSRSInformationForDiagnosis")
      .post("/getsrsfordiagnosis")
      .body(ELFileBody("request-bodies/GetSRSInformationForDiagnosis.xml"))
      .check(
        status.is(200),
        substring("atgardsrubrik>Säkerställ att patienten har återbesök inbokat")))
    .pause(50 milliseconds)


  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

