package dev.jpcode.kits.mixin;

import org.spongepowered.asm.mixin.Mixin;
import dev.jpcode.kits.PlayerKitData;
import dev.jpcode.kits.access.ServerPlayerEntityAccess;
import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityAccess {

    private PlayerKitData kits$playerData;

    @Override
    public PlayerKitData kits$getPlayerData() {
        return this.kits$playerData;
    }

    @Override
    public void kits$setPlayerData(PlayerKitData playerData) {
        this.kits$playerData = playerData;
    }
}
