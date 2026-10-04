package net.taskwolf.workflow.trigger;

import net.taskwolf.workflow.component.ComponentInformation;
import net.taskwolf.workflow.component.ComponentNovelty;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;

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
