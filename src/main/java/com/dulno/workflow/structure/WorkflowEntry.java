package com.dulno.workflow.structure;

import com.dulno.core.database.DatabaseColumn;
import com.dulno.core.database.DatabaseRow;
import com.dulno.core.database.DatabaseTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkflowEntry {
  public static WorkflowEntry of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static WorkflowEntry of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("owner")).uuidValue(),
      row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("creator")).uuidValue(),
      row.findCell(columns.indexOf("trigger")).uuidValue(),
      row.findCell(columns.indexOf("actions")).listValue(),
      row.findCell(columns.indexOf("conditions")).listValue(),
      row.findCell(columns.indexOf("loop")).uuidValue(),
      row.findCell(columns.indexOf("modules")).listValue(),
      row.findCell(columns.indexOf("timeZone")).stringValue(),
      row.findCell(columns.indexOf("timeLocale")).stringValue(),
      row.findCell(columns.indexOf("created")).longValue(),
      row.findCell(columns.indexOf("name")).stringValue(),
      row.findCell(columns.indexOf("description")).stringValue(),
      WorkflowState.valueOf(row.findCell(columns.indexOf("state")).stringValue()));
  }

  private final UUID ownerId;
  private final UUID id;
  private final UUID creatorId;
  private final UUID triggerId;
  private final List<UUID> actionIds;
  private final List<UUID> conditionIds;
  private final UUID loopId;
  private final List<String> modules;
  private final String timeZone;
  private final String timeLocale;
  private final long created;
  private final String name;
  private final String description;
  private final WorkflowState state;
}
