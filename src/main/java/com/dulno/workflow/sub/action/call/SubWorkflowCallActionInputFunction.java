package com.dulno.workflow.sub.action.call;

import com.dulno.core.user.User;
import com.dulno.workflow.component.input.DynamicInputComponentVariableFunction;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.sub.trigger.SubWorkflowTrigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.dulno.workflow.trigger.TriggerEntry;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class SubWorkflowCallActionInputFunction
  implements DynamicInputComponentVariableFunction
{
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final SubWorkflowTrigger subWorkflowTrigger;

  @Override
  public CompletableFuture<List<InputComponentVariable>> compile(
    User user, UUID target, JSONObject jsonObject
  ) {
    try {
      var workflowId = UUID.fromString(jsonObject.getString("workflow"));
      return triggerDatabaseTable.triggerExistsByWorkflow(workflowId)
        .thenCompose(exists -> checkTriggerPermission(workflowId, target, exists));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
  }

  private CompletableFuture<List<InputComponentVariable>> checkTriggerPermission(
    UUID workflowId, UUID target, boolean exists
  ) {
    if (!exists) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return triggerDatabaseTable.findTriggerByWorkflow(workflowId)
      .thenCompose(triggerEntry -> findTriggerInputs(triggerEntry, target));
  }

  private CompletableFuture<List<InputComponentVariable>> findTriggerInputs(
    TriggerEntry triggerEntry, UUID target
  ) {
    if (!triggerEntry.ownerId().equals(target) ||
      !triggerEntry.type().equals("sub-workflow-trigger")
    ) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return subWorkflowTrigger.findContent(triggerEntry.id())
      .thenApply(inputs -> createInputVariables((String) inputs.get("inputs")));
  }

  private List<InputComponentVariable> createInputVariables(String rawInputs) {
    try {
      var inputs = new JSONArray(rawInputs).toList().stream()
        .map(entry -> (String) entry).toList();
      var variables = Lists.<InputComponentVariable>newArrayList();
      for (var input : inputs) {
        variables.add(InputComponentVariable.createOptional(input,
          "sub_workflow_" + input, "", InputComponentDataType.TEXT));
      }
      return variables;
    } catch (Exception exception) {
      return Lists.newArrayList();
    }
  }
}
