package com.dulno.workflow.component.input;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class SelectableInputComponentVariable extends InputComponentVariable {
  public static SelectableInputComponentVariable create(
    String displayName, String identifier, String description,
    InputComponentSelect select
  ) {
    return new SelectableInputComponentVariable(displayName, identifier,
      description, select);
  }

  private final InputComponentSelect select;

  private SelectableInputComponentVariable(
    String displayName, String identifier, String description,
    InputComponentSelect select
  ) {
    super(displayName, identifier, description, "", InputComponentDataType.SELECT,
      InputComponentType.REQUIRED);
    this.select = select;
  }
}