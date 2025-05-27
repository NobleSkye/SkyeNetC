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
            listRequests(sender, "pending");
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
                String type = args.length > 1 ? args[1].toLowerCase() : "pending";
                if (!type.equals("pending") && !type.equals("approved") && !type.equals("denied") && !type.equals("all")) {
                    sender.sendMessage(Component.text("Usage: /requests list [pending|approved|denied|all]").color(NamedTextColor.RED));
                    return true;
                }
                listRequests(sender, type);
                break;
            default:
                sender.sendMessage(Component.text("Unknown subcommand. Use: approve, deny, or list").color(NamedTextColor.RED));
        }
        
        return true;
    }
    
    private void listRequests(CommandSender sender, String type) {
        Map<String, RequestData> requests;
        String title;
        NamedTextColor titleColor;
        
        switch (type.toLowerCase()) {
            case "pending":
                requests = plugin.getRequests();
                title = "Pending Creative Mode Requests:";
                titleColor = NamedTextColor.GOLD;
                break;
            case "approved":
                requests = plugin.getApprovedRequests();
                title = "Approved Creative Mode Requests:";
                titleColor = NamedTextColor.GREEN;
                break;
            case "denied":
                requests = plugin.getDeniedRequests();
                title = "Denied Creative Mode Requests:";
                titleColor = NamedTextColor.RED;
                break;
            case "all":
                sender.sendMessage(Component.empty());
                sender.sendMessage(Component.text("=== ALL CREATIVE MODE REQUESTS ===").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.empty());
                
                listRequests(sender, "pending");
                listRequests(sender, "approved");
                listRequests(sender, "denied");
                return;
            default:
                sender.sendMessage(Component.text("Invalid request type. Use: pending, approved, denied, or all").color(NamedTextColor.RED));
                return;
        }
        
        if (requests.isEmpty()) {
            sender.sendMessage(Component.text("There are no " + type + " creative mode requests.").color(NamedTextColor.YELLOW));
            return;
        }
        
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text(title).color(titleColor).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.empty());
        
        requests.entrySet().stream()
                .sorted(Map.Entry.<String, RequestData>comparingByValue(
                        Comparator.comparing(RequestData::getTimestamp).reversed()))
                .forEach(entry -> {
                    String playerName = entry.getKey();
                    RequestData data = entry.getValue();
                    String requestDate = dateFormat.format(new Date(data.getTimestamp()));
                    
                    sender.sendMessage(Component.text("Player: ").color(NamedTextColor.YELLOW).append(Component.text(playerName).color(NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("Reason: ").color(NamedTextColor.YELLOW).append(Component.text(data.getReason()).color(NamedTextColor.WHITE)));
                    sender.sendMessage(Component.text("Requested: ").color(NamedTextColor.YELLOW).append(Component.text(requestDate).color(NamedTextColor.WHITE)));
                    
                    if (type.equals("approved") && data.getApprovedAt() > 0) {
                        String approvedDate = dateFormat.format(new Date(data.getApprovedAt()));
                        sender.sendMessage(Component.text("Approved: ").color(NamedTextColor.GREEN).append(Component.text(approvedDate).color(NamedTextColor.WHITE)));
                    } else if (type.equals("denied") && data.getDeniedAt() > 0) {
                        String deniedDate = dateFormat.format(new Date(data.getDeniedAt()));
                        sender.sendMessage(Component.text("Denied: ").color(NamedTextColor.RED).append(Component.text(deniedDate).color(NamedTextColor.WHITE)));
                    } else if (type.equals("pending")) {
                        sender.sendMessage(Component.text("/requests approve " + playerName + " ").color(NamedTextColor.GREEN)
                                          .append(Component.text("/requests deny " + playerName).color(NamedTextColor.RED)));
                    }
                    
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
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("approve") || args[0].equalsIgnoreCase("deny")) {
                // Return list of players with pending requests
                return new ArrayList<>(plugin.getRequests().keySet());
            } else if (args[0].equalsIgnoreCase("list")) {
                // Return list options for the list command
                return Arrays.asList("pending", "approved", "denied", "all");
            }
        }
        
        return Collections.emptyList();
    }
}