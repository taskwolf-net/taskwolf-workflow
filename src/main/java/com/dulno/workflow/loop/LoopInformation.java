package com.dulno.workflow.loop;

import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class LoopInformation {
  public static LoopInformationBuilder builder() {
    return LoopInformationBuilder.create();
  }

  private final String name;
  private final String description;
  private final String identifier;
  private final List<InputComponentVariable> inputVariables;
  private final List<OutputComponentVariable> outputVariables;
}
