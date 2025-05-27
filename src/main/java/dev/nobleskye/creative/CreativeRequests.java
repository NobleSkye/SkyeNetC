package dev.nobleskye.creative;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreativeRequests extends JavaPlugin implements Listener {
    private File requestsFile;
    private YamlConfiguration requestsConfig;
    
    // Configuration options
    private long creativeDuration = -1;
    private boolean notifyAdmins = true;
    private boolean allowReason = true;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Load configuration
        loadConfigValues();
        
        // Initialize requests file
        setupRequestsFile();
        
        // Register commands
        getCommand("request").setExecutor(new RequestCommand(this));
        getCommand("requests").setExecutor(new RequestsCommand(this));
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("CreativeRequests has been enabled!");
    }
    
    @Override
    public void onDisable() {
        saveRequests();
        getLogger().info("CreativeRequests has been disabled!");
    }
    
    private void loadConfigValues() {
        creativeDuration = getConfig().getLong("creative-duration", -1);
        notifyAdmins = getConfig().getBoolean("notify-admins", true);
        allowReason = getConfig().getBoolean("allow-reason", true);
    }
    
    private void setupRequestsFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        
        requestsFile = new File(getDataFolder(), "requests.yml");
        
        if (!requestsFile.exists()) {
            try {
                requestsFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create requests.yml file!");
                e.printStackTrace();
            }
        }
        
        requestsConfig = YamlConfiguration.loadConfiguration(requestsFile);
        
        // Initialize requests section if it doesn't exist
        if (!requestsConfig.contains("requests")) {
            requestsConfig.createSection("requests");
            saveRequests();
        }
    }
    
    public void saveRequests() {
        try {
            requestsConfig.save(requestsFile);
        } catch (IOException e) {
            getLogger().severe("Could not save requests.yml file!");
            e.printStackTrace();
        }
    }
    
    public void addRequest(String playerName, String reason) {
        var requests = requestsConfig.getConfigurationSection("requests");
        if (requests == null) {
            requests = requestsConfig.createSection("requests");
        }
        
        long timestamp = System.currentTimeMillis();
        
        requests.set(playerName + ".timestamp", timestamp);
        requests.set(playerName + ".reason", reason);
        requests.set(playerName + ".pending", true);
        
        saveRequests();
    }
    
    public Map<String, RequestData> getRequests() {
        var requests = requestsConfig.getConfigurationSection("requests");
        Map<String, RequestData> requestMap = new HashMap<>();
        
        if (requests == null) return requestMap;
        
        for (String playerName : requests.getKeys(false)) {
            if (requests.getBoolean(playerName + ".pending", false)) {
                long timestamp = requests.getLong(playerName + ".timestamp");
                String reason = requests.getString(playerName + ".reason", "No reason provided");
                
                requestMap.put(playerName, new RequestData(playerName, reason, timestamp));
            }
        }
        
        return requestMap;
    }
    
    public List<RequestData> getPendingRequests() {
        return new ArrayList<>(getRequests().values());
    }
    
    public Map<String, RequestData> getApprovedRequests() {
        var requests = requestsConfig.getConfigurationSection("requests");
        Map<String, RequestData> requestMap = new HashMap<>();
        
        if (requests == null) return requestMap;
        
        for (String playerName : requests.getKeys(false)) {
            if (requests.getBoolean(playerName + ".approved", false) && 
                !requests.getBoolean(playerName + ".pending", false)) {
                long timestamp = requests.getLong(playerName + ".timestamp");
                long approvedAt = requests.getLong(playerName + ".approvedAt");
                String reason = requests.getString(playerName + ".reason", "No reason provided");
                
                RequestData data = new RequestData(playerName, reason, timestamp);
                data.setApprovedAt(approvedAt);
                requestMap.put(playerName, data);
            }
        }
        
        return requestMap;
    }
    
    public Map<String, RequestData> getDeniedRequests() {
        var requests = requestsConfig.getConfigurationSection("requests");
        Map<String, RequestData> requestMap = new HashMap<>();
        
        if (requests == null) return requestMap;
        
        for (String playerName : requests.getKeys(false)) {
            if (!requests.getBoolean(playerName + ".approved", false) && 
                !requests.getBoolean(playerName + ".pending", false) &&
                requests.contains(playerName + ".deniedAt")) {
                long timestamp = requests.getLong(playerName + ".timestamp");
                long deniedAt = requests.getLong(playerName + ".deniedAt");
                String reason = requests.getString(playerName + ".reason", "No reason provided");
                
                RequestData data = new RequestData(playerName, reason, timestamp);
                data.setDeniedAt(deniedAt);
                requestMap.put(playerName, data);
            }
        }
        
        return requestMap;
    }
    
    public boolean approveRequest(String playerName) {
        var requests = requestsConfig.getConfigurationSection("requests");
        if (requests == null) return false;
        
        if (!requests.contains(playerName) || !requests.getBoolean(playerName + ".pending", false)) {
            return false;
        }
        
        requests.set(playerName + ".pending", false);
        requests.set(playerName + ".approved", true);
        requests.set(playerName + ".approvedAt", System.currentTimeMillis());
        
        saveRequests();
        return true;
    }
    
    public boolean denyRequest(String playerName) {
        var requests = requestsConfig.getConfigurationSection("requests");
        if (requests == null) return false;
        
        if (!requests.contains(playerName) || !requests.getBoolean(playerName + ".pending", false)) {
            return false;
        }
        
        requests.set(playerName + ".pending", false);
        requests.set(playerName + ".approved", false);
        requests.set(playerName + ".deniedAt", System.currentTimeMillis());
        
        saveRequests();
        return true;
    }
    
    public boolean hasPendingRequest(String playerName) {
        var requests = requestsConfig.getConfigurationSection("requests");
        if (requests == null) return false;
        return requests.getBoolean(playerName + ".pending", false);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Send join message to player about creative requests
        player.sendMessage(Component.empty());
        player.sendMessage(
            Component.text("Welcome! ").color(NamedTextColor.GREEN)
                .append(Component.text("Need creative mode? Use ").color(NamedTextColor.YELLOW))
                .append(Component.text("/request creative")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.suggestCommand("/request creative "))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to suggest command"))))
                .append(Component.text(" to get creative!").color(NamedTextColor.YELLOW))
        );
        player.sendMessage(Component.empty());
        
        // Notify moderators about pending requests
        if (player.hasPermission("creativerequests.admin")) {
            List<RequestData> pendingRequests = getPendingRequests();
            if (!pendingRequests.isEmpty()) {
                player.sendMessage(Component.empty());
                player.sendMessage(
                    Component.text("⚠ Moderator Alert ⚠").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                );
                player.sendMessage(
                    Component.text("There are ").color(NamedTextColor.YELLOW)
                        .append(Component.text(pendingRequests.size()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(" pending creative mode request(s)").color(NamedTextColor.YELLOW))
                );
                player.sendMessage(
                    Component.text("Use ").color(NamedTextColor.YELLOW)
                        .append(Component.text("/requests list")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/requests list"))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to view pending requests"))))
                        .append(Component.text(" to view them").color(NamedTextColor.YELLOW))
                );
                player.sendMessage(Component.empty());
            }
        }
    }
    
    // Getters for configuration
    public long getCreativeDuration() { return creativeDuration; }
    public boolean isNotifyAdmins() { return notifyAdmins; }
    public boolean isAllowReason() { return allowReason; }
}