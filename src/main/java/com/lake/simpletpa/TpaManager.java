package com.lake.simpletpa;

import com.lake.simpletpa.util.MessageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton manager for handling all teleport requests.
 * Stores requests in memory (RAM) with no persistence.
 */
public class TpaManager {
    private static TpaManager instance;
    private final Map<UUID, TeleportRequest> pendingRequests;
    private final Map<UUID, Long> cooldowns;
    private final Set<UUID> ignoringPlayers;

    private TpaManager() {
        this.pendingRequests = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.ignoringPlayers = new HashSet<>();
    }

    /**
     * Gets the singleton instance of TpaManager.
     */
    public static TpaManager getInstance() {
        if (instance == null) {
            instance = new TpaManager();
        }
        return instance;
    }

    /**
     * Adds or updates a teleport request.
     * The request is keyed by the target's UUID for easy lookup.
     * 
     * @param request The teleport request to add
     */
    public void addRequest(TeleportRequest request) {
        pendingRequests.put(request.getTarget(), request);
    }

    /**
     * Retrieves a pending request for the given target player.
     * 
     * @param targetUUID The UUID of the target player
     * @return The pending request, or null if none exists
     */
    public TeleportRequest getRequest(UUID targetUUID) {
        return pendingRequests.get(targetUUID);
    }

    /**
     * Removes a request from the pending list.
     * 
     * @param targetUUID The UUID of the target player
     */
    public void removeRequest(UUID targetUUID) {
        pendingRequests.remove(targetUUID);
    }

    /**
     * Checks for and removes expired requests.
     * Notifies both players when a request expires.
     * 
     * @param server The Minecraft server instance for player lookup
     */
    public void cleanExpired(MinecraftServer server) {
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, TeleportRequest> entry : pendingRequests.entrySet()) {
            TeleportRequest request = entry.getValue();

            if (request.isExpired()) {
                toRemove.add(entry.getKey());

                // Notify both players
                ServerPlayer sender = server.getPlayerList().getPlayer(request.getSender());
                ServerPlayer target = server.getPlayerList().getPlayer(request.getTarget());

                if (sender != null) {
                    MessageUtils.send(sender, TpaConfig.MESSAGES.requestExpired.get());
                }

                if (target != null) {
                    MessageUtils.send(target, TpaConfig.MESSAGES.requestExpired.get());
                }
            }
        }

        // Remove expired requests
        for (UUID uuid : toRemove) {
            pendingRequests.remove(uuid);
        }
    }

    /**
     * Sets a cooldown for a player.
     * 
     * @param playerUuid UUID of the player
     */
    public void setCooldown(UUID playerUuid) {
        cooldowns.put(playerUuid, System.currentTimeMillis());
    }

    /**
     * Checks if a player is on cooldown.
     * 
     * @param playerUuid UUID of the player
     * @return true if player is on cooldown
     */
    public boolean isOnCooldown(UUID playerUuid) {
        return getRemainingCooldown(playerUuid) > 0;
    }

    /**
     * Gets the remaining cooldown time for a player in seconds.
     * 
     * @param playerUuid UUID of the player
     * @return Remaining cooldown in seconds, or 0 if no cooldown
     */
    public long getRemainingCooldown(UUID playerUuid) {
        if (!cooldowns.containsKey(playerUuid)) {
            return 0;
        }

        long lastUse = cooldowns.get(playerUuid);
        long cooldownMs = TpaConfig.GENERAL.cooldown.get() * 1000L;
        long elapsed = System.currentTimeMillis() - lastUse;
        long remaining = cooldownMs - elapsed;

        if (remaining <= 0) {
            cooldowns.remove(playerUuid);
            return 0;
        }

        return (remaining + 999) / 1000; // Round up to nearest second
    }

    /**
     * Toggles the ignore status for a player.
     * 
     * @param playerUuid UUID of the player
     * @return true if now ignoring, false if now accepting
     */
    public boolean toggleIgnore(UUID playerUuid) {
        if (ignoringPlayers.contains(playerUuid)) {
            ignoringPlayers.remove(playerUuid);
            return false; // Now accepting
        } else {
            ignoringPlayers.add(playerUuid);
            return true; // Now ignoring
        }
    }

    /**
     * Checks if a player is ignoring teleport requests.
     * 
     * @param playerUuid UUID of the player
     * @return true if player is ignoring requests
     */
    public boolean isIgnoring(UUID playerUuid) {
        return ignoringPlayers.contains(playerUuid);
    }
}
