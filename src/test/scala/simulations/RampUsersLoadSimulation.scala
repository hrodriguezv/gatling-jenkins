package simulations
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class RampUsersLoadSimulation extends Simulation {
  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))


  def getAllVideoGames() = {
    exec(
      http("Get all video games").get("videogames").check(status.is(200))
    )
  }

  def getSpecificVideoGame() = {
    exec(
      http("Get specific video").get("videogames/1").check(status.in(200 to 210))
    )
  }

  val scn = scenario("Ramp Up Users Load Simulation")
    .exec(getAllVideoGames())
    .pause(5 seconds)
    .exec(getSpecificVideoGame())
    .pause(5 seconds)
    .exec(getAllVideoGames())


  setUp(
    scn.inject(
      nothingFor(5 seconds),
      //constantUsersPerSec(10) during(10 seconds)
      rampUsersPerSec(1) to 5 during(20 seconds)
    ).protocols(httpConf.inferHtmlResources())
  )


}
