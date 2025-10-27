package com.quizy.combatlog.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import com.quizy.combatlog.QuizyCombatLog;

import java.util.List;
import java.util.Map;
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
                        String actionBarMessage = buildActionBarMessage(player, remainingTime);
                        MessageUtils.sendActionBar(player, actionBarMessage);
                    }
                }
            }
        }, 0L, 20L); // Update every second
    }
    
    private String buildActionBarMessage(Player player, int remainingTime) {
        Map<String, Integer> opponents = plugin.getCombatManager().getCombatOpponentsWithTime(player);
        
        if (opponents.isEmpty()) {
            // Fallback to basic format
            String actionBarFormat = plugin.getConfigManager().getActionBarFormat();
            return actionBarFormat.replace("{time}", String.valueOf(remainingTime));
        }
        
        StringBuilder messageBuilder = new StringBuilder();
        String multipleOpponentsFormat = plugin.getConfigManager().getActionBarMultipleOpponentsFormat();
        
        if (opponents.size() == 1) {
            // Single opponent format
            String singleOpponentFormat = plugin.getConfigManager().getActionBarSingleOpponentFormat();
            String opponentName = opponents.keySet().iterator().next();
            int opponentTime = opponents.values().iterator().next();
            
            messageBuilder.append(singleOpponentFormat
                    .replace("{opponent}", opponentName)
                    .replace("{time}", String.valueOf(opponentTime)));
        } else {
            // Multiple opponents format
            messageBuilder.append(multipleOpponentsFormat.replace("{time}", String.valueOf(remainingTime)));
            messageBuilder.append(" ");
            
            boolean first = true;
            for (Map.Entry<String, Integer> entry : opponents.entrySet()) {
                if (!first) {
                    messageBuilder.append(", ");
                }
                messageBuilder.append("&e").append(entry.getKey())
                           .append("&7(").append(entry.getValue()).append("s)");
                first = false;
            }
        }
        
        return MessageUtils.colorize(messageBuilder.toString());
    }
    
    public void stopActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }
}