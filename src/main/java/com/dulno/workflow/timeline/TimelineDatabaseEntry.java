package com.dulno.workflow.timeline;

import com.dulno.core.database.DatabaseRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TimelineDatabaseEntry {
  public static TimelineDatabaseEntry of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).longValue(), row.findCell(3).stringValue(),
      row.findCell(4).stringValue());
  }

  private final UUID id;
  private final UUID workflowId;
  private final long time;
  private final String type;
  private final String content;
}
