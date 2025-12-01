package com.lake.simpletpa.commands;

import com.lake.simpletpa.RequestType;
import com.lake.simpletpa.TeleportRequest;
import com.lake.simpletpa.TpaManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
            sender.sendSystemMessage(Component.literal("§cYou cannot send a teleport request to yourself!"));
            return 0;
        }

        // Create and store the request
        TeleportRequest request = new TeleportRequest(sender.getUUID(), target.getUUID(), RequestType.TPA);
        TpaManager.getInstance().addRequest(request);

        // Notify sender
        sender.sendSystemMessage(Component.literal("§aTeleport request sent to " + target.getName().getString() + "."));

        // Create clickable message for target
        Component acceptButton = Component.literal("§a[Accept]")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to accept"))));

        Component denyButton = Component.literal("§c[Deny]")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to deny"))));

        Component message = Component.literal("§e" + sender.getName().getString() + " wants to teleport to you. ")
                .append(acceptButton)
                .append(Component.literal(" "))
                .append(denyButton);

        target.sendSystemMessage(message);
        target.sendSystemMessage(Component.literal("§7This request will expire in 2 minutes."));

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
            sender.sendSystemMessage(Component.literal("§cYou cannot send a teleport request to yourself!"));
            return 0;
        }

        // Create and store the request
        TeleportRequest request = new TeleportRequest(sender.getUUID(), target.getUUID(), RequestType.TPA_HERE);
        TpaManager.getInstance().addRequest(request);

        // Notify sender
        sender.sendSystemMessage(Component.literal("§aTeleport request sent to " + target.getName().getString() + "."));

        // Create clickable message for target
        Component acceptButton = Component.literal("§a[Accept]")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to accept"))));

        Component denyButton = Component.literal("§c[Deny]")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to deny"))));

        Component message = Component.literal("§e" + sender.getName().getString() + " wants you to teleport to them. ")
                .append(acceptButton)
                .append(Component.literal(" "))
                .append(denyButton);

        target.sendSystemMessage(message);
        target.sendSystemMessage(Component.literal("§7This request will expire in 2 minutes."));

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
            executor.sendSystemMessage(Component.literal("§cYou don't have any pending teleport requests."));
            return 0;
        }

        // Check if request has expired
        if (request.isExpired()) {
            TpaManager.getInstance().removeRequest(executor.getUUID());
            executor.sendSystemMessage(Component.literal("§cThat teleport request has expired."));
            return 0;
        }

        // Get the sender
        ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayer(request.getSender());

        if (sender == null) {
            TpaManager.getInstance().removeRequest(executor.getUUID());
            executor.sendSystemMessage(Component.literal("§cThat player is no longer online."));
            return 0;
        }

        // Perform the teleport based on request type
        if (request.getType() == RequestType.TPA) {
            // Sender teleports to target (executor)
            ServerLevel targetLevel = executor.serverLevel();
            sender.teleportTo(targetLevel,
                    executor.getX(), executor.getY(), executor.getZ(),
                    executor.getYRot(), executor.getXRot());

            sender.sendSystemMessage(Component.literal("§aTeleporting to " + executor.getName().getString() + "..."));
            executor.sendSystemMessage(
                    Component.literal("§a" + sender.getName().getString() + " has teleported to you."));
        } else {
            // Target (executor) teleports to sender
            ServerLevel senderLevel = sender.serverLevel();
            executor.teleportTo(senderLevel,
                    sender.getX(), sender.getY(), sender.getZ(),
                    sender.getYRot(), sender.getXRot());

            executor.sendSystemMessage(Component.literal("§aTeleporting to " + sender.getName().getString() + "..."));
            sender.sendSystemMessage(
                    Component.literal("§a" + executor.getName().getString() + " has teleported to you."));
        }

        // Remove the request
        TpaManager.getInstance().removeRequest(executor.getUUID());

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
            executor.sendSystemMessage(Component.literal("§cYou don't have any pending teleport requests."));
            return 0;
        }

        // Get the sender to notify them
        ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayer(request.getSender());

        if (sender != null) {
            sender.sendSystemMessage(
                    Component.literal("§c" + executor.getName().getString() + " has denied your teleport request."));
        }

        executor.sendSystemMessage(Component.literal("§cTeleport request denied."));

        // Remove the request
        TpaManager.getInstance().removeRequest(executor.getUUID());

        return 1;
    }
}
