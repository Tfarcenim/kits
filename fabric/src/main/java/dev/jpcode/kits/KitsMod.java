package dev.jpcode.kits;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import dev.jpcode.kits.access.ServerPlayerEntityAccess;
import dev.jpcode.kits.config.KitsConfig;

public class KitsMod implements ModInitializer {
    public static final KitsConfig CONFIG = new KitsConfig(
        Path.of("./config/kits.properties"),
        "Kits Config",
        "https://github.com/John-Paul-R/kits/wiki/Basic-Usage"
    );
    public static final Map<String, Kit> KIT_MAP = new HashMap<String, Kit>();
    private static File kitsDir;
    private static Path userDataDir;

    private static Kit starterKit;

    public static File getKitsDir() {
        return kitsDir;
    }

    public static Path getUserDataDirDir() {
        return userDataDir;
    }

    public static Kit getStarterKit() {
        return starterKit;
    }

    @Override
    public void onInitialize() {
        Kits.LOGGER.info("Kits is getting ready...");

        KitPerms.init();

        PlayerDataManager playerDataManager = new PlayerDataManager();

        ServerLifecycleEvents.SERVER_STARTING.register(KitsMod::reloadKits);

        CommandRegistrationCallback.EVENT.register(KitsCommandRegistry::register);

        Kits.LOGGER.info("Kits initialized.");
    }

    public static void reloadKits(MinecraftServer server) {
        KIT_MAP.clear();
        kitsDir = server.getServerDirectory().toPath().resolve("config/kits").toFile();
        userDataDir = server.getWorldPath(LevelResource.ROOT).resolve("kits_user_data");

        // if the dir was not just created, load all kits from dir.
        if (!kitsDir.mkdirs()) {
            File[] kitFiles = kitsDir.listFiles();
            if (kitFiles == null) {
                throw new IllegalStateException(
                    String.format("Failed to list files in the kits directory ('%s')", kitsDir.getPath()));
            }
            for (File kitFile : kitFiles) {
                try {
                    Kits.LOGGER.info(String.format("Loading kit '%s'", kitFile.getName()));
                    CompoundTag kitNbt = NbtIo.read(kitFile);
                    String fileName = kitFile.getName();
                    String kitName = fileName.substring(0, fileName.length() - 4);
                    KIT_MAP.put(kitName, Kit.fromNbt(kitNbt));
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        CONFIG.loadOrCreateProperties();
    }

    public static Stream<Map.Entry<String, Kit>> getAllKitsForPlayer(ServerPlayer player) {
        var source = player.createCommandSourceStack();
        return KIT_MAP.entrySet()
            .stream()
            .filter(kitEntry ->
                KitPerms.checkKit(source, kitEntry.getKey())
            );
    }

    public static Stream<Map.Entry<String, Kit>> getClaimableKitsForPlayer(ServerPlayer player) {
        var playerData = ((ServerPlayerEntityAccess) player).kits$getPlayerData();
        long currentTime = Util.getEpochMillis();

        return getAllKitsForPlayer(player)
            .filter(entry -> {
                long remainingTime = (playerData.getKitUsedTime(entry.getKey()) + entry.getValue().cooldown()) - currentTime;
                return remainingTime <= 0;
            });
    }

    /**
     * Suggests existing kits that the user has permissions for.
     *
     * @param context
     * @param builder
     * @return suggestions for existing kits that the user has permissions for.
     */
    public static CompletableFuture<Suggestions> suggestionProvider(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSourceStack source = context.getSource();
        return ListSuggestion.getSuggestionsBuilder(
            builder,
            getAllKitsForPlayer(source.getPlayer())
                .map(Map.Entry::getKey)
                .toList());
    }

    public static void setStarterKit(String s) {
        if (s == null) {
            starterKit = null;
        } else {
            starterKit = KIT_MAP.get(s);
            if (starterKit == null) {
                Kits.LOGGER.warn(String.format("Provided starter kit name, '%s' could not be found.", s));
            } else {
                Kits.LOGGER.info(String.format("Starter kit set to '%s'", s));
            }
        }
    }

}
