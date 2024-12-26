package com.dulno.workflow.action;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Action<T extends ActionExecutor> {
  /**
   * The type that is used to identify the action
   * @return The identifier
   */
  String type();

  /**
   * Is used to collect important information of the action and to
   * represent externally
   * @return The information of the action
   */
  ActionInformation information();

  /**
   * Is used to setup the action
   * Mainly called when the module where the action is located is loaded
   * Used in most cases to set up the content database
   */
  void initialize();

  /**
   * Is called when a new action is to be stored
   * @param actionId The id of the new action
   * @param content The content of the new action
   * @return A future that is completed when insertion is completed
   */
  CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content);

  /**
   * Is used to find the content of a stored action
   * @param actionId The action id
   * @return The future content of the action
   */
  CompletableFuture<Map<String, Object>> findContent(UUID actionId);

  /**
   * Is used to build an {@link ActionExecutor}
   * @param actionId The id of the action for which an executor is to be build
   * @return The ActionExecutor
   */
  CompletableFuture<T> build(UUID actionId);

  /**
   * Is called when a action should be deleted
   * @param actionId The id of the action
   * @return A future that is completed when insertion is completed
   */
  CompletableFuture<Void> delete(UUID actionId);
}
