package com.dulno.workflow.loop;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class LoopInformationRepository {
  private final List<LoopInformation> loops = Lists.newArrayList();

  /**
   * Registers a new loop information
   * @param information The information of the loop
   */
  public void register(LoopInformation information) {
    loops.add(information);
  }

  /**
   * Unregisters a loop information
   * @param information The information that is to be unregistered
   */
  public void unregister(LoopInformation information) {
    loops.remove(information);
  }

  /**
   * Is used to find loop information by identifier
   * @param identifier The identifier to locate the information
   * @return The loop information if it could be found
   */
  public Optional<LoopInformation> findByIdentifier(String identifier) {
    return loops.stream().filter(information ->
      information.identifier().equals(identifier)).findFirst();
  }

  /**
   * Is used to find all registered loop information
   * @return The list of loop information
   */
  public List<LoopInformation> findAll() {
    return List.copyOf(loops);
  }
}
