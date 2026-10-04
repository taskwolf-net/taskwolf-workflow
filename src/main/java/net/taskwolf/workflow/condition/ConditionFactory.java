package net.taskwolf.workflow.condition;

import net.taskwolf.workflow.condition.number.ConditionNumberGreaterThan;
import net.taskwolf.workflow.condition.number.ConditionNumberSmallerThan;
import net.taskwolf.workflow.condition.text.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ConditionFactory {
  public Condition create(String type, String content) {
    var json = new JSONObject(content);
    var inputValue = json.getString("inputValue");
    var comparativeValue = json.getString("comparativeValue");
    return switch (type) {
      case "condition-text-equals" ->
        ConditionTextEquals.create(inputValue, comparativeValue);
      case "condition-text-not-equals" ->
        ConditionTextNotEquals.create(inputValue, comparativeValue);
      case "condition-text-contains" ->
        ConditionTextContains.create(inputValue, comparativeValue);
      case "condition-text-starts-with" ->
        ConditionTextStartsWith.create(inputValue, comparativeValue);
      case "condition-text-ends-with" ->
        ConditionTextEndsWith.create(inputValue, comparativeValue);
      case "condition-number-is-greater" ->
        ConditionNumberGreaterThan.create(inputValue, comparativeValue);
      case "condition-number-is-smaller" ->
        ConditionNumberSmallerThan.create(inputValue, comparativeValue);
      default -> null;
    };
  }
}