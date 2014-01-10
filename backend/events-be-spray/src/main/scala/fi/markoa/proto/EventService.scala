package fi.markoa.proto

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json.DefaultJsonProtocol

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

case class Event(id: String, title: String, category: String, description: Option[String],
    startTime: Date, duration: Int)

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
  val myRoute =
    path("events") {
      get {
        respondWithMediaType(`text/plain`) {
          complete {
            "get"
          }
        }
      } ~
      post {
        complete("post")
      }
    } ~
    path("events" / IntNumber) { eventId =>
      entity(as[Event]) { person => 
      put {
        complete("put: "+eventId+", "+person)
      } ~
      delete {
        complete("del: "+eventId)
      }
      }
    }
}
