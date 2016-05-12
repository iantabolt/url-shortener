package lt.tabo

import akka.actor.{Actor, Props}
import akka.pattern.ask
import spray.routing._
import spray.http._
import MediaTypes._
import akka.util.Timeout
import lt.tabo.UrlShortener.{GetUrl, Shorten}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class UrlShortenerServiceActor extends Actor with UrlShortenerService {
  val urlShortener = context.actorOf(Props[UrlShortener])

  def executionContext = context.dispatcher

  implicit val timeout = Timeout(10.millis)

  def shorten(url: String) = (urlShortener ? Shorten(url)).mapTo[String]

  def getUrl(short: String) = (urlShortener ? GetUrl(short)).mapTo[Option[String]]

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait UrlShortenerService extends HttpService {
  implicit def executionContext: ExecutionContext

  def shorten(url: String): Future[String]

  def getUrl(short: String): Future[Option[String]]

  val myRoute =
    post {
      path("shorten") {
        extract(_.request.entity.asString) { url =>
          onSuccess(shorten(url)) { short =>
            complete(short)
          }
        }
      } ~ path("getUrl") {
        extract(_.request.entity.asString) { short =>
          onSuccess(getUrl(short)) { url =>
            complete(url)
          }
        }
      }
    } ~ get {
      path(PathMatcher("[a-zA-Z0-9]+".r)) { short =>
        onSuccess(getUrl(short)) {
          case Some(url) =>
            redirect(Uri(url), StatusCodes.Found)
          case None =>
            complete(HttpResponse(StatusCodes.NotFound))
        }
      }
    }
}