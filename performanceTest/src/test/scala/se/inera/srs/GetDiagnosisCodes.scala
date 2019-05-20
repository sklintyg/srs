package se.inera.srs
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class GetDiagnosisCodes extends Simulation {

  val numberOfUsers = 100

  val testpersonnummer = csv("data/intyg.csv").circular

  val scn = scenario("GetDiagnosisCodes")
    .feed(testpersonnummer)
    .exec(http("GetDiagnosisCodes")
      .post("/diagnosiscodes")
      .body(ELFileBody("request-bodies/GetDiagnosisCodes.xml"))
      .check(
        status.is(200),
        substring("<code>F43</code>")))
    .pause(50 milliseconds)

  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))
}

