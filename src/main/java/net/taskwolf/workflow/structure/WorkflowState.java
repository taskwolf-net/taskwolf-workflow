package net.taskwolf.workflow.structure;

public enum WorkflowState {
  OPERATIONAL,
  FAILING;

  public boolean isOperational() {
    return this == OPERATIONAL;
  }

  public boolean isFailing() {
    return this == FAILING;
  }
}
