package lt.tabo.shorturl

import java.net.URL

import akka.actor.{Actor, Props}
import akka.pattern.ask
import spray.routing._
import spray.http._
import MediaTypes._
import akka.util.Timeout
import UrlShortener.{GetUrl, Shorten}
import spray.httpx.unmarshalling.Unmarshaller

import scala.concurrent.Future
import scala.concurrent.duration._

class UrlShortenerService extends Actor with HttpService {
  import context.dispatcher
  implicit val timeout = Timeout(50.millis)

  val urlShortener = context.actorOf(Props[UrlShortener])

  def actorRefFactory = context

  def shorten(url: URL): Future[String] =
    (urlShortener ? Shorten(url)).mapTo[String]

  def getUrl(short: String): Future[Option[URL]] =
    (urlShortener ? GetUrl(short)).mapTo[Option[URL]]

  implicit val UrlUnmarshaller: Unmarshaller[URL] =
    Unmarshaller.delegate[String, URL](ContentTypeRange.`*`)(new URL(_))

  def receive = runRoute {
    post {
      path("shorten") {
        entity(as[URL]) { url =>
          onSuccess(shorten(url)) { short =>
            complete(short)
          }
        }
      } ~ path("getUrl") {
        extract(_.request.entity.asString) { short =>
          onSuccess(getUrl(short)) { url =>
            complete(url.toString)
          }
        }
      }
    } ~ get {
      path(PathMatcher("[a-zA-Z0-9]+".r)) { short =>
        onSuccess(getUrl(short)) {
          case Some(url) =>
            redirect(Uri(url.toString), StatusCodes.Found)
          case None =>
            complete(HttpResponse(StatusCodes.NotFound))
        }
      }
    }
  }
}
