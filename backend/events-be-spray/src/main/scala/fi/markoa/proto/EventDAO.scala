package fi.markoa.proto

import java.util.Date
import com.datastax.driver.core.{Cluster, Session, Row, ResultSet, ResultSetFuture, BoundStatement};
import com.datastax.driver.core.utils.UUIDs
import java.util.UUID
import java.util.concurrent.{TimeUnit, TimeoutException}
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.util._
import ExecutionContext.Implicits.global
import com.google.common.util.concurrent._

trait EventDAOI {
  def create(e: Event): Future[String]
  def read(id: String): Event
  def update(id: String, e: Event): Future[Unit]
  def delete(id: String): Unit
  def list: Future[List[Event]]
}

class EventDAO extends EventDAOI {
  val cluster = Cluster.builder().addContactPoint("localhost").build()
  val session = cluster.connect("events")
  val createStmt1 = session.prepare("INSERT INTO event (id, title, category, startTime, duration) VALUES (?,?, ?, ?, ?)");
  val createStmt2 = session.prepare("INSERT INTO event (id, title, category, startTime, duration, description) VALUES (?,?, ?, ?, ?, ?)");
  val readStmt = session.prepare("SELECT * FROM event WHERE id = ?")
  val deleteStmt = session.prepare("DELETE FROM event WHERE id = ?")
  val listStmt = session.prepare("SELECT * FROM event")
  
  def create(e: Event): Future[String] = {
    val id = UUIDs.random()
    createOrUpdate(id, e, (rs: ResultSet) => id.toString())
  }
  
  private def createOrUpdate[T](id: UUID, e: Event, t: ResultSet => T): Future[T] = {
    var ps: BoundStatement =
      if(e.description == null)
        createStmt1.bind(id, e.title, e.category, e.startTime, new Integer(e.duration))
      else
        createStmt2.bind(id, e.title, e.category, e.startTime, new Integer(e.duration))
    FutureAdapter(session.executeAsync(ps), t)
  }

  def read(id: String): Event  = {
    getDummyEvent
  }

  def update(id: String, e: Event): Future[Unit]  = {
    createOrUpdate(UUID.fromString(id), e, (rs: ResultSet) => {} )
  }

  def delete(id: String): Unit  = {
  }
  
  def list: Future[List[Event]] = {
    val t = (rs: ResultSet) => for(r <- rs.all().asScala.toList) yield eventFromRow(r)
    FutureAdapter(session.executeAsync(listStmt.bind()), t)
  }
  
  private def eventFromRow(r: Row): Event = {
    val desc = r.getString("description")
    Event(Some(r.getUUID("id").toString()), r.getString("title"), r.getString("category"),
        if(desc != null) Some(desc) else None, r.getDate("startTime"), r.getInt("duration"))
  }

  private def getDummyEvent(): Event = {
    Event(Some(""), "", "", Some(""), new Date(), 1)
  }
  
  private def getStatements() {
  }

}

object EventDAO {
  val eventDAO = new EventDAO
  def apply(u: Unit) = {
    eventDAO
  }
}

// adapt Guava Future to Scala future.
// transform the success result using function t.
object FutureAdapter {
  def apply[T, U](f: ListenableFuture[T], t: T => U): Future[U] = {
    val p = promise[U]
    Futures.addCallback(f, new FutureCallback[T]() {
      def onSuccess(rs: T) {
  	    p.complete(Success(t(rs)))
   	  }
      def onFailure(throwable: Throwable) {
   	    p.failure(throwable)
   	  }
    });
    p.future
  }
}
