package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CsvFeederToCustom extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  var idNumbers = (1 to 10).iterator
  var customFeeder = Iterator.continually(Map("gameId"-> idNumbers.next()))

  def getSpecificVideoGame() = {
    repeat(10){
      feed(customFeeder)
      .exec(
        http("Get specific video game").get("videogames/${gameId}")
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
