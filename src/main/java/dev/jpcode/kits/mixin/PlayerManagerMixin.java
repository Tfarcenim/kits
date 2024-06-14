package dev.jpcode.kits.mixin;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import dev.jpcode.kits.PlayerDataManager;
import dev.jpcode.kits.events.PlayerConnectCallback;
import dev.jpcode.kits.events.PlayerLeaveCallback;
import dev.jpcode.kits.events.PlayerRespawnCallback;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    public void onPlayerConnect(Connection connection, ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerConnectCallback.EVENT_HEAD.invoker().onPlayerConnect(connection, player);
    }

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void onPlayerConnectTail(Connection connection, ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerConnectCallback.EVENT_RETURN.invoker().onPlayerConnect(connection, player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onPlayerLeave(ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerLeaveCallback.EVENT.invoker().onPlayerLeave(player);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    @Inject(
        method = "respawnPlayer",
        at = @At(
            value = "INVOKE",
            // This target is near-immediately after the new ServerPlayerEntity is
            // created. This lets us update the EC PlayerData, sooner, might be
            // before the new ServerPlayerEntity is fully initialized.
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRespawnPlayer(
        ServerPlayer oldServerPlayerEntity
        , boolean alive
        , CallbackInfoReturnable<ServerPlayer> cir
        , BlockPos blockPos
        , float f
        , boolean bl
        , ServerLevel serverWorld
        , Optional optional
        , ServerLevel serverWorld2
        , ServerPlayer serverPlayerEntity
    ) {
        PlayerDataManager.handlePlayerDataRespawnSync(oldServerPlayerEntity, serverPlayerEntity);
    }

    @Inject(method = "respawnPlayer", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getLevelProperties()Lnet/minecraft/world/WorldProperties;"
        ), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRespawnPlayer(ServerPlayer oldServerPlayerEntity, boolean alive, CallbackInfoReturnable<ServerPlayer> cir,
                                BlockPos blockPos,
                                float f,
                                boolean bl,
                                ServerLevel serverWorld,
                                Optional optional2,
                                ServerLevel serverWorld2,
                                ServerPlayer serverPlayerEntity,
                                boolean bl2
    ) {
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(oldServerPlayerEntity, serverPlayerEntity);
    }
}
