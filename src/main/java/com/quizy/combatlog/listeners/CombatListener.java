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
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        if (damager.equals(victim)) return;
        
        // Do not start combat if either is inside a disabled area
        if (plugin.getAreaManager().isInDisabledArea(damager.getLocation()) || plugin.getAreaManager().isInDisabledArea(victim.getLocation())) {
            return;
        }
        
        // Optional: gate by disabled worlds (if configured)
        if (plugin.getConfig().getStringList("disabled-worlds").contains(damager.getWorld().getName())) {
            return;
        }
        
        plugin.getCombatManager().addToCombat(damager, victim);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getCombatManager().removeFromCombat(player);
        plugin.getCombatManager().clearOpponentsWith(player);
    }
}
