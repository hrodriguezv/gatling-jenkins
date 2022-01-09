import io.gatling.core.Predef._
import io.gatling.http.Predef._

class MyFirstTest extends Simulation {


  // http Configuration
  val httpConf = http.baseUrl("http://localhost:8080/app").header("Accept", "application/json").proxy(Proxy("localhost", 8866))

  // Scenario Definition
  val scn = scenario("My First Test").exec(
    http("Get All Games").get("/videogames")
  )

  // Load Scenario
  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpConf)
  )
}
