package com.dulno.workflow.sub.select;

import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.user.User;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentSelectEntry;
import com.dulno.workflow.structure.WorkflowDatabaseTable;
import com.dulno.workflow.structure.WorkflowEntry;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.dulno.workflow.trigger.TriggerEntry;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public class SubWorkflowSelect implements InputComponentSelect {
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final WorkflowDatabaseTable workflowDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return triggerDatabaseTable.findTriggerByOwnerAndType(target,
        "sub-workflow-trigger")
      .thenCompose(this::findWorkflows)
      .thenApply(workflows -> workflows.stream()
        .map(workflow -> InputComponentSelectEntry.create(
          workflow.id().toString(), workflow.name()))
        .toList());
  }

  private CompletableFuture<List<WorkflowEntry>> findWorkflows(
    List<TriggerEntry> triggers
  ) {
    return AsyncIterator.execute(triggers, trigger ->
      workflowDatabaseTable.findWorkflow(trigger.workflowId()));
  }
}
