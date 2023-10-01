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
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMainPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class QuestNpcsPrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public QuestNpcsPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("stageEditorNPCs");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
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
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                if (context.getSessionData(pref + Key.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) context.getSessionData(pref + Key.S_DELIVERY_NPCS);
                    final LinkedList<ItemStack> items 
                            = (LinkedList<ItemStack>) context.getSessionData(pref + Key.S_DELIVERY_ITEMS);
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
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                if (context.getSessionData(pref + Key.S_NPCS_TO_TALK_TO) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) context.getSessionData(pref + Key.S_NPCS_TO_TALK_TO);
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
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                if (context.getSessionData(pref + Key.S_NPCS_TO_KILL) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<String> npcs
                            = (LinkedList<String>) context.getSessionData(pref + Key.S_NPCS_TO_KILL);
                    final LinkedList<Integer> amounts 
                            = (LinkedList<Integer>) context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS);
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
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);

        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                return new QuestNpcsDeliveryListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoCitizens"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 2:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                return new QuestNpcsIdsToTalkToPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoCitizens"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 3:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                return new QuestNpcsKillListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoCitizens"));
                return new QuestStageMainPrompt(stageNum, context);
            }
        case 4:
            try {
                return new QuestStageMainPrompt(stageNum, context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new QuestNpcsPrompt(stageNum, context);
        }
    }
    
    public class QuestNpcsDeliveryListPrompt extends QuestsEditorNumericPrompt {

        public QuestNpcsDeliveryListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorDeliverItems");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
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
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + Key.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> deliveryItems
                            = (List<ItemStack>) context.getSessionData(pref + Key.S_DELIVERY_ITEMS);
                    if (deliveryItems != null) {
                        for (final ItemStack is : deliveryItems) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(BukkitItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + Key.S_DELIVERY_NPCS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryNpcs = (List<String>) context.getSessionData(pref + Key.S_DELIVERY_NPCS);
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
                if (context.getSessionData(pref + Key.S_DELIVERY_MESSAGES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryMessages
                            = (List<String>) context.getSessionData(pref + Key.S_DELIVERY_MESSAGES);
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

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getBasicPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (context.getSessionData(pref + Key.S_DELIVERY_ITEMS) != null) {
                    final List<ItemStack> itemRew
                            = (List<ItemStack>) context.getSessionData(pref + Key.S_DELIVERY_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) context.getSessionData("tempStack"));
                    }
                    context.setSessionData(pref + Key.S_DELIVERY_ITEMS, itemRew);
                } else {
                    final LinkedList<ItemStack> itemRews = new LinkedList<>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + Key.S_DELIVERY_ITEMS, itemRews);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, QuestNpcsDeliveryListPrompt.this);
            case 2:
                return new QuestNpcDeliveryNpcsPrompt(context);
            case 3:
                return new QuestNpcDeliveryMessagesPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("cleared"));
                context.setSessionData(pref + Key.S_DELIVERY_ITEMS, null);
                context.setSessionData(pref + Key.S_DELIVERY_NPCS, null);
                context.setSessionData(pref + Key.S_DELIVERY_MESSAGES, null);
                return new QuestNpcsDeliveryListPrompt(context);
            case 5:
                final int one;
                final int two;
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + Key.S_DELIVERY_ITEMS);
                final List<UUID> npcs = (List<UUID>) context.getSessionData(pref + Key.S_DELIVERY_NPCS);
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
                    if (context.getSessionData(pref + Key.S_DELIVERY_MESSAGES) == null && one != 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNoDeliveryMessage"));
                        return new QuestNpcsDeliveryListPrompt(context);
                    } else {
                        return new QuestNpcsPrompt(stageNum, context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestNpcsDeliveryListPrompt(context);
                }
            default:
                return new QuestNpcsPrompt(stageNum, context);
            }
        }
    }

    public class QuestNpcDeliveryNpcsPrompt extends QuestsEditorStringPrompt {
        
        public QuestNpcDeliveryNpcsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText(context);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> npcs = context.getSessionData(pref + Key.S_DELIVERY_NPCS) != null
                        ? (LinkedList<String>) context.getSessionData(pref + Key.S_DELIVERY_NPCS) : new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (plugin.getDependencies().getNpcEntity(uuid) != null && npcs != null) {
                            npcs.add(uuid.toString());
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            return new QuestNpcDeliveryNpcsPrompt(context);
                        }
                    } catch (final IllegalArgumentException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", input));
                        return new QuestNpcDeliveryNpcsPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_DELIVERY_NPCS, npcs);

                LinkedList<String> messages = new LinkedList<>();
                if (context.getSessionData(pref + Key.S_DELIVERY_MESSAGES) != null) {
                    messages = (LinkedList<String>) context.getSessionData(pref + Key.S_DELIVERY_MESSAGES);
                }
                if (messages != null && messages.size() == 0) {
                    messages.add(BukkitLang.get("thankYouMore"));
                }
                context.setSessionData(pref + Key.S_DELIVERY_MESSAGES, messages);
            }
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            return new QuestNpcsDeliveryListPrompt(context);
        }
    }

    public class QuestNpcDeliveryMessagesPrompt extends QuestsEditorStringPrompt {

        public QuestNpcDeliveryMessagesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorDeliveryMessagesPrompt");
        }
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + BukkitLang.get("stageEditorNPCNote");
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(BukkitLang.get("charSemi"));
                final LinkedList<String> messages = new LinkedList<>(Arrays.asList(args));
                context.setSessionData(pref + Key.S_DELIVERY_MESSAGES, messages);
            }
            return new QuestNpcsDeliveryListPrompt(context);
        }
    }

    public class QuestNpcsIdsToTalkToPrompt extends QuestsEditorStringPrompt {

        public QuestNpcsIdsToTalkToPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("enterOrClearNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText(context);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> npcs = context.getSessionData(pref + Key.S_NPCS_TO_TALK_TO) != null
                        ? (LinkedList<String>) context.getSessionData(pref + Key.S_NPCS_TO_TALK_TO) : new LinkedList<>();
                for (final String s : args) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (plugin.getDependencies().getNpcEntity(uuid) != null && npcs != null) {
                            npcs.add(uuid.toString());
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            return new QuestNpcsIdsToTalkToPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", s));
                        return new QuestNpcsIdsToTalkToPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_NPCS_TO_TALK_TO, npcs);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(pref + Key.S_NPCS_TO_TALK_TO, null);
            }
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            return new QuestStageMainPrompt(stageNum, context);
        }
    }

    public class QuestNpcsKillListPrompt extends QuestsEditorNumericPrompt {

        public QuestNpcsKillListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorNPCs");
        }

        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
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
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                    if (context.getSessionData(pref + Key.S_NPCS_TO_KILL) == null) {
                        return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<String> npcsToKill = (List<String>) context.getSessionData(pref + Key.S_NPCS_TO_KILL);
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
                if (context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> npcsToKillAmounts
                            = (List<Integer>) context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS);
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestNpcIdsToKillPrompt(context);
            case 2:
                return new QuestNpcAmountsToKillPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + Key.S_NPCS_TO_KILL, null);
                context.setSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS, null);
                return new QuestNpcsKillListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<UUID> kill = (List<UUID>) context.getSessionData(pref + Key.S_NPCS_TO_KILL);
                final List<Integer> killAmounts
                        = (List<Integer>) context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS);
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
                    return new QuestStageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestNpcsKillListPrompt(context);
                }
            default:
                return new QuestNpcsPrompt(stageNum, context);
            }
        }
    }

    public class QuestNpcIdsToKillPrompt extends QuestsEditorStringPrompt {

        public QuestNpcIdsToKillPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText(context);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> npcs = context.getSessionData(pref + Key.S_NPCS_TO_KILL) != null
                        ? (LinkedList<String>) context.getSessionData(pref + Key.S_NPCS_TO_KILL) : new LinkedList<>();
                for (final String s : args) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (plugin.getDependencies().getNpcEntity(uuid) != null && npcs != null) {
                            npcs.add(uuid.toString());
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            return new QuestNpcIdsToKillPrompt(context);
                        }
                    } catch (final IllegalArgumentException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", s));
                        return new QuestNpcIdsToKillPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_NPCS_TO_KILL, npcs);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) context.getSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS);
                }
                if (npcs != null && amounts != null) {
                    for (int i = 0; i < npcs.size(); i++) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                context.setSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
            selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            return new QuestNpcsKillListPrompt(context);
        }
    }

    public class QuestNpcAmountsToKillPrompt extends QuestsEditorStringPrompt {
        
        public QuestNpcAmountsToKillPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorKillNPCsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new QuestNpcAmountsToKillPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", s));
                        return new QuestNpcAmountsToKillPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            return new QuestNpcsKillListPrompt(context);
        }
    }
}
