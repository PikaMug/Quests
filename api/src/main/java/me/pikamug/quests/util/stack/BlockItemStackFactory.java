package me.pikamug.quests.util.stack;

import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockItemStackFactory {

    BlockItemStack of(final Block block);
    BlockItemStack of(final Material type, final int amount, final short durability);
    BlockItemStack clone(final BlockItemStack original, final int amount);
    BlockItemStack clone(final BlockItemStack original, final int amount, final short durability);
}
