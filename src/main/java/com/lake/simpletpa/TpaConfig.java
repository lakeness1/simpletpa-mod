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
                                        .comment("Tiempo en segundos para que expire una solicitud de teletransporte",
                                                        "Range: > 1")
                                        .defineInRange("requestExpiration", 120, 1, 600);

                        teleportWarmup = builder
                                        .comment("Tiempo de espera en segundos antes de teletransportar (0 = instantáneo)",
                                                        "Range: 0 ~ 60")
                                        .defineInRange("teleportWarmup", 3, 0, 60);

                        cooldown = builder
                                        .comment("Tiempo de espera en segundos entre usos del comando (0 = sin cooldown)",
                                                        "Range: >= 0")
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
                public final ForgeConfigSpec.ConfigValue<String> toggleEnabled;
                public final ForgeConfigSpec.ConfigValue<String> toggleDisabled;
                public final ForgeConfigSpec.ConfigValue<String> targetIgnoring;

                public Messages(ForgeConfigSpec.Builder builder) {
                        builder.comment("Mensajes personalizables (usa & para códigos de color)")
                                        .push("messages");

                        requestSent = builder
                                        .comment("Mensaje enviado al jugador que inicia el comando /tpa.",
                                                        "Placeholders:",
                                                        "  %s - Nombre del jugador objetivo (quien recibe la solicitud).",
                                                        "Usa '&' para códigos de color (ej: &6 para dorado).")
                                        .define("requestSent", "&aSolicitud enviada a &6%s&a.");

                        requestReceived = builder
                                        .comment("Mensaje recibido cuando alguien quiere teletransportarse a ti.",
                                                        "Placeholders:",
                                                        "  %s - Nombre del jugador que envió la solicitud.")
                                        .define("requestReceived", "&e%s &aquiere teletransportarse a ti.");

                        requestReceivedHere = builder
                                        .comment("Mensaje cuando alguien quiere que te teletransportes a ellos.",
                                                        "Placeholders:",
                                                        "  %s - Nombre del jugador que envió la solicitud.")
                                        .define("requestReceivedHere", "&e%s &aquiere que te teletransportes a ellos.");

                        teleporting = builder
                                        .comment("Mensaje al teletransportarse exitosamente.")
                                        .define("teleporting", "&aTeletransportando...");

                        warmupStart = builder
                                        .comment("Mensaje al iniciar el warmup (cuenta regresiva).",
                                                        "Placeholders:",
                                                        "  %d - Número de segundos a esperar.")
                                        .define("warmupStart", "&eNo te muevas durante &6%d &esegundos...");

                        warmupCancelled = builder
                                        .comment("Mensaje cuando se cancela el warmup por movimiento.")
                                        .define("warmupCancelled", "&cTe has movido. Teletransporte cancelado.");

                        requestExpired = builder
                                        .comment("Mensaje cuando expira una solicitud.")
                                        .define("requestExpired", "&cLa solicitud de teletransporte ha expirado.");

                        requestDenied = builder
                                        .comment("Mensaje cuando se rechaza una solicitud.")
                                        .define("requestDenied", "&cSolicitud de teletransporte denegada.");

                        noRequest = builder
                                        .comment("Mensaje cuando no hay solicitudes pendientes.")
                                        .define("noRequest", "&cNo tienes solicitudes pendientes.");

                        playerOffline = builder
                                        .comment("Mensaje cuando el jugador objetivo no está conectado.")
                                        .define("playerOffline", "&cEse jugador no está conectado.");

                        cannotTeleportSelf = builder
                                        .comment("Mensaje cuando intentas enviarte una solicitud a ti mismo.")
                                        .define("cannotTeleportSelf",
                                                        "&c¡No puedes enviarte una solicitud a ti mismo!");

                        onCooldown = builder
                                        .comment("Mensaje cuando el comando está en cooldown.",
                                                        "Placeholders:",
                                                        "  %d - Segundos restantes de cooldown.")
                                        .define("onCooldown",
                                                        "&cDebes esperar &6%d &csegundos antes de usar este comando de nuevo.");

                        requestAccepted = builder
                                        .comment("Mensaje cuando se acepta tu solicitud.",
                                                        "Placeholders:",
                                                        "  %s - Nombre del jugador que aceptó.")
                                        .define("requestAccepted", "&e%s &aha aceptado tu solicitud.");

                        toggleEnabled = builder
                                        .comment("Mensaje cuando activas el modo ignorar solicitudes.")
                                        .define("toggleEnabled",
                                                        "&eAhora estás &cIGNORANDO &etodas las solicitudes de teletransporte.");

                        toggleDisabled = builder
                                        .comment("Mensaje cuando desactivas el modo ignorar solicitudes.")
                                        .define("toggleDisabled",
                                                        "&eAhora estás &aACEPTANDO &esolicitudes de teletransporte.");

                        targetIgnoring = builder
                                        .comment("Mensaje cuando intentas enviar una solicitud a alguien que tiene el toggle activo.")
                                        .define("targetIgnoring",
                                                        "&cEse jugador no está aceptando solicitudes de teletransporte.");

                        builder.pop();
                }
        }
}
