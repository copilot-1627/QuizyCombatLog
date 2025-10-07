package com.quizy.combatlog.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    
    public static void sendActionBar(Player player, String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorize(message)));
        } catch (Exception e) {
            // Fallback for older versions
            player.sendMessage(colorize(message));
        }
    }
}