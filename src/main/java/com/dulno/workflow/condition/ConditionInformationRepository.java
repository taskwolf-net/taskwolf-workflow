package com.dulno.workflow.condition;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class ConditionInformationRepository {
  private final List<ConditionInformation> conditions = Lists.newArrayList();

  /**
   * Registers a new condition information
   * @param information The information of the condition
   */
  public void register(ConditionInformation information) {
    conditions.add(information);
  }

  /**
   * Unregisters a condition information
   * @param information The information that is to be unregistered
   */
  public void unregister(ConditionInformation information) {
    conditions.remove(information);
  }

  /**
   * Is used to find condition information by identifier
   * @param identifier The identifier to locate the information
   * @return The condition information if it could be found
   */
  public Optional<ConditionInformation> findByIdentifier(String identifier) {
    return conditions.stream().filter(information ->
      information.identifier().equals(identifier)).findFirst();
  }

  /**
   * Is used to find all registered condition information
   * @return The list of condition information
   */
  public List<ConditionInformation> findAll() {
    return List.copyOf(conditions);
  }
}
