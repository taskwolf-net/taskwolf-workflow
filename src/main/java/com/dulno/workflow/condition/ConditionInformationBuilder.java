package com.dulno.workflow.condition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class ConditionInformationBuilder {
  private String name = "Unknown";
  private ConditionDataType dataType;
  private String identifier;

  /**
   * Gives the condition information a name
   * @param name The name of the condition (It is best to pass the locales key)
   * @return The information builder
   */
  public ConditionInformationBuilder withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Gives the condition information a data type
   * @param dataType The datatype of the condition
   * @return The information builder
   */
  public ConditionInformationBuilder withDataType(ConditionDataType dataType) {
    this.dataType = dataType;
    return this;
  }

  /**
   * Gives the condition information an identifier
   * @param identifier The identifier that is used to uniquely identifier condition
   * @return The information builder
   */
  public ConditionInformationBuilder withIdentifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * Is called when information build process is completed
   * @return The condition information
   */
  public ConditionInformation build() {
    return ConditionInformation.create(name, dataType, identifier);
  }
}

