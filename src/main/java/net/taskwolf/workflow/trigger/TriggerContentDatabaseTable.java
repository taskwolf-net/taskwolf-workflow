package net.taskwolf.workflow.trigger;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TriggerContentDatabaseTable extends DatabaseTable {
  public static TriggerContentDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace,
    String tableName, List<DatabaseColumn> contentColumns
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.addAll(contentColumns);
    return new TriggerContentDatabaseTable(connection, keyspace, tableName, columns);
  }

  private TriggerContentDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertContent(
    UUID triggerId, DatabaseRow content
  ) {
    return insert(DatabaseRow.of(triggerId).concat(content));
  }

  public CompletableFuture<Void> updateContent(
    UUID triggerId, DatabaseRow content
  ) {
    return update(triggerId, DatabaseRow.of(triggerId).concat(content));
  }

  public CompletableFuture<Void> deleteContent(UUID triggerId) {
    return delete(triggerId);
  }

  public CompletableFuture<Boolean> contentExists(UUID triggerId) {
    return exists(triggerId);
  }

  public CompletableFuture<DatabaseRow> findContent(UUID triggerId) {
    return selectRow(triggerId);
  }

  public CompletableFuture<List<DatabaseRow>> findContentByCondition(
    DatabaseCondition condition
  ) {
    return selectRows(condition);
  }
}
