package fi.markoa.proto.events;

import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Event data access object interface.
 * Provides an asynchronous interface with lifecycle callback methods.
 *
 * @author marko asplund
 */
public interface EventDAO {
  void init();
  void destroy();

  ListenableFuture<String> create(Event e);
  ListenableFuture<Event> read(String id);
  ListenableFuture<Void> update(String id, Event event);
  ListenableFuture<Void> delete(String id);
  ListenableFuture<List<Event>> list();
}