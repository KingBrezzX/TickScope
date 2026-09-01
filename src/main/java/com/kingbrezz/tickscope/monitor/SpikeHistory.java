package com.kingbrezz.tickscope.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpikeHistory {

    private static final int MAX_EVENTS = 200;

    private final List<SpikeEvent> events =
            Collections.synchronizedList(
                    new ArrayList<>()
            );

    public void add(SpikeEvent event) {

        synchronized (events) {

            events.add(event);

            while (events.size() > MAX_EVENTS) {
                events.remove(0);
            }
        }
    }

    public List<SpikeEvent> getEvents() {

        synchronized (events) {
            return List.copyOf(events);
        }
    }

    public void clear() {

        synchronized (events) {
            events.clear();
        }
    }
}
