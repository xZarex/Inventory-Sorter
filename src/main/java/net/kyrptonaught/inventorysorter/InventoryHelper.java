package net.kyrptonaught.inventorysorter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.client.InventorySorterModClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InventoryHelper {
    static void sortInv(Inventory inv, int startSlot, int invSize, SortCases.SortType sortType) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < invSize; i++)
            addStackWithMerge(stacks, inv.getStack(startSlot + i));

        stacks.sort(Comparator.comparing(stack -> SortCases.getStringForSort(stack, sortType)));
        if (stacks.size() == 0) return;
        for (int i = 0; i < invSize; i++)
            inv.setStack(startSlot + i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
        inv.markDirty();
    }

    private static void addStackWithMerge(List<ItemStack> stacks, ItemStack newStack) {
        if (newStack.getItem() == Items.AIR) return;
        if (newStack.isStackable() && newStack.getCount() != newStack.getMaxCount())
            for (int j = stacks.size() - 1; j >= 0; j--) {
                ItemStack oldStack = stacks.get(j);
                if (canMergeItems(newStack, oldStack)) {
                    combineStacks(newStack, oldStack);
                    if (oldStack.getItem() == Items.AIR || oldStack.getCount() == 0) stacks.remove(j);
                }
            }
        stacks.add(newStack);
    }

    private static void combineStacks(ItemStack stack, ItemStack stack2) {
        if (stack.getMaxCount() >= stack.getCount() + stack2.getCount()) {
            stack.increment(stack2.getCount());
            stack2.setCount(0);
        }
        int maxInsertAmount = Math.min(stack.getMaxCount() - stack.getCount(), stack2.getCount());
        stack.increment(maxInsertAmount);
        stack2.decrement(maxInsertAmount);
    }

    private static boolean canMergeItems(ItemStack itemStack_1, ItemStack itemStack_2) {
        if (!itemStack_1.isStackable() || !itemStack_2.isStackable())
            return false;
        if (itemStack_1.getCount() == itemStack_1.getMaxCount() || itemStack_2.getCount() == itemStack_2.getMaxCount())
            return false;
        if (itemStack_1.getItem() != itemStack_2.getItem())
            return false;
        if (itemStack_1.getDamage() != itemStack_2.getDamage())
            return false;
        return ItemStack.areNbtEqual(itemStack_1, itemStack_2);
    }

    @Environment(EnvType.CLIENT)
    public static Boolean isPlayerOnlyInventory(Screen currentScreen) {
        return InventorySorterModClient.getBlacklist().blacklistedInventories.contains(currentScreen.getClass().getName()) ||
                InventorySorterModClient.getBlacklist().defaultBlacklist.contains(currentScreen.getClass().getName()) || !isSortableContainer((HandledScreen) currentScreen);
    }

    public static boolean isSortableContainer(HandledScreen currentScreen) {
        int numSlots = currentScreen.getScreenHandler().slots.size();
        if (numSlots <= 36) return false;
        return numSlots - 36 >= 9;
    }
}