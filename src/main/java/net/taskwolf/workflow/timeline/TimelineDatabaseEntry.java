package net.taskwolf.workflow.timeline;

import net.taskwolf.core.database.DatabaseColumn;
import net.taskwolf.core.database.DatabaseRow;
import net.taskwolf.core.database.DatabaseTable;
import net.taskwolf.workflow.structure.WorkflowEntry;
import net.taskwolf.workflow.structure.WorkflowState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TimelineDatabaseEntry {
  public static TimelineDatabaseEntry of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static TimelineDatabaseEntry of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("workflow")).uuidValue(),
      row.findCell(columns.indexOf("time")).longValue(),
      row.findCell(columns.indexOf("type")).stringValue(),
      row.findCell(columns.indexOf("content")).stringValue());
  }

  private final UUID id;
  private final UUID workflowId;
  private final long time;
  private final String type;
  private final String content;
}
