package dev.nobleskye.creative;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreativeRequests extends JavaPlugin {
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
    
    // Getters for configuration
    public long getCreativeDuration() { return creativeDuration; }
    public boolean isNotifyAdmins() { return notifyAdmins; }
    public boolean isAllowReason() { return allowReason; }
}