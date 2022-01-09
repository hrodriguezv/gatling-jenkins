package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

class UsingHashTable extends Simulation {

  // Http Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8866))

  var idNumbers = (11 to 20).iterator
  val random = new Random();

  def randomString(length: Int) = {
    random.alphanumeric.filter(_.isLetter).take(length).mkString
  }


}
