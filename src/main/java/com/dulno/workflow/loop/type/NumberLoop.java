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

public final class NumberLoop extends Loop {
  public static LoopInformation information() {
    return LoopInformation.builder()
      .withName("loop.number.name")
      .withDescription("loop.number.description")
      .withIdentifier("loop-number")
      .withInputVariable(InputComponentVariable.createRequired("loop.number.input.start.name",
        "loopStart", "loop.number.input.start.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("loop.number.input.end.name",
        "loopEnd", "loop.number.input.end.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("loop.number.input.limit.name",
        "loopLimit", "loop.number.input.limit.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("loop.number.output.index", "loopIndex"))
      .withOutputVariable(OutputComponentVariable.create("loop.number.output.start", "loopStart"))
      .withOutputVariable(OutputComponentVariable.create("loop.number.output.end", "loopEnd"))
      .build();
  }

  public static NumberLoop of(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, JSONObject content
  ) {
    return create(operationDatabaseTable, maintenanceSchedule, errorRepository,
      loopEntry, stepGenerator, bundle, content.getString("loopStart"),
      content.getString("loopEnd"), content.has("loopLimit") ?
        Optional.of(content.getString("loopLimit")) : Optional.empty());
  }

  public static NumberLoop create(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, String start, String end, Optional<String> limit
  ) {
    return new NumberLoop(operationDatabaseTable, maintenanceSchedule,
      errorRepository, loopEntry, stepGenerator, bundle, start, end, limit);
  }

  private String start;
  private String end;
  private Optional<String> limit;

  private NumberLoop(
    OperationDatabaseTable operationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, ErrorRepository errorRepository,
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle, String start, String end, Optional<String> limit
  ) {
    super(operationDatabaseTable, maintenanceSchedule, errorRepository, loopEntry,
      stepGenerator, bundle);
    this.start = start;
    this.end = end;
    this.limit = limit;
  }

  @Override
  public CompletableFuture<WorkflowStepResult> execute(
    Map<String, Object> information
  ) {
    try {
      var placeholderDissolve = PlaceholderDissolve.create(information);
      start = placeholderDissolve.dissolve(start);
      var start = Integer.parseInt(this.start);
      end = placeholderDissolve.dissolve(end);
      var end = Integer.parseInt(this.end);
      if (limit.isPresent()) {
        limit = Optional.of(placeholderDissolve.dissolve(limit.get()));
      }
      var limit = this.limit.map(Integer::parseInt).orElseGet(() -> end - start);
      return loopAsynchronously(start, end, limit, information);
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(
        WorkflowStepResult.failure("loop.number.failure.wrong.format"));
    }
  }

  private CompletableFuture<WorkflowStepResult> loopAsynchronously(
    int start, int end, int limit, Map<String, Object> information
  ) {
    var futureResponse = new CompletableFuture<WorkflowStepResult>();
    new Thread(() -> futureResponse.complete(
      loopSynchronously(start, end, limit, information))).start();
    return futureResponse;
  }

  private WorkflowStepResult loopSynchronously(
    int start, int end, int limit, Map<String, Object> information
  ) {
    for (var i = start; i < Math.min(end, start + limit); i++) {
      information.putAll(prepareInformation("step" + loopEntry().index(),
        createIterationInformation(start, end, limit, i)));
      var iterationResult = iterate(information).join();
      if (iterationResult.isFailure()) {
        return iterationResult;
      }
    }
    return WorkflowStepResult.success();
  }

  private Map<String, Object> createIterationInformation(
    int start, int end, int limit, int index
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("loopIndex", index);
    information.put("loopStart", start);
    information.put("loopEnd", Math.min(end, start + limit));
    return information;
  }
}
