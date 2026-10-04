package net.taskwolf.workflow.structure;

import net.taskwolf.core.database.*;
import net.taskwolf.core.database.condition.DatabaseComparison;
import net.taskwolf.core.database.condition.DatabaseCondition;
import net.taskwolf.core.database.paging.DatabaseDirection;
import net.taskwolf.core.database.paging.DatabaseOrder;
import net.taskwolf.core.database.paging.DatabasePage;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WorkflowDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "workflow";

  public static WorkflowDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID,
      DatabaseColumn.Type.PARTITION_KEY));
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.CLUSTERING_KEY));
    columns.add(DatabaseColumn.create("creator", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("trigger", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("actions", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("conditions", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("loop", DatabaseDataType.UUID));
    columns.add(DatabaseListColumn.create("modules", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("timeZone", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("timeLocale", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("created", DatabaseDataType.BIGINT));
    columns.add(DatabaseColumn.create("name", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("description", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("state", DatabaseDataType.TEXT));
    var table = new WorkflowDatabaseTable(connection, keyspace, TABLE_NAME, columns);
    table.createIfNotExists();
    table.createIndexIfNotExists("modules");
    table.createIndexIfNotExists("name",
      "'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
        "{'mode': 'CONTAINS', 'analyzer_class': " +
        "'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer', " +
        "'case_sensitive': 'false'}");
    table.initializeViews();
    return table;
  }

  private DatabaseTable idView;
  private DatabaseTable triggerView;
  private DatabaseTable nameView;
  private DatabaseTable creatorView;
  private DatabaseTable createdView;

  private WorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  private void initializeViews() {
    idView = createMaterializedViewIfNotExists("id_view", "id",
      DatabaseColumn.Type.PARTITION_KEY);
    triggerView = createMaterializedViewIfNotExists("trigger_view", "trigger",
      DatabaseColumn.Type.PARTITION_KEY);
    nameView = createMaterializedViewIfNotExists("name_view", "name");
    creatorView = createMaterializedViewIfNotExists("creator_view", "creator");
    createdView = createMaterializedViewIfNotExists("created_view", "created");
  }

  public CompletableFuture<Void> insertWorkflow(WorkflowEntry entry) {
    return insertWorkflow(entry.ownerId(), entry.id(), entry.creatorId(),
      entry.triggerId(), entry.actionIds(), entry.conditionIds(), entry.loopId(),
      entry.modules(), entry.timeZone(), entry.timeLocale(), entry.created(),
      entry.name(), entry.description(), entry.state().toString());
  }

  public CompletableFuture<Void> insertWorkflow(
    UUID id, UUID ownerId, UUID creatorId, UUID triggerId, List<UUID> actionIds,
    List<UUID> conditionIds, UUID loopId, List<String> modules, String timeZone,
    String timeLocale, long created, String name, String description, String state
  ) {
    return insert(DatabaseRow.of(ownerId, id, creatorId, triggerId, actionIds,
      conditionIds, loopId, modules, timeZone, timeLocale, created, name,
      description, state));
  }

  public CompletableFuture<Void> updateWorkflowState(
    WorkflowEntry entry, WorkflowState state
  ) {
    return update(DatabaseCondition.of("owner", entry.ownerId(), "id", entry.id()),
      DatabaseRow.of(entry.ownerId(), entry.id(), entry.creatorId(),
        entry.triggerId(), entry.actionIds(), entry.conditionIds(), entry.loopId(),
        entry.modules(), entry.timeZone(), entry.timeLocale(), entry.created(),
        entry.name(), entry.description(), state.toString()));
  }

  public CompletableFuture<Void> deleteWorkflow(UUID workflowId) {
    return findWorkflow(workflowId).thenAccept(workflow ->
      delete(DatabaseCondition.of("owner", workflow.ownerId(), "id", workflow.id())));
  }

  public CompletableFuture<UUID> generateAvailableWorkflowId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    workflowExists(id).thenApply(exists -> exists ?
      generateAvailableWorkflowId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public CompletableFuture<Boolean> workflowExists(UUID workflowId) {
    return idView.exists(DatabaseCondition.of("id", workflowId));
  }

  public CompletableFuture<WorkflowEntry> findWorkflow(UUID workflowId) {
    return idView.selectRow(DatabaseCondition.of("id", workflowId))
      .thenApply(row -> WorkflowEntry.of(row, idView));
  }

  private static final int PAGE_SIZE = 5;

  public CompletableFuture<DatabasePage<WorkflowEntry>> findWorkflowsOfOwner(
    UUID ownerId, int targetPage, String sortingColumn, DatabaseOrder sortingOrder,
    String search, String module, UUID creatorId, long startTime, long endTime,
    String state
  ) {
    if (!search.isEmpty()) {
      var condition = DatabaseCondition.of(DatabaseComparison.create("owner", ownerId),
        DatabaseComparison.create("name", "%" + search + "%", DatabaseComparison.Type.LIKE));
      return selectRows(condition, PAGE_SIZE).thenApply(rows ->
        createWorkflowPage(DatabasePage.create(rows, "", 1), this));
    }
    var view = findTargetView(sortingColumn);
    return view.selectPage(ownerId,
        createWorkflowsConditions(module, creatorId, startTime, endTime, state),
        sortingOrder, PAGE_SIZE, targetPage)
      .thenApply(page -> createWorkflowPage(page, view));
  }

  public CompletableFuture<DatabasePage<WorkflowEntry>> findWorkflowsOfOwner(
    UUID ownerId, String pageState, DatabaseDirection startingPoint,
    DatabaseDirection direction, String sortingColumn, DatabaseOrder sortingOrder,
    String module, UUID creatorId, long startTime, long endTime, String state
  ) {
    var view = findTargetView(sortingColumn);
    return view.shiftPage(ownerId,
        createWorkflowsConditions(module, creatorId, startTime, endTime, state),
        sortingOrder, PAGE_SIZE, pageState, startingPoint, direction)
      .thenApply(page -> createWorkflowPage(page, view));
  }

  private DatabaseTable findTargetView(String sortingColumn) {
    if (sortingColumn.equals("name")) {
      return nameView;
    } else if (sortingColumn.equals("creator")) {
      return creatorView;
    } else if (sortingColumn.equals("created")) {
      return createdView;
    }
    return null;
  }

  private DatabaseCondition createWorkflowsConditions(
    String module, UUID creatorId, long startTime, long endTime, String state
  ) {
    var comparisons = Lists.<DatabaseComparison>newArrayList();
    if (module != null) {
      comparisons.add(DatabaseComparison.create("modules", module,
        DatabaseComparison.Type.CONTAINS));
    }
    if (creatorId != null) {
      comparisons.add(DatabaseComparison.create("creator", creatorId));
    }
    if (startTime > 0) {
      comparisons.add(DatabaseComparison.create("created", startTime,
        DatabaseComparison.Type.GREATER_EQUALS));
    }
    if (endTime > 0) {
      comparisons.add(DatabaseComparison.create("created", endTime,
        DatabaseComparison.Type.SMALLER_EQUALS));
    }
    if (state != null) {
      comparisons.add(DatabaseComparison.create("state", state));
    }
    return DatabaseCondition.create(comparisons);
  }

  private DatabasePage<WorkflowEntry> createWorkflowPage(
    DatabasePage<DatabaseRow> page, DatabaseTable table
  ) {
    return DatabasePage.create(
      page.content().stream().map(row -> WorkflowEntry.of(row, table)).toList(),
      page.pageState(), page.pageNumber());
  }

  public CompletableFuture<Long> findWorkflowCount(UUID ownerId) {
    return count(DatabaseCondition.of("owner", ownerId));
  }

  public CompletableFuture<List<WorkflowEntry>> findAllWorkflowsOfOwner(
    UUID ownerId
  ) {
    return selectRows(DatabaseCondition.of("owner", ownerId)).thenApply(rows ->
      rows.stream().map(row -> WorkflowEntry.of(row, this)).toList());
  }

  public CompletableFuture<List<WorkflowEntry>> findWorkflowByModule(
    UUID ownerId, String module
  ) {
    var condition = DatabaseCondition.of(DatabaseComparison.create("owner", ownerId),
      DatabaseComparison.create("modules", module, DatabaseComparison.Type.CONTAINS));
    return selectRows(condition)
      .thenApply(rows -> rows.stream().map(row -> WorkflowEntry.of(row, this))
        .toList());
  }

  public CompletableFuture<WorkflowEntry> findWorkflowByTrigger(UUID triggerId) {
    return triggerView.selectRow(DatabaseCondition.of("trigger", triggerId))
      .thenApply(row -> WorkflowEntry.of(row, triggerView));
  }
}
