package com.dulno.workflow.timeline.entry;

import com.dulno.core.locale.Translation;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowPresentationEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUserIfExists(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowPresentationEntry create(long time, String creator) {
    return new TimelineWorkflowPresentationEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowPresentationEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.presentation.title");
  }

  @Override
  public String description(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.presentation.description")
      .replace("%ACTOR%", actor);
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}