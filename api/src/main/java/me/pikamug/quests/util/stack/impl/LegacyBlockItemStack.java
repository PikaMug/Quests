package me.pikamug.quests.util.stack.impl;

import me.pikamug.quests.util.stack.BlockItemStack;
import me.pikamug.quests.util.stack.BlockItemStackFactory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Redstone;

public class LegacyBlockItemStack implements BlockItemStack {

    public static final BlockItemStackFactory FACTORY = new Factory();

    private final MaterialData materialData;
    private int amount;

    private LegacyBlockItemStack(MaterialData materialData, int amount) {
        this.materialData = materialData;
        this.amount = amount;
    }

    public MaterialData getMaterialData() {
        return materialData;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public short getDurability() {
        return materialData.getData();
    }

    @Override
    public Material getType() {
        return materialData.getItemType();
    }

    @Override
    public boolean matches(BlockItemStack other) {
        if (other == null) {
            return false;
        }

        MaterialData blockData = Factory.getBlockData(other);
        return this.materialData.equals(blockData) && (getDurability() == 0 || getDurability() == other.getDurability());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public static class Factory implements BlockItemStackFactory {

        @Override
        public BlockItemStack of(Block block) {
            MaterialData data = block.getState().getData();
            return new LegacyBlockItemStack(data, 1);
        }

        @Override
        public BlockItemStack of(Material type, int amount, short durability) {
            MaterialData data = type.getNewData((byte) durability);
            return new LegacyBlockItemStack(data, amount);
        }

        @Override
        public BlockItemStack clone(BlockItemStack original, int amount) {
            MaterialData data = getBlockData(original);
            return new LegacyBlockItemStack(data, amount);
        }

        @Override
        public BlockItemStack clone(BlockItemStack original, int amount, short durability) {
            MaterialData data = getBlockData(original);
            data.setData((byte) durability);
            return new LegacyBlockItemStack(data, amount);
        }

        private static MaterialData getBlockData(BlockItemStack stack) {
            if (stack instanceof LegacyBlockItemStack) {
                return ((LegacyBlockItemStack)stack).materialData;
            }

            return stack.getType().getNewData((byte) stack.getDurability());
        }
    }
}
