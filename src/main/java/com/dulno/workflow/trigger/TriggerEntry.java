package com.dulno.workflow.trigger;

import com.dulno.core.database.DatabaseColumn;
import com.dulno.core.database.DatabaseRow;
import com.dulno.core.database.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class TriggerEntry {
  public static TriggerEntry of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static TriggerEntry of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("owner")).uuidValue(),
      row.findCell(columns.indexOf("workflow")).uuidValue(),
      row.findCell(columns.indexOf("module")).stringValue(),
      row.findCell(columns.indexOf("type")).stringValue(),
      TriggerState.valueOf(row.findCell(columns.indexOf("state")).stringValue()));
  }

  private final UUID id;
  private final UUID ownerId;
  private final UUID workflowId;
  private final String module;
  private final String type;
  private TriggerState state;

  public void changeState(TriggerState state) {
    this.state = state;
  }
}
