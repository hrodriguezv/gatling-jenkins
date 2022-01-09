package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CsvFeeder extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  val csvFeeder = csv("data/gameVideos.csv").circular

  def getSpecificVideoGame() = {
    repeat(10){
      feed(csvFeeder)
      .exec(
        http("Get specific video game").get("videogames/${gameId}")
          .check(jsonPath("$.name").is("${gameName}"))
          .check(status.is(200))
      ).pause(1)
    }
  }

  // Scenario definition
  val scn = scenario("Calling endpoint using CSV feeder")
  .exec(
   getSpecificVideoGame()
  )

  // Load Scenario
  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpConf)
  )

}
