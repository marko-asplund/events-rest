package fi.markoa.proto.events;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ServletContextListener for initializing and destroying EventDAO implementation.
 *
 * Usually we'd use dependencies injection e.g. with Spring framework but
 * this time let's try to minimize the dependencies and use mechanisms provided by Java SE + Servlet API.
 *
 * @author marko asplund
 */
@WebListener
public class ContextListener implements ServletContextListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);
  private static final String CONFIG_FILE_NAME = "/events.properties";

  public void contextInitialized(ServletContextEvent sce) {
    Properties conf;
    try (InputStream is = sce.getServletContext().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
      if(is == null)
        throw new IOException("file not found: "+CONFIG_FILE_NAME);
      conf = new Properties();
      conf.load(is);
    } catch (IOException ex) {
      LOGGER.error("failed to open config file", ex);
      throw new RuntimeException(ex);
    }
    EventDAO d = new EventDAOCassImpl();
    d.init(conf);
    sce.getServletContext().setAttribute("eventDAO", d);
    LOGGER.debug("contextInitialized: "+sce);
  }

  public void contextDestroyed(ServletContextEvent sce) {
    LOGGER.debug("contextDestroyed: "+sce);
    EventDAO d = (EventDAO) sce.getServletContext().getAttribute("eventDAO");
    if(d != null)
      d.destroy();
  }

}
