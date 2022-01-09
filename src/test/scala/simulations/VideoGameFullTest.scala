package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class VideoGameFullTest extends Simulation {

  // HTTP Configuration
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  /** Variables */
  // for the helper methods
  val random = new Random();
  val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val now = LocalDate.now();
  var idNumbers = (2500 to 3000).iterator

  /** Helper methods */
  // for the custom feeder, or the defaults for the runtime parameters... and anything
  def randomString(length: Int) = {
    random.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomDate(startDate: LocalDate, random: Random): String = {
    startDate.minusDays(random.nextInt(30)).format(dateFormat)
  }

  def getPropertyEnv(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def userCount: Int = {
    getPropertyEnv("USERS", "1").toInt
  }

  def rampDuration: Int = {
    getPropertyEnv("RAMP_DURATION", "10").toInt
  }

  def testDuration: Int = {
    getPropertyEnv("TEST_DURATION", "60").toInt
  }

  /** Custom Feeder */
  // to generate the date for the Create new Game JSON
  val customFeeder = Iterator.continually(
    Map(
      "gameId" -> idNumbers.next(),
      "name" -> ("CustomGame-" + randomString(5)),
      "releaseDate" -> randomDate(LocalDate.now(), random),
      "reviewScore" -> random.nextInt(100),
      "category" -> ("Category-" + randomString(6)),
      "rating" -> ("Rating-"+ randomString(4))
    ))

  /*** HTTP CALLS ***/
  def getAllVideoGames() = {
    exec(
      http("Get all video games")
        .get("videogames")
        .check(status.is(200))
    )
  }

  def postNewGame() = {
    exec(
          http("Create a new Game")
            .post("videogames/")
            .body(
              StringBody(
                "{" +
                  "\n\t\"id\": ${gameId}, " +
                  "\n\t\"name\": \"${name}\", " +
                  "\n\t\"releaseDate\": \"${releaseDate}\", " +
                  "\n\t\"reviewScore\": ${reviewScore}, " +
                  "\n\t\"category\": \"${category}\", " +
                  "\n\t\"rating\": \"${rating}\" " +
                  "}"
              )
            ).asJson
            .check(status.in(500 to 600))
        )
    }

  def getSpecificVideoGame() = {
       exec(
          http("Get specific video game").get("videogames/${gameId}")
            .check(jsonPath("$.name").is("${name}"))
            .check(status.is(200))
        )
  }

  def deleteVideoGame() = {
    exec(
      http("Delete specific video game").delete("videogames/${gameId}")
        .check(status.is(200))
    )
  }

  // add other calls here
  /** SCENARIO DESIGN */
  // using the http call, create a scenario that does the following:
  // 1. Get all games
  // 2. Create new Game
  // 3. Get details of that single
  // 4. Delete the game

  val scn = scenario("Final Simulation")
    .exec(
      getAllVideoGames()
    )
    .pause(2)
    .forever() {
      feed(customFeeder)
        .exec(postNewGame())
        .pause(2)
        .exec(getSpecificVideoGame())
        .pause(2)
        .exec(deleteVideoGame())
        .pause(2)
   }

  /** Before & After */
  // to print out message at the start and end of the test
  before{
    println("=========================================================================================================")
    println(s"Running tests with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total duration configured: ${testDuration} seconds")
    println("=========================================================================================================")
  }

  /** SETUP LOAD SIMULATION */
  // create a scenario that has runtime parameters for:
  // 1. Users
  // 2. Ramp up time
  // 3. Test duration
  setUp(
    scn.inject(nothingFor(5 seconds), rampUsers(userCount) during (rampDuration second))
  )
    .protocols(httpConf)
    .maxDuration(testDuration seconds)

  after{
    println("=========================================================================================================")
    println("Simulation finished successfully!")
    println("=========================================================================================================")
  }
}