package com.quizy.combatlog.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.managers.AreaManager;
import com.quizy.combatlog.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatConfigStickListener implements Listener {
    
    private final QuizyCombatLog plugin;
    private final NamespacedKey configStickKey;
    private final NamespacedKey areaNameKey;
    private final Map<UUID, Location> firstCorners;
    
    public CombatConfigStickListener(QuizyCombatLog plugin) {
        this.plugin = plugin;
        this.configStickKey = new NamespacedKey(plugin, "config_stick");
        this.areaNameKey = new NamespacedKey(plugin, "area_name");
        this.firstCorners = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.STICK) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(configStickKey, PersistentDataType.BOOLEAN)) {
            return;
        }
        
        String areaName = container.get(areaNameKey, PersistentDataType.STRING);
        if (areaName == null) {
            return;
        }
        
        event.setCancelled(true);
        
        Location clickedLocation = event.getClickedBlock().getLocation();
        UUID playerUUID = player.getUniqueId();
        
        if (!firstCorners.containsKey(playerUUID)) {
            // Set first corner
            firstCorners.put(playerUUID, clickedLocation);
            MessageUtils.sendMessage(player, "§aFirst corner set at (" + 
                    clickedLocation.getBlockX() + ", " + 
                    clickedLocation.getBlockY() + ", " + 
                    clickedLocation.getBlockZ() + ").");
        } else {
            // Set second corner and create area
            Location firstCorner = firstCorners.remove(playerUUID);
            
            plugin.getAreaManager().addDisabledArea(areaName, player.getWorld(), firstCorner, clickedLocation);
            
            MessageUtils.sendMessage(player, "§aSecond corner set. Area §e" + areaName + "§a saved successfully!");
            MessageUtils.sendMessage(player, "§e Combat Configure Stick §7removed.");
            
            // Remove the stick from player's hand
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null || (from.getBlockX() == to.getBlockX() && 
                          from.getBlockY() == to.getBlockY() && 
                          from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Check if player is in combat
        if (!plugin.getCombatManager().isInCombat(player)) {
            return;
        }
        
        // Check if player is trying to enter a disabled area
        AreaManager.DisabledArea area = plugin.getAreaManager().getDisabledAreaAt(to);
        if (area != null && area.isJoiningDisabled()) {
            // Check if they're coming from outside the area
            if (!area.contains(from)) {
                event.setCancelled(true);
                
                // Push player back
                Vector direction = from.toVector().subtract(to.toVector()).normalize();
                Location pushLocation = from.add(direction.multiply(10));
                
                // Ensure the push location is safe
                pushLocation.setY(Math.max(pushLocation.getY(), from.getY()));
                player.teleport(pushLocation);
                
                MessageUtils.sendMessage(player, "§cYou cannot enter §e" + area.getName() + "§c while in combat!");
            }
        }
    }
    
    public ItemStack createConfigStick(String areaName) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtils.colorize("§eCombat Configure Stick"));
            meta.setLore(java.util.Arrays.asList(
                    MessageUtils.colorize("§7Right-click two corners to define"),
                    MessageUtils.colorize("§7the protected area.")
            ));
            
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(configStickKey, PersistentDataType.BOOLEAN, true);
            container.set(areaNameKey, PersistentDataType.STRING, areaName);
            
            stick.setItemMeta(meta);
        }
        
        return stick;
    }
}