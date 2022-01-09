package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CheckResponseCode extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  // Scenario definition
  val scn = scenario("Request GetVideos 3 times with Pauses")
  // 1st part
  .exec(
    http("Get all video games - 1st call").get("videogames")
      .check(status.is(200))
  ).pause(5)
  // 2nd part
  .exec(
    http("Get specific video game - 2nd call").get("videogames/1")
      .check(status.in(200, 210))
  ).pause(1, 20)
  // 3rd part
  .exec(
    http("Get all video games - 3rd call").get("videogames")
      .check(status.not(400))
  ).pause(3000.milliseconds)

  // Load Scenario
  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpConf)
  )

}
