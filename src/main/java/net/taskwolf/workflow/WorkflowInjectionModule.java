package net.taskwolf.workflow;

import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.module.ModuleLoader;
import net.taskwolf.workflow.operation.OperationDatabaseTable;
import net.taskwolf.workflow.throttle.WorkflowThrottleDatabaseTable;
import net.taskwolf.workflow.timeline.TimelineDatabaseTable;
import net.taskwolf.workflow.action.ActionInjectionModule;
import net.taskwolf.workflow.condition.ConditionInjectionModule;
import net.taskwolf.workflow.loop.LoopInjectionModule;
import net.taskwolf.workflow.structure.WorkflowDatabaseTable;
import net.taskwolf.workflow.trigger.TriggerInjectionModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class WorkflowInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(TriggerInjectionModule.create());
    install(ActionInjectionModule.create());
    install(ConditionInjectionModule.create());
    install(LoopInjectionModule.create());
  }

  @Provides
  @Singleton
  WorkflowModule provideWorkflowModule(ModuleLoader moduleLoader) {
    return (WorkflowModule) moduleLoader.findModule("workflow").get();
  }

  @Provides
  @Singleton
  WorkflowDatabaseTable provideWorkflowDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return WorkflowDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  TimelineDatabaseTable provideTimelineDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return TimelineDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  OperationDatabaseTable provideOperationDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var operationDatabaseTable = OperationDatabaseTable.create(connection,
      keyspace);
    operationDatabaseTable.createIfNotExists();
    return operationDatabaseTable;
  }

  @Provides
  @Singleton
  WorkflowThrottleDatabaseTable provideWorkflowThrottleDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var workflowThrottleDatabaseTable = WorkflowThrottleDatabaseTable.create(
      connection, keyspace);
    workflowThrottleDatabaseTable.createIfNotExists();
    return workflowThrottleDatabaseTable;
  }
}
