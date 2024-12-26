package com.dulno.workflow.timeline.entry;

import com.dulno.core.locale.Translation;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowLoopRemoveEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUserIfExists(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowLoopRemoveEntry create(long time, String creator) {
    return new TimelineWorkflowLoopRemoveEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowLoopRemoveEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.loop.remove.title");
  }

  @Override
  public String description(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.loop.remove.description")
      .replace("%ACTOR%", actor);
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}
