package com.dulno.workflow;

import com.dulno.core.database.condition.DatabaseCondition;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.*;
import com.dulno.core.worker.WorkerDistribution;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.workflow.condition.ConditionInformationRepository;
import com.dulno.workflow.condition.number.ConditionNumberGreaterThan;
import com.dulno.workflow.condition.number.ConditionNumberSmallerThan;
import com.dulno.workflow.condition.text.ConditionTextEndsWith;
import com.dulno.workflow.condition.text.ConditionTextEquals;
import com.dulno.workflow.condition.text.ConditionTextStartsWith;
import com.dulno.workflow.integration.Integration;
import com.dulno.workflow.loop.LoopInformationRepository;
import com.dulno.workflow.loop.type.ItemLoop;
import com.dulno.workflow.loop.type.NumberLoop;
import com.dulno.workflow.loop.type.TextLoop;
import com.dulno.workflow.structure.Workflow;
import com.dulno.workflow.structure.WorkflowDatabaseTable;
import com.dulno.workflow.structure.WorkflowEntry;
import com.dulno.workflow.structure.WorkflowFactory;
import com.dulno.workflow.trigger.Trigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.dulno.workflow.trigger.TriggerEntry;
import com.dulno.workflow.trigger.TriggerInformation;
import com.google.inject.Injector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ModuleDescription(name = "workflow", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.HIGH)
public final class WorkflowModule extends Module {
  private Log log;
  private ModuleLoader moduleLoader;
  private TriggerDatabaseTable triggerDatabaseTable;
  private WorkflowDatabaseTable workflowDatabaseTable;
  private WorkerDistribution distribution;
  private WorkflowFactory workflowFactory;

