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

import java.util.LinkedList;
import java.util.List;

import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class BlocksPrompt extends FixedSetPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;
    private final QuestFactory questFactory;

    public BlocksPrompt(Quests plugin, int stageNum, QuestFactory qf) {
        super("1", "2", "3", "4", "5", "6");
        this.plugin = plugin;
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
        this.questFactory = qf;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);
        String text = ChatColor.AQUA + "- " + Lang.get("stageEditorBlocks") + " -\n";
        if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorBreakBlocks") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorBreakBlocks") + "\n";
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                        + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorDamageBlocks") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorDamageBlocks") + "\n";
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                        + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorPlaceBlocks") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorPlaceBlocks") + "\n";
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                        + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorUseBlocks") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorUseBlocks") + "\n";
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_USE_NAMES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                        + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "5 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorCutBlocks") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "5 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorCutBlocks") + "\n";
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_CUT_NAMES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS);
            for (int i = 0; i < names.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                        + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
            }
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "6 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("1")) {
            return new BreakBlockListPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            return new DamageBlockListPrompt();
        } else if (input.equalsIgnoreCase("3")) {
            return new PlaceBlockListPrompt();
        } else if (input.equalsIgnoreCase("4")) {
            return new UseBlockListPrompt();
        } else if (input.equalsIgnoreCase("5")) {
            return new CutBlockListPrompt();
        }
        try {
            return new StageMainPrompt(plugin, stageNum, context, questFactory);
        } catch (Exception e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    private class BreakBlockListPrompt extends FixedSetPrompt {

        public BreakBlockListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorBreakBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noNamesSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                }
                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_BREAK_DURABILITY) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Short s : getBlockDurability(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new BreakBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new BreakBlockListPrompt();
                } else {
                    return new BreakBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new BreakBlockListPrompt();
                } else {
                    return new BreakBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_BREAK_NAMES, null);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, null);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, null);
                return new BreakBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) != null) {
                    one = ((List<Integer>) context.getSessionData(pref + CK.S_BREAK_NAMES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {                    
                    int missing;
                    LinkedList<Short> elements;
                    if (context.getSessionData(pref + CK.S_BREAK_DURABILITY) != null) {
                        missing = one - ((List<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY)).size();
                        elements = (LinkedList<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY);
                    } else {
                        missing = one;
                        elements = new LinkedList<Short>();
                    }
                    for (int i = 0; i < missing; i++) {
                        elements.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_BREAK_DURABILITY, elements);
                    return new BlocksPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new BreakBlockListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
        }

        @SuppressWarnings("unchecked")
        private List<Short> getBlockDurability(ConversationContext context) {
            return (List<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY);
        }
    }

    private class BreakBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                        + Lang.get("stageEditorNotSolid"));
                                return new BreakBlockNamesPrompt();
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new BreakBlockNamesPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockNamesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_NAMES, names);
            }
            return new BreakBlockListPrompt();
        }
    }

    private class BreakBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new BreakBlockAmountsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amounts);
            }
            return new BreakBlockListPrompt();
        }
    }

    private class BreakBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Short> durability = new LinkedList<Short>();
                for (String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new BreakBlockDurabilityPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockDurabilityPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durability);
            }
            return new BreakBlockListPrompt();
        }
    }

    private class DamageBlockListPrompt extends FixedSetPrompt {

        public DamageBlockListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorDamageBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noNamesSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                }
                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Short s : getBlockDurability(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new DamageBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new DamageBlockListPrompt();
                } else {
                    return new DamageBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new DamageBlockListPrompt();
                } else {
                    return new DamageBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, null);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, null);
                return new DamageBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    int missing;
                    LinkedList<Short> elements;
                    if (context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) != null) {
                        missing = one - ((List<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY)).size();
                        elements = (LinkedList<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY);
                    } else {
                        missing = one;
                        elements = new LinkedList<Short>();
                    }
                    for (int i = 0; i < missing; i++) {
                        elements.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, elements);
                    return new BlocksPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new DamageBlockListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
        }

        @SuppressWarnings("unchecked")
        private List<Short> getBlockDurability(ConversationContext context) {
            return (List<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY);
        }
    }

    private class DamageBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                        + Lang.get("stageEditorNotSolid"));
                                return new DamageBlockNamesPrompt();
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new DamageBlockNamesPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockNamesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
            }
            return new DamageBlockListPrompt();
        }
    }

    private class DamageBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new DamageBlockAmountsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amounts);
            }
            return new DamageBlockListPrompt();
        }
    }

    private class DamageBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Short> durability = new LinkedList<Short>();
                for (String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new DamageBlockDurabilityPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockDurabilityPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durability);
            }
            return new DamageBlockListPrompt();
        }
    }

    private class PlaceBlockListPrompt extends FixedSetPrompt {

        public PlaceBlockListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorPlaceBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noNamesSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                }
                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_PLACE_DURABILITY) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Short s : getBlockDurability(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new PlaceBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new PlaceBlockListPrompt();
                } else {
                    return new PlaceBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new PlaceBlockListPrompt();
                } else {
                    return new PlaceBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_PLACE_NAMES, null);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, null);
                return new PlaceBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_PLACE_NAMES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    int missing;
                    LinkedList<Short> elements;
                    if (context.getSessionData(pref + CK.S_PLACE_DURABILITY) != null) {
                        missing = one - ((List<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY)).size();
                        elements = (LinkedList<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY);
                    } else {
                        missing = one;
                        elements = new LinkedList<Short>();
                    }
                    for (int i = 0; i < missing; i++) {
                        elements.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_PLACE_DURABILITY, elements);
                    return new BlocksPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new PlaceBlockListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
        }

        @SuppressWarnings("unchecked")
        private List<Short> getBlockDurability(ConversationContext context) {
            return (List<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY);
        }
    }

    private class PlaceBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                        + Lang.get("stageEditorNotSolid"));
                                return new PlaceBlockNamesPrompt();
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new PlaceBlockNamesPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockNamesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_NAMES, names);
            }
            return new PlaceBlockListPrompt();
        }
    }

    private class PlaceBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new PlaceBlockAmountsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amounts);
            }
            return new PlaceBlockListPrompt();
        }
    }

    private class PlaceBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Short> durability = new LinkedList<Short>();
                for (String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new PlaceBlockDurabilityPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockDurabilityPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durability);
            }
            return new PlaceBlockListPrompt();
        }
    }

    private class UseBlockListPrompt extends FixedSetPrompt {

        public UseBlockListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorUseBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noNamesSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                }
                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_USE_DURABILITY) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Short s : getBlockDurability(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new UseBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new UseBlockListPrompt();
                } else {
                    return new UseBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new UseBlockListPrompt();
                } else {
                    return new UseBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_USE_NAMES, null);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_USE_DURABILITY, null);
                return new UseBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_USE_NAMES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_USE_NAMES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    int missing;
                    LinkedList<Short> elements;
                    if (context.getSessionData(pref + CK.S_USE_DURABILITY) != null) {
                        missing = one - ((List<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY)).size();
                        elements = (LinkedList<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY);
                    } else {
                        missing = one;
                        elements = new LinkedList<Short>();
                    }
                    for (int i = 0; i < missing; i++) {
                        elements.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_USE_DURABILITY, elements);
                    return new BlocksPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new UseBlockListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_USE_NAMES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
        }

        @SuppressWarnings("unchecked")
        private List<Short> getBlockDurability(ConversationContext context) {
            return (List<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY);
        }
    }

    private class UseBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                        + Lang.get("stageEditorNotSolid"));
                                return new UseBlockNamesPrompt();
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new UseBlockNamesPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockNamesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_USE_NAMES, names);
            }
            return new UseBlockListPrompt();
        }
    }

    private class UseBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new UseBlockAmountsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amounts);
            }
            return new UseBlockListPrompt();
        }
    }

    private class UseBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Short> durability = new LinkedList<Short>();
                for (String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new UseBlockDurabilityPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockDurabilityPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_USE_DURABILITY, durability);
            }
            return new UseBlockListPrompt();
        }
    }

    private class CutBlockListPrompt extends FixedSetPrompt {

        public CutBlockListPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorCutBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noNamesSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY  + " - " 
                        + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                }
                if (context.getSessionData(pref + CK.S_CUT_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetBlockAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_CUT_DURABILITY) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - "
                            + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - "
                            + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Short s : getBlockDurability(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new CutBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new CutBlockListPrompt();
                } else {
                    return new CutBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoBlockNames"));
                    return new CutBlockListPrompt();
                } else {
                    return new CutBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_CUT_NAMES, null);
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, null);
                context.setSessionData(pref + CK.S_CUT_DURABILITY, null);
                return new CutBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_CUT_NAMES) != null) {
                    one = ((List<Integer>) context.getSessionData(pref + CK.S_CUT_NAMES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_CUT_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    int missing;
                    LinkedList<Short> elements;
                    if (context.getSessionData(pref + CK.S_CUT_DURABILITY) != null) {
                        missing = one - ((List<Short>) context.getSessionData(pref + CK.S_CUT_DURABILITY)).size();
                        elements = (LinkedList<Short>) context.getSessionData(pref + CK.S_CUT_DURABILITY);
                    } else {
                        missing = one;
                        elements = new LinkedList<Short>();
                    }
                    for (int i = 0; i < missing; i++) {
                        elements.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_CUT_DURABILITY, elements);
                    return new BlocksPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new CutBlockListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_CUT_NAMES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS);
        }

        @SuppressWarnings("unchecked")
        private List<Short> getBlockDurability(ConversationContext context) {
            return (List<Short>) context.getSessionData(pref + CK.S_CUT_DURABILITY);
        }
    }

    private class CutBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {
                    try {
                        Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " "
                                        + Lang.get("stageEditorNotSolid"));
                                return new CutBlockNamesPrompt();
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " "
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new CutBlockNamesPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockNamesPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_CUT_NAMES, names);
            }
            return new CutBlockListPrompt();
        }
    }

    private class CutBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new CutBlockAmountsPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, amounts);
            }
            return new CutBlockListPrompt();
        }
    }

    private class CutBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Short> durability = new LinkedList<Short>();
                for (String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new CutBlockDurabilityPrompt();
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockDurabilityPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_CUT_DURABILITY, durability);
            }
            return new CutBlockListPrompt();
        }
    }
}
