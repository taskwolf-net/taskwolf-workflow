package net.taskwolf.workflow.condition.text;

import net.taskwolf.workflow.condition.Condition;
import net.taskwolf.workflow.condition.ConditionDataType;
import net.taskwolf.workflow.condition.ConditionInformation;
import net.taskwolf.workflow.step.WorkflowStepResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ConditionTextEndsWith extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.text.ends.with")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("condition-text-ends-with").build();
  }

  public static ConditionTextEndsWith create(
    String inputValue, String comparativeValue
  ) {
    return new ConditionTextEndsWith(inputValue, comparativeValue);
  }

  private ConditionTextEndsWith(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    dissolve(information);
    return CompletableFuture.completedFuture(WorkflowStepResult.success(
      inputValue().endsWith(comparativeValue())));
  }
}
