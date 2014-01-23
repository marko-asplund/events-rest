package fi.markoa.proto.events;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/events")
public class EventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(EventServlet.class);
	private EventDAOCassImpl eventDAO;

	// TODO: initialization
	// cassandra access
	// JSON serialization / deserialization
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.info("doGet");
		eventDAO.list();
		super.doGet(req, resp);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		LOGGER.debug("init(");
		eventDAO = (EventDAOCassImpl) config.getServletContext().getAttribute("eventDAO");
	}

	@Override
	public void destroy() {
		LOGGER.debug("destroy()");
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

}
