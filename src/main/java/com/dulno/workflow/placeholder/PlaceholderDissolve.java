package com.dulno.workflow.placeholder;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class PlaceholderDissolve {
  private final Map<String, Object> context;

  /**
   * Used by triggers / actions to resolve their placeholders ("%...%")
   * @param value The text from which the placeholders are to be resolved
   * @return The finished text (with resolved placeholders)
   */
  public String dissolve(String value) {
    for (var entry : context.entrySet()) {
      value = value.replace("%" + entry.getKey() + "%", entry.getValue().toString());
    }
    return value;
  }
}
