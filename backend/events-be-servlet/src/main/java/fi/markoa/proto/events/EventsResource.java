package fi.markoa.proto.events;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

// TODO
//Java 8 + lambda
// initialization: current model or JAX-RS Singleton?
// Date format change?
// Exception handling? jax-rs exception mapper?

/**
 * JAX-RS 2 resource class implementing a RESTful interface for events.
 *
 * @author marko asplund
 */
@Path("events")
public class EventsResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventsResource.class);
  private EventDAO eventDAO;

  public EventsResource(@Context ServletContext ctx) {
    LOGGER.debug("EventsResource: "+ctx);
    eventDAO = (EventDAO) ctx.getAttribute("eventDAO");
    LOGGER.debug("ctx: "+eventDAO);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  public void create(Event e, @Suspended AsyncResponse ar) {
    LOGGER.debug("create "+e);
    addResponseCallback(eventDAO.create(e), ar);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public void read(@PathParam("id") String id, @Suspended AsyncResponse ar) {
    LOGGER.debug("read "+id);
    addResponseCallback(eventDAO.read(id), ar);
  }

  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void update(@PathParam("id") String id, Event e, @Suspended AsyncResponse ar) {
    LOGGER.debug("update "+id);
    addResponseCallback(eventDAO.update(id, e), ar);
  }

  @DELETE
  @Path("/{id}")
  public void delete(@PathParam("id") String id, @Suspended AsyncResponse ar) {
    LOGGER.debug("delete: "+id);
    addResponseCallback(eventDAO.delete(id), ar);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public void listEvents(@Suspended AsyncResponse ar) {
    LOGGER.debug("listEvents");
    addResponseCallback(eventDAO.list(), ar);
  }

  private <T> void addResponseCallback(ListenableFuture<T> f, final AsyncResponse ar) {
    Futures.addCallback(f, new FutureCallback<T>() {
      @Override
      public void onFailure(Throwable exception) {
        ar.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
      }
      @Override
      public void onSuccess(T result) {
        LOGGER.debug("onSuccess");
        ar.resume(result);
      }
    });
  }
}
