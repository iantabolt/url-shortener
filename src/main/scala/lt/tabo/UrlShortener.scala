package lt.tabo

import akka.actor.Actor

import scala.collection.mutable.ArrayBuffer

class UrlShortener extends Actor {
  import UrlShortener._

  // TODO: Use sql database or something
  val urlIndex = ArrayBuffer[String]()

  // Add a url to the index and return its position
  def index(url: String): Int = {
    urlIndex += url
    urlIndex.length
  }

  override def receive = {
    case Shorten(url) =>
      sender ! Base62.encode(index(url))
    case GetUrl(short: String) =>
      sender ! urlIndex.lift(Base62.decode(short) - 1)
    case GetClicks(short) =>
      sender ! None
  }

}

object UrlShortener {
  case class Shorten(url: String) // : String
  case class GetUrl(short: String) // : Option[String]
  case class GetClicks(short: String) // : Option[Int]
}
