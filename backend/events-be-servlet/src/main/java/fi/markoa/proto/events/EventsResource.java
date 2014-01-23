package fi.markoa.proto.events;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

// TODO: initialization
// cassandra access
// JSON serialization / deserialization

@Path("events")
public class EventsResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventsResource.class);
	private EventDAOCassImpl eventDAO;
	
	public EventsResource(@Context ServletContext ctx) {
		LOGGER.debug("EventsResource: "+ctx);
		eventDAO = (EventDAOCassImpl) ctx.getAttribute("eventDAO");
		LOGGER.debug("ctx: "+eventDAO);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void listEvents(@Suspended final AsyncResponse ar) {
	  LOGGER.debug("listEvents");
	  ListenableFuture<List<Event>> f = eventDAO.list();
	  Futures.addCallback(f, new FutureCallback<List<Event>>() {
      @Override
      public void onFailure(Throwable exception) {
        ar.resume("failure");
      }
      @Override
      public void onSuccess(List<Event> result) {
        LOGGER.debug("onSuccess");
        ar.resume(result);
      }
	  });
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Event getEvent() {
	  return new Event("123", "foo", "bar", "baz", new Date(), 1);
	}
	
}
