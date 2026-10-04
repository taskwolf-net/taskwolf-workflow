package net.taskwolf.workflow.action;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class ActionRepository {
  private final List<Action<? extends ActionExecutor>> actions = Lists.newArrayList();

  public void registerAction(Action<? extends ActionExecutor> action) {
    actions.add(action);
  }

  public void unregisterAction(Action<? extends ActionExecutor> action) {
    actions.remove(action);
  }

  public Optional<Action<? extends ActionExecutor>> findAction(String type) {
    return actions.stream().filter(action -> action.type().equals(type)).findFirst();
  }

  public boolean isEmpty() {
    return actions.isEmpty();
  }

  public List<Action<? extends ActionExecutor>> allActions() {
    return List.copyOf(actions);
  }
}