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
        // Also reload area manager
        if (plugin.getAreaManager() != null) {
            plugin.getAreaManager().reloadAreas();
        }
    }
    
    // Combat Configuration
    public int getCombatDuration() {
        return config.getInt("combat.duration", 15);
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
    
    // Hologram Configuration
    public String getHologramMaterial() {
        return config.getString("holograms.boundary-material", "RED_STAINED_GLASS_PANE");
    }
    
    public int getHologramBoundaryRange() {
        return config.getInt("holograms.boundary-range", 5);
    }
    
    public boolean isHologramEnabled() {
        return config.getBoolean("holograms.enabled", true);
    }
    
    // Message Configuration
    public String getCombatStartedMessage() {
        return config.getString("messages.combat-started", "&4&l⚔ &8» &7You have entered &c&lcombat&7! &8(&7Do not log out&8)");
    }
    
    public String getCombatEndedMessage() {
        return config.getString("messages.combat-ended", "&2&l✔ &8» &7You are no longer in &ccombat&7!");
    }
    
    public String getCommandBlockedMessage() {
        return config.getString("messages.command-blocked", "&4&l✖ &8» &7Commands are &cdisabled &7during combat!");
    }
    
    public String getPlayerCombatLoggedMessage() {
        return config.getString("messages.player-combat-logged", "&4&l☠ &8» &c{player} &7has combat logged and died!");
    }
    
    // Action Bar Configuration
    public String getActionBarFormat() {
        return config.getString("actionbar.format", "&c&lCOMBAT &8» &f{time}s remaining");
    }
    
    public String getActionBarSingleOpponentFormat() {
        return config.getString("actionbar.single-opponent-format", "&c&lCOMBAT &8» &e{opponent} &7(&f{time}s&7)");
    }
    
    public String getActionBarMultipleOpponentsFormat() {
        return config.getString("actionbar.multiple-opponents-format", "&c&lCOMBAT &7(&f{time}s&7) &8»");
    }
    
    public boolean isActionBarEnabled() {
        return config.getBoolean("actionbar.enabled", true);
    }
    
    // Combat Configure Stick Messages
    public String getConfigStickName() {
        return config.getString("messages.config-stick.name", "&eCombat Configure Stick");
    }
    
    public String getConfigStickLore1() {
        return config.getString("messages.config-stick.lore1", "&7Right-click two corners to define");
    }
    
    public String getConfigStickLore2() {
        return config.getString("messages.config-stick.lore2", "&7the protected area.");
    }
    
    public String getFirstCornerSetMessage() {
        return config.getString("messages.first-corner-set", "&a&l✔ &8» &7First corner set at ({x}, {y}, {z})");
    }
    
    public String getSecondCornerSetMessage() {
        return config.getString("messages.second-corner-set", "&a&l✔ &8» &7Second corner set. Area &e{areaName} &7saved successfully!");
    }
    
    public String getConfigStickRemovedMessage() {
        return config.getString("messages.config-stick-removed", "&e&l⚠ &8» &7Combat Configure Stick removed.");
    }
    
    public String getCannotEnterAreaMessage() {
        return config.getString("messages.cannot-enter-area", "&4&l✖ &8» &7You cannot enter &e{areaName} &7while in combat!");
    }
    
    // Area Management Messages
    public String getAreaAlreadyExistsMessage() {
        return config.getString("messages.area-already-exists", "&4&l✖ &8» &7Area &e{areaName} &7already exists!");
    }
    
    public String getAreaDoesNotExistMessage() {
        return config.getString("messages.area-does-not-exist", "&4&l✖ &8» &7Area &e{areaName} &7does not exist!");
    }
    
    public String getAreaRemovedMessage() {
        return config.getString("messages.area-removed", "&c&l✔ &8» &7Area &e{areaName} &7has been removed from disabled zones.");
    }
    
    public String getJoiningDisabledMessage() {
        return config.getString("messages.joining-disabled", "&c&l✔ &8» &7Players in combat can no longer enter &e{areaName}&c.");
    }
    
    public String getJoiningEnabledMessage() {
        return config.getString("messages.joining-enabled", "&a&l✔ &8» &7Players in combat can now enter &e{areaName} &7again.");
    }
    
    public String getConfigStickGivenMessage() {
        return config.getString("messages.config-stick-given", "&a&l✔ &8» &7You received a &eCombat Configure Stick &7for area &e{areaName}&7!");
    }
    
    // Command messages
    public String getHelpMessage() {
        return config.getString("messages.help", 
            "&8&l&m─────────────────────────────────────────────────────────\n" +
            "&e&lQuizyCombatLog &8» &7Commands Help\n" +
            "&8&l&m─────────────────────────────────────────────────────────\n" +
            "&7• &e/qcl help &8» &7Show this help message\n" +
            "&7• &e/qcl reload &8» &7Reload plugin configuration\n" +
            "&7• &e/qcl combattime <seconds> &8» &7Set combat duration\n" +
            "&7• &e/qcl inventorydrop <true/false> &8» &7Toggle inventory drop\n" +
            "&7• &e/qcl set disable.area <name> &8» &7Create no-combat zone\n" +
            "&7• &e/qcl remove disable.area <name> &8» &7Remove no-combat zone\n" +
            "&7• &e/qcl joining disable <name> &8» &7Prevent combat entry\n" +
            "&7• &e/qcl joining enable <name> &8» &7Allow combat entry\n" +
            "&8&l&m─────────────────────────────────────────────────────────");
    }
    
    public String getNoPermissionMessage() {
        return config.getString("messages.no-permission", "&4&l✖ &8» &7You don't have permission to use this command!");
    }
    
    public String getReloadMessage() {
        return config.getString("messages.reload", "&2&l✔ &8» &7Configuration and areas reloaded successfully!");
    }
    
    public String getCombatTimeSetMessage(int time) {
        String message = config.getString("messages.combat-time-set", "&2&l✔ &8» &7Combat time set to &e{time} &7seconds!");
        return message.replace("{time}", String.valueOf(time));
    }
    
    public String getInventoryDropSetMessage(boolean enabled) {
        String message = config.getString("messages.inventory-drop-set", "&2&l✔ &8» &7Inventory drop &e{status}&7!");
        return message.replace("{status}", enabled ? "enabled" : "disabled");
    }
    
    public String getInvalidNumberMessage() {
        return config.getString("messages.invalid-number", "&4&l✖ &8» &7Please enter a valid number!");
    }
    
    public String getInvalidBooleanMessage() {
        return config.getString("messages.invalid-boolean", "&4&l✖ &8» &7Please enter 'true' or 'false'!");
    }
    
    public String getPlayerOnlyCommandMessage() {
        return config.getString("messages.player-only", "&4&l✖ &8» &7This command can only be used by players!");
    }
    
    public String getUsageSetAreaMessage() {
        return config.getString("messages.usage-set-area", "&4&l✖ &8» &7Usage: /qcl set disable.area <areaName>");
    }
    
    public String getUsageRemoveAreaMessage() {
        return config.getString("messages.usage-remove-area", "&4&l✖ &8» &7Usage: /qcl remove disable.area <areaName>");
    }
    
    public String getUsageJoiningMessage() {
        return config.getString("messages.usage-joining", "&4&l✖ &8» &7Usage: /qcl joining <disable/enable> <areaName>");
    }
    
    public String getUsageCombatTimeMessage() {
        return config.getString("messages.usage-combat-time", "&4&l✖ &8» &7Usage: /qcl combattime <seconds>");
    }
    
    public String getUsageInventoryDropMessage() {
        return config.getString("messages.usage-inventory-drop", "&4&l✖ &8» &7Usage: /qcl inventorydrop <true/false>");
    }
}