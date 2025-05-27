package dev.nobleskye.creative;

public class RequestData {
    private final String playerName;
    private final String reason;
    private final long timestamp;
    
    public RequestData(String playerName, String reason, long timestamp) {
        this.playerName = playerName;
        this.reason = reason;
        this.timestamp = timestamp;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}