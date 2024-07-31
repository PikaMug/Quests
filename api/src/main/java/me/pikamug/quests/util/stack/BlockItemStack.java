package me.pikamug.quests.util.stack;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface BlockItemStack {

    static BlockItemStack of(final ItemStack original) {
        if (original == null) {
            return null;
        }

        return of(original.getType(), original.getAmount(), original.getDurability());
    }

    static BlockItemStack of(final Block block) {
        return BlockItemStacks.getFactory().of(block);
    }

    static BlockItemStack of(final Material type, final int amount, final short durability) {
        return BlockItemStacks.getFactory().of(type, amount, durability);
    }

    static BlockItemStack clone(final BlockItemStack original, final int amount) {
        return BlockItemStacks.getFactory().clone(original, amount);
    }

    static BlockItemStack clone(final BlockItemStack original, final int amount, final short durability) {
        return BlockItemStacks.getFactory().clone(original, amount, durability);
    }

    Material getType();
    int getAmount();
    short getDurability();

    boolean matches(BlockItemStack other);

}
