package com.dulno.workflow;

import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.core.trigger.TriggerRepository;
import com.google.inject.Injector;

@ModuleDescription(name = "sub-workflow", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class SubWorkflowModule extends Module {
  private Log log;
  private AccountLink accountLink;

  public SubWorkflowModule(Injector injector) {
    super(injector.createChildInjector(WorkflowInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Sub Workflow");
    accountLink = SubWorkflowAccountLink.create();
  }

  @Override
  public void disable() {

  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("sub.workflow", "", "sub-workflow",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public TriggerRepository triggerRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var respository = TriggerRepository.create();
    return respository;
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = ActionRepository.create();
    return repository;
  }
}
