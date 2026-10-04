package net.taskwolf.workflow.action;

import net.taskwolf.core.database.*;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ActionContentDatabaseTable extends DatabaseTable {
  public static ActionContentDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    String tableName, List<DatabaseColumn> contentColumns
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("action", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.addAll(contentColumns);
    return new ActionContentDatabaseTable(connection, keyspace, tableName, columns);
  }

  private ActionContentDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertContent(
    UUID actionId, DatabaseRow content
  ) {
    return insert(DatabaseRow.of(actionId).concat(content));
  }

  public CompletableFuture<Void> updateContent(
    UUID actionId, DatabaseRow content
  ) {
    return update(actionId, DatabaseRow.of(actionId).concat(content));
  }

  public CompletableFuture<Void> deleteContent(UUID actionId) {
    return delete(actionId);
  }

  public CompletableFuture<Boolean> contentExists(UUID actionId) {
    return exists(actionId);
  }

  public CompletableFuture<DatabaseRow> findContent(UUID actionId) {
    return selectRow(actionId);
  }
}
