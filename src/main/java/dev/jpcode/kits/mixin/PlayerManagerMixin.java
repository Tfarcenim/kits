package dev.jpcode.kits.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.jpcode.kits.events.PlayerConnectCallback;
import dev.jpcode.kits.events.PlayerLeaveCallback;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    public void onPlayerConnect(Connection connection, ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerConnectCallback.EVENT_HEAD.invoker().onPlayerConnect(connection, player);
    }

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    public void onPlayerConnectTail(Connection connection, ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerConnectCallback.EVENT_RETURN.invoker().onPlayerConnect(connection, player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onPlayerLeave(ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerLeaveCallback.EVENT.invoker().onPlayerLeave(player);
    }
}
