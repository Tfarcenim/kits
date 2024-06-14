package dev.jpcode.kits;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class Kit {

    private final KitInventory inventory;
    private final long cooldown;
    private @Nullable Item displayItem;
    private ArrayList<String> commands;

    public Kit(KitInventory inventory, long cooldown) {
        this.inventory = inventory;
        this.cooldown = cooldown;
        commands = new ArrayList<>();
    }

    public Kit(KitInventory inventory, long cooldown, @Nullable Item displayItem, ArrayList<String> commands) {
        this.inventory = inventory;
        this.cooldown = cooldown;
        this.displayItem = displayItem;
        this.commands = commands;
    }

    public KitInventory inventory() {
        return inventory;
    }

    public long cooldown() {
        return cooldown;
    }

    public Optional<Item> displayItem() {
        return Optional.ofNullable(displayItem);
    }

    public void setDisplayItem(@Nullable Item item) {
        this.displayItem = item;
    }

    public ArrayList<String> commands() {
        return commands;
    }

    public boolean addCommand(String command) {
        if (commands.contains(command)) return false;
        this.commands.add(command);
        return true;
    }

    public boolean removeCommand(String command) {
        if (commands().isEmpty() || !commands.contains(command)) return false;
        else commands.remove(command);
        return true;
    }

    private static final class StorageKey {
        public static final String INVENTORY = "inventory";
        public static final String COOLDOWN = "cooldown";
        public static final String DISPLAY_ITEM = "display_item";
        public static final String COMMANDS = "commands";
    }

    public void writeNbt(CompoundTag root) {
        root.put(StorageKey.INVENTORY, this.inventory().writeNbt(new ListTag()));
        root.putLong(StorageKey.COOLDOWN, this.cooldown());
        if (this.displayItem().isPresent()) {
            root.putString(
                StorageKey.DISPLAY_ITEM,
                BuiltInRegistries.ITEM.getResourceKey(this.displayItem().get()).get().location().toString());
        }
        if (!commands.isEmpty()) {
            ListTag list = new ListTag();
            for (String command : commands) {
                list.add(StringTag.valueOf(command));
            }
            root.put(StorageKey.COMMANDS, list);
        }
    }

    public static Kit fromNbt(CompoundTag kitNbt) {
        var kitInventory = new KitInventory();

        assert kitNbt != null;
        kitInventory.readNbt(kitNbt.getList(StorageKey.INVENTORY, Tag.TAG_COMPOUND));
        long cooldown = kitNbt.getLong(StorageKey.COOLDOWN);
        var kitDisplayItem = kitNbt.contains(StorageKey.DISPLAY_ITEM)
            ? BuiltInRegistries.ITEM.get(new ResourceLocation(kitNbt.getString(StorageKey.DISPLAY_ITEM)))
            : null;
        ArrayList<String> commands = kitNbt.contains(StorageKey.COMMANDS)
            ? new ArrayList<>(kitNbt.getList(StorageKey.COMMANDS, Tag.TAG_STRING).stream().map(Tag::getAsString).toList())
            : new ArrayList<>();

        return new Kit(kitInventory, cooldown, kitDisplayItem, commands);
    }
}
