package com.dulno.workflow.component.input;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class DynamicInputComponentVariable extends InputComponentVariable {
  public static DynamicInputComponentVariable create(
    String identifier, List<String> requiredPredecessors,
    DynamicInputComponentVariableFunction variableFunction
  ) {
    return new DynamicInputComponentVariable(identifier, requiredPredecessors,
      variableFunction);
  }

  private final List<String> requiredPredecessors;
  private final DynamicInputComponentVariableFunction variableFunction;

  private DynamicInputComponentVariable(
    String identifier, List<String> requiredPredecessors,
    DynamicInputComponentVariableFunction variableFunction
  ) {
    super("", identifier, "", "", InputComponentDataType.DYNAMIC, null);
    this.requiredPredecessors = requiredPredecessors;
    this.variableFunction = variableFunction;
  }
}