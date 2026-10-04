package net.taskwolf.workflow.notification;

import net.taskwolf.core.locale.Translation;
import net.taskwolf.core.mail.Mail;
import net.taskwolf.core.notification.Notification;
import net.taskwolf.core.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public final class WorkflowFailureNotification implements Notification {
  private final Translation translation;
  private final Mail notificationMail;
  private final User user;
  private final String failureMessageKey;

  public void send() {
    var title = translation.translate(user, "workflow.failure.notification.title");
    var body = String.format(
      translation.translate(user, "workflow.failure.notification.body"),
      user.name(), translation.translate(user, failureMessageKey));
    notificationMail.send(user, title, body);
  }
}
