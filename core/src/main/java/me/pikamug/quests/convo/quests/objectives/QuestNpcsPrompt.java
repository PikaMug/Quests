/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.objectives;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMainPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class QuestNpcsPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public QuestNpcsPrompt(final int stageNum, final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("stageEditorNPCs");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.BLUE;
            case 4:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliverItems");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorTalkToNPCs");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorKillNPCs");
        case 4:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch(number) {
        case 1:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS);
                    final LinkedList<ItemStack> items 
                            = (LinkedList<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                    if (npcs != null && items != null) {
                        for (int i = 0; i < npcs.size(); i++) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(BukkitItemUtil.getName(items.get(i))).append(ChatColor.GRAY).append(" x ")
                                    .append(ChatColor.AQUA).append(items.get(i).getAmount()).append(ChatColor.GRAY)
                                    .append(" ").append(BukkitLang.get("to")).append(" ").append(ChatColor.BLUE)
                                    .append(plugin.getDependencies().getNpcName(UUID.fromString(npcs.get(i))));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + " (" + BukkitLang.get("notInstalled") + ")";
            }
        case 2:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO);
                    if (npcs != null) {
                        for (final String npc : npcs) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(plugin.getDependencies().getNpcName(UUID.fromString(npc)));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 3:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL);
                    final LinkedList<Integer> amounts 
                            = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                    if (npcs != null && amounts != null) {
                        for (int i = 0; i < npcs.size(); i++) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(plugin.getDependencies().getNpcName(UUID.fromString(npcs.get(i))))
                                    .append(ChatColor.GRAY).append(" x ").append(ChatColor.AQUA).append(amounts.get(i));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 4:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        SessionData.set(uuid, pref, Boolean.TRUE);

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = Bukkit.getEntity(uuid);
        switch(input.intValue()) {
        case 1:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNpcsDeliveryListPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoCitizens"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
        case 2:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNpcsIdsToTalkToPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoCitizens"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
        case 3:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNpcsKillListPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoCitizens"));
                new QuestStageMainPrompt(stageNum, uuid).start();
            }
        case 4:
            try {
                new QuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return;
            }
        default:
            new QuestNpcsPrompt(stageNum, uuid).start();
        }
    }
    
    public class QuestNpcsDeliveryListPrompt extends QuestsEditorIntegerPrompt {

        public QuestNpcsDeliveryListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorDeliverItems");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                    return ChatColor.BLUE;
                case 4:
                    return ChatColor.RED;
                case 5:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorNPCUniqueIds");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryMessages");
            case 4:
                return ChatColor.RED + BukkitLang.get("clear");
            case 5:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> deliveryItems
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                    if (deliveryItems != null) {
                        for (final ItemStack is : deliveryItems) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(BukkitItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryNpcs = (List<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS);
                    if (deliveryNpcs != null) {
                        for (final String s : deliveryNpcs) {
                            final UUID uuid = UUID.fromString(s);
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(plugin.getDependencies().getNpcName(uuid)).append(ChatColor.GRAY)
                                    .append(" (").append(ChatColor.BLUE).append(s).append(ChatColor.GRAY).append(")");
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryMessages
                            = (List<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES);
                    if (deliveryMessages != null) {
                        for (final String s : deliveryMessages) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append("\"").append(s).append("\"");
                        }
                    }
                    return text.toString();
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS) != null) {
                    final List<ItemStack> itemRew
                            = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    }
                    SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, itemRew);
                } else {
                    final LinkedList<ItemStack> itemRews = new LinkedList<>();
                    itemRews.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, itemRews);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestNpcsDeliveryListPrompt.this);
            case 2:
                new QuestNpcDeliveryNpcsPrompt(uuid).start();
            case 3:
                new QuestNpcDeliveryMessagesPrompt(uuid).start();
            case 4:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("cleared"));
                SessionData.set(uuid, pref + Key.S_DELIVERY_ITEMS, null);
                SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, null);
                SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, null);
                new QuestNpcsDeliveryListPrompt(uuid).start();
            case 5:
                final int one;
                final int two;
                final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, pref + Key.S_DELIVERY_ITEMS);
                final List<UUID> npcs = (List<UUID>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS);
                if (items != null) {
                    one = items.size();
                } else {
                    one = 0;
                }
                if (npcs != null) {
                    two = npcs.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    if (SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) == null && one != 0) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("stageEditorNoDeliveryMessage"));
                        new QuestNpcsDeliveryListPrompt(uuid).start();
                    } else {
                        new QuestNpcsPrompt(stageNum, uuid).start();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestNpcsDeliveryListPrompt(uuid).start();
                }
            default:
                new QuestNpcsPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestNpcDeliveryNpcsPrompt extends QuestsEditorStringPrompt {
        
        public QuestNpcDeliveryNpcsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (Bukkit.getEntity(uuid) instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> npcs = SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) != null
                        ? (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_NPCS) : new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(uuid)) {
                            npcs.add(uuid.toString());
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            new QuestNpcDeliveryNpcsPrompt(uuid).start();
                        }
                    } catch (final IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", input));
                        new QuestNpcDeliveryNpcsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_DELIVERY_NPCS, npcs);

                LinkedList<String> messages = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES) != null) {
                    messages = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_DELIVERY_MESSAGES);
                }
                if (messages != null && npcs != null) {
                    for (int i = 0; i < npcs.size(); i++) {
                        if (i >= messages.size()) {
                            messages.add(ChatColor.RESET + BukkitLang.get("thankYouMore"));
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, messages);
            }
            if (sender instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) sender).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new QuestNpcsDeliveryListPrompt(uuid).start();
        }
    }

    public class QuestNpcDeliveryMessagesPrompt extends QuestsEditorStringPrompt {

        public QuestNpcDeliveryMessagesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorDeliveryMessagesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText() + "\n" + ChatColor.GOLD
                    + BukkitLang.get("stageEditorNPCNote");
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
                final LinkedList<String> messages = new LinkedList<>(Arrays.asList(args));
                SessionData.set(uuid, pref + Key.S_DELIVERY_MESSAGES, messages);
            }
            new QuestNpcsDeliveryListPrompt(uuid).start();
        }
    }

    public class QuestNpcsIdsToTalkToPrompt extends QuestsEditorStringPrompt {

        public QuestNpcsIdsToTalkToPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("enterOrClearNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (Bukkit.getEntity(uuid) instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> npcs = SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) != null
                        ? (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_TALK_TO) : new LinkedList<>();
                for (final String s : args) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(uuid)) {
                            npcs.add(uuid.toString());
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            new QuestNpcsIdsToTalkToPrompt(uuid).start();
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("stageEditorNotListOfUniqueIds").replace("<data>", s));
                        new QuestNpcsIdsToTalkToPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, npcs);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, pref + Key.S_NPCS_TO_TALK_TO, null);
            }
            if (sender instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) sender).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new QuestStageMainPrompt(stageNum, uuid).start();
        }
    }

    public class QuestNpcsKillListPrompt extends QuestsEditorIntegerPrompt {

        public QuestNpcsKillListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorNPCs");
        }

        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorNPCUniqueIds");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetKillAmounts");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (plugin.getDependencies().hasAnyNpcDependencies()) {
                    if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> npcsToKill = (List<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL);
                        if (npcsToKill != null) {
                            for (final String s : npcsToKill) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                        .append(plugin.getDependencies().getNpcName(UUID.fromString(s)))
                                        .append(ChatColor.GRAY).append(" (").append(ChatColor.AQUA).append(s)
                                        .append(ChatColor.GRAY).append(")");
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatColor.GRAY + " (" + BukkitLang.get("notInstalled") + ")";
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> npcsToKillAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                    if (npcsToKillAmounts != null) {
                        for (final Integer i : npcsToKillAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new QuestNpcIdsToKillPrompt(uuid).start();
            case 2:
                new QuestNpcAmountsToKillPrompt(uuid).start();
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, null);
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, null);
                new QuestNpcsKillListPrompt(uuid).start();
            case 4:
                final int one;
                final int two;
                final List<UUID> kill = (List<UUID>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL);
                final List<Integer> killAmounts
                        = (List<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                if (kill != null) {
                    one = kill.size();
                } else {
                    one = 0;
                }
                if (killAmounts != null) {
                    two = killAmounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    new QuestStageMainPrompt(stageNum, uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestNpcsKillListPrompt(uuid).start();
                }
            default:
                new QuestNpcsPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestNpcIdsToKillPrompt extends QuestsEditorStringPrompt {

        public QuestNpcIdsToKillPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (Bukkit.getEntity(uuid) instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> npcs = SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) != null
                        ? (LinkedList<String>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL) : new LinkedList<>();
                for (final String s : args) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(uuid)) {
                            npcs.add(uuid.toString());
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            new QuestNpcIdsToKillPrompt(uuid).start();
                        }
                    } catch (final IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("stageEditorNotListOfUniqueIds").replace("<data>", s));
                        new QuestNpcIdsToKillPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL, npcs);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                }
                if (npcs != null && amounts != null) {
                    for (int i = 0; i < npcs.size(); i++) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
            selectingNpcs.remove(((Player) sender).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            new QuestNpcsKillListPrompt(uuid).start();
        }
    }

    public class QuestNpcAmountsToKillPrompt extends QuestsEditorStringPrompt {
        
        public QuestNpcAmountsToKillPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorKillNPCsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new QuestNpcAmountsToKillPrompt(uuid).start();
                        }
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("stageEditorNotListOfUniqueIds").replace("<data>", s));
                        new QuestNpcAmountsToKillPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            new QuestNpcsKillListPrompt(uuid).start();
        }
    }
}
