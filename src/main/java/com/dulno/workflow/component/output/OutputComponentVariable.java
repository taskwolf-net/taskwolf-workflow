package com.dulno.workflow.component.output;

import com.dulno.workflow.component.ComponentVariable;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class OutputComponentVariable extends ComponentVariable {
  public static OutputComponentVariable create(
    String displayName, String identifier
  ) {
    return new OutputComponentVariable(displayName, identifier);
  }

  protected OutputComponentVariable(String displayName, String identifier) {
    super(displayName, identifier);
  }
}
