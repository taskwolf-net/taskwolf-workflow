package com.dulno.workflow.action;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ActionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "action";

  public static ActionDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("module", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("stepIndex", DatabaseDataType.INT));
    var table = new ActionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable workflowView;

  private ActionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    workflowView = createMaterializedViewIfNotExists("workflow_view", "workflow",
      DatabaseColumn.Type.PARTITION_KEY);
  }

  public CompletableFuture<Void> insertAction(ActionEntry entry) {
    return insertAction(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.module(), entry.type(), entry.index());
  }

  public CompletableFuture<Void> insertAction(
    UUID id, UUID ownerId, UUID workflowId, String module,
    String type, int index
  ) {
    return insert(DatabaseRow.of(id, ownerId, workflowId, module, type, index));
  }

  public CompletableFuture<Void> deleteAction(UUID actionId) {
    return delete(actionId);
  }

  public CompletableFuture<UUID> generateAvailableActionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    actionExists(id).thenApply(exists -> exists ?
      generateAvailableActionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> actionExists(UUID actionId) {
    return exists(actionId);
  }

  public CompletableFuture<ActionEntry> findAction(UUID actionId) {
    return selectRow(actionId).thenApply(row -> ActionEntry.of(row, this));
  }

  public CompletableFuture<List<ActionEntry>> findActionsByWorkflow(
    UUID workflowId
  ) {
    return workflowView.selectRows(DatabaseCondition.of("workflow", workflowId))
      .thenApply(rows -> rows.stream().map(row -> ActionEntry.of(row, workflowView))
        .collect(Collectors.toList()));
  }
}
