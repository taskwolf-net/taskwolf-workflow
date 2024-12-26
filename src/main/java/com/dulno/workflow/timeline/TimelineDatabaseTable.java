package com.dulno.workflow.timeline;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class TimelineDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow_timeline";

  public static TimelineDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    return new TimelineDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private TimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertEntry(TimelineDatabaseEntry entry) {
    return insertEntry(entry.id(), entry.workflowId(), entry.time(), entry.type(),
      entry.content());
  }

  public CompletableFuture<Void> insertEntry(
    UUID id, UUID workflowId, long time, String type, String content
  ) {
    return insert(DatabaseRow.of(id, workflowId, time, type, content));
  }

  public CompletableFuture<Void> deleteEntry(UUID entryId) {
    return delete(entryId);
  }

  public CompletableFuture<UUID> generateAvailableEntryId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    entryExists(id).thenApply(exists -> exists ?
      generateAvailableEntryId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> entryExists(UUID entryId) {
    return exists(entryId);
  }

  public CompletableFuture<TimelineDatabaseEntry> findEntry(UUID entryId) {
    return selectRow(entryId).thenApply(TimelineDatabaseEntry::of);
  }

  public CompletableFuture<List<TimelineDatabaseEntry>> findEntriesByWorkflow(
    UUID workflowId
  ) {
    return selectRows(DatabaseCondition.of("workflow", workflowId))
      .thenApply(rows -> rows.stream().map(TimelineDatabaseEntry::of)
        .collect(Collectors.toList()));
  }
}


