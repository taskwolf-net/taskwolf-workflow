package com.dulno.workflow.action;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class ActionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ActionDatabaseTable provideActionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return ActionDatabaseTable.create(connection, keyspace);
  }
}