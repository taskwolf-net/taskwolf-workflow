package net.taskwolf.workflow.timeline;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.database.paging.DatabaseDirection;
import net.taskwolf.core.database.paging.DatabaseOrder;
import net.taskwolf.core.database.paging.DatabasePage;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TimelineDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow_timeline";

  public static TimelineDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("time", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    var table = new TimelineDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable idView;
  private DatabaseTable timeView;

  private TimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    idView = createMaterializedViewIfNotExists("id_view", "id",
      DatabaseColumn.Type.PARTITION_KEY);
    timeView = createMaterializedViewIfNotExists("time_view", "time");
  }

  public CompletableFuture<Void> insertEntry(TimelineDatabaseEntry entry) {
    return insertEntry(entry.id(), entry.workflowId(), entry.time(), entry.type(),
      entry.content());
  }

  public CompletableFuture<Void> insertEntry(
    UUID id, UUID workflowId, long time, String type, String content
  ) {
    return insert(DatabaseRow.of(workflowId, id, time, type, content),
      "USING TTL " + (60 * 60 * 24 * 30));
  }

  public CompletableFuture<UUID> generateAvailableEntryId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    entryExists(id).thenApply(exists -> exists ?
      generateAvailableEntryId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Void> deleteEntry(UUID entryId) {
    return findEntry(entryId).thenCompose(entry -> delete(
      DatabaseCondition.of("id", entry.id(), "workflow", entry.workflowId())));
  }

  public CompletableFuture<Void> clearWorkflowEntries(UUID workflowId) {
    return delete(DatabaseCondition.of("workflow", workflowId));
  }

  public CompletableFuture<Boolean> entryExists(UUID entryId) {
    return idView.exists(DatabaseCondition.of("id", entryId));
  }

  public CompletableFuture<TimelineDatabaseEntry> findEntry(UUID entryId) {
    return idView.selectRow(DatabaseCondition.of("id", entryId))
      .thenApply(row -> TimelineDatabaseEntry.of(row, idView));
  }

  private static final int PAGE_SIZE = 20;

  public CompletableFuture<DatabasePage<TimelineDatabaseEntry>> firstTimelinePage(
    UUID ownerId
  ) {
    return timeView.selectPage(ownerId, DatabaseCondition.empty(),
        DatabaseOrder.DESCENDING, PAGE_SIZE, 0)
      .thenApply(this::createTimelinePage);
  }

  public CompletableFuture<DatabasePage<TimelineDatabaseEntry>> nextTimelinePage(
    UUID ownerId, String pageState
  ) {
    return timeView.shiftPage(ownerId, DatabaseCondition.empty(),
        DatabaseOrder.DESCENDING, PAGE_SIZE, pageState,
        DatabaseDirection.FORWARD, DatabaseDirection.FORWARD)
      .thenApply(this::createTimelinePage);
  }

  private DatabasePage<TimelineDatabaseEntry> createTimelinePage(
    DatabasePage<DatabaseRow> page
  ) {
    return DatabasePage.create(page.content().stream()
        .map(row -> TimelineDatabaseEntry.of(row, timeView)).toList(),
      page.pageState(), page.pageNumber());
  }
}


