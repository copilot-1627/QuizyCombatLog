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
    private final Set<String> disabledWorlds = new HashSet<>();
    
    public QCLCommand(QuizyCombatLog plugin) {
        this.plugin = plugin;
        // Load disabled worlds from config
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
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("quizycombatlog.reload")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        plugin.getConfigManager().reloadConfig();
        // Reload disabled worlds
        disabledWorlds.clear();
        disabledWorlds.addAll(plugin.getConfig().getStringList("disabled-worlds"));
        MessageUtils.sendMessage(sender, plugin.getConfigManager().getReloadMessage());
        return true;
    }
    
    private boolean handleCombatTime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getUsageCombatTimeMessage());
            return true;
        }
        
        try {
            int time = Integer.parseInt(args[1]);
            if (time < 1) {
                MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Combat time must be at least 1 second!");
                return true;
            }
            
            plugin.getConfigManager().setCombatDuration(time);
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getCombatTimeSetMessage(time));
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getInvalidNumberMessage());
        }
        return true;
    }
    
    private boolean handleInventoryDrop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getUsageInventoryDropMessage());
            return true;
        }
        
        if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
            boolean drop = Boolean.parseBoolean(args[1]);
            plugin.getConfigManager().setInventoryDrop(drop);
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getInventoryDropSetMessage(drop));
        } else {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getInvalidBooleanMessage());
        }
        return true;
    }
    
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
        
        // Check if combat is disabled in this world
        if (disabledWorlds.contains(player.getWorld().getName())) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Combat features are disabled in this world.");
            return true;
        }
        
        String areaName = args[2];
        
        // Check if area already exists
        if (plugin.getAreaManager().getDisabledArea(areaName) != null) {
            String message = plugin.getConfigManager().getAreaAlreadyExistsMessage()
                    .replace("{areaName}", areaName);
            MessageUtils.sendMessage(sender, message);
            return true;
        }
        
        // Give the player the combat configure stick
        ItemStack configStick = plugin.getCombatConfigStickListener().createConfigStick(areaName);
        player.getInventory().addItem(configStick);
        
        String message = plugin.getConfigManager().getConfigStickGivenMessage()
                .replace("{areaName}", areaName);
        MessageUtils.sendMessage(sender, message);
        MessageUtils.sendMessage(sender, "&7Right-click two corners to define the protected area.");
        return true;
    }
    
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        if (args.length < 3 || !args[1].equalsIgnoreCase("disable.area")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getUsageRemoveAreaMessage());
            return true;
        }
        
        String areaName = args[2];
        
        if (plugin.getAreaManager().removeDisabledArea(areaName)) {
            String message = plugin.getConfigManager().getAreaRemovedMessage()
                    .replace("{areaName}", areaName);
            MessageUtils.sendMessage(sender, message);
        } else {
            String message = plugin.getConfigManager().getAreaDoesNotExistMessage()
                    .replace("{areaName}", areaName);
            MessageUtils.sendMessage(sender, message);
        }
        return true;
    }
    
    private boolean handleJoining(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        if (args.length < 3) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getUsageJoiningMessage());
            return true;
        }
        
        String action = args[1].toLowerCase();
        String areaName = args[2];
        
        if (!action.equals("disable") && !action.equals("enable")) {
            MessageUtils.sendMessage(sender, plugin.getConfigManager().getUsageJoiningMessage());
            return true;
        }
        
        if (plugin.getAreaManager().getDisabledArea(areaName) == null) {
            String message = plugin.getConfigManager().getAreaDoesNotExistMessage()
                    .replace("{areaName}", areaName);
            MessageUtils.sendMessage(sender, message);
            return true;
        }
        
        boolean disable = action.equals("disable");
        plugin.getAreaManager().setJoiningDisabled(areaName, disable);
        
        String message;
        if (disable) {
            message = plugin.getConfigManager().getJoiningDisabledMessage()
                    .replace("{areaName}", areaName);
        } else {
            message = plugin.getConfigManager().getJoiningEnabledMessage()
                    .replace("{areaName}", areaName);
        }
        MessageUtils.sendMessage(sender, message);
        return true;
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
            MessageUtils.sendMessage(sender, "&c&l✓ &8» &7Disabled combat features in world &e" + worldName + "&7.");
        } else {
            disabledWorlds.remove(worldName);
            saveDisabledWorlds();
            MessageUtils.sendMessage(sender, "&a&l✓ &8» &7Enabled combat features in world &e" + worldName + "&7.");
        }
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&8&l&m─────────────────────────────────────────────────────────");
        MessageUtils.sendMessage(sender, "&e&lQuizyCombatLog &8» &7Commands Help");
        MessageUtils.sendMessage(sender, "&8&l&m─────────────────────────────────────────────────────────");
        MessageUtils.sendMessage(sender, "&7• &e/qcl help &8» &7Show this help message");
        
        if (sender.hasPermission("quizycombatlog.reload")) {
            MessageUtils.sendMessage(sender, "&7• &e/qcl reload &8» &7Reload plugin configuration");
        }
        
        if (sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, "&7• &e/qcl combattime <seconds> &8» &7Set combat duration");
            MessageUtils.sendMessage(sender, "&7• &e/qcl inventorydrop <true/false> &8» &7Toggle inventory drop");
            MessageUtils.sendMessage(sender, "&7• &e/qcl set disable.area <name> &8» &7Create no-combat zone");
            MessageUtils.sendMessage(sender, "&7• &e/qcl remove disable.area <name> &8» &7Remove no-combat zone");
            MessageUtils.sendMessage(sender, "&7• &e/qcl joining disable <name> &8» &7Prevent combat entry");
            MessageUtils.sendMessage(sender, "&7• &e/qcl joining enable <name> &8» &7Allow combat entry");
            MessageUtils.sendMessage(sender, "&7• &e/qcl world enable/disable <world> &8» &7Toggle combat per world");
        }
        
        MessageUtils.sendMessage(sender, "&8&l&m─────────────────────────────────────────────────────────");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("help");
            if (sender.hasPermission("quizycombatlog.reload")) {
                commands = new ArrayList<>(commands);
                commands.add("reload");
            }
            if (sender.hasPermission("quizycombatlog.admin")) {
                commands = new ArrayList<>(commands);
                commands.addAll(Arrays.asList("combattime", "inventorydrop", "set", "remove", "joining", "world"));
            }
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "set":
                case "remove":
                    if (sender.hasPermission("quizycombatlog.admin")) {
                        completions.add("disable.area");
                    }
                    break;
                case "joining":
                    if (sender.hasPermission("quizycombatlog.admin")) {
                        completions.addAll(Arrays.asList("disable", "enable"));
                    }
                    break;
                case "inventorydrop":
                    if (sender.hasPermission("quizycombatlog.admin")) {
                        completions.addAll(Arrays.asList("true", "false"));
                    }
                    break;
                case "world":
                    if (sender.hasPermission("quizycombatlog.admin")) {
                        completions.addAll(Arrays.asList("enable", "disable"));
                    }
                    break;
            }
        }
        
        if (args.length == 3) {
            if ((args[0].equalsIgnoreCase("remove") && args[1].equalsIgnoreCase("disable.area")) ||
                args[0].equalsIgnoreCase("joining")) {
                if (sender.hasPermission("quizycombatlog.admin")) {
                    completions.addAll(plugin.getAreaManager().getDisabledAreaNames());
                }
            } else if (args[0].equalsIgnoreCase("world")) {
                if (sender.hasPermission("quizycombatlog.admin")) {
                    for (World world : plugin.getServer().getWorlds()) {
                        completions.add(world.getName());
                    }
                }
            }
        }
        
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}