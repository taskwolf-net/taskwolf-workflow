package com.dulno.workflow.condition;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class ConditionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ConditionDatabaseTable provideConditionDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return ConditionDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  ConditionInformationRepository provideConditionInformationRepository() {
    return ConditionInformationRepository.create();
  }
}
