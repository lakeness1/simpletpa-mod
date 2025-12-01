package com.lake.simpletpa.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility class for handling formatted messages with color codes.
 */
public class MessageUtils {

    /**
     * Sends a formatted message to a player.
     * Converts & color codes to § and supports placeholders.
     * 
     * @param player  The player to send the message to
     * @param message The message string (can contain & color codes)
     * @param args    Optional arguments for String.format placeholders
     */
    public static void send(ServerPlayer player, String message, Object... args) {
        String formatted = format(message, args);
        String colored = formatColors(formatted);
        player.sendSystemMessage(Component.literal(colored));
    }

    /**
     * Formats a message with placeholders.
     * 
     * @param message The message template
     * @param args    Arguments to replace placeholders
     * @return Formatted message
     */
    public static String format(String message, Object... args) {
        if (args.length == 0) {
            return message;
        }
        try {
            return String.format(message, args);
        } catch (Exception e) {
            // If formatting fails, return original message
            return message;
        }
    }

    /**
     * Converts & color codes to § (Minecraft color codes).
     * Supports all standard Minecraft color codes.
     * 
     * @param message Message with & color codes
     * @return Message with § color codes
     */
    public static String formatColors(String message) {
        if (message == null) {
            return "";
        }

        // Replace & with § for color codes
        // Supports: 0-9, a-f, k-o, r (all Minecraft color codes)
        return message.replaceAll("&([0-9a-fk-or])", "§$1");
    }

    /**
     * Formats time in seconds to a readable string.
     * 
     * @param seconds Number of seconds
     * @return Formatted time string
     */
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " segundo" + (seconds != 1 ? "s" : "");
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (remainingSeconds == 0) {
            return minutes + " minuto" + (minutes != 1 ? "s" : "");
        }

        return minutes + " minuto" + (minutes != 1 ? "s" : "") +
                " y " + remainingSeconds + " segundo" + (remainingSeconds != 1 ? "s" : "");
    }
}
