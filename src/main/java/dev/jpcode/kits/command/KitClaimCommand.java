package dev.jpcode.kits.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.kits.Kit;
import dev.jpcode.kits.KitPerms;
import dev.jpcode.kits.PlayerKitData;
import dev.jpcode.kits.TimeUtil;
import dev.jpcode.kits.access.ServerPlayerEntityAccess;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

import static dev.jpcode.kits.InventoryUtil.offerAllCopies;
import static dev.jpcode.kits.KitUtil.runCommands;
import static dev.jpcode.kits.KitsMod.KIT_MAP;

public class KitClaimCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String kitName = StringArgumentType.getString(context, "kit_name");
        return exec(context.getSource().getPlayerOrException(), kitName);
    }

    public static int exec(ServerPlayer player, String kitName) {
        PlayerKitData playerData = ((ServerPlayerEntityAccess) player).kits$getPlayerData();
        var commandSource = player.createCommandSourceStack();

        Kit kit = KIT_MAP.get(kitName);
        long currentTime = Util.getEpochMillis();
        long remainingTime = (playerData.getKitUsedTime(kitName) + kit.cooldown()) - currentTime;

        if (!KitPerms.checkKit(commandSource, kitName)) {
            commandSource.sendFailure(Component.nullToEmpty(String.format(
                "Insufficient permissions for kit '%s'.",
                kitName)));
            return -1;
        } else if (remainingTime > 0) {
            commandSource.sendFailure(Component.nullToEmpty(
                String.format(
                    "Kit '%s' is on cooldown. %s remaining.",
                    kitName,
                    TimeUtil.formatTime(remainingTime)
                )));
            return -2;
        }

        Inventory playerInventory = player.getInventory();
        playerData.useKit(kitName);
        offerAllCopies(kit.inventory(), playerInventory);
        if (!kit.commands().isEmpty()) runCommands(player, kit.commands());

        commandSource.sendSuccess(() ->
            Component.nullToEmpty(String.format("Successfully claimed kit '%s'!", kitName)),
            commandSource.getServer().shouldInformAdmins()
        );

        return 1;
    }
}
