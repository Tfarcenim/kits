package dev.jpcode.kits.mixin.sgui;

import java.util.OptionalInt;


import dev.jpcode.kits.sgui.impl.PlayerExtensions;

import dev.jpcode.kits.sgui.virtual.SguiScreenHandlerFactory;

import dev.jpcode.kits.sgui.virtual.VirtualScreenHandlerInterface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements PlayerExtensions {
    @Shadow
    public abstract void doCloseContainer();

    @Unique
    private boolean sgui$ignoreNext = false;

    public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V", shift = At.Shift.BEFORE))
    private void sgui$dontForceCloseFor(MenuProvider factory, CallbackInfoReturnable<OptionalInt> cir) {
        if (factory instanceof SguiScreenHandlerFactory<?> sguiScreenHandlerFactory && !sguiScreenHandlerFactory.gui().resetMousePosition()) {
            this.sgui$ignoreNext = true;
        }
    }

    @Inject(method = "closeContainer", at = @At("HEAD"), cancellable = true)
    private void sgui$ignoreClosing(CallbackInfo ci) {
        if (this.sgui$ignoreNext) {
            this.sgui$ignoreNext = false;
            this.doCloseContainer();
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void sgui$onDeath(DamageSource source, CallbackInfo ci) {
        if (this.containerMenu instanceof VirtualScreenHandlerInterface handler) {
            handler.getGui().close(true);
        }
    }

    @Override
    public void sgui$ignoreNextClose() {
        this.sgui$ignoreNext = true;
    }
}
