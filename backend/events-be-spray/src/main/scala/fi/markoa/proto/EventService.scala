package fi.markoa.proto

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json.DefaultJsonProtocol
import scala.util._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import java.util.Date
import java.text.SimpleDateFormat
import java.text.DateFormat

import spray.json.JsonFormat
import spray.json.JsString
import spray.json.JsValue

class EventServiceActor extends Actor with EventService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}

trait MyJsonProtocol extends DefaultJsonProtocol {
  // FIXME: perf, thread-safety, yoda?, error handling
  implicit object DateJsonFormat extends JsonFormat[Date] {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	def write(x: Date) = JsString(df.format(x))
	def read(value: JsValue) = value match {
      case JsString(x) => df.parse(x.replace("Z", "+0000"))
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
