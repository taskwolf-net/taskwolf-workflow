package net.taskwolf.workflow.component.output;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DynamicOutputComponentVariableFunction {
  /**
   * Is called to determine output variables dynamically, later
   * @param currentContent The content of the current component
   * @param previousComponents The previous selected components (sequential)
   * @return A future that contains the list of output variables
   */
  CompletableFuture<List<OutputComponentVariable>> compile(JSONObject currentContent,
    List<JSONObject> previousComponents);
}