package simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CodeReuseWithObjects extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  def getAllVideoGames(): ChainBuilder = {
    repeat(3){
      exec(
        http("Get all video games").get("videogames")
          .check(status.is(200))
      )
    }
  }

  def getSpecificVideoGame(): ChainBuilder = {
    repeat(5){
      exec(
        http("Get specific video game").get("videogames/1")
          .check(status.in(200 to 210))
      )
    }
  }

  // Scenario definition
  val scn = scenario("Reuse code with objects")
  // 1st part
  .exec(
    getAllVideoGames()
  ).pause(5)
  // 2nd part
  .exec(
   getSpecificVideoGame()
  ).pause(1, 20)
  // 3rd part
  .exec(
    getAllVideoGames()
  ).pause(3000.milliseconds)

  // Load Scenario
  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpConf)
  )

}
