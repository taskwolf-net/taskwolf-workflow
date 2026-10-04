package net.taskwolf.workflow.loop.type;

import net.taskwolf.core.bundle.Bundle;
import net.taskwolf.core.error.ErrorRepository;
import net.taskwolf.core.maintenance.MaintenanceSchedule;
import net.taskwolf.workflow.WorkflowModule;
import net.taskwolf.workflow.component.ComponentInformation;
import net.taskwolf.workflow.component.ComponentVariable;
import net.taskwolf.workflow.component.input.InputComponentDataType;
import net.taskwolf.workflow.component.input.InputComponentVariable;
import net.taskwolf.workflow.component.output.DynamicOutputComponentVariable;
import net.taskwolf.workflow.component.output.ListOutputComponentVariable;
import net.taskwolf.workflow.component.output.OutputComponentVariable;
import net.taskwolf.workflow.action.Action;
import net.taskwolf.workflow.loop.Loop;
import net.taskwolf.workflow.loop.LoopEntry;
import net.taskwolf.workflow.loop.LoopInformation;
import net.taskwolf.workflow.operation.OperationDatabaseTable;
import net.taskwolf.workflow.placeholder.PlaceholderDissolve;
import net.taskwolf.workflow.step.WorkflowStepCompound;
import net.taskwolf.workflow.step.WorkflowStepResult;
import net.taskwolf.workflow.trigger.Trigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class ItemLoop extends Loop {
  public static LoopInformation information(WorkflowModule workflowModule) {
    return LoopInformation.builder()
      .withName("loop.item.name")
      .withDescription("loop.item.description")
      .withIdentifier("loop-item")
      .withInputVariable(InputComponentVariable.createRequired("loop.item.input.list.name",
        "loopList", "loop.item.input.list.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("loop.item.input.limit.name",
        "loopLimit", "loop.item.input.limit.description", InputComponentDataType.TEXT))
      .withOutputVariable(DynamicOutputComponentVariable.create(
        (currentContent, previousActions) -> CompletableFuture.completedFuture(
          findListOutputs(currentContent, previousActions, workflowModule))))
      .withOutputVariable(OutputComponentVariable.create("loop.item.output.index", "loopIndex"))
      .withOutputVariable(OutputComponentVariable.create("loop.item.output.iterations", "loopIterations"))
      .withOutputVariable(OutputComponentVariable.create("loop.item.output.list", "loopList"))
      .build();
  }

  private static List<OutputComponentVariable> findListOutputs(
    JSONObject loopContent, List<JSONObject> previousComponents,
    WorkflowModule workflowModule
  ) {
    try {
      if (previousComponents.isEmpty()) {
        return Lists.newArrayList();
      }
      var loopList = loopContent.getString("loopList").trim();
      var percentageCount = loopList.length() - loopList.replace("%", "").length();
      if (percentageCount != 2 || !loopList.startsWith("%") || !loopList.endsWith("%")) {
        return Lists.newArrayList();
      }
      var loopListPlaceholder = loopList.replace("%", "").split("-");
      var kind = loopListPlaceholder[0];
      var placeholderId = loopListPlaceholder[1];
      var outputs = Optional.<List<OutputComponentVariable>>empty();
      if (kind.equalsIgnoreCase("trigger")) {
        outputs = findListVariableOutputs(placeholderId,
          previousComponents.get(0), true, workflowModule);
      } else {
        outputs = findListVariableOutputs(placeholderId,
          previousComponents.get(Integer.parseInt(kind.replace("step", "")) + 1),
          false, workflowModule);
      }
      return outputs.orElse(Lists.newArrayList());
    } catch (Exception exception) {
      return Lists.newArrayList();
    }
  }

  private static Optional<List<OutputComponentVariable>> findListVariableOutputs(
    String loopListPlaceholder, JSONObject rawComponent, boolean isTrigger,
    WorkflowModule workflowModule
  ) {
    var outputs = findComponentOutputs(rawComponent, isTrigger, workflowModule);
    Predicate<ComponentVariable> outputCondition =
      output -> output.identifier().equals(loopListPlaceholder);
    if (outputs.stream().noneMatch(outputCondition)) {
      return Optional.empty();
    }
    var variable = outputs.stream().filter(outputCondition).findFirst().get();
    if (variable instanceof ListOutputComponentVariable listVariable) {
      return Optional.of(listVariable.list());
    }
    return Optional.empty();
  }

  private static List<OutputComponentVariable> findComponentOutputs(
    JSONObject rawComponent, boolean isTrigger, WorkflowModule workflowModule
  ) {
    Optional<ComponentInformation> information;
    if (isTrigger) {
      information = workflowModule.findTrigger(rawComponent.getString("module"),
        rawComponent.getString("type")).map(Trigger::information);
    } else {
      information = workflowModule.findAction(rawComponent.getString("module"),
        rawComponent.getString("type")).map(Action::information);
    }
    return information.map(ComponentInformation::outputVariables)
      .orElse(Lists.newArrayList());
  }

  public static ItemLoop of(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, JSONObject content
  ) {
    return create(operationDatabaseTable, maintenanceSchedule, errorRepository,
      loopEntry, stepGenerator, bundle, content.getString("loopList"),
      content.has("loopLimit") ? Optional.of(content.getString("loopLimit")) :
        Optional.empty());
  }

  public static ItemLoop create(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, String list, Optional<String> limit
  ) {
    return new ItemLoop(operationDatabaseTable, maintenanceSchedule,
      errorRepository, loopEntry, stepGenerator, bundle, list, limit);
  }

  private String list;
  private Optional<String> limit;

  private ItemLoop(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, String list, Optional<String> limit
  ) {
    super(operationDatabaseTable, maintenanceSchedule, errorRepository, loopEntry,
      stepGenerator, bundle);
    this.list = list;
    this.limit = limit;
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    try {
      var placeholderDissolve = PlaceholderDissolve.create(information);
      list = placeholderDissolve.dissolve(list);
      var list = new JSONArray(this.list).toList().stream()
        .map(value -> (Map<String, Object>) value).toList();
      if (limit.isPresent()) {
        limit = Optional.of(placeholderDissolve.dissolve(limit.get()));
      }
      var limit = this.limit.map(Integer::parseInt);
      return loopAsynchronously(list, limit, information);
    } catch (NumberFormatException exception) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("loop.item.failure.wrong.format.limit"));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("loop.item.failure.wrong.format.list"));
    }
  }

  private CompletableFuture<WorkflowStepResult> loopAsynchronously(
    List<Map<String, Object>> list, Optional<Integer> limit,
    Map<String, Object> information
  ) {
    var futureResponse = new CompletableFuture<WorkflowStepResult>();
    new Thread(() -> futureResponse.complete(
      loopSynchronously(list, limit, information))).start();
    return futureResponse;
  }

  private WorkflowStepResult loopSynchronously(
    List<Map<String, Object>> list, Optional<Integer> limit,
    Map<String, Object> information
  ) {
    var iterations = limit.map(value -> Math.min(list.size(), value))
      .orElseGet(list::size);
    for (var i = 0; i < iterations; i++) {
      information.putAll(prepareInformation("step" + loopEntry().index(),
        createIterationInformation(list.get(i), i + 1, iterations)));
      var iterationResult = iterate(information).join();
      if (iterationResult.isFailure()) {
        return iterationResult;
      }
    }
    return WorkflowStepResult.success();
  }

  private Map<String, Object> createIterationInformation(
    Map<String, Object> item, int index, int iterations
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.putAll(item);
    information.put("loopIndex", index);
    information.put("loopIterations", iterations);
    information.put("loopList", list);
    return information;
  }
}