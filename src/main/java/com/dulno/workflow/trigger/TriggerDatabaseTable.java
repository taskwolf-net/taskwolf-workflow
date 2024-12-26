package com.dulno.workflow.trigger;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class TriggerDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "trigger";

  public static TriggerDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("module", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("state", DatabaseDataType.TEXT));
    var table = new TriggerDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable workflowView;

  private TriggerDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    workflowView = createMaterializedViewIfNotExists("workflow_view", "workflow",
      DatabaseColumn.Type.PARTITION_KEY);
  }

  public CompletableFuture<Void> insertTrigger(TriggerEntry entry) {
    return insertTrigger(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.module(), entry.type(), entry.state().toString());
  }

  public CompletableFuture<Void> insertTrigger(
    UUID id, UUID ownerId, UUID workflowId, String module, String type,
    String state
  ) {
    return insert(DatabaseRow.of(id, ownerId, workflowId, module, type, state));
  }

  public CompletableFuture<Void> changeState(UUID triggerId, TriggerState state) {
    return findTrigger(triggerId).thenCompose(entry -> changeState(entry, state));
  }

  private CompletableFuture<Void> changeState(TriggerEntry entry, TriggerState state) {
    entry.changeState(state);
    return updateTrigger(entry);
  }

  private CompletableFuture<Void> updateTrigger(TriggerEntry entry) {
    return update(entry.id(), DatabaseRow.of(entry.id(), entry.ownerId(),
      entry.workflowId(), entry.module(), entry.type(), entry.state().toString()));
  }

  public CompletableFuture<Void> deleteTrigger(UUID triggerId) {
    return delete(triggerId);
  }

  public CompletableFuture<UUID> generateAvailableTriggerId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    triggerExists(id).thenApply(exists -> exists ?
      generateAvailableTriggerId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> triggerExists(UUID triggerId) {
    return exists(triggerId);
  }

  public CompletableFuture<TriggerEntry> findTrigger(UUID triggerId) {
    return selectRow(triggerId).thenApply(row -> TriggerEntry.of(row, this));
  }

  public CompletableFuture<TriggerEntry> findTriggerByWorkflow(UUID workflowId) {
    return workflowView.selectRow(DatabaseCondition.of("workflow", workflowId))
      .thenApply(row -> TriggerEntry.of(row, workflowView));
  }
}
