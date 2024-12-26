package com.dulno.workflow.component.output;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class DynamicOutputComponentVariable extends OutputComponentVariable {
  public static DynamicOutputComponentVariable create(
    DynamicOutputComponentVariableFunction variableFunction
  ) {
    return new DynamicOutputComponentVariable(variableFunction);
  }

  private final DynamicOutputComponentVariableFunction variableFunction;

  private DynamicOutputComponentVariable(
    DynamicOutputComponentVariableFunction variableFunction
  ) {
    super("", "");
    this.variableFunction = variableFunction;
  }
}
