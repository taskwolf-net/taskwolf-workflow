package com.dulno.workflow.timeline;

import com.dulno.workflow.timeline.entry.TimelineEntry;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public final class Timeline {
  private final List<TimelineEntry> entries;

  public List<TimelineEntry> findAllEntries() {
    return List.copyOf(entries);
  }
}
