package com.dulno.workflow.component;

import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ComponentInformation {
  @Getter
  private final String name;
  @Getter
  private final String description;
  @Getter
  private final ComponentNovelty novelty;
  private final List<InputComponentVariable> inputVariables;
  private final List<OutputComponentVariable> outputVariables;

  public List<InputComponentVariable> inputVariables() {
    return List.copyOf(inputVariables);
  }

  public List<OutputComponentVariable> outputVariables() {
    return List.copyOf(outputVariables);
  }
}
