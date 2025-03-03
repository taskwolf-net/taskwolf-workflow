package com.dulno.workflow.loop.type;

import com.dulno.core.bundle.Bundle;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
import com.dulno.workflow.loop.Loop;
import com.dulno.workflow.loop.LoopEntry;
import com.dulno.workflow.loop.LoopInformation;
import com.dulno.workflow.operation.OperationDatabaseTable;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.dulno.workflow.step.WorkflowStepCompound;
import com.dulno.workflow.step.WorkflowStepResult;
import com.google.common.collect.Maps;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public final class TextLoop extends Loop {
  public static LoopInformation information() {
    return LoopInformation.builder()
      .withName("loop.text.name")
      .withDescription("loop.text.description")
      .withIdentifier("loop-text")
      .withInputVariable(InputComponentVariable.createRequired("loop.text.input.text.name",
        "loopText", "loop.text.input.text.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("loop.text.input.divider.name",
        "loopDivider", "loop.text.input.divider.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("loop.text.input.limit.name",
        "loopLimit", "loop.text.input.limit.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("loop.text.output.section", "loopSection"))
      .withOutputVariable(OutputComponentVariable.create("loop.text.output.index", "loopIndex"))
      .withOutputVariable(OutputComponentVariable.create("loop.text.output.iterations", "loopIterations"))
      .withOutputVariable(OutputComponentVariable.create("loop.text.output.text", "loopText"))
      .withOutputVariable(OutputComponentVariable.create("loop.text.output.divider", "loopDivider"))
      .build();
  }

  public static TextLoop of(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, JSONObject content
  ) {
    return create(operationDatabaseTable, maintenanceSchedule, errorRepository,
      loopEntry, stepGenerator, bundle, content.getString("loopText"),
      content.getString("loopDivider"), content.has("loopLimit") ?
        Optional.of(content.getString("loopLimit")) : Optional.empty());
  }

  public static TextLoop create(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, String text, String divider, Optional<String> limit
  ) {
    return new TextLoop(operationDatabaseTable, maintenanceSchedule,
      errorRepository, loopEntry, stepGenerator, bundle, text, divider, limit);
  }

  private String text;
  private String divider;
  private Optional<String> limit;

  private TextLoop(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, String text, String divider, Optional<String> limit
  ) {
    super(operationDatabaseTable, maintenanceSchedule, errorRepository, loopEntry,
      stepGenerator, bundle);
    this.text = text;
    this.divider = divider;
    this.limit = limit;
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    try {
      var placeholderDissolve = PlaceholderDissolve.create(information);
      text = placeholderDissolve.dissolve(text);
      divider = placeholderDissolve.dissolve(divider);
      if (limit.isPresent()) {
        limit = Optional.of(placeholderDissolve.dissolve(limit.get()));
      }
      var limit = this.limit.map(Integer::parseInt);
      return loopAsynchronously(limit, information);
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("loop.text.failure.wrong.format"));
    }
  }

  private CompletableFuture<WorkflowStepResult> loopAsynchronously(
    Optional<Integer> limit, Map<String, Object> information
  ) {
    var futureResponse = new CompletableFuture<WorkflowStepResult>();
    new Thread(() -> futureResponse.complete(
      loopSynchronously(limit, information))).start();
    return futureResponse;
  }

  private WorkflowStepResult loopSynchronously(
    Optional<Integer> limit, Map<String, Object> information
  ) {
    var parts = text.split(divider);
    var iterations = limit.map(value -> Math.min(parts.length, value))
      .orElseGet(() -> parts.length);
    for (var i = 0; i < iterations; i++) {
      information.putAll(prepareInformation("step" + loopEntry().index(),
        createIterationInformation(parts[i], i + 1, iterations)));
      var iterationResult = iterate(information).join();
      if (iterationResult.isFailure()) {
        return iterationResult;
      }
    }
    return WorkflowStepResult.success();
  }

  private Map<String, Object> createIterationInformation(
    String section, int index, int iterations
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("loopSection", section);
    information.put("loopIndex", index);
    information.put("loopIterations", iterations);
    information.put("loopText", text);
    information.put("loopDivider", divider);
    return information;
  }
}