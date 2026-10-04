package net.taskwolf.workflow.timeline.entry;

import net.taskwolf.core.user.UserDatabaseTable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TimelineEntryFactory {
  private final UserDatabaseTable userDatabaseTable;

  public CompletableFuture<TimelineEntry> create(long time, String type, String content) {
    var json = new JSONObject(content);
    if (type.equals("timeline-workflow-create")) {
      return TimelineWorkflowCreateEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-execute")) {
      return CompletableFuture.completedFuture(TimelineWorkflowExecuteEntry.create(time));
    }
    if (type.equals("timeline-workflow-failure")) {
      return CompletableFuture.completedFuture(TimelineWorkflowFailureEntry.of(time, json));
    }
    if (type.equals("timeline-workflow-presentation")) {
      return TimelineWorkflowPresentationEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-action-add")) {
      return TimelineWorkflowActionAddEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-action-remove")) {
      return TimelineWorkflowActionRemoveEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-condition-add")) {
      return TimelineWorkflowConditionAddEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-condition-remove")) {
      return TimelineWorkflowConditionRemoveEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-loop-add")) {
      return TimelineWorkflowLoopAddEntry.of(time, userDatabaseTable, json);
    }
    if (type.equals("timeline-workflow-loop-remove")) {
      return TimelineWorkflowLoopRemoveEntry.of(time, userDatabaseTable, json);
    }
    return null;
  }
}
