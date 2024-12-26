package com.dulno.workflow.step;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
public class WorkflowStepResult {
  /**
   * Creates a new success workflow step result that may continue
   * @return The result
   */
  public static WorkflowStepResult success() {
    return success(true);
  }

  /**
   * Creates a new success workflow step result with empty pass on information
   * @param mayContinue Whether to continue with the next step
   * @return The result
   */
  public static WorkflowStepResult success(boolean mayContinue) {
    return new WorkflowStepResult(WorkflowStepStatus.SUCCESS, mayContinue,
      Maps.newHashMap());
  }

  /**
   * Creates a new success workflow step result that may continue
   * @param passOnInformation The information that is passed to the next component
   * @return The result
   */
  public static WorkflowStepResult success(Map<String, Object> passOnInformation) {
    return new WorkflowStepResult(WorkflowStepStatus.SUCCESS, true,
      passOnInformation);
  }

  /**
   * Creates a new failure workflow step result
   * @param failureMessage The failure message that is displayed in the
   *                      workflow timeline (It is best to pass the locales key)
   * @return The result
   */
  public static WorkflowStepResult failure(String failureMessage) {
    return failure(failureMessage, -1);
  }

  /**
   * Creates a new failure workflow step result
   * @param failureMessage The failure message that is displayed in the
   *                      workflow timeline (It is best to pass the locales key)
   * @param failureStepIndex The index of the step where the failure occurred
   * @return The result
   */
  public static WorkflowStepResult failure(
    String failureMessage, int failureStepIndex
  ) {
    return new WorkflowStepResult(WorkflowStepStatus.FAILURE, failureMessage,
      failureStepIndex);
  }

  private final WorkflowStepStatus status;
  @Getter
  private boolean mayContinue;
  private Map<String, Object> passOnInformation;
  @Getter
  private String failureMessage;
  @Getter
  private int failureStepIndex;

  protected WorkflowStepResult(
    WorkflowStepStatus status, boolean mayContinue,
    Map<String, Object> passOnInformation
  ) {
    this.status = status;
    this.mayContinue = mayContinue;
    this.passOnInformation = passOnInformation;
    this.failureMessage = "";
  }

  protected WorkflowStepResult(
    WorkflowStepStatus status, String failureMessage, int failureStepIndex
  ) {
    this.status = status;
    this.mayContinue = false;
    this.failureMessage = failureMessage;
    this.failureStepIndex = failureStepIndex;
  }

  public boolean isSuccess() {
    return status.isSuccess();
  }

  public boolean isFailure() {
    return status.isFailure();
  }

  public Map<String, Object> passOnInformation() {
    return Map.copyOf(passOnInformation);
  }
}
