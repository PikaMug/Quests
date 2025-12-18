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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.RomanNumeral;
import me.pikamug.quests.util.SessionData;
import org.browsit.conversations.api.Conversations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
import java.util.UUID;

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

    private final @NotNull UUID uuid;
    private final Prompt oldPrompt;

    public ItemStackPrompt(final UUID uuid, final Prompt old) {
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
        return BukkitLang.get("createItemTitle");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
            if (SessionData.get(uuid, "tempMeta") != null) {
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
    public String getSelectionText(final int number) {
        switch (number) {
        case 0:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateLoadHand");
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateSetName");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateSetAmount");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateSetDurab");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateSetEnchs");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateSetDisplay");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("itemCreateSetLore");
        case 7:
            if (SessionData.get(uuid, "tempMeta") != null) {
                return ChatColor.DARK_GREEN + BukkitLang.get("itemCreateSetClearMeta");
            } else {
                return ChatColor.GRAY + BukkitLang.get("itemCreateSetClearMeta");
            }
        case 8:
            return ChatColor.RED + BukkitLang.get("cancel");
        case 9:
            return ChatColor.GREEN + BukkitLang.get("done");
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
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final String text = (String) SessionData.get(uuid, "tempName");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitItemUtil.getPrettyItemName(text) + ChatColor.GRAY + ")";
            }
        case 2:
            if (SessionData.get(uuid, "tempAmount") == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer text = (Integer) SessionData.get(uuid, "tempAmount");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, "tempData") == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Short text = (Short) SessionData.get(uuid, "tempData");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 4:
            if (SessionData.get(uuid, "tempEnchantments") == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final Map<Enchantment, Integer> map 
                        = (Map<Enchantment, Integer>) SessionData.get(uuid, "tempEnchantments");
                if (map != null) {
                    for (final Entry<Enchantment, Integer> e : map.entrySet()) {
                        text.append("\n").append(BukkitItemUtil.getPrettyEnchantmentName(e.getKey())).append(" ")
                                .append(RomanNumeral.getNumeral(e.getValue()));
                    }
                }
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, "tempDisplay") == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final String text = (String) SessionData.get(uuid, "tempDisplay");
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 6:
            if (SessionData.get(uuid, "tempLore") == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> list = (List<String>) SessionData.get(uuid, "tempLore");
                if (list != null) {
                    for (final String s : list) {
                        text.append("\n").append(s);
                    }
                }
                return ChatColor.GRAY + "(" + ChatColor.AQUA + text + ChatColor.GRAY + ")";
            }
        case 7:
            if (SessionData.get(uuid, "tempMeta") == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedHashMap<String, Object> map 
                        = (LinkedHashMap<String, Object>) SessionData.get(uuid, "tempMeta");
                if (map != null && !map.isEmpty()) {
                    for (final String key : map.keySet()) {
                        if (key.equals("pages")) {
                            final List<String> pages = (List<String>) map.get(key);
                            text.append("\n").append(ChatColor.GRAY).append("  \u2515 ").append(ChatColor.DARK_GREEN)
                                    .append(key).append("=").append(pages.size());
                        } else {
                            text.append("\n").append(ChatColor.GRAY).append("  \u2515 ").append(ChatColor.DARK_GREEN)
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

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenNumericPromptEvent event = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        Bukkit.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
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
                                text.append("\n").append(ChatColor.GRAY).append("  \u2515 ").append(ChatColor.DARK_GREEN)
                                        .append(key).append("=").append(pages.size());
                            } else {
                                text.append("\n").append(ChatColor.GRAY).append("  \u2515 ").append(ChatColor.DARK_GREEN)
                                        .append(key).append("=").append(map.get(key));
                            }
                        }
                    }
                }
            }
        }
        int start = 0;
        final CommandSender sender = Bukkit.getEntity(uuid);
        if (!(sender instanceof Player)) {
            start = 1;
        }
        for (int i = start; i <= size-1; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final @NotNull Number input) {
        acceptInput(uuid, input, null);
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void acceptInput(final UUID uuid, final Number input, final ItemStack item) {
        final CommandSender sender = Bukkit.getEntity(uuid);
        switch (input.intValue()) {
        case 0:
            if (sender instanceof Player) {
                SessionData.set(uuid, "tempMeta", null);
                
                final Player player = (Player) sender;
                final ItemStack is = item == null ? player.getItemInHand() : item;
                if (is.getType().equals(Material.AIR)) {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoItem"));
                } else {
                    SessionData.set(uuid, "tempName", is.getType().name());
                    SessionData.set(uuid, "tempAmount", is.getAmount());
                    SessionData.set(uuid, "tempData", null);
                    SessionData.set(uuid, "tempEnchantments", null);
                    SessionData.set(uuid, "tempDisplay", null);
                    SessionData.set(uuid, "tempLore", null);
                    if (is.getDurability() != 0) {
                        SessionData.set(uuid, "tempData", is.getDurability());
                    }
                    if (!is.getEnchantments().isEmpty()) {
                        SessionData.set(uuid, "tempEnchantments", new HashMap<>(is.getEnchantments()));
                    }
                    if (is.hasItemMeta()) {
                        final ItemMeta meta = is.getItemMeta();
                        if (meta != null) {
                            if (meta.hasDisplayName()) {
                                final String display = meta.getDisplayName().replace(ChatColor.COLOR_CHAR, '&');
                                SessionData.set(uuid, "tempDisplay", display);
                            }
                            if (meta.getLore() != null) {
                                final LinkedList<String> lore = new LinkedList<>(meta.getLore());
                                SessionData.set(uuid, "tempLore", lore);
                            }
                            final LinkedHashMap<String, Object> map = new LinkedHashMap<>(meta.serialize());
                            map.remove("lore");
                            map.remove("display-name");
                            if (!map.isEmpty()) {
                                SessionData.set(uuid, "tempMeta", map);
                            }
                        }
                    }
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
            }
            new ItemStackPrompt(uuid, oldPrompt).start();
        case 1:
            SessionData.set(uuid, "tempMeta", null);
            new ItemNamePrompt(uuid).start();
        case 2:
            if (SessionData.get(uuid, "tempName") != null) {
                new ItemAmountPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoName"));
                new ItemStackPrompt(uuid, oldPrompt).start();
            }
        case 3:
            if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                new ItemDataPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoNameAmount"));
                new ItemStackPrompt(uuid, oldPrompt).start();
            }
        case 4:
            if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                new ItemEnchantmentPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoNameAmount"));
                new ItemStackPrompt(uuid, oldPrompt).start();
            }
        case 5:
            if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                new ItemDisplayPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoNameAmount"));
                new ItemStackPrompt(uuid, oldPrompt).start();
            }
        case 6:
            if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                new ItemLorePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoNameAmount"));
                new ItemStackPrompt(uuid, oldPrompt).start();
            }
        case 7:
            if (SessionData.get(uuid, "tempName") != null && SessionData.get(uuid, "tempAmount") != null) {
                SessionData.set(uuid, "tempMeta", null);
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoNameAmount"));
            }
            new ItemStackPrompt(uuid, oldPrompt).start();
        case 8:
            clearSessionData(uuid);
            return oldPrompt;
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
                    display = ChatColor.translateAlternateColorCodes('&',
                            (String) Objects.requireNonNull(SessionData.get(uuid, "tempDisplay")));
                }
                if (SessionData.get(uuid, "tempLore") != null) {
                    lore = new ArrayList<>();
                    final LinkedList<String> loadedLore = (LinkedList<String>) SessionData.get(uuid, "tempLore");
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
                        if (SessionData.get(uuid, "tempMeta") != null) {
                            meta = BukkitItemUtil.deserializeItemMeta(meta.getClass(),
                                    (Map<String, Object>) SessionData.get(uuid, "tempMeta"));
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
                    SessionData.set(uuid, "tempStack", stack);
                    return oldPrompt;
                }
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateNoNameAmount"));
                new ItemStackPrompt(uuid, oldPrompt).start();
            }
        default:
            try {
                new ItemStackPrompt(uuid, oldPrompt).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                //return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    public class ItemNamePrompt extends QuestsEditorStringPrompt {

        public ItemNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String s = input.replace(":", "");
                final Material mat = Material.matchMaterial(s.toUpperCase().replace(" ", "_"));
                if (mat == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidName"));
                    new ItemNamePrompt(uuid).start();
                } else {
                    final BukkitQuestsPlugin plugin = BukkitQuestsPlugin.getInstance();
                    if (plugin.hasLimitedAccess(uuid)) {
                        if (plugin.getServer().getRecipesFor(new ItemStack(mat)).isEmpty()) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                            new ItemStackPrompt(uuid, oldPrompt);
                        }
                    }
                    SessionData.set(uuid, "tempName", mat.name());
                    SessionData.set(uuid, "tempAmount", 1);
                    new ItemStackPrompt(uuid, oldPrompt);
                }
            } else {
                new ItemStackPrompt(uuid, oldPrompt);
            }
        }
    }

    public class ItemAmountPrompt extends QuestsEditorStringPrompt {

        public ItemAmountPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterAmount");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 64) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "64"));
                        new ItemAmountPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempAmount", Integer.parseInt(input));
                        new ItemStackPrompt(uuid, oldPrompt);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new ItemAmountPrompt(uuid).start();
                }
            } else {
                new ItemStackPrompt(uuid, oldPrompt);
            }
        }
    }

    public class ItemDataPrompt extends QuestsEditorStringPrompt {

        public ItemDataPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterDurab");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidDurab"));
                        new ItemDataPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempData", Short.parseShort(input));
                        new ItemStackPrompt(uuid, oldPrompt);
                    }
                } catch (final NumberFormatException e) {
                    if (input.equals("*")) {
                        SessionData.set(uuid, "tempData", Short.parseShort("999")); // wildcard value
                        new ItemStackPrompt(uuid, oldPrompt);
                    }
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new ItemDataPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempData", null);
            }
            new ItemStackPrompt(uuid, oldPrompt);
        }
    }

    public class ItemEnchantmentPrompt extends QuestsEditorStringPrompt {

        public ItemEnchantmentPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("enchantmentsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterEnch");
        }

        @Override
        public @NotNull String getPromptText() {
            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            for (final Enchantment e : Enchantment.values()) {
                text.append(ChatColor.GREEN).append(BukkitItemUtil.getPrettyEnchantmentName(e)).append(", ");
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            return text + "\n" + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            final String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(BukkitLang.get("cmdClear")) && !s.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Enchantment e = BukkitItemUtil.getEnchantmentFromPrettyName(BukkitMiscUtil.getCapitalized(s));
                if (e != null) {
                    SessionData.set(uuid, "tempEnchant", e);
                    new ItemEnchantmentLevelPrompt(uuid, BukkitItemUtil.getPrettyEnchantmentName(e));
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidEnch"));
                    new ItemEnchantmentPrompt(uuid).start();
                }
            } else if (s.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempEnchantments", null);
            }
            new ItemStackPrompt(uuid, oldPrompt);
        }
    }
    
    public class ItemEnchantmentLevelPrompt extends QuestsEditorStringPrompt {

        final String enchantment;

        protected ItemEnchantmentLevelPrompt(final UUID uuid, final String ench) {
            super(uuid);
            enchantment = ench;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterLevel").replace("<enchantment>", enchantment);
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatColor.AQUA + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            try {
                final int num = Integer.parseInt(input);
                if (num < 1) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                            .replace("<number>", "1"));
                    new ItemEnchantmentLevelPrompt(uuid, enchantment);
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
                    new ItemStackPrompt(uuid, oldPrompt);
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber").replace("<input>", input));
                new ItemEnchantmentLevelPrompt(uuid, enchantment);
            }
        }
    }

    public class ItemDisplayPrompt extends QuestsEditorStringPrompt {

        public ItemDisplayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterDisplay");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !s.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                s = BukkitConfigUtil.parseString(s);
                SessionData.set(uuid, "tempDisplay", s);
            } else if (s.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, "tempDisplay", null);
            }
            new ItemStackPrompt(uuid, oldPrompt);
        }
    }

    public class ItemLorePrompt extends QuestsEditorStringPrompt {

        public ItemLorePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("itemCreateEnterLore");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                null;
            }
            String s = input.replace(":", "");
            if (!s.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !s.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                s = BukkitConfigUtil.parseString(s);
                final LinkedList<String> lore = new LinkedList<>(Arrays.asList(s.split(BukkitLang.get("charSemi"))));
                SessionData.set(uuid, "tempLore", lore);
            } else if (s.equalsIgnoreCase("clear")) {
                SessionData.set(uuid, "tempLore", null);
            }
            new ItemStackPrompt(uuid, oldPrompt);
        }
    }

    public String getItemData(final UUID uuid) {
        final StringBuilder item = new StringBuilder();
        if (SessionData.get(uuid, "tempDisplay") != null) {
            item.append(ChatColor.LIGHT_PURPLE).append(ChatColor.ITALIC)
                    .append(SessionData.get(uuid, "tempDisplay")).append(ChatColor.RESET).append(" ");
        }
        if (SessionData.get(uuid, "tempName") != null) {
            final String name = (String) SessionData.get(uuid, "tempName");
            item.append(ChatColor.GRAY).append("(").append(ChatColor.AQUA).append(BukkitItemUtil.getPrettyItemName(name));
            if (SessionData.get(uuid, "tempData") != null) {
                item.append(":").append(ChatColor.BLUE).append(SessionData.get(uuid, "tempData"));
            }
            item.append(ChatColor.GRAY).append(")");
        }
        if (SessionData.get(uuid, "tempAmount") != null) {
            item.append(ChatColor.GRAY).append(" x ").append(ChatColor.DARK_AQUA)
                    .append(SessionData.get(uuid, "tempAmount"));
        } else {
            item.append(ChatColor.GRAY).append(" x ").append(ChatColor.DARK_AQUA).append("1");
        }
        if (SessionData.get(uuid, "tempEnchantments") != null) {
            @SuppressWarnings("unchecked")
            final Map<Enchantment, Integer> enchantments
                    = (Map<Enchantment, Integer>) SessionData.get(uuid, "tempEnchantments");
            if (enchantments != null) {
                for (final Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                    item.append("\n").append(ChatColor.GRAY).append("  - ").append(ChatColor.RED)
                            .append(BukkitItemUtil.getPrettyEnchantmentName(e.getKey())).append(" ")
                            .append(RomanNumeral.getNumeral(e.getValue()));
                }
            }
        }
        if (SessionData.get(uuid, "tempLore") != null) {
            @SuppressWarnings("unchecked")
            final List<String> lore = (List<String>) SessionData.get(uuid, "tempLore");
            if (lore != null) {
                for (final String s : lore) {
                    item.append("\n").append(ChatColor.DARK_PURPLE).append(ChatColor.ITALIC).append(s);
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
}
