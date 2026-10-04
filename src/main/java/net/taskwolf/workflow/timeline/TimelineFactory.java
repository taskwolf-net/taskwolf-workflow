package net.taskwolf.workflow.timeline;

import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.workflow.timeline.entry.TimelineEntryFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TimelineFactory {
  private final TimelineEntryFactory timelineEntryFactory;

  public CompletableFuture<Timeline> createTimeline(
    List<TimelineDatabaseEntry> databaseEntries
  ) {
    var futureResponse = new CompletableFuture<Timeline>();
    AsyncIterator.execute(databaseEntries, entry ->
        timelineEntryFactory.create(entry.time(), entry.type(), entry.content()))
      .thenAccept(entries -> futureResponse.complete(Timeline.create(entries)));
    return futureResponse;
  }
}
