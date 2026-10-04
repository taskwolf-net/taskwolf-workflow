package net.taskwolf.workflow.timeline.entry;

import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.user.User;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

@Accessors(fluent = true)
public final class TimelineWorkflowFailureEntry extends TimelineEntry {
  public static TimelineWorkflowFailureEntry of(long time, JSONObject content) {
    return create(time, content.getString("moduleName"),
      content.getString("stepName"), content.getInt("stepIndex"),
      content.getString("message"));
  }

  public static TimelineWorkflowFailureEntry create(
    long time, String moduleName, String stepName, int stepIndex, String message
  ) {
    return new TimelineWorkflowFailureEntry(time, moduleName, stepName,
      stepIndex, message);
  }

  @Getter
  private final String moduleName;
  @Getter
  private final String stepName;
  @Getter
  private final int stepIndex;
  private final String message;

  private TimelineWorkflowFailureEntry(
    long time, String moduleName, String stepName, int stepIndex, String message
  ) {
    super(time);
    this.moduleName = moduleName;
    this.stepName = stepName;
    this.stepIndex = stepIndex;
    this.message = message;
  }

  @Override
  public String title(Translation translation, User user) {
    return translation.translate(user, "workflow.timeline.entry.failed.title");
  }

  @Override
  public String description(Translation translation, User user) {
    return translation.translate(user, message);
  }

  @Override
  public TimelineEntryLevel level() {
    return TimelineEntryLevel.FAILURE;
  }
}