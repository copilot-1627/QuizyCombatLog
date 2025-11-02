package com.quizy.combatlog.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.utils.MessageUtils;

import java.util.List;

public class PlayerListener implements Listener {
    
    private final QuizyCombatLog plugin;
    
    public PlayerListener(QuizyCombatLog plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in combat
        if (!plugin.getCombatManager().isInCombat(player)) {
            return;
        }
        
        // Check if player has admin permission
        if (player.hasPermission("quizycombatlog.admin")) {
            return;
        }
        
        String command = event.getMessage().toLowerCase();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // Check if command is in unblocked list
        List<String> unblockedCommands = plugin.getConfigManager().getUnblockedCommands();
        for (String unblockedCommand : unblockedCommands) {
            if (command.startsWith(unblockedCommand.toLowerCase())) {
                return;
            }
        }
        
        // Block the command
        event.setCancelled(true);
        String blockedMessage = plugin.getConfigManager().getCommandBlockedMessage();
        MessageUtils.sendMessage(player, blockedMessage);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in combat
        if (plugin.getCombatManager().isInCombat(player)) {
            // Drop inventory immediately at the location where player left
            if (plugin.getConfigManager().shouldDropInventory()) {
                // Drop all items from inventory
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
                
                // Drop all items from armor slots
                for (ItemStack item : player.getInventory().getArmorContents()) {
                    if (item != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
                
                // Drop off-hand item
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (offHandItem != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), offHandItem);
                }
                
                // Clear inventory immediately
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getInventory().setItemInOffHand(null);
            }
            
            // Kill player on the spot
            player.setHealth(0.0);
            
            // Broadcast combat log message
            String combatLogMessage = plugin.getConfigManager().getPlayerCombatLoggedMessage()
                    .replace("{player}", player.getName());
            Bukkit.broadcastMessage(MessageUtils.colorize(combatLogMessage));
            
            // Remove from combat tracking
            plugin.getCombatManager().removeFromCombat(player);
        }
    }
}