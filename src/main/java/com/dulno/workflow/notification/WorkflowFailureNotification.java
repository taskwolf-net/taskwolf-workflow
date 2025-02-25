package com.dulno.workflow.notification;

import com.dulno.core.locale.Translation;
import com.dulno.core.mail.Mail;
import com.dulno.core.notification.Notification;
import com.dulno.core.user.User;
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
      translation.translate(user, failureMessageKey));
    notificationMail.send(user, title, body);
  }
}
