package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration.DurationInt


class RuntimeParameters extends Simulation {


  // Simulation Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  def getAllVideoGames() = {
    exec(
      http("Get all video games").get("videogames").check(status.is(200))
    )
  }

  def getPropertyEnv(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def userCount: Int = {
    getPropertyEnv("USERS", "5").toInt
  }
  def rampDuration: Int = {
    getPropertyEnv("RAMP_DURATION", "10").toInt
  }
  def testDuration: Int = {
    getPropertyEnv("TEST_DURATION", "60").toInt
  }

  before{
    println(s"Running tests with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total duration configured: ${testDuration} seconds")
  }

  // scenario definition
  val scn = scenario("Ramp Up Users Load Simulation")
    .forever(){
      exec(getAllVideoGames())
    }

  // load simulation
  setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(userCount) during (rampDuration second)
    )
  ).protocols(httpConf.inferHtmlResources())
    .maxDuration(testDuration seconds)

}
