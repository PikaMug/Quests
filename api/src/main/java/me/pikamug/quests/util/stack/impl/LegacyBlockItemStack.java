package me.pikamug.quests.util.stack.impl;

import me.pikamug.quests.util.stack.BlockItemStack;
import me.pikamug.quests.util.stack.BlockItemStackFactory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public class LegacyBlockItemStack implements BlockItemStack {

    public static final BlockItemStackFactory FACTORY = new Factory();

    private final MaterialData materialData;
    private int amount;

    private LegacyBlockItemStack(final MaterialData materialData, final int amount) {
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

        final MaterialData blockData = Factory.getBlockData(other);
        return this.materialData.equals(blockData) && (getDurability() == 0 || getDurability() == other.getDurability());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public static class Factory implements BlockItemStackFactory {

        @Override
        public BlockItemStack of(final Block block) {
            final MaterialData data = block.getState().getData();
            return new LegacyBlockItemStack(data, 1);
        }

        @Override
        public BlockItemStack of(final Material type, final int amount, final short durability) {
            final MaterialData data = type.getNewData((byte) durability);
            return new LegacyBlockItemStack(data, amount);
        }

        @Override
        public BlockItemStack clone(final BlockItemStack original, final int amount) {
            final MaterialData data = getBlockData(original);
            return new LegacyBlockItemStack(data, amount);
        }

        @Override
        public BlockItemStack clone(final BlockItemStack original, final int amount, final short durability) {
            final MaterialData data = getBlockData(original);
            data.setData((byte) durability);
            return new LegacyBlockItemStack(data, amount);
        }

        private static MaterialData getBlockData(final BlockItemStack stack) {
            if (stack instanceof LegacyBlockItemStack) {
                return ((LegacyBlockItemStack)stack).materialData;
            }

            return stack.getType().getNewData((byte) stack.getDurability());
        }
    }
}
