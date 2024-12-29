package com.dulno.workflow.sub.action.call;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.workflow.WorkflowModule;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.dulno.workflow.structure.Workflow;
import com.dulno.workflow.sub.trigger.SubWorkflowTrigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.dulno.workflow.trigger.TriggerEntry;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class SubWorkflowCallActionExecutor implements ActionExecutor {
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final SubWorkflowTrigger subWorkflowTrigger;
  private final WorkflowModule workflowModule;
  private final UUID workflow;
  private String inputs;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    inputs = dissolve.dissolve(inputs);
    return triggerDatabaseTable.triggerExistsByWorkflow(workflow)
      .thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(boolean triggerExists) {
    if (!triggerExists) {
      return ActionResult.futureFailure("sub.workflow.action.call.workflow.not.found");
    }
    return triggerDatabaseTable.findTriggerByWorkflow(workflow)
      .thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(TriggerEntry triggerEntry) {
    if (!triggerEntry.type().equals("sub-workflow-trigger")) {
      return ActionResult.futureFailure("sub.workflow.action.call.workflow.wrong.trigger");
    }
    return subWorkflowTrigger.findContent(triggerEntry.id())
      .thenCompose(inputs -> execute((String) inputs.get("inputs")));
  }

  private CompletableFuture<ActionResult> execute(String rawTotalInputs) {
    var inputs = new JSONObject(this.inputs);
    var totalInputs = new JSONArray(rawTotalInputs).toList().stream()
      .map(entry -> (String) entry).toList();
    var triggerInformation = Maps.<String, Object>newHashMap();
    for (var input : totalInputs) {
      var value = inputs.has(input) ? inputs.get(input) : "";
      triggerInformation.put("sub_workflow_" + input, value);
    }
    return workflowModule.createWorkflowById(workflow)
      .thenCompose(workflow -> workflow.trigger(triggerInformation)
        .thenApply(result -> ActionResult.success(buildInformation(workflow))));
  }

  private Map<String, Object> buildInformation(Workflow workflow) {
    var information = Maps.<String, Object>newHashMap();
    var workflowInformation = workflow.currentInformation();
    for (var entry : workflowInformation.entrySet()) {
      var key = entry.getKey();
      if (key.contains("sub_workflow")) {
        information.put(key.substring(key.indexOf("sub_workflow_")),
          entry.getValue());
      }
    }
    return information;
  }
}
