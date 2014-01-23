package fi.markoa.proto.events;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class ContextListener implements ServletContextListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);

	public void contextInitialized(ServletContextEvent sce) {
		EventDAOCassImpl d = new EventDAOCassImpl();
		d.init();
		sce.getServletContext().setAttribute("eventDAO", d);
		LOGGER.debug("contextInitialized: "+sce);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		LOGGER.debug("contextDestroyed: "+sce);
		EventDAOCassImpl d = (EventDAOCassImpl) sce.getServletContext().getAttribute("eventDAO");
		d.destroy();
	}

}
