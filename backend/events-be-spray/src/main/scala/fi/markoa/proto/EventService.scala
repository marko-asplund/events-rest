package fi.markoa.proto

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

class EventServiceActor extends Actor with EventService {
  def actorRefFactory = context
  def receive = runRoute(myRoute)
}

trait EventService extends HttpService {
  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/plain`) {
          complete {
		"hello, world"
          }
        }
      }
    }
}
