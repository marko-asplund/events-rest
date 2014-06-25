package fi.markoa.proto.events;

/**
 * Custom exception class for entity ID format exceptions.
 *
 * @author marko asplund
 */
public class EntityIdException extends RuntimeException {
  public EntityIdException(String message, Throwable cause) {
    super(message, cause);
  }
}
