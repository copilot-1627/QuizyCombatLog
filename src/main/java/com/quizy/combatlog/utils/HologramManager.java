package com.quizy.combatlog.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.managers.AreaManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HologramManager {
    
    private final QuizyCombatLog plugin;
    private final Map<UUID, Set<Location>> playerHolograms;
    
    public HologramManager(QuizyCombatLog plugin) {
        this.plugin = plugin;
        this.playerHolograms = new HashMap<>();
    }
    
    public void showBoundaryHolograms(Player player, Location playerLocation) {
        UUID playerUUID = player.getUniqueId();
        Set<Location> currentHolograms = playerHolograms.computeIfAbsent(playerUUID, k -> new HashSet<>());
        
        // Clear existing holograms first
        clearHolograms(player, currentHolograms);
        
        // Check all disabled areas
        for (String areaName : plugin.getAreaManager().getDisabledAreaNames()) {
            AreaManager.DisabledArea area = plugin.getAreaManager().getDisabledArea(areaName);
            if (area != null && area.isJoiningDisabled() && area.getWorld().equals(playerLocation.getWorld())) {
                showAreaBoundary(player, area, playerLocation, currentHolograms);
            }
        }
    }
    
    private void showAreaBoundary(Player player, AreaManager.DisabledArea area, Location playerLocation, Set<Location> holograms) {
        Location corner1 = area.getCorner1();
        Location corner2 = area.getCorner2();
        
        // Get boundary range from config
        int boundaryRange = plugin.getConfigManager().getHologramBoundaryRange();
        
        // Check if player is within boundary range
        if (getDistanceToArea(playerLocation, area) > boundaryRange) {
            return;
        }
        
        Material hologramMaterial = Material.valueOf(plugin.getConfigManager().getHologramMaterial());
        
        // Show boundary blocks around the area
        int minX = (int) Math.floor(corner1.getX());
        int maxX = (int) Math.floor(corner2.getX());
        int minY = (int) Math.floor(corner1.getY());
        int maxY = (int) Math.floor(corner2.getY());
        int minZ = (int) Math.floor(corner1.getZ());
        int maxZ = (int) Math.floor(corner2.getZ());
        
        // Show boundary on all 6 faces of the area
        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ - 1; z <= maxZ + 1; z++) {
                    // Only show boundary blocks, not the entire area
                    if (isBoundaryBlock(x, y, z, minX, maxX, minY, maxY, minZ, maxZ)) {
                        Location hologramLoc = new Location(area.getWorld(), x, y, z);
                        
                        // Only show if within player's view distance and close enough
                        if (hologramLoc.distance(playerLocation) <= 10) {
                            player.sendBlockChange(hologramLoc, hologramMaterial.createBlockData());
                            holograms.add(hologramLoc);
                        }
                    }
                }
            }
        }
    }
    
    private boolean isBoundaryBlock(int x, int y, int z, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        // Check if block is on the boundary of the area (1 block outside the area)
        boolean onXBoundary = (x == minX - 1 || x == maxX + 1);
        boolean onZBoundary = (z == minZ - 1 || z == maxZ + 1);
        boolean onYBoundary = (y == minY || y == maxY);
        
        // Block is on boundary if it's on at least one face
        return (onXBoundary && z >= minZ - 1 && z <= maxZ + 1 && y >= minY && y <= maxY) ||
               (onZBoundary && x >= minX - 1 && x <= maxX + 1 && y >= minY && y <= maxY) ||
               (onYBoundary && x >= minX - 1 && x <= maxX + 1 && z >= minZ - 1 && z <= maxZ + 1);
    }
    
    private double getDistanceToArea(Location playerLocation, AreaManager.DisabledArea area) {
        Location corner1 = area.getCorner1();
        Location corner2 = area.getCorner2();
        
        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();
        double playerZ = playerLocation.getZ();
        
        // Calculate closest point on the area to the player
        double closestX = Math.max(corner1.getX(), Math.min(playerX, corner2.getX()));
        double closestY = Math.max(corner1.getY(), Math.min(playerY, corner2.getY()));
        double closestZ = Math.max(corner1.getZ(), Math.min(playerZ, corner2.getZ()));
        
        // Calculate distance from player to closest point
        double dx = playerX - closestX;
        double dy = playerY - closestY;
        double dz = playerZ - closestZ;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    private void clearHolograms(Player player, Set<Location> holograms) {
        for (Location hologramLoc : holograms) {
            // Reset block to its original state
            player.sendBlockChange(hologramLoc, hologramLoc.getBlock().getBlockData());
        }
        holograms.clear();
    }
    
    public void removeHolograms(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<Location> holograms = playerHolograms.get(playerUUID);
        
        if (holograms != null) {
            clearHolograms(player, holograms);
            playerHolograms.remove(playerUUID);
        }
    }
    
    public void removeAllHolograms() {
        for (UUID playerUUID : new HashSet<>(playerHolograms.keySet())) {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                removeHolograms(player);
            }
        }
        playerHolograms.clear();
    }
}