package net.taskwolf.workflow.component.input;

import net.taskwolf.workflow.component.ComponentVariable;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class InputComponentVariable extends ComponentVariable {
  public static InputComponentVariable createSelect(
    String displayName, String identifier, String description,
    InputComponentSelect select
  ) {
    return SelectableInputComponentVariable.create(displayName, identifier,
      description, select);
  }

  public static InputComponentVariable createDynamic(
    String identifier, List<String> requiredPredecessor,
    DynamicInputComponentVariableFunction variableFunction
  ) {
    return DynamicInputComponentVariable.create(identifier, requiredPredecessor,
      variableFunction);
  }

  public static InputComponentVariable createRequired(
    String displayName, String identifier, String description,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      displayName, dataType, InputComponentType.REQUIRED);
  }

  public static InputComponentVariable createRequired(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      placeholder, dataType, InputComponentType.REQUIRED);
  }

  public static InputComponentVariable createOptional(
    String displayName, String identifier, String description,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      displayName, dataType, InputComponentType.OPTIONAL);
  }

  public static InputComponentVariable createOptional(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      placeholder, dataType, InputComponentType.OPTIONAL);
  }

  public static InputComponentVariable create(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType, InputComponentType type
  ) {
    return new InputComponentVariable(displayName, identifier, description,
      placeholder, dataType, type);
  }

  private final String description;
  private final String placeholder;
  private final InputComponentDataType dataType;
  private final InputComponentType type;

  protected InputComponentVariable(
    String displayName, String identifier, String description, String placeholder,
    InputComponentDataType dataType, InputComponentType type
  ) {
    super(displayName, identifier);
    this.description = description;
    this.placeholder = placeholder;
    this.dataType = dataType;
    this.type = type;
  }
}
