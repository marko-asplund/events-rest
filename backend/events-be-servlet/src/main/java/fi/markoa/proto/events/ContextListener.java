package fi.markoa.proto.events;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServletContextListener for initializing and destroying EventDAO implementation.
 *
 * @author marko asplund
 */
@WebListener
public class ContextListener implements ServletContextListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);

  public void contextInitialized(ServletContextEvent sce) {
    EventDAO d = new EventDAOCassImpl();
    d.init();
    sce.getServletContext().setAttribute("eventDAO", d);
    LOGGER.debug("contextInitialized: "+sce);
  }

  public void contextDestroyed(ServletContextEvent sce) {
    LOGGER.debug("contextDestroyed: "+sce);
    EventDAO d = (EventDAO) sce.getServletContext().getAttribute("eventDAO");
    d.destroy();
  }

}
