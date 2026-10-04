package net.taskwolf.workflow.condition.number;

import net.taskwolf.workflow.condition.Condition;
import net.taskwolf.workflow.condition.ConditionDataType;
import net.taskwolf.workflow.condition.ConditionInformation;
import net.taskwolf.workflow.step.WorkflowStepResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ConditionNumberSmallerThan extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.number.smaller.than")
      .withDataType(ConditionDataType.NUMBER)
      .withIdentifier("condition-number-is-smaller").build();
  }

  public static ConditionNumberSmallerThan create(
    String inputValue, String comparativeValue
  ) {
    return new ConditionNumberSmallerThan(inputValue, comparativeValue);
  }

  private ConditionNumberSmallerThan(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    dissolve(information);
    if (!inputValue().matches("-?\\d+(\\.\\d+)?") ||
      !comparativeValue().matches("-?\\d+(\\.\\d+)?")
    ) {
      return CompletableFuture.completedFuture(WorkflowStepResult.failure(
        "condition.not.a.number"));
    }
    return CompletableFuture.completedFuture(WorkflowStepResult.success(
      Integer.parseInt(inputValue()) < Integer.parseInt(comparativeValue())));
  }
}
