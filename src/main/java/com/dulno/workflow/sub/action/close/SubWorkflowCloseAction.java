package com.dulno.workflow.sub.action.close;

import com.dulno.core.database.*;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class SubWorkflowCloseAction implements Action<SubWorkflowCloseActionExecutor> {
  public static SubWorkflowCloseAction create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("outputs", DatabaseDataType.TEXT));
    return new SubWorkflowCloseAction(
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_sub_workflow_close", contentColumns));
  }

  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "sub-workflow-close-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("sub.workflow.action.close.name")
      .withDescription("sub.workflow.action.close.description")
      .withInputVariable(InputComponentVariable.createRequired("sub.workflow.action.close.input.outputs.name",
        "outputs", "sub.workflow.action.close.input.outputs.description", InputComponentDataType.MAP))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId,
      DatabaseRow.of(content.get("outputs")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId)
      .thenApply(row -> Map.of("outputs", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<SubWorkflowCloseActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      SubWorkflowCloseActionExecutor.create(content.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
