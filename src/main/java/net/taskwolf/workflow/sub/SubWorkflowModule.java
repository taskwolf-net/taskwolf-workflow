package net.taskwolf.workflow.sub;

import net.taskwolf.core.account.AccountLink;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.workflow.WorkflowModule;
import net.taskwolf.workflow.action.ActionDatabaseTable;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.integration.Integration;
import net.taskwolf.workflow.structure.WorkflowDatabaseTable;
import net.taskwolf.workflow.sub.action.call.SubWorkflowCallAction;
import net.taskwolf.workflow.sub.action.close.SubWorkflowCloseAction;
import net.taskwolf.workflow.sub.select.SubWorkflowSelect;
import net.taskwolf.workflow.sub.trigger.SubWorkflowTrigger;
import net.taskwolf.workflow.trigger.TriggerDatabaseTable;
import net.taskwolf.workflow.trigger.TriggerRepository;
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
