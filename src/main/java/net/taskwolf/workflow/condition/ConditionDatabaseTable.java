package net.taskwolf.workflow.condition;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ConditionDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "condition";

  public static ConditionDatabaseTable create(
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
    var table = new ConditionDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.initializeViews();
    return table;
  }

  private DatabaseTable workflowView;

  private ConditionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    workflowView = createMaterializedViewIfNotExists("workflow_view", "workflow",
      DatabaseColumn.Type.PARTITION_KEY);
  }

  public CompletableFuture<Void> insertCondition(ConditionEntry entry) {
    return insertCondition(entry.id(), entry.ownerId(), entry.workflowId(),
      entry.type(), entry.content(), entry.index());
  }

  public CompletableFuture<Void> insertCondition(
    UUID id, UUID ownerId, UUID workflowId, String type, String content, int index
  ) {
    return insert(DatabaseRow.of(id, ownerId, workflowId, type, content, index));
  }

  public CompletableFuture<Void> deleteCondition(UUID conditionId) {
    return delete(conditionId);
  }

  public CompletableFuture<UUID> generateAvailableConditionId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    conditionExists(id).thenApply(exists -> exists ?
      generateAvailableConditionId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> conditionExists(UUID conditionId) {
    return exists(conditionId);
  }

  public CompletableFuture<ConditionEntry> findCondition(UUID conditionId) {
    return selectRow(conditionId).thenApply(row -> ConditionEntry.of(row, this));
  }

  public CompletableFuture<List<ConditionEntry>> findConditionsByWorkflow(
    UUID workflowId
  ) {
    return workflowView.selectRows(DatabaseCondition.of("workflow", workflowId))
      .thenApply(rows -> rows.stream().map(row -> ConditionEntry.of(row, workflowView))
        .collect(Collectors.toList()));
  }
}

