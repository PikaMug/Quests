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

package me.blackvein.quests.prompts.quests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

public class NPCsPrompt extends FixedSetPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;
    private final QuestFactory questFactory;

    public NPCsPrompt(Quests plugin, int stageNum, QuestFactory qf) {
        super("1", "2", "3", "4");
        this.plugin = plugin;
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
        this.questFactory = qf;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);
        String text = ChatColor.AQUA + "- " + Lang.get("stageEditorNPCs") + " -\n";
        if (plugin.getDependencies().getCitizens() != null) {
            if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                        + "- " + Lang.get("stageEditorDeliverItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") 
                        + ")\n";
            } else {
                text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                        + "- " + Lang.get("stageEditorDeliverItems") + "\n";
                LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
                LinkedList<ItemStack> items 
                        = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                for (int i = 0; i < npcs.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + ChatColor.GRAY + " "
                            + Lang.get("to") + " " + ChatColor.DARK_AQUA 
                            + plugin.getDependencies().getCitizens().getNPCRegistry().getById(npcs.get(i)).getName()
                            + "\n";
                }
            }
        } else {
            text += ChatColor.GRAY + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.GRAY + "- " 
                    + Lang.get("stageEditorDeliverItems") + ChatColor.GRAY + " (" + Lang.get("questCitNotInstalled") 
                    + ")\n";
        }
        if (plugin.getDependencies().getCitizens() != null) {
            if (context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) == null) {
                text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                        + "- " + Lang.get("stageEditorTalkToNPCs") + ChatColor.GRAY + " (" + Lang.get("noneSet") 
                        + ")\n";
            } else {
                text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                        + "- " + Lang.get("stageEditorTalkToNPCs") + "\n";
                LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO);
                for (int i = 0; i < npcs.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE 
                            + plugin.getDependencies().getCitizens().getNPCRegistry().getById(npcs.get(i)).getName() 
                            + "\n";
                }
            }
        } else {
            text += ChatColor.GRAY + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.GRAY + "- " 
                    + Lang.get("stageEditorTalkToNPCs") + ChatColor.GRAY + " (" + Lang.get("questCitNotInstalled") 
                    + ")\n";
        }
        if (plugin.getDependencies().getCitizens() != null) {
            if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                        + "- " + Lang.get("stageEditorKillNPCs") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                        + "- " + Lang.get("stageEditorKillNPCs") + "\n";
                LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
                LinkedList<Integer> amounts 
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
                for (int i = 0; i < npcs.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE 
                            + plugin.getDependencies().getCitizens().getNPCRegistry().getById(npcs.get(i)).getName() 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + amounts.get(i) + "\n";
                }
            }
        } else {
            text += ChatColor.GRAY + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.GRAY + "- " 
                    + Lang.get("stageEditorKillNPCs") + ChatColor.GRAY + " (" + Lang.get("questCitNotInstalled") 
                    + ")\n";
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("1")) {
            if (plugin.getDependencies().getCitizens() != null) {
                return new DeliveryListPrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(plugin, stageNum, context, questFactory);
            }
        } else if (input.equalsIgnoreCase("2")) {
            if (plugin.getDependencies().getCitizens() != null) {
                return new NPCIDsToTalkToPrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(plugin, stageNum, context, questFactory);
            }
        } else if (input.equalsIgnoreCase("3")) {
            if (plugin.getDependencies().getCitizens() != null) {
                return new NPCKillListPrompt();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoCitizens"));
                return new StageMainPrompt(plugin, stageNum, context, questFactory);
            }
        }
        try {
            return new StageMainPrompt(plugin, stageNum, context, questFactory);
        } catch (Exception e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class DeliveryListPrompt extends FixedSetPrompt {

        public DeliveryListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                    List<ItemStack> itemRews = getItems(context);
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
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorDeliverItems") + " -\n";
            if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                text += ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorDeliveryNPCs") + " (" + Lang.get("noneSet") + ")\n";
                if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorDeliveryMessages") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorDeliveryMessages") + "\n";
                    for (String s : getDeliveryMessages(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + "\"" + s + "\"";
                    }
                }
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                for (ItemStack is : getItems(context)) {
                    text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                }
                if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorDeliveryNPCs") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorDeliveryNPCs") + "\n";
                    for (int i : getDeliveryNPCs(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + " (" 
                                + plugin.getDependencies().getCitizens().getNPCRegistry().getById(i).getName() + ")\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorDeliveryMessages") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorDeliveryMessages") + "\n";
                    for (String s : getDeliveryMessages(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + "\"" + s + "\"\n";
                    }
                }
            }
            text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("clear") + "\n";
            text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("done");
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(DeliveryListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoItems"));
                    return new DeliveryListPrompt();
                } else {
                    return new DeliveryNPCsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                return new DeliveryMessagesPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorDeliveriesCleared"));
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, null);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, null);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, null);
                return new DeliveryListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
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
                        return new DeliveryListPrompt();
                    } else {
                        return new NPCsPrompt(plugin, stageNum, questFactory);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new DeliveryListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getDeliveryNPCs(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
        }

        @SuppressWarnings("unchecked")
        private List<String> getDeliveryMessages(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_DELIVERY_MESSAGES);
        }
    }

    private class DeliveryNPCsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            HashSet<Player> temp = questFactory.getSelectingNpcs();
            temp.add((Player) context.getForWhom());
            questFactory.setSelectingNpcs(temp);
            return ChatColor.YELLOW + Lang.get("stageEditorNPCPrompt") + "\n" + ChatColor.GOLD + Lang.get("npcHint");
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
                            return new DeliveryNPCsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DeliveryNPCsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
            }
            HashSet<Player> temp = questFactory.getSelectingNpcs();
            temp.remove((Player) context.getForWhom());
            questFactory.setSelectingNpcs(temp);
            return new DeliveryListPrompt();
        }
    }

    private class DeliveryMessagesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String note = ChatColor.GOLD + Lang.get("stageEditorNPCNote");
            return ChatColor.YELLOW + Lang.get("stageEditorDeliveryMessagesPrompt") + "\n" + note;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(Lang.get("charSemi"));
                LinkedList<String> messages = new LinkedList<String>();
                messages.addAll(Arrays.asList(args));
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, messages);
            }
            return new DeliveryListPrompt();
        }
    }

    private class NPCIDsToTalkToPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            HashSet<Player> temp = questFactory.getSelectingNpcs();
            temp.add((Player) context.getForWhom());
            questFactory.setSelectingNpcs(temp);
            return ChatColor.YELLOW + Lang.get("stageEditorNPCToTalkToPrompt") + "\n" + ChatColor.GOLD 
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
                            return new NPCIDsToTalkToPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NPCIDsToTalkToPrompt();
                    }
                }
                HashSet<Player> temp = questFactory.getSelectingNpcs();
                temp.remove((Player) context.getForWhom());
                questFactory.setSelectingNpcs(temp);
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, null);
            }
            return new StageMainPrompt(plugin, stageNum, context, questFactory);
        }
    }

    private class NPCKillListPrompt extends FixedSetPrompt {

        public NPCKillListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorKillNPCs") + " -\n";
            if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetKillIds") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetKillAmounts") + " (" + Lang.get("noIdsSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("cancel") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetKillIds") + "\n";
                for (Integer i : getNPCIds(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA 
                            + plugin.getDependencies().getCitizens().getNPCRegistry().getById(i).getName() 
                            + ChatColor.DARK_AQUA + " (" + i + ")\n";
                }
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetKillAmounts") + "\n";
                    for (Integer i : getKillAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.BLUE + i + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new NpcIdsToKillPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoNPCs"));
                    return new NPCKillListPrompt();
                } else {
                    return new NpcAmountsToKillPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, null);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, null);
                return new NPCKillListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
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
                    return new StageMainPrompt(plugin, stageNum, context, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new NPCKillListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getNPCIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getKillAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
        }
    }

    private class NpcIdsToKillPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            HashSet<Player> temp = questFactory.getSelectingNpcs();
            temp.add((Player) context.getForWhom());
            questFactory.setSelectingNpcs(temp);
            return ChatColor.YELLOW + Lang.get("stageEditorNPCPrompt") + "\n" + ChatColor.GOLD + Lang.get("npcHint");
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
                            return new NpcIdsToKillPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcIdsToKillPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
            }
            HashSet<Player> temp = questFactory.getSelectingNpcs();
            temp.remove((Player) context.getForWhom());
            questFactory.setSelectingNpcs(temp);
            return new NPCKillListPrompt();
        }
    }

    private class NpcAmountsToKillPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorKillNPCsPrompt");
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
                            return new NpcAmountsToKillPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcAmountsToKillPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, amounts);
            }
            return new NPCKillListPrompt();
        }
    }
}
