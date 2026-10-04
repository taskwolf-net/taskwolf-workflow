package net.taskwolf.workflow.component.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class InputComponentSelectEntry {
  private final String identifier;
  private final String name;
}
