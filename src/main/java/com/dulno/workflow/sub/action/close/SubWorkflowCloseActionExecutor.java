package com.dulno.workflow.sub.action.close;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class SubWorkflowCloseActionExecutor implements ActionExecutor {
  private String outputs;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var formattedInformation = Maps.<String, Object>newHashMap();
    for (var entry : information.entrySet()) {
      formattedInformation.put(entry.getKey(),
        entry.getValue().toString().replace("\\", "\\\\")
          .replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f")
          .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t"));
    }
    var dissolve = PlaceholderDissolve.create(formattedInformation);
    outputs = dissolve.dissolve(outputs);
    return ActionResult.futureSuccess(buildInformation(new JSONObject(outputs)));
  }

  private Map<String, Object> buildInformation(JSONObject outputs) {
    var information = Maps.<String, Object>newHashMap();
    for (var key : outputs.keySet()) {
      information.put("sub_workflow_" + key, outputs.get(key));
    }
    return information;
  }
}
