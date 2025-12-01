package com.lake.simpletpa;

import com.lake.simpletpa.commands.TpaCommands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Main mod class for SimpleTPA.
 * Server-side only teleport request system with configuration and warmup.
 */
@Mod("simpletpa")
public class SimpleTPA {

    public SimpleTPA() {
        // Register configuration (COMMON type for global settings)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TpaConfig.SPEC, "simpletpa-common.toml");

        // Register this class to the event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Registers all TPA commands when the server starts.
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TpaCommands.register(event.getDispatcher());
    }

    /**
     * Server tick event handler for cleaning up expired requests and checking
     * warmups.
     * Checks once per second (every 20 ticks) to avoid performance impact.
     */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        // Only run on the END phase to avoid running twice per tick
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Only check once per second (20 ticks per second)
        if (event.getServer().getTickCount() % 20 == 0) {
            // Clean expired teleport requests
            TpaManager.getInstance().cleanExpired(event.getServer());

            // Check warmup tasks (movement detection and completion)
            WarmupManager.getInstance().checkWarmups(event.getServer());
        }
    }
}
