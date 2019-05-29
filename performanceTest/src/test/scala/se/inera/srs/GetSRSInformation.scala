package se.inera.srs
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class GetSRSInformationOne extends Simulation {

  val numberOfUsers = 100

  val scn = scenario("GetSRSInformationOne")
    .exec(http("GetSRSInformation")
      .post("/getsrs")
      .body(ELFileBody("request-bodies/GetSRSInformation-riskkategori-2.xml"))
      .check(
        status.is(200),
		substring("<person-id>195801080214</person-id>"),
        substring("<ns2:code>F43</ns2:code>"),
		substring("<riskkategori>2</riskkategori>")
		))
    .pause(50 milliseconds)


  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

