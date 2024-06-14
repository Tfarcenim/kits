package dev.jpcode.kits;

import java.util.LinkedHashMap;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import dev.jpcode.kits.access.ServerPlayerEntityAccess;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerDataManager {

    private final LinkedHashMap<UUID, PlayerKitData> dataMap;
    private static PlayerDataManager instance;

    public PlayerDataManager() {
        instance = this;
        this.dataMap = new LinkedHashMap<>();
    }

    static {
        ServerPlayConnectionEvents.JOIN.register(PlayerDataManager::onPlayerConnect);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerDataManager::onPlayerLeave);
        ServerPlayerEvents.COPY_FROM.register(PlayerDataManager::handlePlayerDataRespawnSync);
    }

    public static void onPlayerConnect(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = handler.player;
        PlayerKitData playerData = instance.addPlayer(player);
        ((ServerPlayerEntityAccess) player).kits$setPlayerData(playerData);

//        // Detect 1st-join
//        GameProfile gameProfile = player.getGameProfile();
//        UserCache userCache = player.getServer().getUserCache();
//        GameProfile gameProfile2 = userCache.getByUuid(gameProfile.getId());
//        if (gameProfile2 == null) {
//            // Player is new. Do first-join things...
//
//        }

        // Detect if player has gotten starter kit
        if (!playerData.hasReceivedStarterKit()) {
            Kit starterKit = KitsMod.getStarterKit();
            if (starterKit != null) {
                KitUtil.giveKit(player, starterKit);
                if (!starterKit.commands().isEmpty())
                    KitUtil.runCommands(player, starterKit.commands());
                playerData.setHasReceivedStarterKit(true);
                if (KitsMod.CONFIG.starterKitSetCooldown.getValue())
                    playerData.useKit(KitsMod.CONFIG.starterKit.getValue());
            }
        }
    }

    public static void onPlayerLeave(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        // Auto-saving should be handled by WorldSaveHandlerMixin. (PlayerData saves when MC server saves players)
        ServerPlayer player = handler.player;
        instance.unloadPlayerData(player);
        ((ServerPlayerEntityAccess) player).kits$getPlayerData().save();
    }

    public static void handlePlayerDataRespawnSync(ServerPlayer oldPlayerEntity, ServerPlayer newPlayerEntity,boolean alive) {
        var oldPlayerAccess = ((ServerPlayerEntityAccess) oldPlayerEntity);
        var newPlayerAccess = ((ServerPlayerEntityAccess) newPlayerEntity);

        PlayerKitData playerData = oldPlayerAccess.kits$getPlayerData();
        playerData.setPlayer(newPlayerEntity);
        newPlayerAccess.kits$setPlayerData(playerData);
    }

    public PlayerKitData addPlayer(ServerPlayer player) {
        PlayerKitData playerData = PlayerKitDataFactory.create(player);
        dataMap.put(player.getUUID(), playerData);
        return playerData;
    }

    private void unloadPlayerData(ServerPlayer player) {
        this.dataMap.remove(player.getUUID());
    }

}
