package com.dulno.workflow.loop;

import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class LoopInformationBuilder {
  private String name = "Unknown";
  private String description = "";
  private String identifier;
  private final List<InputComponentVariable> inputVariables = Lists.newArrayList();
  private final List<OutputComponentVariable> outputVariables = Lists.newArrayList();

  /**
   * Gives the condition information a name
   * @param name The name of the condition (It is best to pass the locales key)
   * @return The information builder
   */
  public LoopInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Gives the condition information a description
   * @param description The description of the condition (It is best to pass
   *                   the locales key)
   * @return The information builder
   */
  public LoopInformationBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Gives the condition information an identifier
   * @param identifier The identifier that is used to uniquely identifier condition
   * @return The information builder
   */
  public LoopInformationBuilder withIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * Adds a new input variable to the loop information
   * @param variable The new input variable
   * @return The information builder
   */
  public LoopInformationBuilder withInputVariable(InputComponentVariable variable) {
    inputVariables.add(variable);
    return this;
  }

  /**
   * Adds a new output variable to the loop
   * @param variable The new output variable
   * @return The information builder
   */
  public LoopInformationBuilder withOutputVariable(OutputComponentVariable variable) {
    outputVariables.add(variable);
    return this;
  }

  /**
   * Is called when information build process is completed
   * @return The condition information
   */
  public LoopInformation build() {
    return LoopInformation.create(name, description, identifier,
      inputVariables, outputVariables);
  }
}

