package dev.jpcode.kits;

import java.io.File;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

public abstract class PlayerData extends SavedData {

    private ServerPlayer player;
    private final File saveFile;

    PlayerData(ServerPlayer player, File saveFile) {
        this.player = player;
        this.saveFile = saveFile;
    }

    public void setPlayer(ServerPlayer serverPlayerEntity) {
        this.player = serverPlayerEntity;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public abstract void fromNbt(CompoundTag nbtCompound3);

    public File getSaveFile() {
        return this.saveFile;
    }

    public void save() {
        super.save(saveFile);
    }

}
