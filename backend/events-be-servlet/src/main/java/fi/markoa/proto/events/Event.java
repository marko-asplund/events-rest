package fi.markoa.proto.events;

import java.util.Date;

/**
 * Event domain object.
 *
 * @author marko asplund
 */
public class Event {
  private String id;
  private String title;
  private String category;
  private String description;
  private Date startTime;
  private int duration;

  public Event() {
  }

  public Event(String id, String title, String category, String description, Date startTime, int duration) {
    super();
    this.id = id;
    this.title = title;
    this.category = category;
    this.description = description;
    this.startTime = startTime;
    this.duration = duration;
  }

  public String getId() {
    return id;
  }
  public String getTitle() {
    return title;
  }
  public String getCategory() {
    return category;
  }
  public String getDescription() {
    return description;
  }
  public Date getStartTime() {
    return startTime;
  }
  public int getDuration() {
    return duration;
  }


}
