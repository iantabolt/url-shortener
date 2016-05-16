package lt.tabo.shorturl

import java.net.URL

import akka.actor.Actor

import scala.collection.mutable

class UrlShortener extends Actor {
  import UrlShortener._

  // TODO: Use sql database or something
  val urlIndex = mutable.ArrayBuffer[URL]()
  val reverseIndex = mutable.Map[URL, Int]()

  // Add a url to the index and return its position
  def index(url: URL): Int = {
    reverseIndex.getOrElseUpdate(url, {
      urlIndex += url
      urlIndex.length
    })
  }

  override def receive = {
    case Shorten(url) =>
      sender ! Base62.encode(index(url))
    case GetUrl(short) =>
      sender ! urlIndex.lift(Base62.decode(short) - 1)
    case GetClicks(short) =>
      sender ! None
  }

}

object UrlShortener {
  case class Shorten(url: URL) // : String
  case class GetUrl(short: String) // : Option[URL]
  case class GetClicks(short: String) // : Option[Int]
}
