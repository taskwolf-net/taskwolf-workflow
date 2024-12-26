package com.dulno.workflow.step;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorkflowStep {
  /**
   * Is used to execute a workflow step (e.g. action, condition, loop)
   * @param information External information that are fed into the
   *                    execution process
   * @return A future {@link WorkflowStepResult}
   */
  CompletableFuture<? extends WorkflowStepResult> execute(
    Map<String, Object> information);
}
