package dev.jpcode.kits;

import java.util.Iterator;
import java.util.List;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

public class KitInventory implements Container {

    public static final int MAIN_SIZE = 36;
    private static final int HOTBAR_SIZE = 9;
    public static final int OFF_HAND_SLOT = 40;
    public static final int NOT_FOUND = -1;
    public static final int[] ARMOR_SLOTS = new int[]{0, 1, 2, 3};
    public static final int[] HELMET_SLOTS = new int[]{3};

    public final NonNullList<ItemStack> main;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offHand;
    private final List<NonNullList<ItemStack>> combinedInventory;
    private int changeCount;

    public KitInventory() {
        this.main = NonNullList.withSize(MAIN_SIZE, ItemStack.EMPTY);
        this.armor = NonNullList.withSize(ARMOR_SLOTS.length, ItemStack.EMPTY);
        this.offHand = NonNullList.withSize(1, ItemStack.EMPTY);
        this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
    }

    public static int getHotbarSize() {
        return 9;
    }

    public static boolean isValidHotbarIndex(int slot) {
        return slot >= 0 && slot < 9;
    }

    private boolean canStackAddMore(@NotNull ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty()
            && ItemStack.isSameItemSameTags(existingStack, stack)
            && existingStack.isStackable()
            && existingStack.getCount() < existingStack.getMaxStackSize()
            && existingStack.getCount() < this.getMaxStackSize();
    }

