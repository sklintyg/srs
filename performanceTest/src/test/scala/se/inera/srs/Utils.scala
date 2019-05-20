package se.inera.srs

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import scala.io.Source._
import collection.mutable.{ HashMap, MultiMap, Set }
import scala.xml.XML

import java.util.UUID

import scalaj.http._

object Utils {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080" ) + "/services"
}
