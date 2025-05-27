package dev.nobleskye.creative;

public class RequestData {
    private final String playerName;
    private final String reason;
    private final long timestamp;
    private long approvedAt = -1;
    private long deniedAt = -1;
    
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
    
    public long getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(long approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public long getDeniedAt() {
        return deniedAt;
    }
    
    public void setDeniedAt(long deniedAt) {
        this.deniedAt = deniedAt;
    }
}