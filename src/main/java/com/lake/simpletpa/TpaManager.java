package com.lake.simpletpa;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
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

    private TpaManager() {
        this.pendingRequests = new HashMap<>();
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
                    sender.sendSystemMessage(Component.literal("§cYour teleport request has expired."));
                }

                if (target != null) {
                    target.sendSystemMessage(Component.literal("§cThe teleport request has expired."));
                }
            }
        }

        // Remove expired requests
        for (UUID uuid : toRemove) {
            pendingRequests.remove(uuid);
        }
    }
}
