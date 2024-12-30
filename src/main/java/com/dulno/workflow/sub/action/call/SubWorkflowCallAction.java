package com.dulno.workflow.sub.action.call;

import com.dulno.core.database.*;
import com.dulno.workflow.WorkflowModule;
import com.dulno.workflow.action.*;
import com.dulno.workflow.component.input.DynamicInputComponentVariable;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.DynamicOutputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.dulno.workflow.sub.action.close.SubWorkflowCloseAction;
import com.dulno.workflow.sub.trigger.SubWorkflowTrigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class SubWorkflowCallAction implements Action<SubWorkflowCallActionExecutor> {
  public static SubWorkflowCallAction create(
    TriggerDatabaseTable triggerDatabaseTable, ActionDatabaseTable actionDatabaseTable,
    SubWorkflowTrigger subWorkflowTrigger,
    SubWorkflowCloseAction subWorkflowCloseAction, WorkflowModule workflowModule,
    InputComponentSelect subWorkflowSelect, DatabaseConnection databaseConnection,
    DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("workflow", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("inputs", DatabaseDataType.TEXT));
    return new SubWorkflowCallAction(triggerDatabaseTable, actionDatabaseTable,
      subWorkflowTrigger, subWorkflowCloseAction, workflowModule, subWorkflowSelect,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_sub_workflow_call", contentColumns));
  }

  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final SubWorkflowTrigger subWorkflowTrigger;
  private final SubWorkflowCloseAction subWorkflowCloseAction;
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
      .withOutputVariable(DynamicOutputComponentVariable.create(
        (currentContent, previousActions) -> findSubWorkflowOutputs(currentContent)))
      .build();
  }

  private CompletableFuture<List<OutputComponentVariable>> findSubWorkflowOutputs(
    JSONObject actionContent
  ) {
    try {
      var workflow = UUID.fromString(actionContent.getString("workflow"));
      return actionDatabaseTable.findActionsByWorkflowAndType(workflow,
        "sub-workflow-close-action").thenCompose(this::findSubWorkflowAction);
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
  }

  private CompletableFuture<List<OutputComponentVariable>> findSubWorkflowAction(
    List<ActionEntry> actions
  ) {
    if (actions.isEmpty()) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return subWorkflowCloseAction.findContent(actions.get(0).id())
      .thenApply(this::assemblySubWorkflowOutputVariables);
  }

  private List<OutputComponentVariable> assemblySubWorkflowOutputVariables(
    Map<String, Object> actionContent
  ) {
    var variables = Lists.<OutputComponentVariable>newArrayList();
    var outputs = new JSONObject((String) actionContent.get("outputs"));
    for (var key : outputs.keySet()) {
      if (key.isEmpty() || key.isBlank()) {
        continue;
      }
      variables.add(OutputComponentVariable.create(key, "sub_workflow_" + key));
    }
    return variables;
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId)
      .concat(encodeContent(content)));
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
    content.put("workflow", row.findCell(2).uuidValue().toString());
    var inputs = new JSONObject(row.findCell(3).stringValue());
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
        content.findCell(2).uuidValue(), content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
