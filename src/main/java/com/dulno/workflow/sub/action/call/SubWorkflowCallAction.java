package com.dulno.workflow.sub.action.call;

import com.dulno.core.database.*;
import com.dulno.workflow.WorkflowModule;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.workflow.component.input.DynamicInputComponentVariable;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.sub.trigger.SubWorkflowTrigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class SubWorkflowCallAction implements Action<SubWorkflowCallActionExecutor> {
  public static SubWorkflowCallAction create(
    TriggerDatabaseTable triggerDatabaseTable, SubWorkflowTrigger subWorkflowTrigger,
    WorkflowModule workflowModule, InputComponentSelect subWorkflowSelect,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("inputs", DatabaseDataType.TEXT));
    return new SubWorkflowCallAction(triggerDatabaseTable, subWorkflowTrigger,
      workflowModule, subWorkflowSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_sub_workflow_call", contentColumns));
  }

  private final TriggerDatabaseTable triggerDatabaseTable;
  private final SubWorkflowTrigger subWorkflowTrigger;
  private final WorkflowModule workflowModule;
  private final InputComponentSelect subWorkflowSelect;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "sub-workflow-call-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("sub.workflow.action.call.name")
      .withDescription("sub.workflow.action.call.description")
      .withInputVariable(InputComponentVariable.createSelect("sub.workflow.action.call.input.workflow.name",
        "workflow", "sub.workflow.action.call.input.workflow.description", subWorkflowSelect))
      .withInputVariable(DynamicInputComponentVariable.create("inputs",
        Lists.newArrayList("workflow"),
        SubWorkflowCallActionInputFunction.create(triggerDatabaseTable,
          subWorkflowTrigger)))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, encodeContent(content));
  }

  private DatabaseRow encodeContent(Map<String, Object> content) {
    var inputs = new JSONObject();
    for (var entry : content.entrySet()) {
      if (entry.getKey().contains("sub_workflow_")) {
        inputs.put(entry.getKey().replace("sub_workflow_", ""), entry.getValue());
      }
    }
    return DatabaseRow.of(UUID.fromString((String) content.get("workflow")),
      inputs.toString());
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId)
      .thenApply(this::decodeContent);
  }

  private Map<String, Object> decodeContent(DatabaseRow row) {
    var content = Maps.<String, Object>newHashMap();
    content.put("workflow", row.findCell(1).uuidValue().toString());
    var inputs = new JSONObject(row.findCell(2).stringValue());
    for (var entry : inputs.keySet()) {
      content.put("sub_workflow_" + entry, inputs.getString(entry));
    }
    return content;
  }

  @Override
  public CompletableFuture<SubWorkflowCallActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      SubWorkflowCallActionExecutor.create(triggerDatabaseTable,
        subWorkflowTrigger, workflowModule, content.findCell(1).uuidValue(),
        content.findCell(2).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
