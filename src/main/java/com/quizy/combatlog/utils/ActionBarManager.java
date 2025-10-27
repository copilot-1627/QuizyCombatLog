package com.quizy.combatlog.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import com.quizy.combatlog.QuizyCombatLog;

import java.util.UUID;

public class ActionBarManager {
    
    private final QuizyCombatLog plugin;
    private BukkitTask actionBarTask;
    
    public ActionBarManager(QuizyCombatLog plugin) {
        this.plugin = plugin;
    }
    
    public void startActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Only run if action bar is enabled
            if (!plugin.getConfigManager().isActionBarEnabled()) {
                return;
            }
            
            for (UUID playerUUID : plugin.getCombatManager().getPlayersInCombat()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    int remainingTime = plugin.getCombatManager().getRemainingCombatTime(player);
                    if (remainingTime > 0) {
                        String actionBarFormat = plugin.getConfigManager().getActionBarFormat();
                        String actionBarMessage = actionBarFormat.replace("{time}", String.valueOf(remainingTime));
                        MessageUtils.sendActionBar(player, actionBarMessage);
                    }
                }
            }
        }, 0L, 20L); // Update every second
    }
    
    public void stopActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }
}