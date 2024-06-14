package dev.jpcode.kits.command;

import java.io.IOException;
import java.util.List;

import dev.jpcode.kits.Kits;

import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jpcode.kits.Kit;

import static dev.jpcode.kits.KitsCommandRegistry.saveKit;

public final class KitCommandsManagerCommand {
    private KitCommandsManagerCommand() {}

    public static int listCommandsForKit(CommandContext<CommandSourceStack> context) {
        String kitName = StringArgumentType.getString(context, "kit_name");
        CommandSourceStack source = context.getSource();
        Kit kit = getKit(kitName);

        MutableComponent message = Component.literal(String.format("Kit '%s'", kitName));
        if (!kit.commands().isEmpty()) {
            message.append(" (click a command to remove)");
            List<String> commands = kit.commands();
            for (int i = 1; i <= commands.size(); i++) {
                String command = commands.get(i - 1);
                message.append(Component.literal(String.format("\n#%d: %s", i, command))
                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        String.format("/kit commands %s remove %s", kitName, command)))));
            }
        } else {
            message.append("\nDoes not have any commands");
        }

        source.sendSuccess(() -> message, false);
        return 1;
    }

    public static int addCommandToKit(CommandContext<CommandSourceStack> context) {
        String kitName = StringArgumentType.getString(context, "kit_name");
        CommandSourceStack source = context.getSource();
        Kit kit = getKit(kitName);

        String command = StringArgumentType.getString(context, "command")
            .replaceFirst("^/", ""); // remove first slash at the start

        try {
            boolean added = kit.addCommand(command);
            if (!added) throw new CommandRuntimeException(Component.literal("Command already exists in this kit."));
            saveKit(kitName, kit);
            source.sendSuccess(() ->
                    Component.literal(String.format("Added command \"%s\" to kit '%s'", command, kitName)),
                true);
        } catch (IOException e) {
            throw new CommandRuntimeException(Component.literal("Failed to save kit."));
        }
        return 1;
    }

    public static int removeCommandFromKit(CommandContext<CommandSourceStack> context) {
        String kitName = StringArgumentType.getString(context, "kit_name");
        CommandSourceStack source = context.getSource();
        Kit kit = getKit(kitName);

        String command = StringArgumentType.getString(context, "command");

        try {
            boolean existed = kit.removeCommand(command);
            if (!existed) throw new CommandRuntimeException(Component.literal("That command is not in this kit."));
            saveKit(kitName, kit);
            source.sendSuccess(() ->
                    Component.literal(String.format("Removed command \"%s\" from kit '%s'. (click to re-add)", command, kitName))
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            String.format("/kit commands %s add %s", kitName, command)))),
                true);
        } catch (IOException e) {
            throw new CommandRuntimeException(Component.literal("Failed to save kit."));
        }
        return 1;
    }

    private static Kit getKit(String kitName) {
        if (!Kits.KIT_MAP.containsKey(kitName)) {
            throw new CommandRuntimeException(Component.literal(String.format("Kit '%s' does not exist", kitName)));
        }
        return Kits.KIT_MAP.get(kitName);
    }
}
