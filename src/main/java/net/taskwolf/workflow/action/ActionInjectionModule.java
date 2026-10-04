package net.taskwolf.workflow.action;

import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
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