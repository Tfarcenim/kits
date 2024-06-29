package dev.jpcode.kits;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.jpcode.kits.access.ServerPlayerEntityAccess;

import dev.jpcode.kits.platform.Services;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.level.storage.LevelResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class Kits {

    public static final String MOD_ID = "kits";
    public static final String MOD_NAME = "Kits";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final Map<String, Kit> KIT_MAP = new HashMap<>();
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

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {
        Kits.LOGGER.info("Kits initialized.");
        PlayerDataManager playerDataManager = new PlayerDataManager();
    }

    public static void onPlayerConnect(ServerPlayer player) {

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
    }


    public static Stream<Map.Entry<String, Kit>> getAllKitsForPlayer(ServerPlayer player) {
        var source = player.createCommandSourceStack();
        return KIT_MAP.entrySet()
            .stream()
            .filter(kitEntry ->
                checkKit(source, kitEntry.getKey())
            );
    }

    public static boolean checkKit(CommandSourceStack source, String kitName) {
        return Services.PLATFORM.canUseKit(source, kitName);
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

}
