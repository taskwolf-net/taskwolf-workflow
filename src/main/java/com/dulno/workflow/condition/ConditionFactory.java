package com.dulno.workflow.condition;

import com.dulno.workflow.condition.number.ConditionNumberGreaterThan;
import com.dulno.workflow.condition.number.ConditionNumberSmallerThan;
import com.dulno.workflow.condition.text.ConditionTextEndsWith;
import com.dulno.workflow.condition.text.ConditionTextEquals;
import com.dulno.workflow.condition.text.ConditionTextStartsWith;
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
    if (type.equals("condition-text-equals")) {
      return ConditionTextEquals.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-text-starts-with")) {
      return ConditionTextStartsWith.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-text-ends-with")) {
      return ConditionTextEndsWith.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-number-is-greater")) {
      return ConditionNumberGreaterThan.create(inputValue, comparativeValue);
    }
    if (type.equals("condition-number-is-smaller")) {
      return ConditionNumberSmallerThan.create(inputValue, comparativeValue);
    }
    return null;
  }
}