package net.taskwolf.workflow.trigger;

public enum TriggerState {
  ARMED,
  DISABLED;

  public boolean isArmed() {
    return this == ARMED;
  }

  public boolean isDisabled() {
    return this == DISABLED;
  }
}
