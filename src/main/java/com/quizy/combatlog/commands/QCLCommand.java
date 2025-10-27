package com.quizy.combatlog.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.utils.MessageUtils;

import java.util.*;
import java.util.stream.Collectors;

public class QCLCommand implements CommandExecutor, TabCompleter {
    
    private final QuizyCombatLog plugin;
    // Worlds toggle map (in-memory + config-backed)
    private final Set<String> disabledWorlds = new HashSet<>();
    
    public QCLCommand(QuizyCombatLog plugin) {
        this.plugin = plugin;
        // Load from config
        disabledWorlds.addAll(plugin.getConfig().getStringList("disabled-worlds"));
    }
    
    private boolean isWorldEnabled(World world) {
        return !disabledWorlds.contains(world.getName());
    }
    
    private void saveDisabledWorlds() {
        plugin.getConfig().set("disabled-worlds", new ArrayList<>(disabledWorlds));
        plugin.saveConfig();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "combattime":
                return handleCombatTime(sender, args);
            case "inventorydrop":
                return handleInventoryDrop(sender, args);
            case "set":
                return handleSet(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "joining":
                return handleJoining(sender, args);
            case "world":
                return handleWorld(sender, args);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    private boolean handleWorld(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        if (args.length < 3) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl world <enable/disable> <world>");
            return true;
        }
        String action = args[1].toLowerCase();
        String worldName = args[2];
        if (!action.equals("enable") && !action.equals("disable")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl world <enable/disable> <world>");
            return true;
        }
        if (action.equals("disable")) {
            disabledWorlds.add(worldName);
            saveDisabledWorlds();
            MessageUtils.sendMessage(sender, "&c&l✔ &8» &7Disabled combat features in world &e" + worldName + "&7.");
        } else {
            disabledWorlds.remove(worldName);
            saveDisabledWorlds();
            MessageUtils.sendMessage(sender, "&a&l✔ &8» &7Enabled combat features in world &e" + worldName + "&7.");
        }
        return true;
    }
    
    private boolean worldIsDisabled(Player p) {
        return disabledWorlds.contains(p.getWorld().getName());
    }
    
    // ... existing handlers below unchanged except we gate player-world interactions
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getPlayerOnlyCommandMessage());
            return true;
        }
        if (args.length < 3 || !args[1].equalsIgnoreCase("disable.area")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getUsageSetAreaMessage());
            return true;
        }
        Player player = (Player) sender;
        if (worldIsDisabled(player)) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Combat features are disabled in this world.");
            return true;
        }
        String areaName = args[2];
        if (plugin.getAreaManager().getDisabledArea(areaName) != null) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getAreaAlreadyExistsMessage().replace("{areaName}", areaName));
            return true;
        }
        ItemStack configStick = plugin.getCombatConfigStickListener().createConfigStick(areaName);
        player.getInventory().addItem(configStick);
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getConfigStickGivenMessage().replace("{areaName}", areaName));
        return true;
    }
    
    // Tab completion updated to include 'world'
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> commands = new ArrayList<>(Arrays.asList("help"));
            if (sender.hasPermission("quizycombatlog.reload")) commands.add("reload");
            if (sender.hasPermission("quizycombatlog.admin")) commands.addAll(Arrays.asList("combattime", "inventorydrop", "set", "remove", "joining", "world"));
            return commands.stream().filter(c -> c.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("world")) {
            completions.addAll(Arrays.asList("enable", "disable"));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("world")) {
            for (World w : plugin.getServer().getWorlds()) completions.add(w.getName());
        }
        return completions.stream().filter(c -> c.toLowerCase().startsWith(args[args.length-1].toLowerCase())).collect(Collectors.toList());
    }

    // other existing methods (reload, combattime, inventorydrop, remove, joining) remain same as previously updated
}
