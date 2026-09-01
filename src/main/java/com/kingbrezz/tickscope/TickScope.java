package com.kingbrezz.tickscope;

import com.kingbrezz.tickscope.admin.AdminActionManager;
import com.kingbrezz.tickscope.ai.RecommendationEngine;
import com.kingbrezz.tickscope.analysis.AnalysisManager;
import com.kingbrezz.tickscope.analysis.EntityAnalyzer;
import com.kingbrezz.tickscope.analysis.TileEntityAnalyzer;
import com.kingbrezz.tickscope.command.TickScopeCommand;
import com.kingbrezz.tickscope.history.HistoryManager;
import com.kingbrezz.tickscope.history.HistoryScheduler;
import com.kingbrezz.tickscope.i18n.MessageManager;
import com.kingbrezz.tickscope.monitor.PerformanceMonitor;
import com.kingbrezz.tickscope.monitor.SpikeDetector;
import com.kingbrezz.tickscope.monitor.SpikeMonitor;
import com.kingbrezz.tickscope.web.TickScopeWebServer;
import com.kingbrezz.tickscope.web.TokenManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TickScope extends JavaPlugin {
    private static TickScope instance;
    private PerformanceMonitor performanceMonitor;
    private SpikeDetector spikeDetector;
    private SpikeMonitor spikeMonitor;
    private AnalysisManager analysisManager;
    private EntityAnalyzer entityAnalyzer;
    private TileEntityAnalyzer tileEntityAnalyzer;
    private RecommendationEngine recommendationEngine;
    private HistoryManager historyManager;
    private HistoryScheduler historyScheduler;
    private TokenManager tokenManager;
    private TickScopeWebServer webServer;
    private AdminActionManager adminActionManager;
    private MessageManager messageManager;

    @Override public void onEnable() {
        instance = this;
        saveDefaultConfig();
        messageManager = new MessageManager(this);

        tokenManager = new TokenManager(this);
        tokenManager.load();
        adminActionManager = new AdminActionManager(this);

        performanceMonitor = new PerformanceMonitor(this);
        performanceMonitor.start();

        spikeDetector = new SpikeDetector(this, performanceMonitor);
        spikeDetector.start();

        spikeMonitor = new SpikeMonitor(this, performanceMonitor);
        spikeMonitor.start();

        analysisManager = new AnalysisManager(this);
        analysisManager.start();

        entityAnalyzer = new EntityAnalyzer(this);
        tileEntityAnalyzer = new TileEntityAnalyzer(this);
        recommendationEngine = new RecommendationEngine();

        historyManager = new HistoryManager(this);
        historyManager.load();

        historyScheduler = new HistoryScheduler(this, historyManager);
        historyScheduler.start();

        TickScopeCommand command = new TickScopeCommand(this);
        if (getCommand("tickscope") != null) getCommand("tickscope").setExecutor(command);
        if (getCommand("token") != null) getCommand("token").setExecutor(command);

        if (getConfig().getBoolean("web.enabled", true)) {
            webServer = new TickScopeWebServer(this);
            try { webServer.start(); }
            catch (Exception e) { getLogger().severe("Web server failed: " + e.getMessage()); }
        }
        getLogger().info("TickScope enabled.");
    }

    @Override public void onDisable() {
        if (webServer != null) webServer.stop();
        if (historyManager != null) historyManager.save();
        if (historyScheduler != null) historyScheduler.stop();
        if (analysisManager != null) analysisManager.stop();
        if (spikeMonitor != null) spikeMonitor.stop();
        if (spikeDetector != null) spikeDetector.stop();
        if (performanceMonitor != null) performanceMonitor.stop();
        instance = null;
    }

    public synchronized void reloadRuntime() {
        if (webServer != null) webServer.stop();
        if (historyScheduler != null) historyScheduler.stop();
        if (analysisManager != null) analysisManager.stop();
        if (spikeMonitor != null) spikeMonitor.stop();
        if (spikeDetector != null) spikeDetector.stop();
        if (performanceMonitor != null) performanceMonitor.stop();

        reloadConfig();
        if (messageManager != null) messageManager.reload();

        performanceMonitor.start();
        spikeDetector.start();
        spikeMonitor.start();
        analysisManager.start();
        historyScheduler.start();

        if (getConfig().getBoolean("web.enabled", true)) {
            webServer = new TickScopeWebServer(this);
            try {
                webServer.start();
            } catch (Exception exception) {
                getLogger().severe("Web server reload failed: " + exception.getMessage());
            }
        }
    }

    public static TickScope getInstance() { return instance; }
    public PerformanceMonitor getPerformanceMonitor() { return performanceMonitor; }
    public SpikeDetector getSpikeDetector() { return spikeDetector; }
    public SpikeMonitor getSpikeMonitor() { return spikeMonitor; }
    public AnalysisManager getAnalysisManager() { return analysisManager; }
    public EntityAnalyzer getEntityAnalyzer() { return entityAnalyzer; }
    public TileEntityAnalyzer getTileEntityAnalyzer() { return tileEntityAnalyzer; }
    public RecommendationEngine getRecommendationEngine() { return recommendationEngine; }
    public HistoryManager getHistoryManager() { return historyManager; }
    public TokenManager getTokenManager() { return tokenManager; }
    public TickScopeWebServer getWebServer() { return webServer; }
    public AdminActionManager getAdminActionManager() { return adminActionManager; }
    public MessageManager getMessageManager() { return messageManager; }
}
