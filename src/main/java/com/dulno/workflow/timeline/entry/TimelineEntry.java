package com.dulno.workflow.timeline.entry;

import com.dulno.core.locale.Translation;
import com.dulno.core.user.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TimelineEntry {
  private final long time;

  /**
   * Creates the title for the timeline entry
   * (must already be translated for the user)
   * @param translation Used for translation
   * @param user The user for whom the title is to be translated
   * @return The title of the timeline entry
   */
  public abstract String title(Translation translation, User user);

  /**
   * Creates the description for the timeline entry
   * (must already be translated for the user)
   * @param translation Used for translation
   * @param user The user for whom the description is to be translated
   * @return The description of the timeline entry
   */
  public abstract String description(Translation translation, User user);

  /**
   * Enables a different representation of different timeline entries
   * (the level primarily determines the color of the entry)
   * @return The level of the timeline entry
   */
  public abstract TimelineEntryLevel level();

  public long rawTime() {
    return time;
  }

  public String formattedTime() {
    var calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(calendar.getTime());
  }
}
