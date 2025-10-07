package com.quizy.combatlog.managers;

import org.bukkit.configuration.file.FileConfiguration;
import com.quizy.combatlog.QuizyCombatLog;

import java.util.List;

public class ConfigManager {
    
    private final QuizyCombatLog plugin;
    private FileConfiguration config;
    
    public ConfigManager(QuizyCombatLog plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    // Combat Configuration
    public int getCombatDuration() {
        return config.getInt("combat.duration", 10);
    }
    
    public void setCombatDuration(int duration) {
        config.set("combat.duration", duration);
        plugin.saveConfig();
    }
    
    public boolean shouldDropInventory() {
        return config.getBoolean("combat.inventory-drop", true);
    }
    
    public void setInventoryDrop(boolean drop) {
        config.set("combat.inventory-drop", drop);
        plugin.saveConfig();
    }
    
    public List<String> getUnblockedCommands() {
        return config.getStringList("combat.unblocked-commands");
    }
    
    // Message Configuration
    public String getCombatStartedMessage() {
        return config.getString("messages.combat-started", "&4&l⚔ &8» &7You have entered &d&lcombat&7! &8(&7Do not log out&8)");
    }
    
    public String getCombatEndedMessage() {
        return config.getString("messages.combat-ended", "&2&l✔ &8» &7You are no longer in &dcombat&7!");
    }
    
    public String getCommandBlockedMessage() {
        return config.getString("messages.command-blocked", "&4&l✖ &8» &7Commands are &ddisabled &7during combat!");
    }
    
    public String getPlayerCombatLoggedMessage() {
        return config.getString("messages.player-combat-logged", "&4&l☠ &8» &d{player} &7has combat logged!");
    }
    
    // Action Bar Configuration
    public String getActionBarFormat() {
        return config.getString("actionbar.format", "&d&lCOMBAT &8» &f{time}s");
    }
    
    // Command messages
    public String getHelpMessage() {
        return "&d&l⚔ QuizyCombatLog Commands:\n" +
               "&7• &f/qcl help &8- &7Shows this help menu\n" +
               "&7• &f/qcl reload &8- &7Reloads the plugin configuration\n" +
               "&7• &f/qcl combattime <seconds> &8- &7Sets combat timer duration\n" +
               "&7• &f/qcl inventorydrop <true/false> &8- &7Toggles inventory drop on combat log";
    }
    
    public String getNoPermissionMessage() {
        return "&4&l✖ &8» &7You don't have permission to use this command!";
    }
    
    public String getReloadMessage() {
        return "&2&l✔ &8» &7Configuration reloaded successfully!";
    }
    
    public String getCombatTimeSetMessage(int time) {
        return "&2&l✔ &8» &7Combat time set to &d" + time + " &7seconds!";
    }
    
    public String getInventoryDropSetMessage(boolean enabled) {
        return "&2&l✔ &8» &7Inventory drop &d" + (enabled ? "enabled" : "disabled") + "&7!";
    }
    
    public String getInvalidNumberMessage() {
        return "&4&l✖ &8» &7Please enter a valid number!";
    }
    
    public String getInvalidBooleanMessage() {
        return "&4&l✖ &8» &7Please enter 'true' or 'false'!";
    }
}