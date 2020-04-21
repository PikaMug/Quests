/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.quests.objectives;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class NPCsPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;

    public NPCsPrompt(int stageNum, ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 4;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("stageEditorNPCs");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
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
    
    public String getSelectionText(ConversationContext context, int number) {
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
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
                    LinkedList<ItemStack> items 
                            = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                    for (int i = 0; i < npcs.size(); i++) {
                        text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                                + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + ChatColor.GRAY 
                                + " " + Lang.get("to") + " " + ChatColor.DARK_AQUA 
                                + plugin.getDependencies().getCitizens().getNPCRegistry().getById(npcs.get(i)).getName()
                                + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + " (" + Lang.get("questCitNotInstalled") + ")";
            }
        case 2:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    LinkedList<Integer> npcs 
                            = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO);
                    for (int i = 0; i < npcs.size(); i++) {
                        text += ChatColor.GRAY + "     - " + ChatColor.BLUE 
                                + plugin.getDependencies().getCitizens().getNPCRegistry().getById(npcs.get(i)).getName() 
                                + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("questCitNotInstalled") + ")";
            }
        case 3:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
                    LinkedList<Integer> amounts 
                            = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
                    for (int i = 0; i < npcs.size(); i++) {
                        text += ChatColor.GRAY + "     - " + ChatColor.BLUE 
                                + plugin.getDependencies().getCitizens().getNPCRegistry().getById(npcs.get(i)).getName() 
                                + ChatColor.GRAY + " x " + ChatColor.AQUA + amounts.get(i) + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("questCitNotInstalled") + ")";
            }
        case 4:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);

        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);

        String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch(input.intValue()) {
        case 1:
            if (plugin.getDependencies().getCitizens() != null) {
                return new DeliveryListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(stageNum, context);
            }
        case 2:
            if (plugin.getDependencies().getCitizens() != null) {
                return new NPCIDsToTalkToPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(stageNum, context);
            }
        case 3:
            if (plugin.getDependencies().getCitizens() != null) {
                return new NPCKillListPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(stageNum, context);
            }
        case 4:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new NPCsPrompt(stageNum, context);
        }
    }
    
    public class DeliveryListPrompt extends QuestsEditorNumericPrompt {

        public DeliveryListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorDeliverItems");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
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
        
        public String getSelectionText(ConversationContext context, int number) {
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
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (ItemStack is : (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (int i : (List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + " (" 
                                + plugin.getDependencies().getCitizens().getNPCRegistry().getById(i).getName() + ")\n";
                    }
                    return text;
                }
            case 3:
                if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_DELIVERY_MESSAGES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + "\"" + s + "\"\n";
                    }
                    return text;
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
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                    List<ItemStack> itemRews = (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_DELIVERY_ITEMS, itemRews);
                } else {
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_DELIVERY_ITEMS, itemRews);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(DeliveryListPrompt.this);
            case 2:
                return new DeliveryNPCsPrompt(context);
            case 3:
                return new DeliveryMessagesPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("Cleared"));
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, null);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, null);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, null);
                return new DeliveryListPrompt(context);
            case 5:
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                    one = ((List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null && one != 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoDeliveryMessage"));
                        return new DeliveryListPrompt(context);
                    } else {
                        return new NPCsPrompt(stageNum, context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new DeliveryListPrompt(context);
                }
            default:
                return new NPCsPrompt(stageNum, context);
            }
        }
    }

    public class DeliveryNPCsPrompt extends QuestsEditorStringPrompt {
        
        public DeliveryNPCsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorNPCPrompt");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {
                    try {
                        Integer i = Integer.parseInt(s);
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + "" + i + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidNPC"));
                            return new DeliveryNPCsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DeliveryNPCsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
            }
            Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.remove(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return new DeliveryListPrompt(context);
        }
    }

    public class DeliveryMessagesPrompt extends QuestsEditorStringPrompt {
        
        public DeliveryMessagesPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorDeliveryMessagesPrompt");
        }
        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + Lang.get("stageEditorNPCNote");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(Lang.get("charSemi"));
                LinkedList<String> messages = new LinkedList<String>();
                messages.addAll(Arrays.asList(args));
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, messages);
            }
            return new DeliveryListPrompt(context);
        }
    }

    public class NPCIDsToTalkToPrompt extends QuestsEditorStringPrompt {
        
        public NPCIDsToTalkToPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorNPCToTalkToPrompt");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD 
                    + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {
                    try {
                        Integer i = Integer.parseInt(s);
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + "" + i + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidNPC"));
                            return new NPCIDsToTalkToPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NPCIDsToTalkToPrompt(context);
                    }
                }
                Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
                temp.remove(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(temp);
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, null);
            }
            return new StageMainPrompt(stageNum, context);
        }
    }

    public class NPCKillListPrompt extends QuestsEditorNumericPrompt {

        public NPCKillListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorNPCs");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
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
        
        public String getSelectionText(ConversationContext context, int number) {
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
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (plugin.getDependencies().getCitizens() != null) {
                    if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        String text = "\n";
                        for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL)) {
                            text += ChatColor.GRAY + "     - " + ChatColor.AQUA 
                                    + plugin.getDependencies().getCitizens().getNPCRegistry().getById(i).getName() 
                                    + ChatColor.DARK_AQUA + " (" + i + ")\n";
                        }
                        return text;
                    }
                } else {
                    return ChatColor.GRAY + " (" + Lang.get("questCitNotInstalled") + ")";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.BLUE + i + "\n";
                    }
                    return text;
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch(input.intValue()) {
            case 1:
                return new NpcIdsToKillPrompt(context);
            case 2:
                return new NpcAmountsToKillPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, null);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, null);
                return new NPCKillListPrompt(context);
            case 4:
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) != null) {
                    one = ((List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new StageMainPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new NPCKillListPrompt(context);
                }
            default:
                return new NPCsPrompt(stageNum, context);
            }
        }
    }

    public class NpcIdsToKillPrompt extends QuestsEditorStringPrompt {
        
        public NpcIdsToKillPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorNPCPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return ChatColor.YELLOW + getQueryText(context) + "\n" + ChatColor.GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {
                    try {
                        Integer i = Integer.parseInt(s);
                        if (plugin.getDependencies().getCitizens().getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + "" + i + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidNPC"));
                            return new NpcIdsToKillPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcIdsToKillPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
            }
            Set<UUID> temp = plugin.getQuestFactory().getSelectingNpcs();
            temp.remove(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(temp);
            return new NPCKillListPrompt(context);
        }
    }

    public class NpcAmountsToKillPrompt extends QuestsEditorStringPrompt {
        
        public NpcAmountsToKillPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorKillNPCsPrompt");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {
                    try {
                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new NpcAmountsToKillPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcAmountsToKillPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            return new NPCKillListPrompt(context);
        }
    }
}
