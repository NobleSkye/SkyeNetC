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

import java.text.SimpleDateFormat;
import java.util.*;

public class RequestsCommand implements CommandExecutor, TabCompleter {
    private final CreativeRequests plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    
    public RequestsCommand(CreativeRequests plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("creativerequests.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            // Display all pending requests
            listRequests(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "approve":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /requests approve <player>").color(NamedTextColor.RED));
                    return true;
                }
                approveRequest(sender, args[1]);
                break;
            case "deny":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /requests deny <player>").color(NamedTextColor.RED));
                    return true;
                }
                denyRequest(sender, args[1]);
                break;
            case "list":
                listRequests(sender);
                break;
            default:
                sender.sendMessage(Component.text("Unknown subcommand. Use: approve, deny, or list").color(NamedTextColor.RED));
        }
        
        return true;
    }
    
    private void listRequests(CommandSender sender) {
        Map<String, RequestData> requests = plugin.getRequests();
        
        if (requests.isEmpty()) {
            sender.sendMessage(Component.text("There are no pending creative mode requests.").color(NamedTextColor.YELLOW));
            return;
        }
        
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Pending Creative Mode Requests:").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.empty());
        
        requests.entrySet().stream()
                .sorted(Map.Entry.<String, RequestData>comparingByValue(
                        Comparator.comparing(RequestData::getTimestamp).reversed()))
                .forEach(entry -> {
                    String playerName = entry.getKey();
                    RequestData data = entry.getValue();
                    String date = dateFormat.format(new Date(data.getTimestamp()));
                    
                    sender.sendMessage(Component.text("Player: ").color(NamedTextColor.YELLOW).append(Component.text(playerName).color(NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("Reason: ").color(NamedTextColor.YELLOW).append(Component.text(data.getReason()).color(NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("Requested: ").color(NamedTextColor.YELLOW).append(Component.text(date).color(NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("/requests approve " + playerName + " ").color(NamedTextColor.GREEN)
                                      .append(Component.text("/requests deny " + playerName).color(NamedTextColor.RED)));
                    sender.sendMessage(Component.empty());
                });
    }
    
    private void approveRequest(CommandSender sender, String playerName) {
        if (!plugin.hasPendingRequest(playerName)) {
            sender.sendMessage(Component.text("No pending request found for player " + playerName + ".").color(NamedTextColor.RED));
            return;
        }
        
        boolean success = plugin.approveRequest(playerName);
        
        if (success) {
            sender.sendMessage(Component.text("You have approved " + playerName + "'s creative mode request.").color(NamedTextColor.GREEN));
            
            // Set the player to creative mode if they are online
            Player targetPlayer = plugin.getServer().getPlayer(playerName);
            if (targetPlayer != null) {
                targetPlayer.setGameMode(GameMode.CREATIVE);
                targetPlayer.sendMessage(Component.empty());
                targetPlayer.sendMessage(Component.text("Your creative mode request has been approved!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
                targetPlayer.sendMessage(Component.empty());
                
                // Notify other admins
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("creativerequests.admin") && !p.equals(sender))
                        .forEach(admin -> {
                            admin.sendMessage(Component.text(playerName + "'s creative mode request was approved by " +
                                            (sender instanceof Player ? sender.getName() : "CONSOLE") + ".").color(NamedTextColor.GREEN));
                        });
            }
        } else {
            sender.sendMessage(Component.text("Failed to approve the request. It may have been already processed.").color(NamedTextColor.RED));
        }
    }
    
    private void denyRequest(CommandSender sender, String playerName) {
        if (!plugin.hasPendingRequest(playerName)) {
            sender.sendMessage(Component.text("No pending request found for player " + playerName + ".").color(NamedTextColor.RED));
            return;
        }
        
        boolean success = plugin.denyRequest(playerName);
        
        if (success) {
            sender.sendMessage(Component.text("You have denied " + playerName + "'s creative mode request.").color(NamedTextColor.YELLOW));
            
            // Notify the player if they're online
            Player targetPlayer = plugin.getServer().getPlayer(playerName);
            if (targetPlayer != null) {
                targetPlayer.sendMessage(Component.empty());
                targetPlayer.sendMessage(Component.text("Your creative mode request has been denied.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                targetPlayer.sendMessage(Component.empty());
                
                // Notify other admins
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("creativerequests.admin") && !p.equals(sender))
                        .forEach(admin -> {
                            admin.sendMessage(Component.text(playerName + "'s creative mode request was denied by " +
                                            (sender instanceof Player ? sender.getName() : "CONSOLE") + ".").color(NamedTextColor.YELLOW));
                        });
            }
        } else {
            sender.sendMessage(Component.text("Failed to deny the request. It may have been already processed.").color(NamedTextColor.RED));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("creativerequests.admin")) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            return Arrays.asList("approve", "deny", "list");
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("approve") || args[0].equalsIgnoreCase("deny"))) {
            // Return list of players with pending requests
            return new ArrayList<>(plugin.getRequests().keySet());
        }
        
        return Collections.emptyList();
    }
}