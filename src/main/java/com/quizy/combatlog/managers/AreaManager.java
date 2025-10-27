package com.quizy.combatlog.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import com.quizy.combatlog.QuizyCombatLog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AreaManager {
    
    private final QuizyCombatLog plugin;
    private final File disabledAreasFile;
    private YamlConfiguration disabledAreasConfig;
    private final Map<String, DisabledArea> disabledAreas;
    
    public AreaManager(QuizyCombatLog plugin) {
        this.plugin = plugin;
        this.disabledAreasFile = new File(plugin.getDataFolder(), "disabled-areas.yml");
        this.disabledAreas = new HashMap<>();
        loadDisabledAreas();
    }
    
    public void reloadAreas() {
        disabledAreas.clear();
        loadDisabledAreas();
    }
    
    private void loadDisabledAreas() {
        if (!disabledAreasFile.exists()) {
            try {
                disabledAreasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create disabled-areas.yml file: " + e.getMessage());
                return;
            }
        }
        
        disabledAreasConfig = YamlConfiguration.loadConfiguration(disabledAreasFile);
        
        for (String areaName : disabledAreasConfig.getKeys(false)) {
            ConfigurationSection section = disabledAreasConfig.getConfigurationSection(areaName);
            if (section == null) continue;
            
            String worldName = section.getString("world");
            if (worldName == null) continue;
            
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for area '" + areaName + "'");
                continue;
            }
            
            ConfigurationSection corner1Section = section.getConfigurationSection("corner1");
            ConfigurationSection corner2Section = section.getConfigurationSection("corner2");
            
            if (corner1Section == null || corner2Section == null) {
                plugin.getLogger().warning("Invalid corner data for area '" + areaName + "'");
                continue;
            }
            
            Location corner1 = new Location(world,
                    corner1Section.getDouble("x"),
                    corner1Section.getDouble("y"),
                    corner1Section.getDouble("z"));
            
            Location corner2 = new Location(world,
                    corner2Section.getDouble("x"),
                    corner2Section.getDouble("y"),
                    corner2Section.getDouble("z"));
            
            boolean joiningDisabled = section.getBoolean("joining_disabled", false);
            
            DisabledArea area = new DisabledArea(areaName, world, corner1, corner2, joiningDisabled);
            disabledAreas.put(areaName, area);
        }
        
        plugin.getLogger().info("Loaded " + disabledAreas.size() + " disabled combat areas.");
    }
    
    public void saveDisabledAreas() {
        try {
            disabledAreasConfig.save(disabledAreasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save disabled-areas.yml file: " + e.getMessage());
        }
    }
    
    public void addDisabledArea(String name, World world, Location corner1, Location corner2) {
        // Ensure corner1 is the lower corner and corner2 is the upper corner
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        Location lowerCorner = new Location(world, minX, minY, minZ);
        Location upperCorner = new Location(world, maxX, maxY, maxZ);
        
        DisabledArea area = new DisabledArea(name, world, lowerCorner, upperCorner, false);
        disabledAreas.put(name, area);
        
        // Save to config
        String path = name + ".";
        disabledAreasConfig.set(path + "world", world.getName());
        disabledAreasConfig.set(path + "corner1.x", lowerCorner.getX());
        disabledAreasConfig.set(path + "corner1.y", lowerCorner.getY());
        disabledAreasConfig.set(path + "corner1.z", lowerCorner.getZ());
        disabledAreasConfig.set(path + "corner2.x", upperCorner.getX());
        disabledAreasConfig.set(path + "corner2.y", upperCorner.getY());
        disabledAreasConfig.set(path + "corner2.z", upperCorner.getZ());
        disabledAreasConfig.set(path + "joining_disabled", false);
        
        saveDisabledAreas();
        plugin.getLogger().info("Created disabled combat area: " + name + " in world " + world.getName());
    }
    
    public boolean removeDisabledArea(String name) {
        if (!disabledAreas.containsKey(name)) {
            return false;
        }
        
        disabledAreas.remove(name);
        disabledAreasConfig.set(name, null);
        saveDisabledAreas();
        plugin.getLogger().info("Removed disabled combat area: " + name);
        return true;
    }
    
    public void setJoiningDisabled(String name, boolean disabled) {
        DisabledArea area = disabledAreas.get(name);
        if (area != null) {
            area.setJoiningDisabled(disabled);
            disabledAreasConfig.set(name + ".joining_disabled", disabled);
            saveDisabledAreas();
            plugin.getLogger().info("Area " + name + " joining " + (disabled ? "disabled" : "enabled"));
        }
    }
    
    public boolean isInDisabledArea(Location location) {
        for (DisabledArea area : disabledAreas.values()) {
            if (area.contains(location)) {
                return true;
            }
        }
        return false;
    }
    
    public DisabledArea getDisabledAreaAt(Location location) {
        for (DisabledArea area : disabledAreas.values()) {
            if (area.contains(location)) {
                return area;
            }
        }
        return null;
    }
    
    public DisabledArea getDisabledArea(String name) {
        return disabledAreas.get(name);
    }
    
    public Set<String> getDisabledAreaNames() {
        return disabledAreas.keySet();
    }
    
    public static class DisabledArea {
        private final String name;
        private final World world;
        private final Location corner1;
        private final Location corner2;
        private boolean joiningDisabled;
        
        public DisabledArea(String name, World world, Location corner1, Location corner2, boolean joiningDisabled) {
            this.name = name;
            this.world = world;
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.joiningDisabled = joiningDisabled;
        }
        
        public boolean contains(Location location) {
            if (!location.getWorld().equals(world)) {
                return false;
            }
            
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            
            return x >= corner1.getX() && x <= corner2.getX() &&
                   y >= corner1.getY() && y <= corner2.getY() &&
                   z >= corner1.getZ() && z <= corner2.getZ();
        }
        
        public String getName() {
            return name;
        }
        
        public World getWorld() {
            return world;
        }
        
        public Location getCorner1() {
            return corner1;
        }
        
        public Location getCorner2() {
            return corner2;
        }
        
        public boolean isJoiningDisabled() {
            return joiningDisabled;
        }
        
        public void setJoiningDisabled(boolean joiningDisabled) {
            this.joiningDisabled = joiningDisabled;
        }
    }
}