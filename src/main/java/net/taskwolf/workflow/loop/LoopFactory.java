package net.taskwolf.workflow.loop;

import net.taskwolf.core.bundle.Bundle;
import net.taskwolf.core.error.ErrorRepository;
import net.taskwolf.core.maintenance.MaintenanceSchedule;
import net.taskwolf.workflow.loop.type.ItemLoop;
import net.taskwolf.workflow.loop.type.NumberLoop;
import net.taskwolf.workflow.loop.type.TextLoop;
import net.taskwolf.workflow.operation.OperationDatabaseTable;
import net.taskwolf.workflow.step.WorkflowStepCompound;
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