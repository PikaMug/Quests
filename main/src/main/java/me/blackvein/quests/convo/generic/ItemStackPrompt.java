/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo.generic;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Stores ItemStack in "tempStack" context data<p>
 * Stores name in "tempName" context data<p>
 * Stores amount in "tempAmount" context data<p>
 * Stores durability in "tempData" context data<p>
 * Stores enchantments in "tempEnchantments" context data<p>
 * Stores display name in "tempDisplay" context data<p>
 * Stores lore in "tempLore" context data<p>
 * Stores metadata in "tempMeta" context data
 */
public class ItemStackPrompt extends QuestsEditorNumericPrompt {

    private final Prompt oldPrompt;

    public ItemStackPrompt(final ConversationContext context, final Prompt old) {
        super(context);
        oldPrompt = old;
    }
    
    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("createItemTitle");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            return ChatColor.BLUE;
        case 7:
            if (context.getSessionData("tempMeta") != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 8:
            return ChatColor.RED;
        case 9:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 0:
            return ChatColor.YELLOW + Lang.get("itemCreateLoadHand");
        case 1:
            return ChatColor.YELLOW + Lang.get("itemCreateSetName");
        case 2:
            return ChatColor.YELLOW + Lang.get("itemCreateSetAmount");
        case 3:
            return ChatColor.YELLOW + Lang.get("itemCreateSetDurab");
        case 4:
            return ChatColor.YELLOW + Lang.get("itemCreateSetEnchs");
        case 5:
            return ChatColor.YELLOW + Lang.get("itemCreateSetDisplay");
        case 6:
            return ChatColor.YELLOW + Lang.get("itemCreateSetLore");
        case 7:
            if (context.getSessionData("tempMeta") != null) {
                return ChatColor.DARK_GREEN + Lang.get("itemCreateSetClearMeta");
            } else {
                return ChatColor.GRAY + Lang.get("itemCreateSetClearMeta");
            }
        case 8:
            return ChatColor.RED + Lang.get("cancel");
        case 9:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 0:
        case 8:
        case 9:
            return "";
        case 1:
            if (context.getSessionData("tempName") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final String text = (String) context.getSessionData("tempName");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + ItemUtil.getPrettyItemName(text) + ChatColor.GRAY + ")";
            }
        case 2:
            if (context.getSessionData("tempAmount") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer text = (Integer) context.getSessionData("tempAmount");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData("tempData") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Short text = (Short) context.getSessionData("tempData");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 4:
            if (context.getSessionData("tempEnchantments") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final Map<Enchantment, Integer> map 
                        = (Map<Enchantment, Integer>) context.getSessionData("tempEnchantments");
                if (map != null) {
                    for (final Entry<Enchantment, Integer> e : map.entrySet()) {
                        text.append("\n").append(ItemUtil.getPrettyEnchantmentName(e.getKey())).append(" ")
                                .append(RomanNumeral.getNumeral(e.getValue()));
                    }
                }
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData("tempDisplay") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final String text = (String) context.getSessionData("tempDisplay");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 6:
            if (context.getSessionData("tempLore") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> list = (List<String>) context.getSessionData("tempLore");
                if (list != null) {
                    for (final String s : list) {
                        text.append("\n").append(s);
                    }
                }
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 7:
            if (context.getSessionData("tempMeta") == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedHashMap<String, Object> map 
                        = (LinkedHashMap<String, Object>) context.getSessionData("tempMeta");
                if (map != null && !map.isEmpty()) {
                    for (final String key : map.keySet()) {
                        if (key.equals("pages")) {
                            final List<String> pages = (List<String>) map.get(key);
                            text.append("\n").append(ChatColor.GRAY).append("\u2515 ").append(ChatColor.DARK_GREEN)
                                    .append(key).append("=").append(pages.size());
                        } else {
                            text.append("\n").append(ChatColor.GRAY).append("\u2515 ").append(ChatColor.DARK_GREEN)
                                    .append(key).append("=").append(map.get(key));
                        }
                    }
                }
                return text.toString();
            }
        default:
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        Bukkit.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
        if (context.getSessionData("tempName") != null) {
            final String stackData = getItemData(context);
            if (stackData != null) {
                text.append("\n").append(stackData);
                if (context.getSessionData("tempMeta") != null) {
                    final LinkedHashMap<String, Object> map 
                            = (LinkedHashMap<String, Object>) context.getSessionData("tempMeta");
                    if (map != null && !map.isEmpty()) {
                        for (final String key : map.keySet()) {
                            if (key.equals("pages")) {
                                final List<String> pages = (List<String>) map.get(key);
                                text.append("\n").append(ChatColor.GRAY).append("\u2515 ").append(ChatColor.DARK_GREEN)
                                        .append(key).append("=").append(pages.size());
                            } else {
                                text.append("\n").append(ChatColor.GRAY).append("\u2515 ").append(ChatColor.DARK_GREEN)
                                        .append(key).append("=").append(map.get(key));
                            }
                        }
                    }
                }
            }
        }
        int start = 0;
        if (!(context.getForWhom() instanceof Player)) {
            start = 1;
        }
        for (int i = start; i <= size-1; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final @NotNull Number input) {
        return acceptValidatedInput(context, input, null);
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
    public Prompt acceptValidatedInput(final ConversationContext context, final Number input, final ItemStack item) {
        switch (input.intValue()) {
        case 0:
            if (context.getForWhom() instanceof Player) {
                context.setSessionData("tempMeta", null);
                
                final Player player = (Player) context.getForWhom();
                final ItemStack is = item == null ? player.getItemInHand() : item;
                if (is.getType().equals(Material.AIR)) {
                    player.sendMessage(ChatColor.RED + Lang.get("itemCreateNoItem"));
                } else {
                    context.setSessionData("tempName", is.getType().name());
                    context.setSessionData("tempAmount", is.getAmount());
                    context.setSessionData("tempData", null);
                    context.setSessionData("tempEnchantments", null);
                    context.setSessionData("tempDisplay", null);
                    context.setSessionData("tempLore", null);
                    if (is.getDurability() != 0) {
                        context.setSessionData("tempData", is.getDurability());
                    }
                    if (!is.getEnchantments().isEmpty()) {
                        context.setSessionData("tempEnchantments", new HashMap<>(is.getEnchantments()));
                    }
                    if (is.hasItemMeta()) {
                        final ItemMeta meta = is.getItemMeta();
                        if (meta != null) {
                            final String display = meta.getDisplayName().replace(ChatColor.COLOR_CHAR, '&');
                            context.setSessionData("tempDisplay", display);
                            if (meta.getLore() != null) {
                                final LinkedList<String> lore = new LinkedList<>(meta.getLore());
                                context.setSessionData("tempLore", lore);
                            }
                            final LinkedHashMap<String, Object> map = new LinkedHashMap<>(meta.serialize());
                            map.remove("lore");
                            map.remove("display-name");
                            if (!map.isEmpty()) {
                                context.setSessionData("tempMeta", map);
                            }
                        }
                    }
                }
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
            }
            return new ItemStackPrompt(context, oldPrompt);
        case 1:
            context.setSessionData("tempMeta", null);
            return new ItemNamePrompt(context);
        case 2:
            if (context.getSessionData("tempName") != null) {
                return new ItemAmountPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoName"));
                return new ItemStackPrompt(context, oldPrompt);
            }
        case 3:
            if (context.getSessionData("tempName") != null && context.getSessionData("tempAmount") != null) {
                return new ItemDataPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(context, oldPrompt);
            }
        case 4:
            if (context.getSessionData("tempName") != null && context.getSessionData("tempAmount") != null) {
                return new ItemEnchantmentPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(context, oldPrompt);
            }
        case 5:
            if (context.getSessionData("tempName") != null && context.getSessionData("tempAmount") != null) {
                return new ItemDisplayPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(context, oldPrompt);
            }
        case 6:
            if (context.getSessionData("tempName") != null && context.getSessionData("tempAmount") != null) {
                return new ItemLorePrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(context, oldPrompt);
            }
        case 7:
            if (context.getSessionData("tempName") != null && context.getSessionData("tempAmount") != null) {
                context.setSessionData("tempMeta", null);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
            }
            return new ItemStackPrompt(context, oldPrompt);
        case 8:
            clearSessionData(context);
            return oldPrompt;
        case 9:
            if (context.getSessionData("tempName") != null && context.getSessionData("tempAmount") != null) {
                final String name = (String) context.getSessionData("tempName");
                final Integer amount = (Integer) context.getSessionData("tempAmount");
                Short data = -1;
                Map<Enchantment, Integer> enchs = null;
                String display = null;
                List<String> lore = null;
                if (context.getSessionData("tempData") != null) {
                    data = (Short) context.getSessionData("tempData");
                }
                if (context.getSessionData("tempEnchantments") != null) {
                    enchs = (Map<Enchantment, Integer>) context.getSessionData("tempEnchantments");
                }
                if (context.getSessionData("tempDisplay") != null) {
                    display = ChatColor.translateAlternateColorCodes('&',
                            (String) Objects.requireNonNull(context.getSessionData("tempDisplay")));
                }
                if (context.getSessionData("tempLore") != null) {
                    lore = new ArrayList<>();
                    final LinkedList<String> loadedLore = (LinkedList<String>) context.getSessionData("tempLore");
                    if (loadedLore != null) {
                        for (final String line : loadedLore) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                    }
                }

                if (name != null && amount != null && data != null) {
                    final ItemStack stack = new ItemStack(Objects.requireNonNull(Material.matchMaterial(name)), amount);
                    if (data != -1) {
                        stack.setDurability(data);
                    }

                    ItemMeta meta = stack.getItemMeta();
                    if (meta != null) {
                        if (context.getSessionData("tempMeta") != null) {
                            meta = ItemUtil.deserializeItemMeta(meta.getClass(),
                                    (Map<String, Object>) context.getSessionData("tempMeta"));
                        }
                        if (enchs != null) {
                            for (final Entry<Enchantment, Integer> e : enchs.entrySet()) {
                                meta.addEnchant(e.getKey(), e.getValue(), true);
                            }
                        }
                        if (display != null) {
                            meta.setDisplayName(display);
                        }
                        if (lore != null) {
                            meta.setLore(lore);
                        }
                        stack.setItemMeta(meta);
                    }

                    context.setSessionData("tempStack", stack);
                }
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateNoNameAmount"));
                return new ItemStackPrompt(context, oldPrompt);
            }
        default:
            try {
                return oldPrompt;
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    public class ItemNamePrompt extends QuestsEditorStringPrompt {

        public ItemNamePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String s = input.replace(":", "");
                final Material mat = Material.matchMaterial(s.toUpperCase().replace(" ", "_"));
                if (mat == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidName"));
                    return new ItemNamePrompt(context);
                } else {
                    if (context.getPlugin() instanceof Quests) {
                        final Quests plugin = (Quests)context.getPlugin();
                        if (plugin.hasLimitedAccess(context.getForWhom())) {
                            if (plugin.getServer().getRecipesFor(new ItemStack(mat)).isEmpty()) {
                                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("noPermission"));
                                return new ItemStackPrompt(context, oldPrompt);
                            }
                        }
                    }
                    context.setSessionData("tempName", mat.name());
                    context.setSessionData("tempAmount", 1);
                    return new ItemStackPrompt(context, oldPrompt);
                }
            } else {
                return new ItemStackPrompt(context, oldPrompt);
            }
        }
    }

    public class ItemAmountPrompt extends QuestsEditorStringPrompt {

        public ItemAmountPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterAmount");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 64) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "64"));
                        return new ItemAmountPrompt(context);
                    } else {
                        context.setSessionData("tempAmount", Integer.parseInt(input));
                        return new ItemStackPrompt(context, oldPrompt);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new ItemAmountPrompt(context);
                }
            } else {
                return new ItemStackPrompt(context, oldPrompt);
            }
        }
    }

    public class ItemDataPrompt extends QuestsEditorStringPrompt {

        public ItemDataPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterDurab");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidDurab"));
                        return new ItemDataPrompt(context);
                    } else {
                        context.setSessionData("tempData", Short.parseShort(input));
                        return new ItemStackPrompt(context, oldPrompt);
                    }
                } catch (final NumberFormatException e) {
                    if (input.equals("*")) {
                        context.setSessionData("tempData", Short.parseShort("999")); // wildcard value
                        return new ItemStackPrompt(context, oldPrompt);
                    }
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new ItemDataPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData("tempData", null);
            }
            return new ItemStackPrompt(context, oldPrompt);
        }
    }

    public class ItemEnchantmentPrompt extends QuestsEditorStringPrompt {

        public ItemEnchantmentPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("enchantmentsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterEnch");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            for (final Enchantment e : Enchantment.values()) {
                text.append(ChatColor.GREEN).append(ItemUtil.getPrettyEnchantmentName(e)).append(", ");
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            return text + "\n" + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            final String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(Lang.get("cmdClear")) && !s.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Enchantment e = ItemUtil.getEnchantmentFromPrettyName(MiscUtil.getCapitalized(s));
                if (e != null) {
                    context.setSessionData("tempEnchant", e);
                    return new ItemEnchantmentLevelPrompt(context, ItemUtil.getPrettyEnchantmentName(e));
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidEnch"));
                    return new ItemEnchantmentPrompt(context);
                }
            } else if (s.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData("tempEnchantments", null);
            }
            return new ItemStackPrompt(context, oldPrompt);
        }
    }
    
    public class ItemEnchantmentLevelPrompt extends QuestsEditorStringPrompt {

        final String enchantment;

        protected ItemEnchantmentLevelPrompt(final ConversationContext context, final String ench) {
            super(context);
            enchantment = ench;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterLevel").replace("<enchantment>", enchantment);
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.AQUA + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            try {
                final int num = Integer.parseInt(input);
                if (num < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                            .replace("<number>", "1"));
                    return new ItemEnchantmentLevelPrompt(context, enchantment);
                } else {
                    if (context.getSessionData("tempEnchantments") != null) {
                        @SuppressWarnings("unchecked")
                        final
                        Map<Enchantment, Integer> enchs 
                                = (Map<Enchantment, Integer>) context.getSessionData("tempEnchantments");
                        if (enchs != null) {
                            enchs.put((Enchantment) context.getSessionData("tempEnchant"), num);
                            context.setSessionData("tempEnchantments", enchs);
                        }
                    } else {
                        final Map<Enchantment, Integer> enchs = new HashMap<>();
                        enchs.put((Enchantment) context.getSessionData("tempEnchant"), num);
                        context.setSessionData("tempEnchantments", enchs);
                    }
                    return new ItemStackPrompt(context, oldPrompt);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                return new ItemEnchantmentLevelPrompt(context, enchantment);
            }
        }
    }

    public class ItemDisplayPrompt extends QuestsEditorStringPrompt {

        public ItemDisplayPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterDisplay");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(Lang.get("cmdCancel")) && !s.equalsIgnoreCase(Lang.get("cmdClear"))) {
                s = ConfigUtil.parseString(s);
                context.setSessionData("tempDisplay", s);
            } else if (s.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData("tempDisplay", null);
            }
            return new ItemStackPrompt(context, oldPrompt);
        }
    }

    public class ItemLorePrompt extends QuestsEditorStringPrompt {

        public ItemLorePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("itemCreateEnterLore");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(Lang.get("cmdCancel")) && !s.equalsIgnoreCase(Lang.get("cmdClear"))) {
                s = ConfigUtil.parseString(s);
                final LinkedList<String> lore = new LinkedList<>(Arrays.asList(s.split(Lang.get("charSemi"))));
                context.setSessionData("tempLore", lore);
            } else if (s.equalsIgnoreCase("clear")) {
                context.setSessionData("tempLore", null);
            }
            return new ItemStackPrompt(context, oldPrompt);
        }
    }

    private String getItemData(final ConversationContext context) {
        if (context.getSessionData("tempName") != null) {
            final StringBuilder item;
            if (context.getSessionData("tempDisplay") == null) {
                final String name = (String) context.getSessionData("tempName");
                item = new StringBuilder(ChatColor.AQUA + ItemUtil.getPrettyItemName(name));
                if (context.getSessionData("tempData") != null) {
                    item.append(":").append(ChatColor.BLUE).append(context.getSessionData("tempData"));
                }
            } else {
                item = new StringBuilder(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC
                        + context.getSessionData("tempDisplay") + ChatColor.RESET + "" + ChatColor.GRAY + " (");
                final String name = (String) context.getSessionData("tempName");
                item.append(ChatColor.AQUA).append(ItemUtil.getPrettyItemName(name));
                if (context.getSessionData("tempData") != null) {
                    item.append(":").append(ChatColor.BLUE).append(context.getSessionData("tempData"));
                }
                item.append(ChatColor.GRAY).append(")");
            }
            if (context.getSessionData("tempAmount") != null) {
                item.append(ChatColor.GRAY).append(" x ").append(ChatColor.DARK_AQUA)
                        .append(context.getSessionData("tempAmount"));
            } else {
                item.append(ChatColor.GRAY).append(" x ").append(ChatColor.DARK_AQUA).append("1");
            }
            item.append("\n");
            if (context.getSessionData("tempEnchantments") != null) {
                @SuppressWarnings("unchecked")
                final
                Map<Enchantment, Integer> enchantments 
                        = (Map<Enchantment, Integer>) context.getSessionData("tempEnchantments");
                if (enchantments != null) {
                    for (final Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                        item.append(ChatColor.GRAY).append("  - ").append(ChatColor.RED)
                                .append(ItemUtil.getPrettyEnchantmentName(e.getKey())).append(" ")
                                .append(RomanNumeral.getNumeral(e.getValue())).append("\n");
                    }
                }
            }
            if (context.getSessionData("tempLore") != null) {
                @SuppressWarnings("unchecked")
                final List<String> lore = (List<String>) context.getSessionData("tempLore");
                item.append(ChatColor.DARK_GREEN).append("(Lore)\n\"");
                if (lore != null) {
                    for (final String s : lore) {
                        if (lore.indexOf(s) != (lore.size() - 1)) {
                            item.append(ChatColor.DARK_GREEN).append(ChatColor.ITALIC).append(s).append("\n");
                        } else {
                            item.append(ChatColor.DARK_GREEN).append(ChatColor.ITALIC).append(s).append("\"\n");
                        }
                    }
                }
            }
            item.append("\n");
            return item.toString();
        } else {
            return null;
        }
    }
    
    public static void clearSessionData(final ConversationContext context) {
        context.setSessionData("tempStack", null);
        context.setSessionData("tempName", null);
        context.setSessionData("tempAmount", null);
        context.setSessionData("tempData", null);
        context.setSessionData("tempEnchantments", null);
        context.setSessionData("tempDisplay", null);
        context.setSessionData("tempLore", null);
        context.setSessionData("tempMeta", null);
    }
}