  public WorkflowModule(Injector injector) {
    super(injector.createChildInjector(WorkflowInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Workflow");
    moduleLoader = injector().getInstance(ModuleLoader.class);
    triggerDatabaseTable = injector().getInstance(TriggerDatabaseTable.class);
    workflowDatabaseTable = injector().getInstance(WorkflowDatabaseTable.class);
    distribution = injector().getInstance(WorkerDistribution.class);
    workflowFactory = injector().getInstance(WorkflowFactory.class);
    registerConditions();
    registerLoops();
  }

  private void registerConditions() {
    var repository = injector().getInstance(ConditionInformationRepository.class);
    repository.register(ConditionTextEquals.information());
    repository.register(ConditionTextStartsWith.information());
    repository.register(ConditionTextEndsWith.information());
    repository.register(ConditionNumberGreaterThan.information());
    repository.register(ConditionNumberSmallerThan.information());
  }

  private void registerLoops() {
    var repository = injector().getInstance(LoopInformationRepository.class);
    repository.register(NumberLoop.information());
    repository.register(ItemLoop.information(this));
    repository.register(TextLoop.information());
  }

  /**
   * Is used to find a trigger
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @return The trigger if it could be found
   */
  public Optional<Trigger> findTrigger(String moduleName, String triggerType) {
    var moduleOptional = moduleLoader.findRegisteredModuleById(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get().module();
    if (module instanceof Integration integration) {
      return integration.triggerRepository().findTrigger(triggerType);
    }
    return Optional.empty();
  }

  /**
   * Is used to find an action
   * @param moduleName The name of the module in which the action is located
   * @param actionType The type of the action
   * @return The action if it could be found
   */
  public Optional<Action<? extends ActionExecutor>> findAction(
    String moduleName, String actionType
  ) {
    var moduleOptional = moduleLoader.findRegisteredModuleById(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    var module = moduleOptional.get().module();
    if (module instanceof Integration integration) {
      return integration.actionRepository().findAction(actionType);
    }
    return Optional.empty();
  }

  /**
   * Is used to find the information of a trigger
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @return The information of the trigger if it could be found
   */
  public Optional<TriggerInformation> findTriggerInformation(
    String moduleName, String triggerType
  ) {
    return findTrigger(moduleName, triggerType).map(Trigger::information);
  }

  /**
   * Is used to find the information of an action
   * @param moduleName The name of the module in which the action is located
   * @param actionType The type of the action
   * @return The information of the action if it could be found
   */
  public Optional<ActionInformation> findActionInformation(
    String moduleName, String actionType
  ) {
    return findAction(moduleName, actionType).map(Action::information);
  }

  /**
   * Triggers a workflow
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @param condition The condition for trigger selection
   * @param information The trigger information
   */
  public void triggerWorkflows(
    String moduleName, String triggerType, DatabaseCondition condition,
    Map<String, Object> information
  ) {
    triggerWorkflows(moduleName, triggerType, condition, information, true);
  }

  /**
   * Triggers a workflow
   * @param moduleName The name of the module in which the trigger is located
   * @param triggerType The type of the trigger
   * @param condition The condition for trigger selection
   * @param information The trigger information
   * @param checkDistribution If true there is a distribution check,
   *                          if false there is no distribution check
   */
  public void triggerWorkflows(
    String moduleName, String triggerType, DatabaseCondition condition,
    Map<String, Object> information, boolean checkDistribution
  ) {
    var module = moduleLoader.findRegisteredModuleById(moduleName).get().module();
    if (module instanceof Integration integration) {
      var trigger = integration.triggerRepository().findTrigger(triggerType).get();
      trigger.findEntries(condition).thenAccept(triggers ->
        buildWorkflowTriggers(triggers, moduleName, information, checkDistribution));
    }
  }

  private void buildWorkflowTriggers(
    List<UUID> triggerIds, String moduleName, Map<String, Object> information,
    boolean checkDistribution
  ) {
    AsyncIterator.execute(triggerIds, triggerDatabaseTable::findTrigger)
      .thenAccept(triggers ->
        filterTriggerEntries(triggers, moduleName, checkDistribution)
          .forEach(entry -> createWorkflow(entry.id())
            .thenAccept(workflow -> workflow.trigger(information))));
  }

  /**
   * Is used to find all triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findAllTriggerEntries(
    String module, String type
  ) {
    return findSomeTriggerEntries(module, type, DatabaseCondition.empty(), true);
  }

  /**
   * Is used to find all triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @param checkDistribution If true there is a distribution check,
   *                          if false there is no distribution check
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findAllTriggerEntries(
    String module, String type, boolean checkDistribution
  ) {
    return findSomeTriggerEntries(module, type, DatabaseCondition.empty(),
      checkDistribution);
  }

  /**
   * Is used to find some triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @param condition The condition with that the triggers are found
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findSomeTriggerEntries(
    String module, String type, DatabaseCondition condition
  ) {
    return findSomeTriggerEntries(module, type, condition, true);
  }

  /**
   * Is used to find all triggers of one kind
   * @param module The name of the module in which the triggers are located
   * @param type The type of the trigger
   * @param condition The condition with that the triggers are found
   * @param checkDistribution If true there is a distribution check,
   *                          if false there is no distribution check
   * @return A future that contains the list of trigger entries
   */
  public CompletableFuture<List<TriggerEntry>> findSomeTriggerEntries(
    String module, String type, DatabaseCondition condition,
    boolean checkDistribution
  ) {
    var futureResponse = new CompletableFuture<List<TriggerEntry>>();
    findTrigger(module, type).get().findEntries(condition)
      .thenAccept(entries -> AsyncIterator.execute(entries,
          triggerDatabaseTable::findTrigger)
        .thenAccept(triggers -> futureResponse.complete(
          filterTriggerEntries(triggers, module, checkDistribution))));
    return futureResponse;
  }

  private List<TriggerEntry> filterTriggerEntries(
    List<TriggerEntry> entries, String module, boolean checkDistribution
  ) {
    var stream = entries.stream();
    if (checkDistribution) {
      stream = stream.filter(entry ->
        distribution.isAssignedUser(module, entry.ownerId()));
    }
    stream = stream.filter(entry -> entry.state().isArmed());
    return stream.toList();
  }

  /**
   * Creates a workflow by trigger id
   * @param triggerId The id of the trigger
   * @return A future that contains the workflow
   */
  public CompletableFuture<Workflow> createWorkflow(UUID triggerId) {
    return workflowDatabaseTable.findWorkflowByTrigger(triggerId)
      .thenCompose(this::createWorkflow);
  }

  /**
   * Creates a workflow by workflow id
   * @param workflowId The id of the workflow
   * @return A future that contains the workflow
   */
  public CompletableFuture<Workflow> createWorkflowById(UUID workflowId) {
    return workflowDatabaseTable.findWorkflow(workflowId)
      .thenCompose(this::createWorkflow);
  }

  /**
   * Creates a workflow by workflow entry
   * @param workflowEntry The workflow entry
   * @return A future that contains the workflow
   */
  public CompletableFuture<Workflow> createWorkflow(WorkflowEntry workflowEntry) {
    return workflowFactory.create(workflowEntry);
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