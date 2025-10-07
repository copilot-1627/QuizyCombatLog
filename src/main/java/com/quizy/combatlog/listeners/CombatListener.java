package com.quizy.combatlog.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.quizy.combatlog.QuizyCombatLog;

public class CombatListener implements Listener {
    
    private final QuizyCombatLog plugin;
    
    public CombatListener(QuizyCombatLog plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // Don't tag if players are the same
        if (damager.equals(victim)) {
            return;
        }
        
        // Add both players to combat
        plugin.getCombatManager().addToCombat(damager, victim);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Remove player from combat when they die
        plugin.getCombatManager().removeFromCombat(player);
    }
}