    public int getEmptySlot() {
        for (int i = 0; i < this.main.size(); ++i) {
            if (this.main.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    public int getSlotWithStack(ItemStack stack) {
        for (int i = 0; i < this.main.size(); ++i) {
            if (!this.main.get(i).isEmpty() && ItemStack.isSameItemSameTags(stack, this.main.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public int indexOf(ItemStack stack) {
        for (int i = 0; i < this.main.size(); ++i) {
            ItemStack itemStack = this.main.get(i);
            if (!this.main.get(i).isEmpty() && ItemStack.isSameItemSameTags(stack, this.main.get(i)) && !this.main.get(i).isDamaged() && !itemStack.isEnchanted() && !itemStack.hasCustomHoverName()) {
                return i;
            }
        }

        return -1;
    }

    public void setItem(int slot, ItemStack stack) {
        NonNullList<ItemStack> defaultedList = null;

        NonNullList<ItemStack> defaultedList2;
        for (Iterator<NonNullList<ItemStack>> combinedInventoryIterator = this.combinedInventory.iterator(); combinedInventoryIterator.hasNext(); slot -= defaultedList2.size()) {
            defaultedList2 = combinedInventoryIterator.next();
            if (slot < defaultedList2.size()) {
                defaultedList = defaultedList2;
                break;
            }
        }

        if (defaultedList != null) {
            defaultedList.set(slot, stack);
        }

    }

    public ListTag writeNbt(ListTag nbtList) {
        int i;
        CompoundTag nbtCompound;
        for (i = 0; i < this.main.size(); ++i) {
            if (!this.main.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte)i);
                this.main.get(i).save(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for (i = 0; i < this.armor.size(); ++i) {
            if (!this.armor.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte)(i + 100));
                this.armor.get(i).save(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for (i = 0; i < this.offHand.size(); ++i) {
            if (!this.offHand.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte)(i + 150));
                this.offHand.get(i).save(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        return nbtList;
    }

    public void readNbt(ListTag nbtList) {
        this.main.clear();
        this.armor.clear();
        this.offHand.clear();

        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.of(nbtCompound);
            if (!itemStack.isEmpty()) {
                if (j >= 0 && j < this.main.size()) {
                    this.main.set(j, itemStack);
                } else if (j >= 100 && j < this.armor.size() + 100) {
                    this.armor.set(j - 100, itemStack);
                } else if (j >= 150 && j < this.offHand.size() + 150) {
                    this.offHand.set(j - 150, itemStack);
                }
            }
        }

    }

    public int getContainerSize() {
        return this.main.size() + this.armor.size() + this.offHand.size();
    }

    public boolean isEmpty() {
        Iterator<ItemStack> var1 = this.main.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                var1 = this.armor.iterator();

                do {
                    if (!var1.hasNext()) {
                        var1 = this.offHand.iterator();

                        do {
                            if (!var1.hasNext()) {
                                return true;
                            }

                            itemStack = var1.next();
                        } while (itemStack.isEmpty());

                        return false;
                    }

                    itemStack = var1.next();
                } while (itemStack.isEmpty());

                return false;
            }

            itemStack = var1.next();
        } while (itemStack.isEmpty());

        return false;
    }

    public ItemStack getItem(int slot) {
        List<ItemStack> list = null;

        NonNullList<ItemStack> defaultedList;
        for (Iterator<NonNullList<ItemStack>> combinedInventoryIterator = this.combinedInventory.iterator(); combinedInventoryIterator.hasNext(); slot -= defaultedList.size()) {
            defaultedList = combinedInventoryIterator.next();
            if (slot < defaultedList.size()) {
                list = defaultedList;
                break;
            }
        }

        return list == null ? ItemStack.EMPTY : list.get(slot);
    }

    public ItemStack getArmorStack(int slot) {
        return this.armor.get(slot);
    }

    public void setChanged() {
        ++this.changeCount;
    }

    public int getChangeCount() {
        return this.changeCount;
    }

    public boolean contains(ItemStack stack) {
        for (NonNullList<ItemStack> itemStacks : this.combinedInventory) {
            for (ItemStack itemStack : itemStacks) {
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameTags(itemStack, stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> tag) {
        for (NonNullList<ItemStack> itemStacks : this.combinedInventory) {
            for (ItemStack itemStack : itemStacks) {
                if (!itemStack.isEmpty() && itemStack.is(tag)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void copyFrom(Inventory other) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, other.getItem(i).copy());
        }
    }

    public void copyFrom(KitInventory other) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, other.getItem(i).copy());
        }
    }

    public void clearContent() {
        this.combinedInventory.forEach(List::clear);
    }

    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> defaultedList = null;

        NonNullList<ItemStack> defaultedList2;
        for (Iterator<NonNullList<ItemStack>> var3 = this.combinedInventory.iterator(); var3.hasNext(); slot -= defaultedList2.size()) {
            defaultedList2 = var3.next();
            if (slot < defaultedList2.size()) {
                defaultedList = defaultedList2;
                break;
            }
        }

        if (defaultedList != null && !defaultedList.get(slot).isEmpty()) {
            ItemStack itemStack = defaultedList.get(slot);
            defaultedList.set(slot, ItemStack.EMPTY);
            return itemStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack removeItem(int slot, int amount) {
        List<ItemStack> list = null;

        NonNullList<ItemStack> defaultedList;
        for (Iterator<NonNullList<ItemStack>> var4 = this.combinedInventory.iterator(); var4.hasNext(); slot -= defaultedList.size()) {
            defaultedList = var4.next();
            if (slot < defaultedList.size()) {
                list = defaultedList;
                break;
            }
        }

        return list != null && !list.get(slot).isEmpty() ? ContainerHelper.removeItem(list, slot, amount) : ItemStack.EMPTY;
    }

    public boolean stillValid(Player player) {
        return false;
    }

    public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
        // Try all main slots
        for (int i = 0; i < this.main.size(); ++i) {
            if (this.canStackAddMore(this.main.get(i), stack)) {
                return i;
            }
        }

        // Try offhand
        if (this.canStackAddMore(this.getItem(OFF_HAND_SLOT), stack)) {
            return OFF_HAND_SLOT;
        }

        return -1;
    }

    private int addStack(ItemStack stack) {
        int i = this.getOccupiedSlotWithRoomForStack(stack);
        if (i == -1) {
            i = this.getEmptySlot();
        }

        return i == -1 ? stack.getCount() : this.addStack(i, stack);
    }

    private int addStack(int slot, ItemStack stack) {
        Item item = stack.getItem();
        int i = stack.getCount();
        ItemStack itemStack = this.getItem(slot);
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(item, 0);
            if (stack.hasTag()) {
                itemStack.setTag(stack.getTag().copy());
            }

            this.setItem(slot, itemStack);
        }

        int j = i;
        if (i > itemStack.getMaxStackSize() - itemStack.getCount()) {
            j = itemStack.getMaxStackSize() - itemStack.getCount();
        }

        if (j > this.getMaxStackSize() - itemStack.getCount()) {
            j = this.getMaxStackSize() - itemStack.getCount();
        }

        if (j == 0) {
            return i;
        } else {
            i -= j;
            itemStack.grow(j);
            itemStack.setPopTime(5);
            return i;
        }
    }

    public boolean insertStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        try {
            if (stack.isDamaged()) {
                if (slot == -1) {
                    slot = this.getEmptySlot();
                }

                if (slot >= 0) {
                    this.main.set(slot, stack.copy());
                    stack.setCount(0);
                    return true;
                }

                return false;

            } else {
                int i;
                do {
                    i = stack.getCount();
                    if (slot == -1) {
                        stack.setCount(this.addStack(stack));
                    } else {
                        stack.setCount(this.addStack(slot, stack));
                    }
                } while (!stack.isEmpty() && stack.getCount() < i);

                return stack.getCount() < i;
            }
        } catch (Throwable ex) {
            CrashReport crashReport = CrashReport.forThrowable(ex, "Adding item to inventory");
            CrashReportCategory crashReportSection = crashReport.addCategory("Item being added");
            crashReportSection.setDetail("Item ID", Item.getId(stack.getItem()));
            crashReportSection.setDetail("Item data", stack.getDamageValue());
            crashReportSection.setDetail("Item name", () -> {
                return stack.getHoverName().getString();
            });
            throw new ReportedException(crashReport);
        }
    }

    public boolean insertStack(ItemStack stack) {
        return this.insertStack(-1, stack);
    }

    public void offerOrDropToPlayer(Inventory playerInventory) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            playerInventory.placeItemBackInInventory(this.getItem(i).copy());
        }
    }

}
