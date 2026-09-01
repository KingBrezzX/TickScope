package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;
import com.kingbrezz.tickscope.monitor.SpikeEvent;
import java.util.List;

public final class SpikeApi {
    private SpikeApi() {}
    public static List<SpikeEvent> get(TickScope plugin) {
        return plugin.getSpikeMonitor() == null ? List.of()
                : plugin.getSpikeMonitor().getHistory().getEvents();
    }
}
