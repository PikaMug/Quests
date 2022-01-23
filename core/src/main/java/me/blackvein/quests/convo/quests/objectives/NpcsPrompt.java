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

package me.blackvein.quests.convo.quests.objectives;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
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

public class NpcsPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;

    public NpcsPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
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
        return Lang.get("stageEditorNPCs");
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
            return ChatColor.YELLOW + Lang.get("stageEditorDeliverItems");
        case 2:
            return ChatColor.YELLOW + Lang.get("stageEditorTalkToNPCs");
        case 3:
            return ChatColor.YELLOW + Lang.get("stageEditorKillNPCs");
        case 4:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
                    final LinkedList<ItemStack> items 
                            = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                    if (npcs != null && items != null) {
                        for (int i = 0; i < npcs.size(); i++) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(ItemUtil.getName(items.get(i))).append(ChatColor.GRAY).append(" x ")
                                    .append(ChatColor.AQUA).append(items.get(i).getAmount()).append(ChatColor.GRAY)
                                    .append(" ").append(Lang.get("to")).append(" ").append(ChatColor.DARK_AQUA)
                                    .append(plugin.getDependencies().getCitizens().getNPCRegistry()
                                    .getById(npcs.get(i)).getName());
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + " (" + Lang.get("notInstalled") + ")";
            }
        case 2:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<Integer> npcs 
                            = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO);
                    if (npcs != null) {
                        for (final Integer npc : npcs) {
                            text.append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(plugin.getDependencies().getCitizens().getNPCRegistry()
                                    .getById(npc).getName()).append("\n");
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 3:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
                    final LinkedList<Integer> amounts 
                            = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
                    if (npcs != null && amounts != null) {
                        for (int i = 0; i < npcs.size(); i++) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(plugin.getDependencies().getCitizens().getNPCRegistry()
                                    .getById(npcs.get(i)).getName()).append(ChatColor.GRAY).append(" x ")
                                    .append(ChatColor.AQUA).append(amounts.get(i));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
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

        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }

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
            if (plugin.getDependencies().getCitizens() != null) {
                return new NpcsDeliveryListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(stageNum, context);
            }
        case 2:
            if (plugin.getDependencies().getCitizens() != null) {
                return new NpcsIdsToTalkToPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(stageNum, context);
            }
        case 3:
            if (plugin.getDependencies().getCitizens() != null) {
                return new NpcsKillListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(stageNum, context);
            }
        case 4:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new NpcsPrompt(stageNum, context);
        }
    }
    
    public class NpcsDeliveryListPrompt extends QuestsEditorNumericPrompt {

        public NpcsDeliveryListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorDeliverItems");
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
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryNPCs");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryMessages");
            case 4:
                return ChatColor.RED + Lang.get("clear");
            case 5:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<ItemStack> deliveryItems
                            = (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                    if (deliveryItems != null) {
                        for (final ItemStack is : deliveryItems) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ")
                                    .append(ItemUtil.getDisplayString(is));
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> deliveryNpcs 
                            = (List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
                    if (deliveryNpcs != null) {
                        for (final int i : deliveryNpcs) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i)
                                    .append(ChatColor.GRAY).append(" (").append(ChatColor.AQUA)
                                    .append(plugin.getDependencies().getCitizens().getNPCRegistry()
                                    .getById(i).getName()).append(ChatColor.GRAY).append(")");
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> deliveryMessages
                            = (List<String>) context.getSessionData(pref + CK.S_DELIVERY_MESSAGES);
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
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                    final List<ItemStack> itemRew
                            = (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                    if (itemRew != null) {
                        itemRew.add((ItemStack) context.getSessionData("tempStack"));
                    }
                    context.setSessionData(pref + CK.S_DELIVERY_ITEMS, itemRew);
                } else {
                    final LinkedList<ItemStack> itemRews = new LinkedList<>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_DELIVERY_ITEMS, itemRews);
                }
                ItemStackPrompt.clearSessionData(context);
            }

            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new ItemStackPrompt(context, NpcsDeliveryListPrompt.this);
            case 2:
                return new NpcDeliveryNpcsPrompt(context);
            case 3:
                return new NpcDeliveryMessagesPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("cleared"));
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, null);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, null);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, null);
                return new NpcsDeliveryListPrompt(context);
            case 5:
                final int one;
                final int two;
                final List<ItemStack> items = (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                final List<Integer> npcs = (List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
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
                    if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null && one != 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoDeliveryMessage"));
                        return new NpcsDeliveryListPrompt(context);
                    } else {
                        return new NpcsPrompt(stageNum, context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new NpcsDeliveryListPrompt(context);
                }
            default:
                return new NpcsPrompt(stageNum, context);
            }
        }
    }

    public class NpcDeliveryNpcsPrompt extends QuestsEditorStringPrompt {
        
        public NpcDeliveryNpcsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorNPCPrompt");
        }

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> npcs = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + "" + i + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidNPC"));
                            return new NpcDeliveryNpcsPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcDeliveryNpcsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
            }
            final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.remove(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return new NpcsDeliveryListPrompt(context);
        }
    }

    public class NpcDeliveryMessagesPrompt extends QuestsEditorStringPrompt {
        
        public NpcDeliveryMessagesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorDeliveryMessagesPrompt");
        }
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + Lang.get("stageEditorNPCNote");
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(Lang.get("charSemi"));
                final LinkedList<String> messages = new LinkedList<>(Arrays.asList(args));
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, messages);
            }
            return new NpcsDeliveryListPrompt(context);
        }
    }

    public class NpcsIdsToTalkToPrompt extends QuestsEditorStringPrompt {
        
        public NpcsIdsToTalkToPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorNPCToTalkToPrompt");
        }

        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD 
                    + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> npcs = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + "" + i + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidNPC"));
                            return new NpcsIdsToTalkToPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcsIdsToTalkToPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, null);
            }
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
                temp.remove(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(temp);
            }
            return new StageMainPrompt(stageNum, context);
        }
    }

    public class NpcsKillListPrompt extends QuestsEditorNumericPrompt {

        public NpcsKillListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorNPCs");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillIds");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillAmounts");
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (plugin.getDependencies().getCitizens() != null) {
                    if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        final StringBuilder text = new StringBuilder();
                        final List<Integer> npcsToKill = (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
                        if (npcsToKill != null) {
                            for (final Integer i : npcsToKill) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                        .append(plugin.getDependencies().getCitizens().getNPCRegistry()
                                        .getById(i).getName()).append(ChatColor.DARK_AQUA).append(" (").append(i)
                                        .append(")");
                            }
                        }
                        return text.toString();
                    }
                } else {
                    return ChatColor.GRAY + " (" + Lang.get("notInstalled") + ")";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> npcsToKillAmounts
                            = (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new NpcIdsToKillPrompt(context);
            case 2:
                return new NpcAmountsToKillPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, null);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, null);
                return new NpcsKillListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<Integer> kill = (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
                final List<Integer> killAmounts
                        = (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
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
                    return new StageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new NpcsKillListPrompt(context);
                }
            default:
                return new NpcsPrompt(stageNum, context);
            }
        }
    }

    public class NpcIdsToKillPrompt extends QuestsEditorStringPrompt {
        
        public NpcIdsToKillPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorNPCPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final ConversationContext context) {
            final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Integer> npcs = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + "" + i + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidNPC"));
                            return new NpcIdsToKillPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcIdsToKillPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
            }
            final Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.remove(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return new NpcsKillListPrompt(context);
        }
    }

    public class NpcAmountsToKillPrompt extends QuestsEditorStringPrompt {
        
        public NpcAmountsToKillPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorKillNPCsPrompt");
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
                final String[] args = input.split(" ");
                final LinkedList<Integer> amounts = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new NpcAmountsToKillPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcAmountsToKillPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            return new NpcsKillListPrompt(context);
        }
    }
}
