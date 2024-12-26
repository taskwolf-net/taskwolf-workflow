package com.dulno.workflow.action;

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
public final class ActionEntry {
  public static ActionEntry of(DatabaseRow row, DatabaseTable table) {
    return of(row, table.columns().stream().map(DatabaseColumn::name).toList());
  }

  public static ActionEntry of(DatabaseRow row, List<String> columns) {
    return create(row.findCell(columns.indexOf("id")).uuidValue(),
      row.findCell(columns.indexOf("owner")).uuidValue(),
      row.findCell(columns.indexOf("workflow")).uuidValue(),
      row.findCell(columns.indexOf("module")).stringValue(),
      row.findCell(columns.indexOf("type")).stringValue(),
      row.findCell(columns.indexOf("stepIndex")).integerValue());
  }

  private final UUID id;
  private final UUID ownerId;
  private final UUID workflowId;
  private final String module;
  private final String type;
  private final int index;
}
