package com.dulno.workflow.structure;

import com.dulno.core.bundle.Bundle;
import com.dulno.core.error.ErrorRepository;
import com.dulno.core.locale.Translation;
import com.dulno.core.mail.Mail;
import com.dulno.core.maintenance.MaintenanceSchedule;
import com.dulno.core.notification.NotificationDatabaseTable;
import com.dulno.core.notification.NotificationSetting;
import com.dulno.core.organization.Organization;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.workflow.notification.WorkflowFailureNotification;
import com.dulno.workflow.operation.Operation;
import com.dulno.workflow.operation.OperationDatabaseTable;
import com.dulno.workflow.step.WorkflowStep;
import com.dulno.workflow.step.WorkflowStepCompound;
import com.dulno.workflow.step.WorkflowStepResult;
import com.dulno.workflow.throttle.WorkflowThrottle;
import com.dulno.workflow.throttle.WorkflowThrottleDatabaseTable;
import com.dulno.workflow.timeline.TimelineDatabaseTable;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.trigger.Trigger;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class Workflow {
  private final WorkflowDatabaseTable workflowDatabaseTable;
  private final TimelineDatabaseTable timelineDatabaseTable;
  private final UserDatabaseTable userDatabaseTable;
  private final OperationDatabaseTable operationDatabaseTable;
  private final WorkflowThrottleDatabaseTable workflowThrottleDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final NotificationDatabaseTable notificationDatabaseTable;
  private final MaintenanceSchedule maintenanceSchedule;
  private final Translation translation;
  private final ErrorRepository errorRepository;
  private final Mail notificationMail;
  private final WorkflowEntry workflowEntry;
  private final Trigger trigger;
  private final List<WorkflowStepCompound> steps;
  private final Bundle bundle;
  private int currentStepIndex = 0;
  private Map<String, Object> currentInformation;

  /**
   * Triggers the workflow
   * @param information The information provided by the trigger
   */
  public CompletableFuture<Boolean> trigger(Map<String, Object> information) {
    if (maintenanceSchedule.isMaintenanceRunning()) {
      return CompletableFuture.completedFuture(false);
    }
    return checkOperationLimitExtension()
      .thenCompose(extensionValue -> triggerLimit(information))
      .exceptionally(this::processWorkflowException);
  }

  private CompletableFuture<Void> checkOperationLimitExtension() {
    return operationDatabaseTable.findOperations(bundle.ownerId())
      .thenCompose(this::checkOperationLimitExtension);
  }

  private CompletableFuture<Void> checkOperationLimitExtension(
    Operation operations
  ) {
    if (System.currentTimeMillis() > operations.expiration()) {
      return operationDatabaseTable.extendExpiration(bundle.ownerId());
    }
    return CompletableFuture.completedFuture(null);
  }

  private Boolean processWorkflowException(Throwable throwable) {
    errorRepository.processError(throwable);
    return false;
  }

  private CompletableFuture<Boolean> triggerLimit(
    Map<String, Object> information
  ) {
    if (bundle.expiration() > 0 && System.currentTimeMillis() > bundle.expiration()) {
      return CompletableFuture.completedFuture(false);
    }
    return WorkflowThrottle.create(workflowThrottleDatabaseTable, bundle.ownerId())
      .registerWorkflowExecution().thenCompose(throttleAllowsExecution ->
        triggerThrottle(information, throttleAllowsExecution));
  }

  private CompletableFuture<Boolean> triggerThrottle(
    Map<String, Object> information, boolean throttleAllowsExecution
  ) {
    if (!throttleAllowsExecution) {
      postExecutionFailure("workflow.throttle.intervention", -1);
      return CompletableFuture.completedFuture(false);
    }
    return trigger.checkExecution(workflowEntry.triggerId())
      .thenCompose(executionPermitted ->
        checkTriggerExecution(information, executionPermitted));
  }

  private CompletableFuture<Boolean> checkTriggerExecution(
    Map<String, Object> information, boolean executionPermitted
  ) {
    if (!executionPermitted) {
      postExecutionFailure("workflow.trigger.execution.refused", -1);
      return CompletableFuture.completedFuture(false);
    }
    currentInformation = Maps.newHashMap(information);
    currentInformation.putAll(timeInformation());
    currentInformation = prepareInformation("trigger", currentInformation);
    return checkOperationLimit(false).thenCompose(this::executeNextStep);
  }

  private Map<String, Object> timeInformation() {
    var timeInformation = Maps.<String, Object>newHashMap();
    var zone = parseTimeZone();
    var locale = parseTimeLocale();
    var unixTime = System.currentTimeMillis();
    var dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTime), zone);
    var timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
      .withLocale(locale).withZone(zone);
    var dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
      .withLocale(locale).withZone(zone);
    timeInformation.put("formattedTime", dateTime.format(timeFormat));
    timeInformation.put("formattedDate", dateTime.format(dateFormat));
    timeInformation.put("unixTime", unixTime);
    return timeInformation;
  }

  private ZoneId parseTimeZone() {
    try {
      return ZoneId.of(workflowEntry.timeZone());
    } catch (Exception exception) {
      return ZoneId.of("Europe/Berlin");
    }
  }

  private Locale parseTimeLocale() {
    try {
      var parts = workflowEntry.timeLocale().split("_");
      if (parts.length == 2) {
        return Locale.of(parts[0], parts[1]);
      } else if (parts.length == 1) {
        return Locale.of(parts[0]);
      } else {
        throw new Exception();
      }
    } catch (Exception exception) {
      return Locale.GERMANY;
    }
  }

  private CompletableFuture<Boolean> executeNextStep(boolean limitReached) {
    if (maintenanceSchedule.isMaintenanceRunning() || limitReached) {
      return CompletableFuture.completedFuture(false);
    }
    if (currentStepIndex >= steps.size()) {
      postExecutionSuccess();
      return CompletableFuture.completedFuture(true);
    }
    var step = steps.get(currentStepIndex).step();
    return step.execute(currentInformation)
      .thenCompose(result -> checkOperationLimit(step)
        .thenCompose(newLimitReached -> processStepResult(result, newLimitReached)));
  }

  private CompletableFuture<Boolean> processStepResult(
    WorkflowStepResult result, boolean limitReached
  ) {
    if (result.isFailure()) {
      postExecutionFailure(result.failureMessage(), result.failureStepIndex());
      return CompletableFuture.completedFuture(false);
    }
    if (!result.mayContinue()) {
      postExecutionSuccess();
      return CompletableFuture.completedFuture(true);
    }
    currentInformation.putAll(prepareInformation("step" + currentStepIndex,
      result.passOnInformation()));
    currentStepIndex++;
    return executeNextStep(limitReached);
  }

  private Map<String, Object> prepareInformation(
    String prefix, Map<String, Object> information
  ) {
    var result = Maps.<String, Object>newHashMap();
    for (var entry : information.entrySet()) {
      result.put(prefix + "-" + entry.getKey(), entry.getValue());
    }
    return result;
  }

  private CompletableFuture<Boolean> checkOperationLimit(WorkflowStep step) {
    if (!(step instanceof ActionExecutor)) {
      return CompletableFuture.completedFuture(false);
    }
    return checkOperationLimit(true);
  }

  private CompletableFuture<Boolean> checkOperationLimit(boolean addOperation) {
    return operationDatabaseTable.findOperations(bundle.ownerId())
      .thenCompose(operation -> checkOperationLimit(operation, addOperation));
  }

  private CompletableFuture<Boolean> checkOperationLimit(
    Operation operation, boolean addOperation
  ) {
    if (operation.operations() + 1 > bundle.workflowOperationLimit()) {
      postExecutionFailure("workflow.operations.limit.reached", -2);
      return CompletableFuture.completedFuture(true);
    }
    if (!addOperation) {
      return CompletableFuture.completedFuture(false);
    }
    return operationDatabaseTable.addOperations(bundle.ownerId(), 1)
      .thenApply(value -> false);
  }

  private void postExecutionSuccess() {
    long currentTime = System.currentTimeMillis();
    if (workflowEntry.state().isFailing()) {
      workflowDatabaseTable.updateWorkflowState(workflowEntry, WorkflowState.OPERATIONAL);
    }
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowEntry.id(), currentTime,
        "timeline-workflow-execute", "{}"));
  }

  private void postExecutionFailure(String failureMessage, int failureStepIndex) {
    long currentTime = System.currentTimeMillis();
    if (workflowEntry.state().isOperational()) {
      workflowDatabaseTable.updateWorkflowState(workflowEntry, WorkflowState.FAILING);
    }
    var currentStep = steps.get(currentStepIndex);
    var timelineContent = Map.of("moduleName", currentStep.moduleName(),
      "stepName", currentStep.stepName(), "stepIndex",
      failureStepIndex == -2 ? currentStepIndex : failureStepIndex,
      "message", failureMessage);
    timelineDatabaseTable.generateAvailableEntryId().thenAccept(id ->
      timelineDatabaseTable.insertEntry(id, workflowEntry.id(), currentTime,
        "timeline-workflow-failure", new JSONObject(timelineContent).toString()));
    findNotificationTarget().thenAccept(target -> notificationDatabaseTable
      .findNotificationSettings(target.id()).thenAccept(setting ->
        sendExecutionFailureNotification(target, setting, failureMessage)));
  }

  private CompletableFuture<User> findNotificationTarget() {
    return userDatabaseTable.userExists(bundle.ownerId())
      .thenCompose(exists -> exists ?
        CompletableFuture.completedFuture(bundle.ownerId()) :
        organizationDatabaseTable.findOrganization(bundle.ownerId())
          .thenApply(Organization::owner))
      .thenCompose(userDatabaseTable::findUser);
  }

  private void sendExecutionFailureNotification(
    User target, NotificationSetting notificationSetting, String failureMessage
  ) {
    if (!notificationSetting.general() || !notificationSetting.workflowFail()) {
      return;
    }
    WorkflowFailureNotification.create(translation, notificationMail, target,
      failureMessage).send();
  }

  public Map<String, Object> currentInformation() {
    return Map.copyOf(currentInformation);
  }
}
