package com.dulno.workflow.trigger;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class TriggerRepository {
  private final List<Trigger> triggers = Lists.newArrayList();

  public void registerTrigger(Trigger trigger) {
    triggers.add(trigger);
  }

  public void unregisterTrigger(Trigger trigger) {
    triggers.remove(trigger);
  }

  public Optional<Trigger> findTrigger(String type) {
    return triggers.stream().filter(trigger -> trigger.type().equals(type)).findFirst();
  }

  public boolean isEmpty() {
    return triggers.isEmpty();
  }

  public List<Trigger> allTriggers() {
    return List.copyOf(triggers);
  }
}
