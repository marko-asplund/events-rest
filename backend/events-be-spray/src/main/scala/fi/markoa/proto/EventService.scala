package fi.markoa.proto

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json.DefaultJsonProtocol
import scala.util._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import java.util.Date

import spray.json.JsonFormat
import spray.json.JsString
import spray.json.JsValue
import spray.routing.directives.OnCompleteFutureMagnet

class EventServiceActor extends Actor with EventService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}

trait MyJsonProtocol extends DefaultJsonProtocol {
  
  // FIXME: error handling
  implicit object DateJsonFormat extends JsonFormat[Date] {
    val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	def write(d: Date) = JsString(df.print(new DateTime(d)))
	def read(value: JsValue) = value match {
      case JsString(ds) => df.parseDateTime(ds).toDate()
      case x => spray.json.deserializationError("Expected String as JsString, but got " + x)
	}
  }
}
    
object MyJsonProtocol extends MyJsonProtocol {
  implicit val EventFormat = jsonFormat6(Event)
}

import MyJsonProtocol._
import spray.httpx.SprayJsonSupport._


trait EventService extends HttpService {

  /*
  private def fff[T](m: OnCompleteFutureMagnet[T]): Directive1[Try[T]] = {
    onComplete(m) {
      case Success(r) => complete(r)
      case Failure(ex) => complete("error: "+ex)
    }
  }*/

  val eventDAO = EventDAO()
  val myRoute =
    path("events") {
      get {
        respondWithMediaType(`application/json`) {
          onComplete(eventDAO.list) {
            case Success(r) => complete(r)
            case Failure(ex) => complete("error: "+ex)
          }
        }
      } ~
      post {
        entity(as[Event]) { event =>
          onComplete(eventDAO.create(event)) {
            case Success(r) => complete(r)
            case Failure(ex) => complete("error: "+ex)
          }
        }
      }
    } ~
    path("events" / Segment) { eventId =>
      put {
        entity(as[Event]) { event =>
          onComplete(eventDAO.update(event.id.get, event)) {
            case Success(r) => complete("")
            case Failure(ex) => complete("error: "+ex)
          }
        }
      } ~
      delete {
        complete("del: "+eventId)
      }
    }
}
