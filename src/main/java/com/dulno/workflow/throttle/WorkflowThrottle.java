package com.dulno.workflow.throttle;

import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowThrottle {
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final UUID targetId;

  private static final long THROTTLE_OBSERVATION_PERIOD = 1000L * 60;
  private static final long THROTTLE_MAXIMUM_EXECUTION_NUMBER = 50;

  /**
   * Used to note a further workflow execution and to check whether
   * this execution is legal
   * @return A future that contains whether the workflow execution is permitted,
   * i.e. whether too many workflows have been executed recently
   * True: Workflow execution is permitted
   * False: Workflow execution is not permitted
   */
  public CompletableFuture<Boolean> registerWorkflowExecution() {
    return workflowThrottleDatabaseTable.findThrottle(targetId)
      .thenApply(this::registerWorkflowExecution);
  }

  private boolean registerWorkflowExecution(
    WorkflowThrottleEntry entry
  ) {
    if (entry.expiration() <= System.currentTimeMillis()) {
      workflowThrottleDatabaseTable.setThrottle(entry, 1,
        System.currentTimeMillis() + THROTTLE_OBSERVATION_PERIOD);
      return true;
    }
    var result = entry.executions() + 1 <= THROTTLE_MAXIMUM_EXECUTION_NUMBER;
    if (result) {
      workflowThrottleDatabaseTable.addThrottleExecution(targetId);
    }
    return result;
  }
}
