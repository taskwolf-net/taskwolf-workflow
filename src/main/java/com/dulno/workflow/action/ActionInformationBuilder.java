package com.dulno.workflow.action;

import com.dulno.workflow.component.ComponentNovelty;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class ActionInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private ComponentNovelty novelty = ComponentNovelty.OLD;
  private final List<InputComponentVariable> inputVariables = Lists.newArrayList();
  private final List<OutputComponentVariable> outputVariables = Lists.newArrayList();

  /**
   * Gives the action information a name
   * @param name The name of the action (It is best to pass the locales key)
   * @return The information builder
   */
  public ActionInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }


  /**
   *  Is used to specify action description
   * @param description The description of the action
   *                    (It is best to pass the locales key)
   * @return The information builder
   */
  public ActionInformationBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Used to set novelty of action
   * You are encouraged to give newer actions this special attribute to
   * indicate the novelty to the customer
   * @param novelty The novelty associated with action
   * @return The information builder
   */
  public ActionInformationBuilder withNovelty(ComponentNovelty novelty) {
    this.novelty = novelty;
    return this;
  }

  /**
   * Adds a new input variable to the action information
   * @param variable The new input variable
   * @return The information builder
   */
  public ActionInformationBuilder withInputVariable(InputComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  /**
   * Adds a new output variable to the action
   * @param variable The new output variable
   * @return The information builder
   */
  public ActionInformationBuilder withOutputVariable(OutputComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  /**
   * Is called when information build process is completed
   * @return The action information
   */
  public ActionInformation build() {
    return ActionInformation.create(name, description, novelty, inputVariables,
      outputVariables);
  }
}
