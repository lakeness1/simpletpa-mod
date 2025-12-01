package com.lake.simpletpa;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuration class for SimpleTPA mod.
 * Defines all configurable values using ForgeConfigSpec.
 */
public class TpaConfig {

    public static final ForgeConfigSpec SPEC;
    public static final General GENERAL;
    public static final Messages MESSAGES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        GENERAL = new General(builder);
        MESSAGES = new Messages(builder);

        SPEC = builder.build();
    }

    /**
     * General configuration category for timing values.
     */
    public static class General {
        public final ForgeConfigSpec.IntValue requestExpiration;
        public final ForgeConfigSpec.IntValue teleportWarmup;
        public final ForgeConfigSpec.IntValue cooldown;

        public General(ForgeConfigSpec.Builder builder) {
            builder.comment("General Settings")
                    .push("general");

            requestExpiration = builder
                    .comment("Tiempo en segundos para que expire una solicitud de teletransporte")
                    .defineInRange("requestExpiration", 120, 1, 600);

            teleportWarmup = builder
                    .comment("Tiempo de espera en segundos antes de teletransportar (0 = instantáneo)")
                    .defineInRange("teleportWarmup", 3, 0, 60);

            cooldown = builder
                    .comment("Tiempo de espera en segundos entre usos del comando (0 = sin cooldown)")
                    .defineInRange("cooldown", 0, 0, 300);

            builder.pop();
        }
    }

    /**
     * Messages configuration category for customizable text.
     * Supports Minecraft color codes using & symbol.
     */
    public static class Messages {
        public final ForgeConfigSpec.ConfigValue<String> requestSent;
        public final ForgeConfigSpec.ConfigValue<String> requestReceived;
        public final ForgeConfigSpec.ConfigValue<String> requestReceivedHere;
        public final ForgeConfigSpec.ConfigValue<String> teleporting;
        public final ForgeConfigSpec.ConfigValue<String> warmupStart;
        public final ForgeConfigSpec.ConfigValue<String> warmupCancelled;
        public final ForgeConfigSpec.ConfigValue<String> requestExpired;
        public final ForgeConfigSpec.ConfigValue<String> requestDenied;
        public final ForgeConfigSpec.ConfigValue<String> noRequest;
        public final ForgeConfigSpec.ConfigValue<String> playerOffline;
        public final ForgeConfigSpec.ConfigValue<String> cannotTeleportSelf;
        public final ForgeConfigSpec.ConfigValue<String> onCooldown;
        public final ForgeConfigSpec.ConfigValue<String> requestAccepted;

        public Messages(ForgeConfigSpec.Builder builder) {
            builder.comment("Mensajes personalizables (usa & para códigos de color)")
                    .push("messages");

            requestSent = builder
                    .comment("Mensaje cuando se envía una solicitud")
                    .define("requestSent", "&aSolicitud enviada a &6%s&a.");

            requestReceived = builder
                    .comment("Mensaje cuando recibes una solicitud de /tpa")
                    .define("requestReceived", "&e%s &aquiere teletransportarse a ti.");

            requestReceivedHere = builder
                    .comment("Mensaje cuando recibes una solicitud de /tpahere")
                    .define("requestReceivedHere", "&e%s &aquiere que te teletransportes a ellos.");

            teleporting = builder
                    .comment("Mensaje al teletransportarse")
                    .define("teleporting", "&aTeletransportando...");

            warmupStart = builder
                    .comment("Mensaje al iniciar el warmup")
                    .define("warmupStart", "&eNo te muevas durante &6%d &esegundos...");

            warmupCancelled = builder
                    .comment("Mensaje cuando se cancela el warmup por movimiento")
                    .define("warmupCancelled", "&cTe has movido. Teletransporte cancelado.");

            requestExpired = builder
                    .comment("Mensaje cuando expira una solicitud")
                    .define("requestExpired", "&cLa solicitud de teletransporte ha expirado.");

            requestDenied = builder
                    .comment("Mensaje cuando se rechaza una solicitud")
                    .define("requestDenied", "&cSolicitud de teletransporte denegada.");

            noRequest = builder
                    .comment("Mensaje cuando no hay solicitudes pendientes")
                    .define("noRequest", "&cNo tienes solicitudes pendientes.");

            playerOffline = builder
                    .comment("Mensaje cuando el jugador no está conectado")
                    .define("playerOffline", "&cEse jugador no está conectado.");

            cannotTeleportSelf = builder
                    .comment("Mensaje cuando intentas teletransportarte a ti mismo")
                    .define("cannotTeleportSelf", "&c¡No puedes enviarte una solicitud a ti mismo!");

            onCooldown = builder
                    .comment("Mensaje cuando el comando está en cooldown")
                    .define("onCooldown", "&cDebes esperar &6%d &csegundos antes de usar este comando de nuevo.");

            requestAccepted = builder
                    .comment("Mensaje cuando se acepta una solicitud")
                    .define("requestAccepted", "&e%s &aha aceptado tu solicitud.");

            builder.pop();
        }
    }
}
