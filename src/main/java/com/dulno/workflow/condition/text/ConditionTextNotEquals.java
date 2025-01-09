package com.dulno.workflow.condition.text;

import com.dulno.workflow.condition.Condition;
import com.dulno.workflow.condition.ConditionDataType;
import com.dulno.workflow.condition.ConditionInformation;
import com.dulno.workflow.step.WorkflowStepResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class ConditionTextNotEquals extends Condition {
  public static ConditionInformation information() {
    return ConditionInformation.builder()
      .withName("condition.text.not.equals")
      .withDataType(ConditionDataType.TEXT)
      .withIdentifier("condition-text-not-equals").build();
  }

  public static ConditionTextNotEquals create(
    String inputValue, String comparativeValue
  ) {
    return new ConditionTextNotEquals(inputValue, comparativeValue);
  }

  private ConditionTextNotEquals(String inputValue, String comparativeValue) {
    super(inputValue, comparativeValue);
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    dissolve(information);
    return CompletableFuture.completedFuture(WorkflowStepResult.success(
      !inputValue().equals(comparativeValue())));
  }
}
