package com.quizy.combatlog.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {
    
    private final QuizyCombatLog plugin;
    private final Map<UUID, Map<UUID, Long>> combatMap; // Player UUID -> Map of opponent UUID and end time
    private final Map<UUID, BukkitTask> combatTasks;
    
    public CombatManager(QuizyCombatLog plugin) {
        this.plugin = plugin;
        this.combatMap = new ConcurrentHashMap<>();
        this.combatTasks = new ConcurrentHashMap<>();
    }
    
    public void addToCombat(Player player, Player opponent) {
        UUID playerUUID = player.getUniqueId();
        UUID opponentUUID = opponent.getUniqueId();
        
        long endTime = System.currentTimeMillis() + (getCombatDuration() * 1000L);
        
        // Add combat entry for both players
        combatMap.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).put(opponentUUID, endTime);
        combatMap.computeIfAbsent(opponentUUID, k -> new ConcurrentHashMap<>()).put(playerUUID, endTime);
        
        // Send combat started message
        String combatStartMessage = plugin.getConfigManager().getCombatStartedMessage();
        MessageUtils.sendMessage(player, combatStartMessage);
        MessageUtils.sendMessage(opponent, combatStartMessage);
        
        // Start combat end task for both players
        startCombatEndTask(player);
        startCombatEndTask(opponent);
    }
    
    private void startCombatEndTask(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Cancel existing task if any
        BukkitTask existingTask = combatTasks.get(playerUUID);
        if (existingTask != null) {
            existingTask.cancel();
        }
        
        // Start new task
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isInCombat(player)) {
                removeCombatTask(playerUUID);
                String combatEndMessage = plugin.getConfigManager().getCombatEndedMessage();
                MessageUtils.sendMessage(player, combatEndMessage);
            }
        }, 20L, 20L); // Check every second
        
        combatTasks.put(playerUUID, task);
    }
    
    public boolean isInCombat(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        
        if (opponents == null || opponents.isEmpty()) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Remove expired combat entries
        opponents.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
        
        // Remove empty map if no opponents left
        if (opponents.isEmpty()) {
            combatMap.remove(playerUUID);
            return false;
        }
        
        return true;
    }
    
    public int getRemainingCombatTime(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        
        if (opponents == null || opponents.isEmpty()) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long minEndTime = opponents.values().stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(currentTime);
        
        return Math.max(0, (int) ((minEndTime - currentTime) / 1000));
    }
    
    public void removeFromCombat(Player player) {
        UUID playerUUID = player.getUniqueId();
        combatMap.remove(playerUUID);
        removeCombatTask(playerUUID);
    }
    
    private void removeCombatTask(UUID playerUUID) {
        BukkitTask task = combatTasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }
    }
    
    public void clearAllCombat() {
        combatMap.clear();
        combatTasks.values().forEach(BukkitTask::cancel);
        combatTasks.clear();
    }
    
    public Set<UUID> getPlayersInCombat() {
        return new HashSet<>(combatMap.keySet());
    }
    
    public void handleCombatLog(Player player) {
        if (plugin.getConfigManager().shouldDropInventory()) {
            player.getInventory().clear();
            player.getEnderChest().clear();
        }
        
        // Broadcast combat log message
        String combatLogMessage = plugin.getConfigManager().getPlayerCombatLoggedMessage()
                .replace("{player}", player.getName());
        Bukkit.broadcastMessage(MessageUtils.colorize(combatLogMessage));
        
        // Remove from combat
        removeFromCombat(player);
        
        // Kill the player
        player.setHealth(0);
    }
    
    private int getCombatDuration() {
        return plugin.getConfigManager().getCombatDuration();
    }
}