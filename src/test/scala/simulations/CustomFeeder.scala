package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

class CustomFeeder extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  var idNumbers = (11 to 20).iterator
  val random = new Random();
  val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def randomString(length: Int) = {
    random.alphanumeric.filter(_.isLetter).take(length).mkString
  }
  def randomDate(startDate: LocalDate, random: Random): String = {
    startDate.minusDays(random.nextInt(30)).format(dateFormat)
  }


  val customFeeder = Iterator.continually(
    Map(
    "gameId" -> idNumbers.next(),
    "name" -> ("CustomGame-" + randomString(5)),
    "releaseDate" -> randomDate(LocalDate.now(), random),
    "reviewScore" -> random.nextInt(100),
    "category" -> ("Category-" + randomString(6)),
    "rating" -> ("Rating-"+ randomString(4))
  ))

  def postNewGame() = {
    repeat(5){
      feed(customFeeder)
        .exec(
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
            .check(status.in(200 to 210))
        ).pause(1)
    }
  }


  // Scenario definition
  val scn = scenario("Post new game")
    .exec(postNewGame())

  // Load Scenario
  setUp(
    scn.inject(atOnceUsers(1)).protocols(httpConf)
  )

}
