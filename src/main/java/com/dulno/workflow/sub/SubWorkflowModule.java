package com.dulno.workflow.sub;

import com.dulno.core.account.AccountLink;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.workflow.WorkflowModule;
import com.dulno.workflow.action.ActionDatabaseTable;
import com.dulno.workflow.action.ActionRepository;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.integration.Integration;
import com.dulno.workflow.structure.WorkflowDatabaseTable;
import com.dulno.workflow.sub.action.call.SubWorkflowCallAction;
import com.dulno.workflow.sub.action.close.SubWorkflowCloseAction;
import com.dulno.workflow.sub.select.SubWorkflowSelect;
import com.dulno.workflow.sub.trigger.SubWorkflowTrigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.dulno.workflow.trigger.TriggerRepository;
import com.google.inject.Injector;

@ModuleDescription(name = "sub-workflow", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class SubWorkflowModule extends Integration {
  private Log log;
  private DatabaseConnection databaseConnection;
  private DatabaseKeyspace databaseKeyspace;
  private TriggerDatabaseTable triggerDatabaseTable;
  private ActionDatabaseTable actionDatabaseTable;
  private WorkflowDatabaseTable workflowDatabaseTable;
  private SubWorkflowTrigger subWorkflowTrigger;
  private AccountLink accountLink;
  private InputComponentSelect subWorkflowSelect;

  public SubWorkflowModule(Injector injector) {
    super(injector);
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Sub Workflow");
    accountLink = SubWorkflowAccountLink.create();
    databaseConnection = injector().getInstance(DatabaseConnection.class);
    databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    triggerDatabaseTable = injector().getInstance(TriggerDatabaseTable.class);
    actionDatabaseTable = injector().getInstance(ActionDatabaseTable.class);
    workflowDatabaseTable = injector().getInstance(WorkflowDatabaseTable.class);
    subWorkflowSelect = SubWorkflowSelect.create(triggerDatabaseTable,
      workflowDatabaseTable);
    subWorkflowTrigger = SubWorkflowTrigger.create(databaseConnection,
      databaseKeyspace);
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
    var repository = TriggerRepository.create();
    repository.registerTrigger(subWorkflowTrigger);
    return repository;
  }

  @Override
  public ActionRepository actionRepository() {
    var repository = ActionRepository.create();
    var closeAction = SubWorkflowCloseAction.create(databaseConnection,
      databaseKeyspace);
    repository.registerAction(SubWorkflowCallAction.create(triggerDatabaseTable,
      actionDatabaseTable, subWorkflowTrigger, closeAction,
      injector().getInstance(WorkflowModule.class), subWorkflowSelect,
      databaseConnection, databaseKeyspace));
    repository.registerAction(closeAction);
    return repository;
  }
}
