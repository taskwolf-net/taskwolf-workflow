package com.dulno.workflow.trigger;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class TriggerInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  TriggerDatabaseTable provideTriggerDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return TriggerDatabaseTable.create(connection, keyspace);
  }
}
