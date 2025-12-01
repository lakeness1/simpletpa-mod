package com.lake.simpletpa;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Represents a warmup task for a player waiting to teleport.
 * Stores all necessary information to track and execute the teleport.
 */
public class WarmupTask {
    private final UUID playerUuid;
    private final UUID targetUuid;
    private final RequestType type;
    private final long startTime;
    private final Vec3 startPosition;
    private final ServerLevel targetLevel;
    private final Vec3 targetPosition;
    private final float targetYaw;
    private final float targetPitch;

    /**
     * Creates a new warmup task.
     * 
     * @param playerUuid     UUID of the player being teleported
     * @param targetUuid     UUID of the target player (for notifications)
     * @param type           Type of teleport request
     * @param startPosition  Starting position of the player
     * @param targetLevel    Destination dimension
     * @param targetPosition Destination coordinates
     * @param targetYaw      Destination yaw rotation
     * @param targetPitch    Destination pitch rotation
     */
    public WarmupTask(UUID playerUuid, UUID targetUuid, RequestType type, Vec3 startPosition,
            ServerLevel targetLevel, Vec3 targetPosition, float targetYaw, float targetPitch) {
        this.playerUuid = playerUuid;
        this.targetUuid = targetUuid;
        this.type = type;
        this.startTime = System.currentTimeMillis();
        this.startPosition = startPosition;
        this.targetLevel = targetLevel;
        this.targetPosition = targetPosition;
        this.targetYaw = targetYaw;
        this.targetPitch = targetPitch;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public RequestType getType() {
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public Vec3 getStartPosition() {
        return startPosition;
    }

    public ServerLevel getTargetLevel() {
        return targetLevel;
    }

    public Vec3 getTargetPosition() {
        return targetPosition;
    }

    public float getTargetYaw() {
        return targetYaw;
    }

    public float getTargetPitch() {
        return targetPitch;
    }

    /**
     * Checks if the warmup period has elapsed.
     * 
     * @param warmupSeconds Configured warmup duration in seconds
     * @return true if warmup is complete
     */
    public boolean isComplete(int warmupSeconds) {
        long elapsedMs = System.currentTimeMillis() - startTime;
        return elapsedMs >= (warmupSeconds * 1000L);
    }
}
