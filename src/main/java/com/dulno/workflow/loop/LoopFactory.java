package com.dulno.workflow.loop;

import com.dulno.core.bundle.Bundle;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.workflow.loop.type.ItemLoop;
import com.dulno.workflow.loop.type.NumberLoop;
import com.dulno.workflow.loop.type.TextLoop;
import com.dulno.workflow.operation.OperationDatabaseTable;
import com.dulno.workflow.step.WorkflowStepCompound;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class LoopFactory {
  private final OperationDatabaseTable operationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final ErrorRepository errorRepository;

  public Loop create(
    LoopEntry loopEntry,
    Callable<CompletableFuture<List<WorkflowStepCompound>>> stepGenerator,
    Bundle bundle
  ) {
    var json = new JSONObject(loopEntry.content());
    var type = loopEntry.type();
    if (type.equals("loop-number")) {
      return NumberLoop.of(operationDatabaseTable, maintenanceSchedule,
        errorRepository, loopEntry, stepGenerator, bundle, json);
    } else if (type.equals("loop-item")) {
      return ItemLoop.of(operationDatabaseTable, maintenanceSchedule,
        errorRepository, loopEntry, stepGenerator, bundle, json);
    } else if (type.equals("loop-text")) {
      return TextLoop.of(operationDatabaseTable, maintenanceSchedule,
        errorRepository, loopEntry, stepGenerator, bundle, json);
    }
    return null;
  }
}