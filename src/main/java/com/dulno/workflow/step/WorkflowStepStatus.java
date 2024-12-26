package com.dulno.workflow.step;

public enum WorkflowStepStatus {
  SUCCESS,
  FAILURE;

  public boolean isSuccess() {
    return this == SUCCESS;
  }

  public boolean isFailure() {
    return this == FAILURE;
  }
}
