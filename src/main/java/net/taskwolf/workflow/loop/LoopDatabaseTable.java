package net.taskwolf.workflow.loop;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LoopDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "loop";

  public static LoopDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("content", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("stepIndex", DatabaseDataType.INT));
    var table = new LoopDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable workflowView;

  private LoopDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    workflowView = createMaterializedViewIfNotExists("workflow_view", "workflow",
      DatabaseColumn.Type.PARTITION_KEY);
  }

  public CompletableFuture<Void> insertLoop(LoopEntry entry) {
    return insertLoop(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.type(), entry.content(), entry.index());
  }

  public CompletableFuture<Void> insertLoop(
    UUID id, UUID ownerId, UUID workflowId, String type, String content, int index
  ) {
    return insert(DatabaseRow.of(id, ownerId, workflowId, type, content, index));
  }

  public CompletableFuture<Void> deleteLoop(UUID loopId) {
    return delete(loopId);
  }

  public CompletableFuture<UUID> generateAvailableLoopId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    loopExists(id).thenApply(exists -> exists ?
      generateAvailableLoopId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> loopExists(UUID loopId) {
    return exists(loopId);
  }

  public CompletableFuture<Boolean> loopExistsByWorkflow(UUID workflowId) {
    return workflowView.exists(DatabaseCondition.of("workflow", workflowId));
  }

  public CompletableFuture<LoopEntry> findLoop(UUID loopId) {
    return selectRow(loopId).thenApply(row -> LoopEntry.of(row, this));
  }

  public CompletableFuture<LoopEntry> findLoopByWorkflow(UUID workflowId) {
    return workflowView.selectRow(DatabaseCondition.of("workflow", workflowId))
      .thenApply(row -> LoopEntry.of(row, workflowView));
  }

  public CompletableFuture<Optional<LoopEntry>> findLoopIfExists(UUID workflowId) {
    return loopExistsByWorkflow(workflowId).thenCompose(exists -> exists ?
      findLoopByWorkflow(workflowId).thenApply(Optional::of) :
      CompletableFuture.completedFuture(Optional.empty()));
  }
}

