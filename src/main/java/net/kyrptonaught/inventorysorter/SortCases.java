package net.kyrptonaught.inventorysorter;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortCases {
    public enum SortType {
        NAME, CATEGORY, MOD, ID
    }

    static String getStringForSort(ItemStack stack, SortType sortType) {
        Item item = stack.getItem();
        String itemName = specialCases(stack);
        switch (sortType) {
            case CATEGORY:
                ItemGroup group = item.getGroup();
                return (group != null ? group.getName() : "zzz") + itemName;
            case MOD:
                return Registry.ITEM.getId(item).getNamespace() + itemName;
            case NAME:
                if (stack.hasCustomName()) return stack.getName() + itemName;
        }


        return itemName;
    }

    private static String specialCases(ItemStack stack) {
        Item item = stack.getItem();
        
        if (stack.getCount() != stack.getMaxCount())
            return stackSize(stack);
        if (item instanceof EnchantedBookItem)
            return enchantedBookNameCase(stack);
        if (item instanceof ToolItem)
            return toolDuribilityCase(stack);
        return item.toString();
    }

    private static String stackSize(ItemStack stack) {
        return stack.getItem().toString() + stack.getCount();
    }

    private static String enchantedBookNameCase(ItemStack stack) {
        NbtList enchants = EnchantedBookItem.getEnchantmentNbt(stack);
        List<String> names = new ArrayList<>();
        StringBuilder enchantNames = new StringBuilder();
        for (int i = 0; i < enchants.size(); i++) {
            NbtCompound enchantTag = enchants.getCompound(i);
            Identifier enchantID = Identifier.tryParse(enchantTag.getString("id"));
            if (enchantID == null) continue;
            Enchantment enchant = Registry.ENCHANTMENT.get(enchantID);
            if (enchant == null) continue;
            names.add(enchant.getName(enchantTag.getInt("lvl")).getString());
        }
        Collections.sort(names);
        for (String enchant : names) {
            enchantNames.append(enchant).append(" ");
        }
        return stack.getItem().toString() + " " + enchants.size() + " " + enchantNames;
    }

    private static String toolDuribilityCase(ItemStack stack) {
        return stack.getItem().toString() + stack.getDamage();
    }
}
