package com.dulno.workflow.loop;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class LoopInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  LoopDatabaseTable provideLoopDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    return LoopDatabaseTable.create(connection, keyspace);
  }

  @Provides
  @Singleton
  LoopInformationRepository provideLoopInformationRepository() {
    return LoopInformationRepository.create();
  }
}
