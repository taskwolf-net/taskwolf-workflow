package com.dulno.workflow.timeline.entry;

import com.dulno.core.locale.Translation;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowCreateEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUserIfExists(UUID.fromString(content.getString("creator")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowCreateEntry create(long time, String creator) {
    return new TimelineWorkflowCreateEntry(time, creator);
  }

  private final String creator;

  private TimelineWorkflowCreateEntry(long time, String creator) {
    super(time);
    this.creator = creator;
  }

  @Override
  public String title(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.created.title");
  }

  @Override
  public String description(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.created.description")
      .replace("%CREATOR%", creator);
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}
