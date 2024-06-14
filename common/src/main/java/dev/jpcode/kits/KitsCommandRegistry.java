package dev.jpcode.kits;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.jpcode.kits.platform.Services;

import eu.pb4.sgui.api.gui.SimpleGuiBuilder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import dev.jpcode.kits.access.ServerPlayerEntityAccess;
import dev.jpcode.kits.command.KitClaimCommand;
import dev.jpcode.kits.command.KitCommandsManagerCommand;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class KitsCommandRegistry {

    private KitsCommandRegistry() {
    }

    static int addKit(CommandContext<CommandSourceStack> context, String kitName, Inventory sourceInventory, long cooldown) {
        var kitInventory = new KitInventory();
        kitInventory.copyFrom(sourceInventory);
        return addKit(context, kitName, new Kit(kitInventory, cooldown));
    }

    static int addKit(CommandContext<CommandSourceStack> context, String kitName, Kit kit) {
        Kits.KIT_MAP.put(kitName, kit);

        try {
            saveKit(kitName, kit);
            context.getSource().sendSuccess(() ->
                Component.nullToEmpty(String.format("Kit '%s' created from current inventory.", kitName)),
                true
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void saveKit(String kitName, Kit kit) throws IOException {
        CompoundTag root = new CompoundTag();
        kit.writeNbt(root);

        NbtIo.write(
            root,
            Kits.getKitsDir().toPath().resolve(String.format("%s.nbt", kitName)).toFile()
        );
    }

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext commandRegistryAccess,
        Commands.CommandSelection registrationEnvironment) {
        CommandNode<CommandSourceStack> kitNode = dispatcher.register(literal("kit"));

        kitNode.addChild(literal("add")
            .requires(require("kits.manage", 4))
            .then(argument("kit_name", StringArgumentType.word())
                .then(argument("cooldown", LongArgumentType.longArg(-1))
                    .executes(context -> addKit(
                        context,
                        StringArgumentType.getString(context, "kit_name"),
                        context.getSource().getPlayer().getInventory(),
                        LongArgumentType.getLong(context, "cooldown")
                    ))
                    .then(argument("time_unit", StringArgumentType.word())
                        .suggests(TimeUtil::suggestions)
                        .executes(context -> addKit(
                            context,
                            StringArgumentType.getString(context, "kit_name"),
                            context.getSource().getPlayer().getInventory(),
                            TimeUtil.parseToMillis(
                                LongArgumentType.getLong(context, "cooldown"),
                                StringArgumentType.getString(context, "time_unit"))
                        ))))
            ).build()
        );

        kitNode.addChild(literal("setDisplayItem")
            .requires(require("kits.manage", 4))
            .then(argument("kit_name", StringArgumentType.word())
                .suggests(Kits::suggestionProvider)
                .then(argument("item", ItemArgument.item(commandRegistryAccess))
                    .executes(context -> {
                        var kitName = StringArgumentType.getString(context, "kit_name");
                        var item = ItemArgument.getItem(context, "item");

                        var existingKit = Kits.KIT_MAP.get(kitName);
                        existingKit.setDisplayItem(item.getItem());
                        try {
                            saveKit(kitName, existingKit);
                        } catch (IOException e) {
                            throw new CommandRuntimeException(Component.literal("Failed to save kit."));
                        }
                        return 0;
                    })
                )
            ).build()
        );

        kitNode.addChild(literal("claim")
            .then(argument("kit_name", StringArgumentType.word())
                .suggests(Kits::suggestionProvider)
                .executes(new KitClaimCommand())
            ).build()
        );

        kitNode.addChild(literal("remove")
            .requires(require("kits.manage", 4))
            .then(argument("kit_name", StringArgumentType.word())
                .suggests(Kits::suggestionProvider)
                .executes(context -> {
                    String kitName = StringArgumentType.getString(context, "kit_name");
                    Kits.KIT_MAP.remove(kitName);

                    try {
                        Files.delete(Kits.getKitsDir().toPath().resolve(kitName + ".nbt"));
                    } catch (IOException e) {
                        context.getSource().sendFailure(Component.nullToEmpty("Could not find kit file on disk."));
                        return -1;
                    }

                    context.getSource().sendSuccess(() -> Component.nullToEmpty(String.format("Removed kit '%s'.", kitName)), true);

                    return 1;
                })
            ).build()
        );

        kitNode.addChild(literal("reload")
            .requires(require("kits.manage", 4))
            .executes(context -> {
                Kits.reloadKits(context.getSource().getServer());
                return 1;
            }).build()
        );

        kitNode.addChild(literal("resetPlayerKit")
            .requires(require("kits.manage", 4))
            .then(argument("players", EntityArgument.players())
                .then(argument("kit_name", StringArgumentType.word())
                    .suggests(Kits::suggestionProvider)
                    .executes(context -> {
                        var kitName = StringArgumentType.getString(context, "kit_name");
                        var targetPlayers = EntityArgument.getPlayers(context, "players");

                        for (var player : targetPlayers) {
                            ((ServerPlayerEntityAccess) player).kits$getPlayerData().resetKitCooldown(kitName);
                        }

                        context.getSource().sendSuccess(() ->
                            Component.literal(String.format("Reset kit '%s' cooldown for %d players", kitName, targetPlayers.size())),
                            true);

                        return 1;
                    })
                )).build()
        );

        kitNode.addChild(literal("resetPlayer")
            .requires(require("kits.manage", 4))
            .then(argument("players", EntityArgument.players())
                .executes(context -> {
                    var targetPlayers = EntityArgument.getPlayers(context, "players");
                    for (var player : targetPlayers) {
                        ((ServerPlayerEntityAccess) player).kits$getPlayerData().resetAllKits();
                    }

                    context.getSource().sendSuccess(() ->
                        Component.literal(String.format("Reset all kit cooldowns for %d players", targetPlayers.size())),
                        true);

                    return 1;
                })
            ).build()
        );

        kitNode.addChild(literal("commands")
            .requires(require("kits.manage", 4))
            .then(argument("kit_name", StringArgumentType.word())
                .suggests(Kits::suggestionProvider)
                .then(literal("list")
                    .executes(KitCommandsManagerCommand::listCommandsForKit)
                )
                .then(literal("add")
                    .then(argument("command", StringArgumentType.greedyString())
                        .executes(KitCommandsManagerCommand::addCommandToKit)
                    )
                )
                .then(literal("remove")
                    .then(argument("command", StringArgumentType.greedyString())
                        .suggests((CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
                            String kitName = StringArgumentType.getString(context, "kit_name");
                            return ListSuggestion.getSuggestionsBuilder(builder, Kits.KIT_MAP.containsKey(kitName)
                                ? Kits.KIT_MAP.get(kitName).commands()
                                : new ArrayList<>());
                        })
                        .executes(KitCommandsManagerCommand::removeCommandFromKit)
                    )
                )
            )
            .build()
        );

        var kitsSguiBuilder = literal("kits")
            .executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                var playerData = ((ServerPlayerEntityAccess) player).kits$getPlayerData();
                var allPlayerKits = Kits.getAllKitsForPlayer(player);

                long currentTime = Util.getEpochMillis();
                Function<Map.Entry<String, Kit>, Boolean> canUseKit = (entry) ->
                    (playerData.getKitUsedTime(entry.getKey()) + entry.getValue().cooldown()) - currentTime <= 0;

                var simpleGuiBuilder = new SimpleGuiBuilder(MenuType.GENERIC_9x3, false);
                simpleGuiBuilder.setLockPlayerInventory(true);
                simpleGuiBuilder.setTitle(Component.literal("Claim Kit"));

                int i = 0;
                for (var kitEntry : allPlayerKits.toList()) {
                    var defaultItemStack = (canUseKit.apply(kitEntry)
                            ? kitEntry.getValue()
                                .displayItem()
                                .orElse(Items.EMERALD_BLOCK)
                            : Items.GRAY_CONCRETE_POWDER)
                        .getDefaultInstance();

                    simpleGuiBuilder.setSlot(
                        i++,
                        createKitItemStack(kitEntry.getKey(), defaultItemStack),
                        (index, type, action, gui) -> {
                            if (type.isLeft) {
                                KitClaimCommand.exec(player, kitEntry.getKey());
                                gui.close();
                            }
                        });
                }

                var simpleGui = simpleGuiBuilder.build(player);
                simpleGui.open();

                return 0;
            });
        dispatcher.register(kitsSguiBuilder);
    }

    private static Predicate<CommandSourceStack> require(String key,int defaultV) {
        return source -> Services.PLATFORM.checkPermission(source,key,defaultV);
    }

    private static ItemStack createKitItemStack(String kitName, ItemStack itemStack) {
        return itemStack
            .copy()
            .setHoverName(Component.literal(kitName));
    }
}
