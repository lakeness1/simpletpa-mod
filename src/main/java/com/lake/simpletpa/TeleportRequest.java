package com.lake.simpletpa;

import java.util.UUID;

/**
 * Represents a teleport request between two players.
 */
public class TeleportRequest {
    private final UUID sender;
    private final UUID target;
    private final RequestType type;
    private final long expirationTime;

    /**
     * Creates a new teleport request.
     * 
     * @param sender The UUID of the player who initiated the request
     * @param target The UUID of the player who receives the request
     * @param type   The type of teleport (TPA or TPA_HERE)
     */
    public TeleportRequest(UUID sender, UUID target, RequestType type) {
        this.sender = sender;
        this.target = target;
        this.type = type;
        // Request expires in 2 minutes (120 seconds)
        this.expirationTime = System.currentTimeMillis() + (120 * 1000);
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getTarget() {
        return target;
    }

    public RequestType getType() {
        return type;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Checks if this request has expired.
     * 
     * @return true if the current time is past the expiration time
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}
