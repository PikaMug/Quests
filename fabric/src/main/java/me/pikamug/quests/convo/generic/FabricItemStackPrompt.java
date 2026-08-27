/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.generic;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.FabricQuestsIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.RomanNumeral;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Stores ItemStack in "tempStack" session data<p>
 * Stores name in "tempName" session data<p>
 * Stores amount in "tempAmount" session data<p>
 * Stores durability in "tempData" session data<p>
 * Stores enchantments in "tempEnchantments" session data<p>
 * Stores display name in "tempDisplay" session data<p>
 * Stores lore in "tempLore" session data<p>
 * Stores metadata in "tempMeta" session data
 */
public class FabricItemStackPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsIntegerPrompt oldPrompt;

    public FabricItemStackPrompt(final @NotNull UUID uuid, final FabricQuestsIntegerPrompt old) {
        super(uuid);
        this.uuid = uuid;
        oldPrompt = old;
    }

    private final int size = 10;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("createItemTitle");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return ChatFormatting.BLUE;
            case 7:
                if (SessionData.get(uuid, "tempMeta") != null) {
                    return ChatFormatting.BLUE;
                } else {
                    return ChatFormatting.GRAY;
                }
            case 8:
                return ChatFormatting.RED;
            case 9:
                return ChatFormatting.GREEN;
            default:
                return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
            case 0:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateLoadHand");
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateSetName");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateSetAmount");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateSetDurab");
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateSetEnchs");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateSetDisplay");
            case 6:
                return ChatFormatting.YELLOW + FabricLang.get("itemCreateSetLore");
            case 7:
                if (SessionData.get(uuid, "tempMeta") != null) {
                    return ChatFormatting.DARK_GREEN + FabricLang.get("itemCreateSetClearMeta");
                } else {
                    return ChatFormatting.GRAY + FabricLang.get("itemCreateSetClearMeta");
                }
            case 8:
                return ChatFormatting.RED + FabricLang.get("cancel");
            case 9:
                return ChatFormatting.GREEN + FabricLang.get("done");
            default:
                return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
            case 0:
            case 8:
            case 9:
                return "";
            case 1:
                if (SessionData.get(uuid, "tempName") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final String text = (String) SessionData.get(uuid, "tempName");
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + getPrettyItemName(text) + ChatFormatting.GRAY + ")";
                }
            case 2:
                if (SessionData.get(uuid, "tempAmount") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final Integer text = (Integer) SessionData.get(uuid, "tempAmount");
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + text + ChatFormatting.GRAY + ")";
                }
            case 3:
                if (SessionData.get(uuid, "tempData") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final Short text = (Short) SessionData.get(uuid, "tempData");
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + text + ChatFormatting.GRAY + ")";
                }
            case 4:
                if (SessionData.get(uuid, "tempEnchantments") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final Map<Enchantment, Integer> map
                            = (Map<Enchantment, Integer>) SessionData.get(uuid, "tempEnchantments");
                    if (map != null) {
                        for (final Map.Entry<Enchantment, Integer> e : map.entrySet()) {
                            text.append("\n").append(getPrettyEnchantmentName(e.getKey())).append(" ")
                                    .append(RomanNumeral.getNumeral(e.getValue()));
                        }
                    }
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + text + ChatFormatting.GRAY + ")";
                }
            case 5:
                if (SessionData.get(uuid, "tempDisplay") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final String text = (String) SessionData.get(uuid, "tempDisplay");
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + text + ChatFormatting.GRAY + ")";
                }
            case 6:
                if (SessionData.get(uuid, "tempLore") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> list = (List<String>) SessionData.get(uuid, "tempLore");
                    if (list != null) {
                        for (final String s : list) {
                            text.append("\n").append(s);
                        }
                    }
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + text + ChatFormatting.GRAY + ")";
                }
            case 7:
                if (SessionData.get(uuid, "tempMeta") == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedHashMap<String, Object> map
                            = (LinkedHashMap<String, Object>) SessionData.get(uuid, "tempMeta");
                    if (map != null && !map.isEmpty()) {
                        for (final String key : map.keySet()) {
                            if (key.equals("pages")) {
                                final List<String> pages = (List<String>) map.get(key);
                                text.append("\n").append(ChatFormatting.GRAY).append("  \u2515 ")
                                        .append(ChatFormatting.DARK_GREEN).append(key).append("=")
                                        .append(pages.size());
                            } else {
                                text.append("\n").append(ChatFormatting.GRAY).append("  \u2515 ")
                                        .append(ChatFormatting.DARK_GREEN).append(key).append("=")
                                        .append(map.get(key));
                            }
                        }
                    }
                    return text.toString();
                }
            default:
                return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + getTitle());
        if (SessionData.get(uuid, "tempName") != null) {
            final String stackData = getItemData(uuid);
            if (stackData != null) {
                text.append("\n").append(stackData);
                if (SessionData.get(uuid, "tempMeta") != null) {
                    final LinkedHashMap<String, Object> map
                            = (LinkedHashMap<String, Object>) SessionData.get(uuid, "tempMeta");
                    if (map != null && !map.isEmpty()) {
                        for (final String key : map.keySet()) {
                            if (key.equals("pages")) {
                                final List<String> pages = (List<String>) map.get(key);
                                text.append("\n").append(ChatFormatting.GRAY).append("  \u2515 ")
                                        .append(ChatFormatting.DARK_GREEN).append(key).append("=")
                                        .append(pages.size());
                            } else {
                                text.append("\n").append(ChatFormatting.GRAY).append("  \u2515 ")
                                        .append(ChatFormatting.DARK_GREEN).append(key).append("=")
                                        .append(map.get(key));
                            }
                        }
                    }
                }
            }
        }
        int start = 0;
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
        if (sender == null) {
            start = 1;
        }
        for (int i = start; i <= size - 1; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
        }
        return sendClickableSelection(text.toString(), FabricQuestsPlugin.getInstance().getQuester(uuid));
    }

    @Override
    public void acceptInput(final @NotNull Number input) {
        acceptInput(uuid, input, null);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    public void acceptInput(final UUID uuid, final Number input, final ItemStack item) {
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
        if (sender == null) {
            return;
        }
        switch (input.intValue()) {
            case 0:
                final ItemStack is = item == null ? sender.getMainHandItem() : item;
                if (is.is(Items.AIR)) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateNoItem")));
                } else {
                    SessionData.set(uuid, "tempMeta", null);
                    SessionData.set(uuid, "tempName", FabricItemUtil.getMaterialName(is));
                    SessionData.set(uuid, "tempAmount", is.getCount());
                    SessionData.set(uuid, "tempData", null);
                    SessionData.set(uuid, "tempEnchantments", null);
                    SessionData.set(uuid, "tempDisplay", null);
                    SessionData.set(uuid, "tempLore", null);
                    if (is.getDamageValue() != 0) {
                        SessionData.set(uuid, "tempData", (short) is.getDamageValue());
                    }
                    if (!is.getAllEnchantments().isEmpty()) {
                        SessionData.set(uuid, "tempEnchantments", new HashMap<>(is.getAllEnchantments()));
                    }
                    if (is.hasCustomHoverName()) {
                        final String display = ChatFormatting.stripFormatting(
                                is.getHoverName().getString()).replace(ChatFormatting.COLOR_CHAR, '&');
                        SessionData.set(uuid, "tempDisplay", display);
                    }
                    final CompoundTag tag = is.getTag();
                    if (tag != null) {
                        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                        if (tag.contains("display")) {
                            final CompoundTag displayTag = tag.getCompound("display");
                            if (displayTag.contains("Lore")) {
                                final net.minecraft.nbt.ListTag loreTag = displayTag.getList("Lore", 8);
                                final LinkedList<String> lore = new LinkedList<>();
                                for (int i = 0; i < loreTag.size(); i++) {
                                    lore.add(loreTag.getString(i));
                                }
                                SessionData.set(uuid, "tempLore", lore);
                            }
                        }
                        if (!map.isEmpty()) {
                            SessionData.set(uuid, "tempMeta", map);
                        }
                    }
                }
                new FabricItemStackPrompt(uuid, oldPrompt).start();
                break;
            case 1:
                SessionData.set(uuid, "tempMeta", null);
                new ItemNamePrompt(uuid).start();
                break;
            case 2:
                if (SessionData.get(uuid, "tempName") != null) {
                    new ItemAmountPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateNoName")));
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
                break;
            case 3:
                if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                    new ItemDataPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateNoNameAmount")));
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
                break;
            case 4:
                if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                    new ItemEnchantmentPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateNoNameAmount")));
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
                break;
            case 5:
                if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                    new ItemDisplayPrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateNoNameAmount")));
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
                break;
            case 6:
                if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                    new ItemLorePrompt(uuid).start();
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateNoNameAmount")));
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
                break;
            case 7:
                if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                    SessionData.set(uuid, "tempMeta", null);
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateNoNameAmount")));
                }
                new FabricItemStackPrompt(uuid, oldPrompt).start();
                break;
            case 8:
                clearSessionData(uuid);
                oldPrompt.start();
                break;
            case 9:
                if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                    final String name = (String) SessionData.get(uuid, "tempName");
                    final Integer amount = (Integer) SessionData.get(uuid, "tempAmount");
                    Short data = -1;
                    Map<Enchantment, Integer> enchs = null;
                    String display = null;
                    List<String> lore = null;
                    if (SessionData.get(uuid, "tempData") != null) {
                        data = (Short) SessionData.get(uuid, "tempData");
                    }
                    if (SessionData.get(uuid, "tempEnchantments") != null) {
                        enchs = (Map<Enchantment, Integer>) SessionData.get(uuid, "tempEnchantments");
                    }
                    if (SessionData.get(uuid, "tempDisplay") != null) {
                        display = ChatFormatting.translateAlternateColorCodes('&',
                                (String) Objects.requireNonNull(SessionData.get(uuid, "tempDisplay")));
                    }
                    if (SessionData.get(uuid, "tempLore") != null) {
                        lore = new ArrayList<>();
                        final LinkedList<String> loadedLore = (LinkedList<String>) SessionData.get(uuid, "tempLore");
                        if (loadedLore != null) {
                            for (final String line : loadedLore) {
                                lore.add(ChatFormatting.translateAlternateColorCodes('&', line));
                            }
                        }
                    }

                    if (name != null && amount != null && data != null) {
                        final ResourceLocation resLoc = new ResourceLocation(name.toLowerCase());
                        final net.minecraft.world.item.Item itemObj = BuiltInRegistries.ITEM.get(resLoc);
                        final ItemStack stack = new ItemStack(itemObj, amount);
                        if (data != -1) {
                            stack.setDamageValue(data);
                        }
                        if (enchs != null) {
                            for (final Map.Entry<Enchantment, Integer> e : enchs.entrySet()) {
                                stack.enchant(e.getKey(), e.getValue());
                            }
                        }
                        if (display != null) {
                            stack.hoverName(Component.literal(display));
                        }
                        if (lore != null) {
                            final CompoundTag tag = stack.getOrCreateTag();
                            CompoundTag displayTag = tag.getCompound("display");
                            if (displayTag == null) {
                                displayTag = new CompoundTag();
                            }
                            final net.minecraft.nbt.ListTag loreTag = new net.minecraft.nbt.ListTag();
                            for (final String line : lore) {
                                loreTag.add(net.minecraft.nbt.StringTag.valueOf(line));
                            }
                            displayTag.put("Lore", loreTag);
                            tag.put("display", displayTag);
                        }
                        SessionData.set(uuid, "tempStack", stack);
                        oldPrompt.start();
                    }
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateNoNameAmount")));
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
                break;
            default:
                try {
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                } catch (final Exception e) {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                    return;
                }
                break;
        }
    }

    public class ItemNamePrompt extends FabricQuestsEditorStringPrompt {

        public ItemNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final String s = input.replace(":", "");
                final ResourceLocation resLoc = new ResourceLocation(s.toLowerCase().replace(" ", "_"));
                final net.minecraft.world.item.Item mat = BuiltInRegistries.ITEM.get(resLoc);
                if (mat == Items.AIR || mat == null) {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateInvalidName")));
                    new ItemNamePrompt(uuid).start();
                } else {
                    SessionData.set(uuid, "tempName", resLoc.toString());
                    SessionData.set(uuid, "tempAmount", 1);
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
            } else {
                new FabricItemStackPrompt(uuid, oldPrompt).start();
            }
        }
    }

    public class ItemAmountPrompt extends FabricQuestsEditorStringPrompt {

        public ItemAmountPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterAmount");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 64) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                                + FabricLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "64")));
                        new ItemAmountPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempAmount", Integer.parseInt(input));
                        new FabricItemStackPrompt(uuid, oldPrompt).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new ItemAmountPrompt(uuid).start();
                }
            } else {
                new FabricItemStackPrompt(uuid, oldPrompt).start();
            }
        }
    }

    public class ItemDataPrompt extends FabricQuestsEditorStringPrompt {

        public ItemDataPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterDurab");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1) {
                        sender.sendSystemMessage(Component.literal(
                                ChatFormatting.RED + FabricLang.get("itemCreateInvalidDurab")));
                        new ItemDataPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempData", Short.parseShort(input));
                        new FabricItemStackPrompt(uuid, oldPrompt).start();
                    }
                } catch (final NumberFormatException e) {
                    if (input.equals("*")) {
                        SessionData.set(uuid, "tempData", Short.parseShort("999"));
                        new FabricItemStackPrompt(uuid, oldPrompt).start();
                    }
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new ItemDataPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempData", null);
            }
            new FabricItemStackPrompt(uuid, oldPrompt).start();
        }
    }

    public class ItemEnchantmentPrompt extends FabricQuestsEditorStringPrompt {

        public ItemEnchantmentPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return FabricLang.get("enchantmentsTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterEnch");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder sb = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            for (final Enchantment e : BuiltInRegistries.ENCHANTMENT) {
                sb.append(ChatFormatting.GREEN).append(getPrettyEnchantmentName(e)).append(", ");
            }
            final String text = sb.substring(0, sb.length() - 2);
            return text + "\n" + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
            final String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(FabricLang.get("cmdClear")) && !s.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                final Enchantment e = getEnchantmentFromPrettyName(capitalize(s));
                if (e != null) {
                    SessionData.set(uuid, "tempEnchant", e);
                    new ItemEnchantmentLevelPrompt(uuid, getPrettyEnchantmentName(e)).start();
                } else {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("itemCreateInvalidEnch")));
                    new ItemEnchantmentPrompt(uuid).start();
                }
            } else if (s.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempEnchantments", null);
            }
            new FabricItemStackPrompt(uuid, oldPrompt).start();
        }
    }

    public class ItemEnchantmentLevelPrompt extends FabricQuestsEditorStringPrompt {

        final String enchantment;

        protected ItemEnchantmentLevelPrompt(final @NotNull UUID uuid, final String ench) {
            super(uuid);
            enchantment = ench;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterLevel").replace("<enchantment>", enchantment);
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.AQUA + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, FabricQuestsPlugin.getInstance());
            try {
                final int num = Integer.parseInt(input);
                if (num < 1) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + FabricLang.get("invalidMinimum").replace("<number>", "1")));
                    new ItemEnchantmentLevelPrompt(uuid, enchantment).start();
                } else {
                    if (SessionData.get(uuid, "tempEnchantments") != null) {
                        @SuppressWarnings("unchecked")
                        final Map<Enchantment, Integer> enchs
                                = (Map<Enchantment, Integer>) SessionData.get(uuid, "tempEnchantments");
                        if (enchs != null) {
                            enchs.put((Enchantment) SessionData.get(uuid, "tempEnchant"), num);
                            SessionData.set(uuid, "tempEnchantments", enchs);
                        }
                    } else {
                        final Map<Enchantment, Integer> enchs = new HashMap<>();
                        enchs.put((Enchantment) SessionData.get(uuid, "tempEnchant"), num);
                        SessionData.set(uuid, "tempEnchantments", enchs);
                    }
                    new FabricItemStackPrompt(uuid, oldPrompt).start();
                }
            } catch (final NumberFormatException e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                        + FabricLang.get("reqNotANumber").replace("<input>", input)));
                new ItemEnchantmentLevelPrompt(uuid, enchantment).start();
            }
        }
    }

    public class ItemDisplayPrompt extends FabricQuestsEditorStringPrompt {

        public ItemDisplayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterDisplay");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !s.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                s = parseString(s);
                SessionData.set(uuid, "tempDisplay", s);
            } else if (s.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempDisplay", null);
            }
            new FabricItemStackPrompt(uuid, oldPrompt).start();
        }
    }

    public class ItemLorePrompt extends FabricQuestsEditorStringPrompt {

        public ItemLorePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("itemCreateEnterLore");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(FabricLang.get("cmdCancel"))
                    && !s.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                s = parseString(s);
                final LinkedList<String> lore = new LinkedList<>(Arrays.asList(s.split(FabricLang.get("charSemi"))));
                SessionData.set(uuid, "tempLore", lore);
            } else if (s.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempLore", null);
            }
            new FabricItemStackPrompt(uuid, oldPrompt).start();
        }
    }

    public String getItemData(final UUID uuid) {
        final StringBuilder item = new StringBuilder();
        if (SessionData.get(uuid, "tempDisplay") != null) {
            item.append(ChatFormatting.LIGHT_PURPLE).append(ChatFormatting.ITALIC)
                    .append(SessionData.get(uuid, "tempDisplay")).append(ChatFormatting.RESET).append(" ");
        }
        if (SessionData.get(uuid, "tempName") != null) {
            final String name = (String) SessionData.get(uuid, "tempName");
            item.append(ChatFormatting.GRAY).append("(").append(ChatFormatting.AQUA)
                    .append(getPrettyItemName(name));
            if (SessionData.get(uuid, "tempData") != null) {
                item.append(":").append(ChatFormatting.BLUE).append(SessionData.get(uuid, "tempData"));
            }
            item.append(ChatFormatting.GRAY).append(")");
        }
        if (SessionData.get(uuid, "tempAmount") != null) {
            item.append(ChatFormatting.GRAY).append(" x ").append(ChatFormatting.DARK_AQUA)
                    .append(SessionData.get(uuid, "tempAmount"));
        } else {
            item.append(ChatFormatting.GRAY).append(" x ").append(ChatFormatting.DARK_AQUA).append("1");
        }
        if (SessionData.get(uuid, "tempEnchantments") != null) {
            @SuppressWarnings("unchecked")
            final Map<Enchantment, Integer> enchantments
                    = (Map<Enchantment, Integer>) SessionData.get(uuid, "tempEnchantments");
            if (enchantments != null) {
                for (final Map.Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                    item.append("\n").append(ChatFormatting.GRAY).append("  - ").append(ChatFormatting.RED)
                            .append(getPrettyEnchantmentName(e.getKey())).append(" ")
                            .append(RomanNumeral.getNumeral(e.getValue()));
                }
            }
        }
        if (SessionData.get(uuid, "tempLore") != null) {
            @SuppressWarnings("unchecked")
            final List<String> lore = (List<String>) SessionData.get(uuid, "tempLore");
            if (lore != null) {
                for (final String s : lore) {
                    item.append("\n").append(ChatFormatting.DARK_PURPLE).append(ChatFormatting.ITALIC).append(s);
                }
            }
        }
        return item.toString();
    }

    public static void clearSessionData(final UUID uuid) {
        SessionData.set(uuid, "tempStack", null);
        SessionData.set(uuid, "tempName", null);
        SessionData.set(uuid, "tempAmount", null);
        SessionData.set(uuid, "tempData", null);
        SessionData.set(uuid, "tempEnchantments", null);
        SessionData.set(uuid, "tempDisplay", null);
        SessionData.set(uuid, "tempLore", null);
        SessionData.set(uuid, "tempMeta", null);
    }

    private static String getPrettyItemName(final String registryName) {
        if (registryName == null || registryName.isEmpty()) return "Unknown";
        final ResourceLocation resLoc = new ResourceLocation(registryName.toLowerCase());
        final net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(resLoc);
        if (item == null || item == Items.AIR) return registryName;
        final ItemStack stack = new ItemStack(item);
        return stack.getHoverName().getString();
    }

    public static String getPrettyEnchantmentName(final Enchantment ench) {
        if (ench == null) return "Unknown";
        final ItemStack stack = new ItemStack(Items.AIR);
        return ench.getDescription().getString();
    }

    private static Enchantment getEnchantmentFromPrettyName(final String name) {
        if (name == null || name.isEmpty()) return null;
        for (final Enchantment e : BuiltInRegistries.ENCHANTMENT) {
            if (e.getDescription().getString().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    private static String capitalize(final String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static String parseString(final String s) {
        String parsed = s;
        parsed = parsed.replace("<black>", ChatFormatting.BLACK.toString());
        parsed = parsed.replace("<darkblue>", ChatFormatting.DARK_BLUE.toString());
        parsed = parsed.replace("<darkgreen>", ChatFormatting.DARK_GREEN.toString());
        parsed = parsed.replace("<darkaqua>", ChatFormatting.DARK_AQUA.toString());
        parsed = parsed.replace("<darkred>", ChatFormatting.DARK_RED.toString());
        parsed = parsed.replace("<purple>", ChatFormatting.DARK_PURPLE.toString());
        parsed = parsed.replace("<gold>", ChatFormatting.GOLD.toString());
        parsed = parsed.replace("<grey>", ChatFormatting.GRAY.toString());
        parsed = parsed.replace("<gray>", ChatFormatting.GRAY.toString());
        parsed = parsed.replace("<darkgrey>", ChatFormatting.DARK_GRAY.toString());
        parsed = parsed.replace("<darkgray>", ChatFormatting.DARK_GRAY.toString());
        parsed = parsed.replace("<blue>", ChatFormatting.BLUE.toString());
        parsed = parsed.replace("<green>", ChatFormatting.GREEN.toString());
        parsed = parsed.replace("<aqua>", ChatFormatting.AQUA.toString());
        parsed = parsed.replace("<red>", ChatFormatting.RED.toString());
        parsed = parsed.replace("<pink>", ChatFormatting.LIGHT_PURPLE.toString());
        parsed = parsed.replace("<lightpurple>", ChatFormatting.LIGHT_PURPLE.toString());
        parsed = parsed.replace("<lightpurple>", ChatFormatting.LIGHT_PURPLE.toString());
        parsed = parsed.replace("<yellow>", ChatFormatting.YELLOW.toString());
        parsed = parsed.replace("<white>", ChatFormatting.WHITE.toString());
        parsed = parsed.replace("<bold>", ChatFormatting.BOLD.toString());
        parsed = parsed.replace("<italic>", ChatFormatting.ITALIC.toString());
        parsed = parsed.replace("<underline>", ChatFormatting.UNDERLINE.toString());
        parsed = parsed.replace("<strikethrough>", ChatFormatting.STRIKETHROUGH.toString());
        parsed = parsed.replace("<reset>", ChatFormatting.RESET.toString());
        return parsed;
    }
}
