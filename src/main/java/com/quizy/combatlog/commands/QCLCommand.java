package com.quizy.combatlog.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.quizy.combatlog.QuizyCombatLog;
import com.quizy.combatlog.utils.MessageUtils;

public class QCLCommand implements CommandExecutor {
    
    private final QuizyCombatLog plugin;
    
    public QCLCommand(QuizyCombatLog plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            String helpMessage = plugin.getConfigManager().getHelpMessage();
            MessageUtils.sendMessage(sender, helpMessage);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("quizycombatlog.reload")) {
                    String noPermMessage = plugin.getConfigManager().getNoPermissionMessage();
                    MessageUtils.sendMessage(sender, noPermMessage);
                    return true;
                }
                
                plugin.getConfigManager().reloadConfig();
                String reloadMessage = plugin.getConfigManager().getReloadMessage();
                MessageUtils.sendMessage(sender, reloadMessage);
                return true;
                
            case "combattime":
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
                
            case "inventorydrop":
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
                
            default:
                String helpMessage = plugin.getConfigManager().getHelpMessage();
                MessageUtils.sendMessage(sender, helpMessage);
                return true;
        }
    }
}