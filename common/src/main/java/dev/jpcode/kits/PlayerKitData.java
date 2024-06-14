package dev.jpcode.kits;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerKitData extends PlayerData {

    private Map<String, Long> kitUsedTimes;
    private boolean hasReceivedStarterKit;

    public PlayerKitData(ServerPlayer player, File saveFile) {
        super(player, saveFile);
        kitUsedTimes = new HashMap<>();
    }

    public void useKit(String kitName) {
        kitUsedTimes.put(kitName, Util.getEpochMillis());
        setDirty();
        save();
    }

    public long getKitUsedTime(String kitName) {
        try {
            return kitUsedTimes.get(kitName);
        } catch (NullPointerException notYetUsed) {
            return 0;
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {

        CompoundTag kitUsedTimesNbt = new CompoundTag();
        kitUsedTimes.forEach(kitUsedTimesNbt::putLong);

        nbt.put("kitUsedTimes", kitUsedTimesNbt);

        nbt.putBoolean("hasReceivedStarterKit", this.hasReceivedStarterKit);

        return nbt;
    }

    @Override
    public void fromNbt(CompoundTag nbtCompound) {
        CompoundTag dataTag = nbtCompound.getCompound("data");
        CompoundTag kitUsedTimesNbt = dataTag.getCompound("kitUsedTimes");
        for (String key : kitUsedTimesNbt.getAllKeys()) {
            this.kitUsedTimes.put(key, kitUsedTimesNbt.getLong(key));
        }
        this.hasReceivedStarterKit = dataTag.getBoolean("hasReceivedStarterKit");
    }

    public boolean hasReceivedStarterKit() {
        return hasReceivedStarterKit;
    }

    public void setHasReceivedStarterKit(boolean hasReceivedStarterKit) {
        this.hasReceivedStarterKit = hasReceivedStarterKit;
        this.setDirty();
        this.save();
    }

    public void resetKitCooldown(String kitName) {
        this.kitUsedTimes.remove(kitName);
        this.setDirty();
    }

    public void resetAllKits() {
        this.kitUsedTimes.clear();
        this.setDirty();
    }
}
