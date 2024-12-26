package com.dulno.workflow.component.input;

import com.dulno.core.user.User;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DynamicInputComponentVariableFunction {
  /**
   * Is used to add inputs at selection time
   * @param user The user that sent the request
   * @param target The id of the user / organization (target)
   * @param content The content of the current component
   * @return The list of input variables
   */
  CompletableFuture<List<InputComponentVariable>> compile(User user,
    UUID target, JSONObject content);
}