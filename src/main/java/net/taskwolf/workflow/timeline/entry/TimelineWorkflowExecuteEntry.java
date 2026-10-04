package net.taskwolf.workflow.timeline.entry;

import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.user.User;

public final class TimelineWorkflowExecuteEntry extends TimelineEntry {
  public static TimelineWorkflowExecuteEntry create(long time) {
    return new TimelineWorkflowExecuteEntry(time);
  }

  private TimelineWorkflowExecuteEntry(long time) {
    super(time);
  }

  @Override
  public String title(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.executed.title");
  }

  @Override
  public String description(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.executed.description");
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.SUCCESS;
  }
}
