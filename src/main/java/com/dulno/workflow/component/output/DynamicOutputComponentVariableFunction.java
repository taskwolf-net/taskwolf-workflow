package com.dulno.workflow.component.output;

import org.json.JSONObject;

import java.util.List;

public interface DynamicOutputComponentVariableFunction {
  /**
   * Is called to determine output variables dynamically, later
   * @param currentContent The content of the current component
   * @param previousComponents The previous selected components (sequential)
   * @return The list of output variables
   */
  List<OutputComponentVariable> compile(JSONObject currentContent,
    List<JSONObject> previousComponents);
}