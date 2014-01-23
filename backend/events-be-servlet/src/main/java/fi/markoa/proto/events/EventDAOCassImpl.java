package fi.markoa.proto.events;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.UUIDs;

public class EventDAOCassImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDAOCassImpl.class);
	private Cluster cluster;
	private Session session;
	private Map<String, PreparedStatement> statements;
	
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
	
	public String create(Event e) {
		UUID id = UUIDs.random();
		
		BoundStatement bs = null;
		if(e.getDescription() == null)
			bs = statements.get("create1").bind(id, e.getTitle(), e.getCategory(), e.getStartTime(), new Integer(e.getDuration()));
		else
			bs = statements.get("create2").bind(id, e.getTitle(), e.getCategory(), e.getStartTime(), new Integer(e.getDuration()));
		ResultSetFuture rs = session.executeAsync(bs);

		return null;
	}
	
	public Event read(String id) {
		return null;
	}
	
	public void update(String id, Event event) {
	}
	
	public void delete(String id) {
	}
	
	public List<Event> list() {
		List<Event> l = new ArrayList<>();
		ResultSet rs = session.execute(statements.get("list").bind());
		for(Row r : rs.all()) {
			l.add(eventFromRow(r));
		}
		LOGGER.debug("list: "+l);
		return l;
	}
	
	private Event eventFromRow(Row r) {
		return new Event(r.getUUID("id").toString(), r.getString("title"), r.getString("category"), r.getString("description"),
				r.getDate("startTime"), r.getInt("duration")
				);
	}
	
	public void destroy() {
		LOGGER.debug("destroy()");
		session.shutdown();
		cluster.shutdown();
	}

}
