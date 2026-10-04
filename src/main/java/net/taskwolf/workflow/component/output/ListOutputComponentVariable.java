package net.taskwolf.workflow.component.output;

import com.google.common.collect.Lists;

import java.util.List;

public final class ListOutputComponentVariable extends OutputComponentVariable {
  public static ListOutputComponentVariable create(
    String displayName, String identifier, OutputComponentVariable... list
  ) {
    return new ListOutputComponentVariable(displayName, identifier,
      Lists.newArrayList(list));
  }

  private final List<OutputComponentVariable> list;

  private ListOutputComponentVariable(
    String displayName, String identifier, List<OutputComponentVariable> list
  ) {
    super(displayName, identifier);
    this.list = list;
  }

  public List<OutputComponentVariable> list() {
    return List.copyOf(list);
  }
}