package com.dulno.workflow.integration;

import com.dulno.core.module.Module;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionRepository;
import com.dulno.workflow.trigger.Trigger;
import com.dulno.workflow.trigger.TriggerRepository;
import com.google.inject.Injector;

public abstract class Integration extends Module {
  protected Integration(Injector injector) {
    super(injector);
  }

  @Override
  public void postEnable() throws Exception {
    triggerRepository().allTriggers().forEach(Trigger::initialize);
    actionRepository().allActions().forEach(Action::initialize);
  }

  /**
   * Is used to store the triggers of the module
   * @return The trigger repository
   */
  public TriggerRepository triggerRepository() {
    return TriggerRepository.create();
  }

  /**
   * Is used to store the actions of the module
   * @return The action repository
   */
  public ActionRepository actionRepository() {
    return ActionRepository.create();
  }
}
