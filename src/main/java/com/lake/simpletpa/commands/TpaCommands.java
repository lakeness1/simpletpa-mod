package com.lake.simpletpa.commands;

import com.lake.simpletpa.RequestType;
import com.lake.simpletpa.TeleportRequest;
import com.lake.simpletpa.TpaConfig;
import com.lake.simpletpa.TpaManager;
import com.lake.simpletpa.WarmupManager;
import com.lake.simpletpa.WarmupTask;
import com.lake.simpletpa.util.MessageUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles registration and execution of all TPA commands.
 */
public class TpaCommands {

        /**
         * Registers all TPA commands with the command dispatcher.
         */
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                // /tpa <player>
                dispatcher.register(
                                Commands.literal("tpa")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                                .executes(TpaCommands::executeTpa)));

                // /tpahere <player>
                dispatcher.register(
                                Commands.literal("tpahere")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                                .executes(TpaCommands::executeTpaHere)));

                // /tpaccept
                dispatcher.register(
                                Commands.literal("tpaccept")
                                                .executes(TpaCommands::executeTpAccept));

                // /tpdeny
                dispatcher.register(
                                Commands.literal("tpdeny")
                                                .executes(TpaCommands::executeTpDeny));
        }

        /**
         * Executes /tpa command - sender wants to teleport to target.
         */
        private static int executeTpa(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                ServerPlayer sender = context.getSource().getPlayerOrException();
                ServerPlayer target = EntityArgument.getPlayer(context, "player");

                // Can't teleport to yourself
                if (sender.getUUID().equals(target.getUUID())) {
                        MessageUtils.send(sender, TpaConfig.MESSAGES.cannotTeleportSelf.get());
                        return 0;
                }

                // Check cooldown
                TpaManager manager = TpaManager.getInstance();
                if (manager.isOnCooldown(sender.getUUID())) {
                        long remaining = manager.getRemainingCooldown(sender.getUUID());
                        MessageUtils.send(sender, TpaConfig.MESSAGES.onCooldown.get(), remaining);
                        return 0;
                }

                // Create and store the request
                TeleportRequest request = new TeleportRequest(sender.getUUID(), target.getUUID(), RequestType.TPA);
                manager.addRequest(request);
                manager.setCooldown(sender.getUUID());

                // Notify sender
                MessageUtils.send(sender, TpaConfig.MESSAGES.requestSent.get(), target.getName().getString());

                // Create clickable message for target
                Component acceptButton = Component.literal(MessageUtils.formatColors("&a[Aceptar]"))
                                .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                "/tpaccept"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.literal("Clic para aceptar"))));

                Component denyButton = Component.literal(MessageUtils.formatColors("&c[Rechazar]"))
                                .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                "/tpdeny"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.literal("Clic para rechazar"))));

                String message = TpaConfig.MESSAGES.requestReceived.get();
                Component fullMessage = Component
                                .literal(MessageUtils
                                                .formatColors(String.format(message, sender.getName().getString())))
                                .append(Component.literal(" "))
                                .append(acceptButton)
                                .append(Component.literal(" "))
                                .append(denyButton);

                target.sendSystemMessage(fullMessage);

                int expirationTime = TpaConfig.GENERAL.requestExpiration.get();
                target.sendSystemMessage(Component
                                .literal(MessageUtils.formatColors("&7Expira en " + expirationTime + " segundos.")));

                return 1;
        }

        /**
         * Executes /tpahere command - sender wants target to teleport to them.
         */
        private static int executeTpaHere(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                ServerPlayer sender = context.getSource().getPlayerOrException();
                ServerPlayer target = EntityArgument.getPlayer(context, "player");

                // Can't teleport yourself to yourself
                if (sender.getUUID().equals(target.getUUID())) {
                        MessageUtils.send(sender, TpaConfig.MESSAGES.cannotTeleportSelf.get());
                        return 0;
                }

                // Check cooldown
                TpaManager manager = TpaManager.getInstance();
                if (manager.isOnCooldown(sender.getUUID())) {
                        long remaining = manager.getRemainingCooldown(sender.getUUID());
                        MessageUtils.send(sender, TpaConfig.MESSAGES.onCooldown.get(), remaining);
                        return 0;
                }

                // Create and store the request
                TeleportRequest request = new TeleportRequest(sender.getUUID(), target.getUUID(), RequestType.TPA_HERE);
                manager.addRequest(request);
                manager.setCooldown(sender.getUUID());

                // Notify sender
                MessageUtils.send(sender, TpaConfig.MESSAGES.requestSent.get(), target.getName().getString());

                // Create clickable message for target
                Component acceptButton = Component.literal(MessageUtils.formatColors("&a[Aceptar]"))
                                .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                "/tpaccept"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.literal("Clic para aceptar"))));

                Component denyButton = Component.literal(MessageUtils.formatColors("&c[Rechazar]"))
                                .setStyle(Style.EMPTY
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                "/tpdeny"))
                                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Component.literal("Clic para rechazar"))));

                String message = TpaConfig.MESSAGES.requestReceivedHere.get();
                Component fullMessage = Component
                                .literal(MessageUtils
                                                .formatColors(String.format(message, sender.getName().getString())))
                                .append(Component.literal(" "))
                                .append(acceptButton)
                                .append(Component.literal(" "))
                                .append(denyButton);

                target.sendSystemMessage(fullMessage);

                int expirationTime = TpaConfig.GENERAL.requestExpiration.get();
                target.sendSystemMessage(Component
                                .literal(MessageUtils.formatColors("&7Expira en " + expirationTime + " segundos.")));

                return 1;
        }

        /**
         * Executes /tpaccept command - accepts a pending teleport request.
         */
        private static int executeTpAccept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                ServerPlayer executor = context.getSource().getPlayerOrException();
                TeleportRequest request = TpaManager.getInstance().getRequest(executor.getUUID());

                // Check if there's a pending request
                if (request == null) {
                        MessageUtils.send(executor, TpaConfig.MESSAGES.noRequest.get());
                        return 0;
                }

                // Check if request has expired
                if (request.isExpired()) {
                        TpaManager.getInstance().removeRequest(executor.getUUID());
                        MessageUtils.send(executor, TpaConfig.MESSAGES.requestExpired.get());
                        return 0;
                }

                // Get the sender
                ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayer(request.getSender());

                if (sender == null) {
                        TpaManager.getInstance().removeRequest(executor.getUUID());
                        MessageUtils.send(executor, TpaConfig.MESSAGES.playerOffline.get());
                        return 0;
                }

                // Remove the request
                TpaManager.getInstance().removeRequest(executor.getUUID());

                // Notify sender
                MessageUtils.send(sender, TpaConfig.MESSAGES.requestAccepted.get(), executor.getName().getString());

                // Determine who teleports and where
                ServerPlayer teleportingPlayer;
                ServerPlayer destinationPlayer;

                if (request.getType() == RequestType.TPA) {
                        // Sender teleports to target (executor)
                        teleportingPlayer = sender;
                        destinationPlayer = executor;
                } else {
                        // Target (executor) teleports to sender
                        teleportingPlayer = executor;
                        destinationPlayer = sender;
                }

                // Check warmup configuration
                int warmupSeconds = TpaConfig.GENERAL.teleportWarmup.get();

                if (warmupSeconds == 0) {
                        // Instant teleport
                        performTeleport(teleportingPlayer, destinationPlayer);
                } else {
                        // Start warmup
                        WarmupTask warmupTask = new WarmupTask(
                                        teleportingPlayer.getUUID(),
                                        destinationPlayer.getUUID(),
                                        request.getType(),
                                        teleportingPlayer.position(),
                                        destinationPlayer.serverLevel(),
                                        destinationPlayer.position(),
                                        destinationPlayer.getYRot(),
                                        destinationPlayer.getXRot());

                        WarmupManager.getInstance().startWarmup(warmupTask);
                        MessageUtils.send(teleportingPlayer, TpaConfig.MESSAGES.warmupStart.get(), warmupSeconds);
                }

                return 1;
        }

        /**
         * Executes /tpdeny command - denies a pending teleport request.
         */
        private static int executeTpDeny(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                ServerPlayer executor = context.getSource().getPlayerOrException();
                TeleportRequest request = TpaManager.getInstance().getRequest(executor.getUUID());

                // Check if there's a pending request
                if (request == null) {
                        MessageUtils.send(executor, TpaConfig.MESSAGES.noRequest.get());
                        return 0;
                }

                // Get the sender to notify them
                ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayer(request.getSender());

                if (sender != null) {
                        MessageUtils.send(sender, TpaConfig.MESSAGES.requestDenied.get());
                }

                MessageUtils.send(executor, TpaConfig.MESSAGES.requestDenied.get());

                // Remove the request
                TpaManager.getInstance().removeRequest(executor.getUUID());

                return 1;
        }

        /**
         * Performs the actual teleportation.
         */
        private static void performTeleport(ServerPlayer teleportingPlayer, ServerPlayer destinationPlayer) {
                ServerLevel targetLevel = destinationPlayer.serverLevel();

                teleportingPlayer.teleportTo(targetLevel,
                                destinationPlayer.getX(), destinationPlayer.getY(), destinationPlayer.getZ(),
                                destinationPlayer.getYRot(), destinationPlayer.getXRot());

                MessageUtils.send(teleportingPlayer, TpaConfig.MESSAGES.teleporting.get());
        }
}
