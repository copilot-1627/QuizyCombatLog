package com.quizy.combatlog;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.quizy.combatlog.commands.QCLCommand;
import com.quizy.combatlog.listeners.CombatListener;
import com.quizy.combatlog.listeners.PlayerListener;
import com.quizy.combatlog.managers.CombatManager;
import com.quizy.combatlog.managers.ConfigManager;
import com.quizy.combatlog.utils.ActionBarManager;

public class QuizyCombatLog extends JavaPlugin {
    
    private static QuizyCombatLog instance;
    private CombatManager combatManager;
    private ConfigManager configManager;
    private ActionBarManager actionBarManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        combatManager = new CombatManager(this);
        actionBarManager = new ActionBarManager(this);
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Register commands
        getCommand("qcl").setExecutor(new QCLCommand(this));
        
        // Start action bar task
        actionBarManager.startActionBarTask();
        
        getLogger().info("QuizyCombatLog has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);
        
        // Clear combat data
        if (combatManager != null) {
            combatManager.clearAllCombat();
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
}