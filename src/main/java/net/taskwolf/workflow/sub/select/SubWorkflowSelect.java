package net.taskwolf.workflow.sub.select;

import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.user.User;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentSelectEntry;
import net.taskwolf.workflow.structure.WorkflowDatabaseTable;
import net.taskwolf.workflow.structure.WorkflowEntry;
import net.taskwolf.workflow.trigger.TriggerDatabaseTable;
import net.taskwolf.workflow.trigger.TriggerEntry;
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
