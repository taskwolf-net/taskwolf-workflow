package net.taskwolf.workflow.throttle;

import net.taskwolf.core.database.DatabaseRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkflowThrottleEntry {
  public static WorkflowThrottleEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).longValue(),
      row.findCell(2).longValue());
  }

  private final UUID targetId;
  private final long executions;
  private final long expiration;
}