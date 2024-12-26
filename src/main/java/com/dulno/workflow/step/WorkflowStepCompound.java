package com.dulno.workflow.step;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class WorkflowStepCompound {
  private final WorkflowStep step;
  private final String moduleName;
  private final String stepName;
}
