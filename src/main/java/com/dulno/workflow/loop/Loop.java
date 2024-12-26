package com.dulno.workflow.loop;

import com.dulno.core.bundle.Bundle;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.operation.Operation;
import com.dulno.workflow.operation.OperationDatabaseTable;
import com.dulno.workflow.step.WorkflowStep;
import com.dulno.workflow.step.WorkflowStepCompound;
import com.dulno.workflow.step.WorkflowStepResult;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Loop implements WorkflowStep {
  private final OperationDatabaseTable operationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final LoopEntry loopEntry;
  private final Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator;
  private final Bundle bundle;

  /**
   * Performs the actual looping
   * @param information The information that can be used for the loop
   * @return The future result of the loop
   */
  public abstract CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information);

  /**
   * Performs a single iteration
   * @param information The information for that specific iteration
   * @return The result of this iteration
   */
  protected CompletableFuture<WorkflowStepResult> iterate(
    Map<String, Object> information
  ) {
    try {
      return stepGenerator.call()
        .thenCompose(steps -> checkOperationLimit(false)
          .thenCompose(limitReached -> executeNextStep(0, steps, information,
            limitReached)));
    } catch (Exception exception) {
      exception.printStackTrace();
      return CompletableFuture.completedFuture(WorkflowStepResult.failure(""));
    }
  }

  private CompletableFuture<WorkflowStepResult> executeNextStep(
    int currentIndex, List<WorkflowStepCompound> steps,
    Map<String, Object> information, boolean limitReached
  ) {
    if (maintenanceSchedule.isMaintenanceRunning()) {
      return CompletableFuture.completedFuture(WorkflowStepResult.success());
    }
    if (limitReached) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("workflow.operations.limit.reached"));
    }
    if (currentIndex >= steps.size()) {
      return CompletableFuture.completedFuture(WorkflowStepResult.success());
    }
    var step = steps.get(currentIndex).step();
    return step.execute(information)
      .thenCompose(result -> checkOperationLimit(step)
        .thenCompose(newLimitReached -> processStepResult(currentIndex, result,
          steps, information, newLimitReached)));
  }

  private CompletableFuture<WorkflowStepResult> processStepResult(
    int currentIndex, WorkflowStepResult result, List<WorkflowStepCompound> steps,
    Map<String, Object> information, boolean limitReached
  ) {
    var currentStepIndex = loopEntry.index() + 1 + currentIndex;
    if (result.isFailure()) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure(result.failureMessage(), currentStepIndex));
    }
    if (!result.mayContinue()) {
      return CompletableFuture.completedFuture(WorkflowStepResult.success());
    }
    information.putAll(prepareInformation("step" + currentStepIndex,
      result.passOnInformation()));
    return executeNextStep(currentIndex + 1, steps, information, limitReached);
  }

  protected Map<String, Object> prepareInformation(
    String prefix, Map<String, Object> information
  ) {
    var result = Maps.<String, Object>newHashMap();
    for (var entry : information.entrySet()) {
      result.put(prefix + "-" + entry.getKey(), entry.getValue());
    }
    return result;
  }

  private CompletableFuture<Boolean> checkOperationLimit(WorkflowStep step) {
    if (!(step instanceof ActionExecutor)) {
      return CompletableFuture.completedFuture(false);
    }
    return checkOperationLimit(true);
  }

  private CompletableFuture<Boolean> checkOperationLimit(boolean addOperation) {
    return operationDatabaseTable.findOperations(bundle.ownerId())
      .thenCompose(operation -> checkOperationLimit(operation, addOperation));
  }

  private CompletableFuture<Boolean> checkOperationLimit(
    Operation operation, boolean addOperation
  ) {
    if (operation.operations() + 1 > bundle.workflowOperationLimit()) {
      return CompletableFuture.completedFuture(true);
    }
    if (!addOperation) {
      return CompletableFuture.completedFuture(false);
    }
    return operationDatabaseTable.addOperations(bundle.ownerId(), 1)
      .thenApply(value -> false);
  }
}
