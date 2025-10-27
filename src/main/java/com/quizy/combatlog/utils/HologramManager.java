package com.quizy.combatlog.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
        if (!plugin.getConfigManager().isHologramEnabled()) return;
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
        
        int boundaryRange = plugin.getConfigManager().getHologramBoundaryRange();
        if (getDistanceToArea(playerLocation, area) > boundaryRange) {
            return;
        }
        
        Material hologramMaterial = Material.matchMaterial(plugin.getConfigManager().getHologramMaterial());
        if (hologramMaterial == null) hologramMaterial = Material.RED_STAINED_GLASS_PANE;
        
        int minX = (int) Math.floor(corner1.getX());
        int maxX = (int) Math.floor(corner2.getX());
        int minY = (int) Math.floor(corner1.getY());
        int maxY = (int) Math.floor(corner2.getY());
        int minZ = (int) Math.floor(corner1.getZ());
        int maxZ = (int) Math.floor(corner2.getZ());
        
        // Draw connected hollow frame: edges only for connectivity
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean onFaceX = (x == minX || x == maxX) && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
                    boolean onFaceZ = (z == minZ || z == maxZ) && y >= minY && y <= maxY && x >= minX && x <= maxX;
                    boolean onFaceY = (y == minY || y == maxY) && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
                    
                    // Keep it hollow: draw only edges/lines
                    boolean isEdge = (
                            (x == minX || x == maxX) && (z == minZ || z == maxZ) && y >= minY && y <= maxY) ||
                            ((x == minX || x == maxX) && (y == minY || y == maxY) && z >= minZ && z <= maxZ) ||
                            ((z == minZ || z == maxZ) && (y == minY || y == maxY) && x >= minX && x <= maxX);
                    
                    if (isEdge) {
                        Location loc = new Location(area.getWorld(), x, y, z);
                        if (loc.distance(playerLocation) <= 32) {
                            player.sendBlockChange(loc, hologramMaterial.createBlockData());
                            holograms.add(loc);
                        }
                    }
                }
            }
        }
    }
    
    private double getDistanceToArea(Location playerLocation, AreaManager.DisabledArea area) {
        Location c1 = area.getCorner1();
        Location c2 = area.getCorner2();
        double px = playerLocation.getX();
        double py = playerLocation.getY();
        double pz = playerLocation.getZ();
        double cx = Math.max(c1.getX(), Math.min(px, c2.getX()));
        double cy = Math.max(c1.getY(), Math.min(py, c2.getY()));
        double cz = Math.max(c1.getZ(), Math.min(pz, c2.getZ()));
        double dx = px - cx, dy = py - cy, dz = pz - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    private void clearHolograms(Player player, Set<Location> holograms) {
        for (Location loc : holograms) {
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
        }
        holograms.clear();
    }
    
    public void removeHolograms(Player player) {
        UUID id = player.getUniqueId();
        Set<Location> set = playerHolograms.get(id);
        if (set != null) {
            clearHolograms(player, set);
            playerHolograms.remove(id);
        }
    }
    
    public void removeAllHolograms() {
        for (UUID id : new HashSet<>(playerHolograms.keySet())) {
            Player p = plugin.getServer().getPlayer(id);
            if (p != null && p.isOnline()) removeHolograms(p);
        }
        playerHolograms.clear();
    }
}
