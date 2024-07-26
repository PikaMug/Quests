package me.pikamug.quests.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.inventory.ItemStack;

public class BlockItemStack {

    private final BlockData blockData;
    private int amount;
    private final short durability;

    private BlockItemStack(BlockData blockData, int amount, short durability) {
        this.blockData = blockData;
        this.amount = amount;
        this.durability = durability;
    }

    public static BlockItemStack of(ItemStack original) {
        if (original == null) {
            return null;
        }

        Material type = original.getType();
        BlockData data = type.createBlockData();

        return new BlockItemStack(data, original.getAmount(), original.getDurability());
    }

    public static BlockItemStack of(BlockData data, int amount) {
        return new BlockItemStack(data, amount, getDurability(data));
    }

    public static BlockItemStack of(Block block) {
        return of(block.getBlockData(), 1);
    }

    public static BlockItemStack of(BlockData data, int amount, short durability) {
        return new BlockItemStack(data, amount, durability);
    }

    public static BlockItemStack clone(BlockItemStack original, int amount) {
        return new BlockItemStack(original.getBlockData(), amount, (short) 0);
    }

    public static BlockItemStack clone(BlockItemStack original, int amount, short durability) {
        return new BlockItemStack(original.getBlockData(), amount, durability);
    }

    private static short getDurability(BlockData data) {
        if (data instanceof Ageable) {
            return (short) ((Ageable)data).getAge();
        }

        if (data instanceof Powerable) {
            return (short) (((Powerable)data).isPowered() ? 1 : 0);
        }

        return 0;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public int getAmount() {
        return amount;
    }

    public short getDurability() {
        return durability;
    }

    public Material getType() {
        return blockData.getMaterial();
    }

    public boolean matches(BlockItemStack other) {
        return blockData.matches(other.getBlockData()) && durability == other.getDurability();
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
