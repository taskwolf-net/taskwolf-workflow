package com.dulno.workflow.trigger;

import com.dulno.workflow.component.ComponentInformation;
import com.dulno.workflow.component.ComponentNovelty;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;

import java.util.List;

public final class TriggerInformation extends ComponentInformation {
  public static TriggerInformationBuilder builder() {
    return TriggerInformationBuilder.create();
  }

  public static TriggerInformation create(
    String name, String description, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    return new TriggerInformation(name, description, novelty,
      inputVariables, outputVariables);
  }

  private TriggerInformation(
    String name, String description, ComponentNovelty novelty,
    List<InputComponentVariable> inputVariables,
    List<OutputComponentVariable> outputVariables
  ) {
    super(name, description, novelty, inputVariables, outputVariables);
  }
}
