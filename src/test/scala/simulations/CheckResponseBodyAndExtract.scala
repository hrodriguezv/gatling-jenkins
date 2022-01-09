package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CheckResponseBodyAndExtract extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  // Scenario definition
  val scn = scenario("Check response using JSON Path")

    .exec(
    http("Get specific video game").get("videogames/1")
      .check(jsonPath("$.name").is("Resident Evil 4"))
    )

    .exec(
      http("Get all video games")
        .get("videogames")
        .check(
          jsonPath("$[1].id").saveAs("gameId")
        )
    )

    // debugging session attributes
    .exec {session => println(session); session}

    .exec(
      http("Get specific video game 2").get("videogames/${gameId}")
        .check(jsonPath("$.name").is("Gran Turismo 3"))
        .check(bodyString.saveAs("responseBody"))
    )

    // debugging session attributes
    .exec {session=> println(session("responseBody").as[String]); session}

  // Load Scenario
  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpConf)
  )

}
