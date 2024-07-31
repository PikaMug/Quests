package me.pikamug.quests.util.stack;

import me.pikamug.quests.util.stack.impl.LegacyBlockItemStack;
import me.pikamug.quests.util.stack.impl.ModernBlockItemStack;

public final class BlockItemStacks {

    private BlockItemStacks() {
    }

    private static BlockItemStackFactory factory;

    public static BlockItemStackFactory getFactory() {
        return factory;
    }

    private static void setFactory(final BlockItemStackFactory factory) {
        if (BlockItemStacks.factory != null) {
            throw new IllegalStateException("Factory is already set");
        }

        BlockItemStacks.factory = factory;
    }

    public static void init(final boolean modern) {
        if (modern) {
            setFactory(ModernBlockItemStack.FACTORY);
        } else {
            setFactory(LegacyBlockItemStack.FACTORY);
        }
    }
}
