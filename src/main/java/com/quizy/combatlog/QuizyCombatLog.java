package com.quizy.combatlog;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.quizy.combatlog.commands.QCLCommand;
import com.quizy.combatlog.listeners.CombatListener;
import com.quizy.combatlog.listeners.PlayerListener;
import com.quizy.combatlog.listeners.CombatConfigStickListener;
import com.quizy.combatlog.managers.AreaManager;
import com.quizy.combatlog.managers.CombatManager;
import com.quizy.combatlog.managers.ConfigManager;
import com.quizy.combatlog.utils.ActionBarManager;

public class QuizyCombatLog extends JavaPlugin {
    
    private static QuizyCombatLog instance;
    private CombatManager combatManager;
    private ConfigManager configManager;
    private ActionBarManager actionBarManager;
    private AreaManager areaManager;
    private CombatConfigStickListener combatConfigStickListener;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        combatManager = new CombatManager(this);
        actionBarManager = new ActionBarManager(this);
        areaManager = new AreaManager(this);
        combatConfigStickListener = new CombatConfigStickListener(this);
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(combatConfigStickListener, this);
        
        // Register commands
        QCLCommand qclCommand = new QCLCommand(this);
        getCommand("qcl").setExecutor(qclCommand);
        getCommand("qcl").setTabCompleter(qclCommand);
        
        // Start action bar task
        actionBarManager.startActionBarTask();
        
        getLogger().info("QuizyCombatLog has been enabled!");
        getLogger().info("Features: Combat Logging, No-Combat Zones, Holographic Boundaries, Multiple Opponent Tracking");
    }
    
    @Override
    public void onDisable() {
        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);
        
        // Clear combat data
        if (combatManager != null) {
            combatManager.clearAllCombat();
        }
        
        // Clear all holograms
        if (combatConfigStickListener != null && combatConfigStickListener.getHologramManager() != null) {
            combatConfigStickListener.getHologramManager().removeAllHolograms();
        }
        
        // Stop action bar task
        if (actionBarManager != null) {
            actionBarManager.stopActionBarTask();
        }
        
        getLogger().info("QuizyCombatLog has been disabled!");
    }
    
    public static QuizyCombatLog getInstance() {
        return instance;
    }
    
    public CombatManager getCombatManager() {
        return combatManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }
    
    public AreaManager getAreaManager() {
        return areaManager;
    }
    
    public CombatConfigStickListener getCombatConfigStickListener() {
        return combatConfigStickListener;
    }
}