package com.kingbrezz.tickscope.monitor;

import java.util.ArrayList;
import java.util.List;

public final class SpikeHistory {
    private final List<SpikeEvent> events = new ArrayList<>();

    public synchronized void add(SpikeEvent event) { events.add(event); }

    public synchronized List<SpikeEvent> getEvents() { return List.copyOf(events); }

    public synchronized void trimTo(int max) {
        while (events.size() > max) events.remove(0);
    }

    public synchronized void clear() { events.clear(); }
}
