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

        // Add/refresh combat entry for both players with new endTime
        combatMap.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).put(opponentUUID, endTime);
        combatMap.computeIfAbsent(opponentUUID, k -> new ConcurrentHashMap<>()).put(playerUUID, endTime);

        if (!wasInCombat) {
            MessageUtils.sendMessage(player, plugin.getConfigManager().getCombatStartedMessage());
        }
        if (!opponentWasInCombat) {
            MessageUtils.sendMessage(opponent, plugin.getConfigManager().getCombatStartedMessage());
        }

        startCombatEndTask(player);
        startCombatEndTask(opponent);
    }

    private void startCombatEndTask(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitTask existingTask = combatTasks.get(playerUUID);
        if (existingTask != null) existingTask.cancel();

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isInCombat(player)) {
                removeCombatTask(playerUUID);
                MessageUtils.sendMessage(player, plugin.getConfigManager().getCombatEndedMessage());
            }
        }, 20L, 20L);
        combatTasks.put(playerUUID, task);
    }

    public boolean isInCombat(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        if (opponents == null || opponents.isEmpty()) return false;

        long now = System.currentTimeMillis();
        opponents.entrySet().removeIf(e -> e.getValue() <= now);
        if (opponents.isEmpty()) {
            combatMap.remove(playerUUID);
            return false;
        }
        return true;
    }

    public int getRemainingCombatTime(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        if (opponents == null || opponents.isEmpty()) return 0;
        long now = System.currentTimeMillis();
        long maxEndTime = opponents.values().stream().mapToLong(Long::longValue).max().orElse(now);
        return Math.max(0, (int) ((maxEndTime - now) / 1000));
    }

    public Map<String, Integer> getCombatOpponentsWithTime(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Long> opponents = combatMap.get(playerUUID);
        Map<String, Integer> out = new HashMap<>();
        if (opponents == null || opponents.isEmpty()) return out;
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> e : opponents.entrySet()) {
            if (e.getValue() > now) {
                Player op = Bukkit.getPlayer(e.getKey());
                if (op != null) out.put(op.getName(), (int) ((e.getValue() - now) / 1000));
            }
        }
        return out;
    }

    public void removeFromCombat(Player player) {
        UUID playerUUID = player.getUniqueId();
        combatMap.remove(playerUUID);
        removeCombatTask(playerUUID);
        // Also remove this player from all other players' opponent maps
        for (Map<UUID, Long> ops : combatMap.values()) {
            ops.remove(playerUUID);
        }
    }

    public void clearOpponentsWith(Player player) {
        UUID playerUUID = player.getUniqueId();
        // Remove the dead player from others and cancel combat for those who now have no opponents
        for (Map.Entry<UUID, Map<UUID, Long>> entry : combatMap.entrySet()) {
            UUID other = entry.getKey();
            Map<UUID, Long> ops = entry.getValue();
            if (ops.remove(playerUUID) != null && ops.isEmpty()) {
                combatMap.remove(other);
                removeCombatTask(other);
                Player p = Bukkit.getPlayer(other);
                if (p != null) MessageUtils.sendMessage(p, plugin.getConfigManager().getCombatEndedMessage());
            }
        }
    }

    private void removeCombatTask(UUID playerUUID) {
        BukkitTask task = combatTasks.remove(playerUUID);
        if (task != null) task.cancel();
    }

    public void clearAllCombat() {
        combatMap.clear();
        combatTasks.values().forEach(BukkitTask::cancel);
        combatTasks.clear();
    }

    public Set<UUID> getPlayersInCombat() { return new HashSet<>(combatMap.keySet()); }

    private int getCombatDuration() { return plugin.getConfigManager().getCombatDuration(); }
}
