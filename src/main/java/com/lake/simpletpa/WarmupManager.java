package com.lake.simpletpa;

import com.lake.simpletpa.util.MessageUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton manager for handling teleport warmup tasks.
 * Tracks players waiting to teleport and validates movement.
 */
public class WarmupManager {
    private static WarmupManager instance;
    private final Map<UUID, WarmupTask> warmingUpPlayers;

    // Movement threshold in blocks
    private static final double MOVEMENT_THRESHOLD = 0.1;

    private WarmupManager() {
        this.warmingUpPlayers = new HashMap<>();
    }

    /**
     * Gets the singleton instance of WarmupManager.
     */
    public static WarmupManager getInstance() {
        if (instance == null) {
            instance = new WarmupManager();
        }
        return instance;
    }

    /**
     * Starts a warmup task for a player.
     * 
     * @param task The warmup task to start
     */
    public void startWarmup(WarmupTask task) {
        warmingUpPlayers.put(task.getPlayerUuid(), task);
    }

    /**
     * Cancels a warmup task for a player.
     * 
     * @param playerUuid UUID of the player
     */
    public void cancelWarmup(UUID playerUuid) {
        warmingUpPlayers.remove(playerUuid);
    }

    /**
     * Checks if a player is currently warming up.
     * 
     * @param playerUuid UUID of the player
     * @return true if player is in warmup
     */
    public boolean isWarming(UUID playerUuid) {
        return warmingUpPlayers.containsKey(playerUuid);
    }

    /**
     * Processes all active warmup tasks.
     * Checks for movement and completion, executes or cancels as needed.
     * 
     * @param server The Minecraft server instance
     */
    public void checkWarmups(MinecraftServer server) {
        if (warmingUpPlayers.isEmpty()) {
            return;
        }

        List<UUID> toRemove = new ArrayList<>();
        int warmupSeconds = TpaConfig.GENERAL.teleportWarmup.get();

        for (Map.Entry<UUID, WarmupTask> entry : warmingUpPlayers.entrySet()) {
            UUID playerUuid = entry.getKey();
            WarmupTask task = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);

            // Player disconnected
            if (player == null) {
                toRemove.add(playerUuid);
                continue;
            }

            // Check for movement
            Vec3 currentPos = player.position();
            double distance = currentPos.distanceTo(task.getStartPosition());

            if (distance > MOVEMENT_THRESHOLD) {
                // Player moved, cancel warmup
                MessageUtils.send(player, TpaConfig.MESSAGES.warmupCancelled.get());
                toRemove.add(playerUuid);
                continue;
            }

            // Check if warmup is complete
            if (task.isComplete(warmupSeconds)) {
                // Execute teleport
                executeTeleport(player, task, server);
                toRemove.add(playerUuid);
            }
        }

        // Remove completed/cancelled tasks
        for (UUID uuid : toRemove) {
            warmingUpPlayers.remove(uuid);
        }
    }

    /**
     * Executes the actual teleportation after warmup completes.
     * 
     * @param player The player to teleport
     * @param task   The warmup task containing destination info
     * @param server The server instance
     */
    private void executeTeleport(ServerPlayer player, WarmupTask task, MinecraftServer server) {
        Vec3 dest = task.getTargetPosition();

        // Teleport the player
        player.teleportTo(task.getTargetLevel(),
                dest.x, dest.y, dest.z,
                task.getTargetYaw(), task.getTargetPitch());

        // Send success messages
        MessageUtils.send(player, TpaConfig.MESSAGES.teleporting.get());

        // Notify target player
        ServerPlayer target = server.getPlayerList().getPlayer(task.getTargetUuid());
        if (target != null) {
            String message = String.format(
                    TpaConfig.MESSAGES.teleporting.get().replace("%s", player.getName().getString()));
            MessageUtils.send(target, message);
        }
    }
}
