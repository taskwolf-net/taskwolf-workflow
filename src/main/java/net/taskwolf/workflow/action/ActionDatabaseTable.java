package net.taskwolf.workflow.action;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
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
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("module", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("stepIndex", DatabaseDataType.INT));
    var table = new ActionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable workflowView;
  private DatabaseTable workflowTypeView;

  private ActionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    workflowView = createMaterializedViewIfNotExists("workflow_view", "workflow",
      DatabaseColumn.Type.PARTITION_KEY);
    initializeWorkflowTypeView();
  }

  private void initializeWorkflowTypeView() {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("type", DatabaseDataType.TEXT,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    workflowTypeView = createMaterializedViewIfNotExists("workflow_type_view", columns);
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
    return delete(DatabaseCondition.of("id", actionId));
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
    return exists(DatabaseCondition.of("id", actionId));
  }

  public CompletableFuture<ActionEntry> findAction(UUID actionId) {
    return selectRow(DatabaseCondition.of("id", actionId))
      .thenApply(row -> ActionEntry.of(row, this));
  }

  public CompletableFuture<List<ActionEntry>> findActionsByWorkflow(
    UUID workflowId
  ) {
    return workflowView.selectRows(DatabaseCondition.of("workflow", workflowId))
      .thenApply(rows -> rows.stream().map(row -> ActionEntry.of(row, workflowView))
        .collect(Collectors.toList()));
  }

  public CompletableFuture<List<ActionEntry>> findActionsByWorkflowAndType(
    UUID workflowId, String type
  ) {
    var condition = DatabaseCondition.of("workflow", workflowId, "type", type);
    return workflowTypeView.selectRows(condition).thenApply(rows ->
      rows.stream().map(row -> ActionEntry.of(row, workflowTypeView)).toList());
  }
}
