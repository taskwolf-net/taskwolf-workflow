package net.taskwolf.workflow.timeline.entry;

import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineWorkflowActionAddEntry extends TimelineEntry {
  public static CompletableFuture<TimelineEntry> of(
    long time, UserDatabaseTable userDatabaseTable, JSONObject content
  ) {
    return userDatabaseTable.findUserIfExists(UUID.fromString(content.getString("actor")))
      .thenApply(user -> create(time, user.name()));
  }

  public static TimelineWorkflowActionAddEntry create(long time, String creator) {
    return new TimelineWorkflowActionAddEntry(time, creator);
  }

  private final String actor;

  private TimelineWorkflowActionAddEntry(long time, String actor) {
    super(time);
    this.actor = actor;
  }

  @Override
  public String title(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.action.add.title");
  }

  @Override
  public String description(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.action.add.description")
      .replace("%ACTOR%", actor);
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.INFO;
  }
}
