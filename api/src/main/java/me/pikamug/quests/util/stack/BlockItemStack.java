package me.pikamug.quests.util.stack;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface BlockItemStack {

    static BlockItemStack of(ItemStack original) {
        if (original == null) {
            return null;
        }

        return of(original.getType(), original.getAmount(), original.getDurability());
    }

    static BlockItemStack of(Block block) {
        return BlockItemStacks.getFactory().of(block);
    }

    static BlockItemStack of(Material type, int amount, short durability) {
        return BlockItemStacks.getFactory().of(type, amount, durability);
    }

    static BlockItemStack clone(BlockItemStack original, int amount) {
        return BlockItemStacks.getFactory().clone(original, amount);
    }

    static BlockItemStack clone(BlockItemStack original, int amount, short durability) {
        return BlockItemStacks.getFactory().clone(original, amount, durability);
    }

    Material getType();
    int getAmount();
    short getDurability();

    boolean matches(BlockItemStack other);

}
