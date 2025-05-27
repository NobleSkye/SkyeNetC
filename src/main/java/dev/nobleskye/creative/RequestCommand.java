package dev.nobleskye.creative;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RequestCommand implements CommandExecutor, TabCompleter {
    private final CreativeRequests plugin;
    
    public RequestCommand(CreativeRequests plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can request creative mode.").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0 || !args[0].equalsIgnoreCase("creative")) {
            sender.sendMessage(Component.text("Usage: /request creative [reason]").color(NamedTextColor.RED));
            return true;
        }
        
        String playerName = player.getName();
        
        if (plugin.hasPendingRequest(playerName)) {
            sender.sendMessage(Component.text("You already have a pending creative mode request.").color(NamedTextColor.YELLOW));
            return true;
        }
        
        // Check if the player is already in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            sender.sendMessage(Component.text("You are already in creative mode!").color(NamedTextColor.YELLOW));
            return true;
        }
        
        // Gather reason if provided
        final String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
        
        // Submit request
        plugin.addRequest(playerName, reason);
        
        player.sendMessage(Component.text("Your request for creative mode has been submitted.").color(NamedTextColor.GREEN));
        player.sendMessage(Component.text("A server administrator will review your request.").color(NamedTextColor.GREEN));
        
        // Notify admins who are online if enabled
        if (plugin.isNotifyAdmins()) {
            plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
                if (onlinePlayer.hasPermission("creativerequests.admin")) {
                    onlinePlayer.sendMessage(Component.empty());
                    onlinePlayer.sendMessage(Component.text("New Creative Request!").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                    onlinePlayer.sendMessage(Component.text("Player: ").color(NamedTextColor.YELLOW).append(Component.text(playerName).color(NamedTextColor.WHITE)));
                    onlinePlayer.sendMessage(Component.text("Reason: ").color(NamedTextColor.YELLOW).append(Component.text(reason).color(NamedTextColor.WHITE)));
                    onlinePlayer.sendMessage(Component.text("Use /requests to view and manage requests.").color(NamedTextColor.GREEN));
                    onlinePlayer.sendMessage(Component.empty());
                }
            });
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("creative");
        }
        return Collections.emptyList();
    }
}