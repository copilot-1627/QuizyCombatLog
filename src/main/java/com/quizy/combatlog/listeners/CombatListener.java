package com.quizy.combatlog.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.quizy.combatlog.QuizyCombatLog;

public class CombatListener implements Listener {
    
    private final QuizyCombatLog plugin;
    
    public CombatListener(QuizyCombatLog plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getCombatManager().removeFromCombat(player);
        // Also remove combat for anyone who was in combat with this player
        plugin.getCombatManager().clearOpponentsWith(player);
    }
}
