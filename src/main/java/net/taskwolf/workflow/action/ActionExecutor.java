package net.taskwolf.workflow.action;

import net.taskwolf.workflow.step.WorkflowStep;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ActionExecutor extends WorkflowStep {
  /**
   * Is used to actually execute an action
   * @param information External information that are fed into the
   *                    execution process
   * @return A future {@link ActionResult}
   */
  CompletableFuture<ActionResult> execute(Map<String, Object> information);
}
