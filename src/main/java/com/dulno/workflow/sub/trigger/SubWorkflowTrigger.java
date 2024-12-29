package com.dulno.workflow.sub.trigger;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.DynamicOutputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.dulno.workflow.trigger.Trigger;
import com.dulno.workflow.trigger.TriggerContentDatabaseTable;
import com.dulno.workflow.trigger.TriggerInformation;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class SubWorkflowTrigger implements Trigger {
  public static SubWorkflowTrigger create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("inputs", DatabaseDataType.TEXT));
    return new SubWorkflowTrigger(TriggerContentDatabaseTable.create(
      databaseConnection, databaseKeyspace, "trigger_sub_workflow", contentColumns));
  }

  private final TriggerContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "sub-workflow-trigger";
  }

  @Override
  public TriggerInformation information() {
    return TriggerInformation.builder()
      .withName("sub.workflow.trigger.name")
      .withDescription("sub.workflow.trigger.description")
      .withInputVariable(InputComponentVariable.createRequired("sub.workflow.trigger.input.inputs.name",
        "inputs", "sub.workflow.trigger.input.inputs.description", InputComponentDataType.LIST))
      .withOutputVariable(DynamicOutputComponentVariable.create(
        (currentContent, previousActions) -> CompletableFuture.completedFuture(
          findSubWorkflowInputs(currentContent))))
      .build();
  }

  private List<OutputComponentVariable> findSubWorkflowInputs(
    JSONObject triggerContent
  ) {
    try {
      var inputs = new JSONArray(triggerContent.getString("inputs"));
      return inputs.toList().stream()
        .map(entry -> (String) entry)
        .filter(entry -> !entry.isEmpty() && !entry.isBlank())
        .map(entry -> OutputComponentVariable.create(entry,
          "sub_workflow_" + entry))
        .toList();
    } catch (Exception exception) {
      return Lists.newArrayList();
    }
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(triggerId,
      DatabaseRow.of(content.get("inputs")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("inputs", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition) {
    return contentDatabaseTable.findContentByCondition(condition).thenApply(
      rows -> rows.stream().map(row -> row.findCell(0).uuidValue()).toList());
  }

  @Override
  public CompletableFuture<Void> delete(UUID triggerId) {
    return contentDatabaseTable.deleteContent(triggerId);
  }
}

