package fi.markoa.proto

import java.util.Date
import com.datastax.driver.core.{Cluster, Session, Row, ResultSet, ResultSetFuture, BoundStatement, PreparedStatement};
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

class CassandraException(val message: String) extends Exception

class EventDAO extends EventDAOI {
  val cass = init("localhost", "events")

  class CassandraConnection(val cluster: Cluster, val session: Session, val statements: Map[String, PreparedStatement])
  
  private def init(host: String, ks: String): CassandraConnection = {
    try {
      val cluster = Cluster.builder().addContactPoint(host).build()
      val session = cluster.connect(ks)
      val statements = Map("create1" -> session.prepare("INSERT INTO event (id, title, category, startTime, duration) VALUES (?,?, ?, ?, ?)"),
          "create2" -> session.prepare("INSERT INTO event (id, title, category, startTime, duration, description) VALUES (?,?, ?, ?, ?, ?)"),
          "read" -> session.prepare("SELECT * FROM event WHERE id = ?"),
          "delete" -> session.prepare("DELETE FROM event WHERE id = ?"),
          "list" -> session.prepare("SELECT * FROM event")
          )
          new CassandraConnection(cluster, session, statements)
    } catch {
      case ex: Exception => throw new CassandraException("failed to initialize Cassandra connection")
    }
  }
  

  def create(e: Event): Future[String] = {
    val id = UUIDs.random()
    createOrUpdate(id, e, (rs: ResultSet) => id.toString())
  }
  
  private def createOrUpdate[T](id: UUID, e: Event, t: ResultSet => T): Future[T] = {
    val ps: BoundStatement =
      if(e.description == null)
        cass.statements("create1").bind(id, e.title, e.category, e.startTime, new Integer(e.duration))
      else
        cass.statements("create2").bind(id, e.title, e.category, e.startTime, new Integer(e.duration))
    FutureAdapter(cass.session.executeAsync(ps), t)
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
    FutureAdapter(cass.session.executeAsync(cass.statements("list").bind()), t)
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
