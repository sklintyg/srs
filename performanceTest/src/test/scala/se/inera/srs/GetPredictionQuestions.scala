package se.inera.srs
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.core.session._
import io.gatling.http.request.Body
import scala.concurrent.duration._
import collection.mutable.{ HashMap, MultiMap, Set }

class GetPredictionQuestions extends Simulation {

  val numberOfUsers = 100

  val scn = scenario("GetPredictionQuestions")
    .exec(http("GetPredictionQuestions")
      .post("/predictionquestions")
      .body(ELFileBody("request-bodies/GetPredictionQuestions.xml"))
      .check(
        status.is(200),
        substring("<ns2:svarstext>Yrkesarbetar /Föräldraledig /Studerar</ns2:svarstext>")))
    .pause(50 milliseconds)


  setUp(scn.inject(rampUsers(numberOfUsers) over (120 seconds)).protocols(Conf.httpConf))

}

