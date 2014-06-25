package fi.markoa.proto.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * custom jax-rs exception mapper class.
 *
 * @author marko asplund
 */
@Provider
public class MyExceptionMapper implements ExceptionMapper<Throwable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyExceptionMapper.class);

  @Override
  public Response toResponse(Throwable ex) {
    LOGGER.debug("*** toResponse: "+ex.getMessage());
    if(ex instanceof EntityIdException) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    } else if(ex.getClass().getPackage().getName().startsWith("com.datastax.driver")) {
      return Response.serverError().entity("data storage error occurred").build();
    }
    return Response.serverError().build();
  }
}
