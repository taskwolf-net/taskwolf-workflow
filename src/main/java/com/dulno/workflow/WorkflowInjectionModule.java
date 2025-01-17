package com.dulno.workflow;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.module.ModuleLoader;
import com.dulno.workflow.operation.OperationDatabaseTable;
import com.dulno.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.workflow.timeline.TimelineDatabaseTable;
import com.dulno.workflow.action.ActionInjectionModule;
import com.dulno.workflow.condition.ConditionInjectionModule;
import com.dulno.workflow.loop.LoopInjectionModule;
import com.dulno.workflow.structure.WorkflowDatabaseTable;
import com.dulno.workflow.trigger.TriggerInjectionModule;
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
