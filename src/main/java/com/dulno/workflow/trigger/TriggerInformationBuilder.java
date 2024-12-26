package com.dulno.workflow.trigger;

import com.dulno.workflow.component.ComponentNovelty;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class TriggerInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private ComponentNovelty novelty = ComponentNovelty.OLD;
  private final List<InputComponentVariable> inputVariables = Lists.newArrayList();
  private final List<OutputComponentVariable> outputVariables = Lists.newArrayList();

  /**
   * Gives the trigger information a name
   * @param name The name of the trigger (It is best to pass the locales key)
   * @return The information builder
   */
  public TriggerInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Is used to specify trigger description
   * @param description The description of the trigger
   *                    (It is best to pass the locales key)
   * @return The information builder
   */
  public TriggerInformationBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Used to set novelty of trigger
   * You are encouraged to give newer triggers this special attribute to
   * indicate the novelty to the customer
   * @param novelty The novelty associated with trigger
   * @return The information builder
   */
  public TriggerInformationBuilder withNovelty(ComponentNovelty novelty) {
    this.novelty = novelty;
    return this;
  }

  /**
   * Adds a new input variable to the trigger information
   * @param variable The new input variable
   * @return The information builder
   */
  public TriggerInformationBuilder withInputVariable(InputComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  /**
   * Adds a new output variable to the trigger
   * @param variable The new output variable
   * @return The information builder
   */
  public TriggerInformationBuilder withOutputVariable(OutputComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  /**
   * Is called when information build process is completed
   * @return The trigger information
   */
  public TriggerInformation build() {
    return TriggerInformation.create(name, description, novelty, inputVariables,
      outputVariables);
  }
}
