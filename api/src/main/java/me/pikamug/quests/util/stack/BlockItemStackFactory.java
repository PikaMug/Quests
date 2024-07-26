package me.pikamug.quests.util.stack;

import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockItemStackFactory {

    BlockItemStack of(Block block);
    BlockItemStack of(Material type, int amount, short durability);
    BlockItemStack clone(BlockItemStack original, int amount);
    BlockItemStack clone(BlockItemStack original, int amount, short durability);
}
