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
        
        boolean wasInCombat = isInCombat(player);
        boolean opponentWasInCombat = isInCombat(opponent);
        
        // Add combat entry for both players
        combatMap.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).put(opponentUUID, endTime);
        combatMap.computeIfAbsent(opponentUUID, k -> new ConcurrentHashMap<>()).put(playerUUID, endTime);
        
        // Send combat started message only if not already in combat
        if (!wasInCombat) {
            String combatStartMessage = plugin.getConfigManager().getCombatStartedMessage();
            MessageUtils.sendMessage(player, combatStartMessage);
        }
        
        if (!opponentWasInCombat) {
            String combatStartMessage = plugin.getConfigManager().getCombatStartedMessage();
            MessageUtils.sendMessage(opponent, combatStartMessage);
        }
        
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
        long maxEndTime = opponents.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(currentTime);
        
        return Math.max(0, (int) ((maxEndTime - currentTime) / 1000));
    }
    
    public List<String> getCombatOpponents(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        List<String> opponentNames = new ArrayList<>();
        
        if (opponents == null || opponents.isEmpty()) {
            return opponentNames;
        }
        
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<UUID, Long> entry : opponents.entrySet()) {
            if (entry.getValue() > currentTime) {
                Player opponent = Bukkit.getPlayer(entry.getKey());
                if (opponent != null) {
                    long timeLeft = (entry.getValue() - currentTime) / 1000;
                    opponentNames.add(opponent.getName() + " (" + timeLeft + "s)");
                }
            }
        }
        
        return opponentNames;
    }
    
    public Map<String, Integer> getCombatOpponentsWithTime(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        Map<String, Integer> opponentsWithTime = new HashMap<>();
        
        if (opponents == null || opponents.isEmpty()) {
            return opponentsWithTime;
        }
        
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<UUID, Long> entry : opponents.entrySet()) {
            if (entry.getValue() > currentTime) {
                Player opponent = Bukkit.getPlayer(entry.getKey());
                if (opponent != null) {
                    int timeLeft = (int) ((entry.getValue() - currentTime) / 1000);
                    opponentsWithTime.put(opponent.getName(), timeLeft);
                }
            }
        }
        
        return opponentsWithTime;
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
    
    private int getCombatDuration() {
        return plugin.getConfigManager().getCombatDuration();
    }
}