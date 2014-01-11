package fi.markoa.proto

import java.util.Date
import com.datastax.driver.core.{Cluster, Session, Row, ResultSet, ResultSetFuture};
import java.util.UUID
import java.util.concurrent.{TimeUnit, TimeoutException}
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.util._
import ExecutionContext.Implicits.global
import com.google.common.util.concurrent._

trait EventDAOI {
  def create(e: Event): String
  def read(id: String): Event
  def update(e: Event): Unit
  def delete(id: String): Unit
  def list: Future[List[Event]]
}

class EventDAO extends EventDAOI {
  val cluster = Cluster.builder().addContactPoint("localhost").build()
  val session = cluster.connect("events")
  val listStmt = session.prepare("SELECT * FROM event")
  
  def create(e: Event): String = {
    ""
  }
  def read(id: String): Event  = {
    getDummyEvent
  }
  def update(e: Event): Unit  = {
    
  }
  def delete(id: String): Unit  = {
    
  }
  def list: Future[List[Event]] = {
    val f = FutureAdapter(session.executeAsync(listStmt.bind()))
    val p = promise[List[Event]]
    f.onComplete({
      case Success(rs) => p.success(for(r <- rs.all().asScala.toList) yield eventFromRow(r))
      case Failure(ex) => p.failure(ex)
    })
    p.future
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

// adapt Guava Future to Scala future
object FutureAdapter {
  def apply[T](f: ListenableFuture[T]): Future[T] = {
    val p = promise[T]
    Futures.addCallback(f, new FutureCallback[T]() {
      def onSuccess(rs: T) {
  	    p.complete(Success(rs))
   	  }
      def onFailure(throwable: Throwable) {
   	    p.failure(throwable)
   	  }
    });
    p.future
  }
}
