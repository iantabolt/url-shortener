package lt.tabo.shorturl

import java.net.URL

import akka.actor.Actor

import scala.collection.mutable
import scala.util.Try

class UrlShortener extends Actor {
  import UrlShortener._

  // TODO: Use sql database or something
  val urlIndex = mutable.ArrayBuffer[URL]()
  val reverseIndex = mutable.Map[URL, Int]()
  val clickCounters = mutable.Map[Int, Int]().withDefaultValue(0)

  // Add a url to the index and return its position
  def shorten(url: URL): String = {
    val index = reverseIndex.getOrElseUpdate(url, {
      urlIndex += url
      urlIndex.length
    })
    Base62.encode(index)
  }

  // Decodes a short url to its int representation.
  // Returns None if it is invalid.
  def getIndex(short: String): Option[Int] = {
    Try(Base62.decode(short) - 1)
      .toOption
      .filter(urlIndex.isDefinedAt)
  }

  def getUrl(short: String): Option[URL] = getIndex(short) map { idx =>
    clickCounters(idx) += 1
    urlIndex(idx)
  }

  def getClicks(short: String): Option[Int] = {
    getIndex(short) map clickCounters
  }

  override def receive = {
    case Shorten(url) =>
      sender ! shorten(url)
    case GetUrl(short) =>
      sender ! getUrl(short)
    case GetClicks(short) =>
      sender ! getClicks(short)
  }
}

object UrlShortener {
  case class Shorten(url: URL) // : String
  case class GetUrl(short: String) // : Option[URL]
  case class GetClicks(short: String) // : Option[Int]
}
