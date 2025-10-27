package com.quizy.combatlog.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QCLCommand implements CommandExecutor, TabCompleter {
    
    private final QuizyCombatLog plugin;
    
    public QCLCommand(QuizyCombatLog plugin) {
        this.plugin = plugin;
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
                
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("quizycombatlog.reload")) {
            String noPermMessage = plugin.getConfigManager().getNoPermissionMessage();
            MessageUtils.sendMessage(sender, noPermMessage);
            return true;
        }
        
        plugin.getConfigManager().reloadConfig();
        String reloadMessage = plugin.getConfigManager().getReloadMessage();
        MessageUtils.sendMessage(sender, reloadMessage);
        return true;
    }
    
    private boolean handleCombatTime(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            String noPermMessage = plugin.getConfigManager().getNoPermissionMessage();
            MessageUtils.sendMessage(sender, noPermMessage);
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl combattime <seconds>");
            return true;
        }
        
        try {
            int time = Integer.parseInt(args[1]);
            if (time < 1) {
                MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Combat time must be at least 1 second!");
                return true;
            }
            
            plugin.getConfigManager().setCombatDuration(time);
            String timeSetMessage = plugin.getConfigManager().getCombatTimeSetMessage(time);
            MessageUtils.sendMessage(sender, timeSetMessage);
        } catch (NumberFormatException e) {
            String invalidNumberMessage = plugin.getConfigManager().getInvalidNumberMessage();
            MessageUtils.sendMessage(sender, invalidNumberMessage);
        }
        return true;
    }
    
    private boolean handleInventoryDrop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            String noPermMessage = plugin.getConfigManager().getNoPermissionMessage();
            MessageUtils.sendMessage(sender, noPermMessage);
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl inventorydrop <true/false>");
            return true;
        }
        
        if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
            boolean drop = Boolean.parseBoolean(args[1]);
            plugin.getConfigManager().setInventoryDrop(drop);
            String dropSetMessage = plugin.getConfigManager().getInventoryDropSetMessage(drop);
            MessageUtils.sendMessage(sender, dropSetMessage);
        } else {
            String invalidBoolMessage = plugin.getConfigManager().getInvalidBooleanMessage();
            MessageUtils.sendMessage(sender, invalidBoolMessage);
        }
        return true;
    }
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7You don't have permission to use this command!");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7This command can only be used by players!");
            return true;
        }
        
        if (args.length < 3 || !args[1].equalsIgnoreCase("disable.area")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl set disable.area <areaName>");
            return true;
        }
        
        Player player = (Player) sender;
        String areaName = args[2];
        
        // Check if area already exists
        if (plugin.getAreaManager().getDisabledArea(areaName) != null) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Area '" + areaName + "' already exists!");
            return true;
        }
        
        // Give the player the combat configure stick
        ItemStack configStick = plugin.getCombatConfigStickListener().createConfigStick(areaName);
        player.getInventory().addItem(configStick);
        
        MessageUtils.sendMessage(sender, "&a&l✓ &8» &7You received a &eCombat Configure Stick&7 for area '&e" + areaName + "&7'!");
        MessageUtils.sendMessage(sender, "&7Right-click two corners to define the protected area.");
        return true;
    }
    
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7You don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 3 || !args[1].equalsIgnoreCase("disable.area")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl remove disable.area <areaName>");
            return true;
        }
        
        String areaName = args[2];
        
        if (plugin.getAreaManager().removeDisabledArea(areaName)) {
            MessageUtils.sendMessage(sender, "&c&l✓ &8» &7Area &e" + areaName + "&c has been removed from disabled zones.");
        } else {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Area '" + areaName + "' does not exist!");
        }
        return true;
    }
    
    private boolean handleJoining(CommandSender sender, String[] args) {
        if (!sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7You don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 3) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl joining <disable/enable> <areaName>");
            return true;
        }
        
        String action = args[1].toLowerCase();
        String areaName = args[2];
        
        if (!action.equals("disable") && !action.equals("enable")) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Usage: /qcl joining <disable/enable> <areaName>");
            return true;
        }
        
        if (plugin.getAreaManager().getDisabledArea(areaName) == null) {
            MessageUtils.sendMessage(sender, "&4&l✖ &8» &7Area '" + areaName + "' does not exist!");
            return true;
        }
        
        boolean disable = action.equals("disable");
        plugin.getAreaManager().setJoiningDisabled(areaName, disable);
        
        if (disable) {
            MessageUtils.sendMessage(sender, "&c&l✓ &8» &7Players in combat can no longer enter &e" + areaName + "&c.");
        } else {
            MessageUtils.sendMessage(sender, "&a&l✓ &8» &7Players in combat can now enter &e" + areaName + "&a again.");
        }
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&8&l&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(sender, "&e&lQuizyCombatLog &8» &7Commands Help");
        MessageUtils.sendMessage(sender, "&8&l&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.sendMessage(sender, "&7• &e/qcl help &8» &7Show this help message");
        
        if (sender.hasPermission("quizycombatlog.reload")) {
            MessageUtils.sendMessage(sender, "&7• &e/qcl reload &8» &7Reload the plugin configuration");
        }
        
        if (sender.hasPermission("quizycombatlog.admin")) {
            MessageUtils.sendMessage(sender, "&7• &e/qcl combattime <seconds> &8» &7Set combat duration");
            MessageUtils.sendMessage(sender, "&7• &e/qcl inventorydrop <true/false> &8» &7Toggle inventory drop on combat log");
            MessageUtils.sendMessage(sender, "&7• &e/qcl set disable.area <name> &8» &7Create a no-combat zone");
            MessageUtils.sendMessage(sender, "&7• &e/qcl remove disable.area <name> &8» &7Remove a no-combat zone");
            MessageUtils.sendMessage(sender, "&7• &e/qcl joining disable <name> &8» &7Prevent combat players from entering");
            MessageUtils.sendMessage(sender, "&7• &e/qcl joining enable <name> &8» &7Allow combat players to enter");
        }
        
        MessageUtils.sendMessage(sender, "&8&l&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
                commands.addAll(Arrays.asList("combattime", "inventorydrop", "set", "remove", "joining"));
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
            }
        }
        
        if (args.length == 3) {
            if ((args[0].equalsIgnoreCase("remove") && args[1].equalsIgnoreCase("disable.area")) ||
                args[0].equalsIgnoreCase("joining")) {
                if (sender.hasPermission("quizycombatlog.admin")) {
                    completions.addAll(plugin.getAreaManager().getDisabledAreaNames());
                }
            }
        }
        
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}