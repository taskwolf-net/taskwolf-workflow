package com.dulno.workflow.trigger;

import com.dulno.core.database.condition.DatabaseCondition;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Trigger {
  /**
   * The type that is used to identify the trigger
   * @return The identifier
   */
  String type();

  /**
   * Is used to collect important information of the trigger and to
   * represent externally
   * @return The information of the trigger
   */
  TriggerInformation information();

  /**
   * Is used to setup the trigger
   * Mainly called when the module where the trigger is located is loaded
   * Used in most cases to set up the content database
   */
  void initialize();

  /**
   * Is called when a new trigger is to be stored
   * @param triggerId The id of the new trigger
   * @param content The content of the new trigger
   * @return A future that is completed when insertion is completed
   */
  CompletableFuture<Void> insert(UUID triggerId, Map<String, Object> content);

  /**
   * Is used to find the content of a stored trigger
   * @param triggerId The id of the trigger
   * @return The future content of the trigger
   */
  CompletableFuture<Map<String, Object>> findContent(UUID triggerId);

  /**
   * Can be used to find a triggers that match a certain condition
   * @param condition The condition to select specific triggers
   * @return The ids of the found triggers
   */
  CompletableFuture<List<UUID>> findEntries(DatabaseCondition condition);

  /**
   * Is called when a trigger should be deleted
   * @param triggerId The trigger id
   * @return A future that is completed when insertion is completed
   */
  CompletableFuture<Void> delete(UUID triggerId);
}
