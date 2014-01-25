package fi.markoa.proto.events;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Event data access object implementation for Cassandra.
 *
 * @author marko asplund
 */
public class EventDAOCassImpl implements EventDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDAOCassImpl.class);
	private Cluster cluster;
	private Session session;
	private Map<String, PreparedStatement> statements;
	
	@Override
  public void init() {
		LOGGER.debug("init()");
		cluster = Cluster.builder().addContactPoint("localhost").build();
		session = cluster.connect("events");
		
		Map<String, PreparedStatement> stmts = new HashMap<>();
		stmts.put("create1", session.prepare("INSERT INTO event (id, title, category, startTime, duration) VALUES (?,?, ?, ?, ?)"));
		stmts.put("create2", session.prepare("INSERT INTO event (id, title, category, startTime, duration, description) VALUES (?,?, ?, ?, ?, ?)"));
		stmts.put("read", session.prepare("SELECT * FROM event WHERE id = ?"));
		stmts.put("delete", session.prepare("DELETE FROM event WHERE id = ?"));
		stmts.put("list", session.prepare("SELECT * FROM event"));
		statements = Collections.unmodifiableMap(stmts);
	}
	
	@Override
  public ListenableFuture<String> create(Event e) {
    final UUID id = UUIDs.random();
    Function<ResultSet, String> transformation = new Function<ResultSet, String>() {
      public String apply(ResultSet rs) {
        return id.toString();
      }
    };

    return createOrUpdate(id, e, transformation);
	}
	
	@Override
  public ListenableFuture<Event> read(String id) {
    Function<ResultSet, Event> transformation = new Function<ResultSet, Event>() {
      public Event apply(ResultSet rs) {
        return eventFromRow(rs.one());
      }
    };

    ResultSetFuture rsf = session.executeAsync(statements.get("read").bind(UUID.fromString(id)));
    return Futures.transform(rsf, transformation);
	}
	
	@Override
  public ListenableFuture<Void> update(String id, Event e) {
    Function<ResultSet, Void> transformation = new Function<ResultSet, Void>() {
      public Void apply(ResultSet rs) {
        return null;
      }
    };
	  return createOrUpdate(UUID.fromString(id), e, transformation);
	}
	
	// TODO: how to determine if the event was found and successfully executed?
	@Override
  public ListenableFuture<Void> delete(final String id) {
    final ResultSetFuture rsf = session.executeAsync(statements.get("delete").bind(UUID.fromString(id)));
	  Function<ResultSet, Void> transformation = new Function<ResultSet, Void>() {
      public Void apply(ResultSet rs) {
        return null;
      }
    };
    return Futures.transform(rsf, transformation);
	}
	
	@Override
  public ListenableFuture<List<Event>> list() {
    Function<ResultSet, List<Event>> transformation = new Function<ResultSet, List<Event>>() {
      public List<Event> apply(ResultSet rs) {
        List<Event> events = new ArrayList<>();
        for(Row r : rs.all()) {
          events.add(eventFromRow(r));
        }
        return events;
      }
    };
		ResultSetFuture rsf = session.executeAsync(statements.get("list").bind());
		return Futures.transform(rsf, transformation);
	}
	
  @Override
  public void destroy() {
    LOGGER.debug("destroy()");
    session.shutdown();
    cluster.shutdown();
  }

	private Event eventFromRow(Row r) {
		return new Event(r.getUUID("id").toString(), r.getString("title"), r.getString("category"), r.getString("description"),
				r.getDate("startTime"), r.getInt("duration"));
	}
	
  private <T> ListenableFuture<T> createOrUpdate(final UUID id, Event e, Function<ResultSet, T> transformation) {
    BoundStatement bs = null;
    if(e.getDescription() == null)
      bs = statements.get("create1").bind(id, e.getTitle(), e.getCategory(), e.getStartTime(), new Integer(e.getDuration()));
    else
      bs = statements.get("create2").bind(id, e.getTitle(), e.getCategory(), e.getStartTime(), new Integer(e.getDuration()));
    ResultSetFuture rsf = session.executeAsync(bs);
    return Futures.transform(rsf, transformation);
  }


}
