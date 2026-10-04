package net.taskwolf.workflow.component;

/**
 * Used to present new components to customers in a special way.
 * This effect occurs when the novelty is set to “NEW”
 */
public enum ComponentNovelty {
  NEW,
  OLD;

  public boolean isNew() {
    return this == NEW;
  }

  public boolean isOld() {
    return this == OLD;
  }
}
