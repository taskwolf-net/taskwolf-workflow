package net.taskwolf.workflow.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ConditionInformation {
  public static ConditionInformationBuilder builder() {
    return ConditionInformationBuilder.create();
  }

  private final String name;
  private final ConditionDataType dataType;
  private final String identifier;
}
