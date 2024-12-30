package com.dulno.workflow.structure;

import com.dulno.core.bundle.Bundle;
import com.dulno.core.bundle.BundleDatabaseTable;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.iterator.AsyncIterator;
import com.dulno.core.locale.Translation;
import com.dulno.core.mail.Mail;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.module.ModuleLoader;
import com.dulno.core.notification.NotificationDatabaseTable;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.organization.team.Team;
import com.dulno.core.organization.team.TeamDatabaseTable;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.workflow.WorkflowModule;
import com.dulno.workflow.integration.Integration;
import com.dulno.workflow.operation.OperationDatabaseTable;
import com.dulno.workflow.step.WorkflowStepCompound;
import com.dulno.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.workflow.timeline.TimelineDatabaseTable;
import com.dulno.workflow.action.ActionDatabaseTable;
import com.dulno.workflow.action.ActionEntry;
import com.dulno.workflow.condition.ConditionDatabaseTable;
import com.dulno.workflow.condition.ConditionEntry;
import com.dulno.workflow.condition.ConditionFactory;
import com.dulno.workflow.condition.ConditionInformationRepository;
import com.dulno.workflow.loop.LoopDatabaseTable;
import com.dulno.workflow.loop.LoopEntry;
import com.dulno.workflow.loop.LoopFactory;
import com.dulno.workflow.loop.LoopInformationRepository;
import com.dulno.workflow.trigger.Trigger;
import com.dulno.workflow.trigger.TriggerDatabaseTable;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class WorkflowFactory {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final WorkflowModule workflowModule;
  private final TriggerDatabaseTable triggerDatabaseTable;
  private final ActionDatabaseTable actionDatabaseTable;
  private final ConditionDatabaseTable conditionDatabaseTable;
  private final ConditionFactory conditionFactory;
  private final ConditionInformationRepository conditionRepository;
  private final LoopDatabaseTable loopDatabaseTable;
  private final LoopFactory loopFactory;
  private final LoopInformationRepository loopRepository;
  private final ModuleLoader moduleLoader;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final BundleDatabaseTable bundleDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final TeamDatabaseTable teamDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Translation translation;
  private final ErrorRepository errorRepository;
  private final Mail notificationMail;

  @Inject
  private WorkflowFactory(
    WorkflowDatabaseTable workflowDatabaseTable, WorkflowModule workflowModule,
    TriggerDatabaseTable triggerDatabaseTable,
    ActionDatabaseTable actionDatabaseTable,
    ConditionDatabaseTable conditionDatabaseTable,
    ConditionFactory conditionFactory,
    ConditionInformationRepository conditionRepository, ModuleLoader moduleLoader,
    LoopDatabaseTable loopDatabaseTable, LoopFactory loopFactory,
    LoopInformationRepository loopRepository,
    TimelineDatabaseTable timelineDatabaseTable,
    UserDatabaseTable userDatabaseTable,
    BundleDatabaseTable bundleDatabaseTable,
    OperationDatabaseTable operationDatabaseTable,
    WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable,
    OrganizationDatabaseTable organizationDatabaseTable,
    TeamDatabaseTable teamDatabaseTable,
    NotificationDatabaseTable notificationDatabaseTable,
    MaintenanceSchedule maintenanceSchedule, Translation translation,
    ErrorRepository errorRepository,
    @Named("notificationMail") Mail notificationMail
  ) {
    this.workflowDatabaseTable = workflowDatabaseTable;
    this.workflowModule = workflowModule;
    this.triggerDatabaseTable = triggerDatabaseTable;
    this.actionDatabaseTable = actionDatabaseTable;
    this.conditionDatabaseTable = conditionDatabaseTable;
    this.conditionFactory = conditionFactory;
    this.conditionRepository = conditionRepository;
    this.loopDatabaseTable = loopDatabaseTable;
    this.loopFactory = loopFactory;
    this.loopRepository = loopRepository;
    this.moduleLoader = moduleLoader;
    this.timelineDatabaseTable = timelineDatabaseTable;
    this.userDatabaseTable = userDatabaseTable;
    this.bundleDatabaseTable = bundleDatabaseTable;
    this.operationDatabaseTable = operationDatabaseTable;
    this.workflowThrottleDatabaseTable = workflowThrottleDatabaseTable;
    this.organizationDatabaseTable = organizationDatabaseTable;
    this.teamDatabaseTable = teamDatabaseTable;
    this.notificationDatabaseTable = notificationDatabaseTable;
    this.maintenanceSchedule = maintenanceSchedule;
    this.translation = translation;
    this.errorRepository = errorRepository;
    this.notificationMail = notificationMail;
  }

  public CompletableFuture<Workflow> create(WorkflowEntry workflowEntry) {
    return findWorkflowBundleOwner(workflowEntry)
      .thenCompose(bundleOwner -> bundleDatabaseTable.findBundle(bundleOwner)
        .thenCompose(bundle -> findWorkflowTrigger(workflowEntry)
          .thenCompose(trigger -> assembleWorkflowSteps(workflowEntry, bundle)
            .thenApply(steps -> assemblyWorkflow(workflowEntry, trigger, steps,
              bundle)))));
  }

  private CompletableFuture<Trigger> findWorkflowTrigger(
    WorkflowEntry workflowEntry
  ) {
    return triggerDatabaseTable.findTrigger(workflowEntry.triggerId())
      .thenApply(triggerEntry -> workflowModule.findTrigger(triggerEntry.module(),
        triggerEntry.type()).get());
  }

  private CompletableFuture<List<WorkflowStepCompound>> assembleWorkflowSteps(
    WorkflowEntry workflowEntry, Bundle bundle
  ) {
    return actionDatabaseTable.findActionsByWorkflow(workflowEntry.id())
      .thenCompose(actions ->
        conditionDatabaseTable.findConditionsByWorkflow(workflowEntry.id())
          .thenCompose(conditions ->
            loopDatabaseTable.findLoopIfExists(workflowEntry.id())
              .thenCompose(loop -> assembleWorkflowSteps(actions, conditions,
                loop, bundle, 0))));
  }

  private CompletableFuture<List<WorkflowStepCompound>> assembleWorkflowSteps(
    List<ActionEntry> actions, List<ConditionEntry> conditions,
    Optional<LoopEntry> loop, Bundle bundle, int startingIndex
  ) {
    var steps = collateWorkflowSteps(actions, conditions, loop, bundle,
      startingIndex);
    return AsyncIterator.execute(steps,
      step -> step.getValue().thenApply(result ->
        new AbstractMap.SimpleEntry<>(step.getKey(), result)))
      .thenApply(result -> result.stream().sorted(Map.Entry.comparingByKey())
        .map(AbstractMap.SimpleEntry::getValue).toList());
  }

  private List<Map.Entry<Integer, CompletableFuture<WorkflowStepCompound>>> collateWorkflowSteps(
    List<ActionEntry> actions, List<ConditionEntry> conditions,
    Optional<LoopEntry> loop, Bundle bundle, int startingIndex
  ) {
    var steps = Lists.<Map.Entry<Integer,
      CompletableFuture<WorkflowStepCompound>>>newArrayList();
    var index = startingIndex;
    var currentStep = findWorkflowStep(actions, conditions, loop, bundle, index);
    while (currentStep != null) {
      steps.add(new AbstractMap.SimpleEntry<>(index, currentStep));
      if (loop.isPresent() && loop.get().index() == index) {
        break;
      }
      index++;
      currentStep = findWorkflowStep(actions, conditions, loop, bundle, index);
    }
    return steps;
  }

  private CompletableFuture<WorkflowStepCompound> findWorkflowStep(
    List<ActionEntry> actions, List<ConditionEntry> conditions,
    Optional<LoopEntry> loop, Bundle bundle, int index
  ) {
    var action = actions.stream().filter(entry -> entry.index() == index)
      .findFirst();
    if (action.isPresent()) {
      return prepareAction(action.get());
    }
    var condition = conditions.stream().filter(entry -> entry.index() == index)
      .findFirst();
    if (condition.isPresent()) {
      return prepareCondition(condition.get());
    }
    if (loop.isPresent() && loop.get().index() == index) {
      return prepareLoop(loop.get(), bundle, index, actions, conditions);
    }
    return null;
  }

  private CompletableFuture<WorkflowStepCompound> prepareAction(ActionEntry entry) {
    var module = moduleLoader.findRegisteredModuleById(entry.module()).get().module();
    if (module instanceof Integration integration) {
      var action = integration.actionRepository().findAction(entry.type()).get();
      return action.build(entry.id()).thenApply(value ->
        WorkflowStepCompound.create(value, integration.moduleInformation().name(),
          action.information().name()));
    }
    return CompletableFuture.completedFuture(null);
  }

  private CompletableFuture<WorkflowStepCompound> prepareCondition(
    ConditionEntry entry
  ) {
    var condition = conditionRepository.findByIdentifier(entry.type()).get();
    return CompletableFuture.completedFuture(
      WorkflowStepCompound.create(conditionFactory.create(
        entry.type(), entry.content()), "condition", condition.name()));
  }

  private CompletableFuture<WorkflowStepCompound> prepareLoop(
    LoopEntry entry, Bundle bundle, int index, List<ActionEntry> actions,
    List<ConditionEntry> conditions
  ) {
    var loop = loopRepository.findByIdentifier(entry.type()).get();
    return CompletableFuture.completedFuture(
      WorkflowStepCompound.create(loopFactory.create(entry,
        () -> assembleWorkflowSteps(actions, conditions, Optional.empty(),
          bundle, index + 1), bundle),
        "loop", loop.name()));
  }

  private Workflow assemblyWorkflow(
    WorkflowEntry workflowEntry, Trigger trigger,
    List<WorkflowStepCompound> steps, Bundle bundle
  ) {
    return Workflow.create(workflowDatabaseTable, timelineDatabaseTable,
      userDatabaseTable, operationDatabaseTable, workflowThrottleDatabaseTable,
      organizationDatabaseTable, notificationDatabaseTable, maintenanceSchedule,
      translation, errorRepository, notificationMail, workflowEntry, trigger,
      steps, bundle);
  }

  private CompletableFuture<UUID> findWorkflowBundleOwner(WorkflowEntry workflow) {
    var owner = workflow.ownerId();
    return userDatabaseTable.userExists(owner)
      .thenCompose(userExists -> organizationDatabaseTable.organizationExists(owner)
        .thenCompose(organizationExists -> userExists || organizationExists ?
          CompletableFuture.completedFuture(owner) :
          teamDatabaseTable.findTeam(owner).thenApply(Team::organizationId)));
  }
}
