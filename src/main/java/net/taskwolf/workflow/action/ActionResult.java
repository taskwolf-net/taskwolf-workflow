package net.taskwolf.workflow.action;

import net.taskwolf.workflow.step.WorkflowStepResult;
import net.taskwolf.workflow.step.WorkflowStepStatus;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Accessors(fluent = true)
public final class ActionResult extends WorkflowStepResult {
  /**
   * Creates a new future success action result
   * @param information The information that is passed to the next component
   * @return The future result
   */
  public static CompletableFuture<ActionResult> futureSuccess(
    Map<String, Object> information
  ) {
    return CompletableFuture.completedFuture(success(information));
  }

  /**
   * Creates a new future failure action result
   * @param failureMessage The failure message that is displayed in the
   *                       workflow timeline (It is best to pass the locales key)
   * @return The future result
   */
  public static CompletableFuture<ActionResult> futureFailure(
    String failureMessage
  ) {
    return CompletableFuture.completedFuture(failure(failureMessage));
  }

  /**
   * Creates a new success action result
   * @param information The information that is passed to the next component
   * @return The result
   */
  public static ActionResult success(Map<String, Object> information) {
    return new ActionResult(WorkflowStepStatus.SUCCESS, true, information);
  }

  /**
   * Creates a new failure action result
   * @param failureMessage The failure message that is displayed in the
   *                      workflow timeline (It is best to pass the locales key)
   * @return The result
   */
  public static ActionResult failure(String failureMessage) {
    return new ActionResult(WorkflowStepStatus.FAILURE, failureMessage);
  }

  private ActionResult(
    WorkflowStepStatus status, boolean mayContinue,
    Map<String, Object> passOnInformation
  ) {
    super(status, mayContinue, passOnInformation);
  }

  private ActionResult(WorkflowStepStatus status, String failureMessage) {
    super(status, failureMessage, -2);
  }
}
