package com.dulno.workflow;

import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.google.inject.Injector;

@ModuleDescription(name = "workflow", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.HIGH)
public final class WorkflowModule extends Module {
  private Log log;

  public WorkflowModule(Injector injector) {
    super(injector.createChildInjector(WorkflowInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Workflow");
  }

  @Override
  public void disable() {

  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Workflow", "", "",
      ModuleInformation.Type.HIDDEN);
  }
}