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

import java.util.LinkedList;
import java.util.List;

import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class BlocksPrompt extends QuestsEditorNumericPrompt {
    private final int stageNum;
    private final String pref;

    public BlocksPrompt(int stageNum, ConversationContext context) {
        super(context);
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 6;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("stageEditorBlocks");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return ChatColor.BLUE;
            case 6:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("stageEditorBreakBlocks");
        case 2:
            return ChatColor.YELLOW + Lang.get("stageEditorDamageBlocks");
        case 3:
            return ChatColor.YELLOW + Lang.get("stageEditorPlaceBlocks");
        case 4:
            return ChatColor.YELLOW + Lang.get("stageEditorUseBlocks");
        case 5:
            return ChatColor.YELLOW + Lang.get("stageEditorCutBlocks");
        case 6:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
                for (int i = 0; i < names.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
                return text;
            }
        case 2:
            if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
                for (int i = 0; i < names.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
                return text;
            }
        case 3:
            if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
                for (int i = 0; i < names.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
                return text;
            }
        case 4:
            if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_USE_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
                for (int i = 0; i < names.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
                return text;
            }
        case 5:
            if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_CUT_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS);
                for (int i = 0; i < names.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getPrettyItemName(names.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
                return text;
            }
        case 6:
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
            return new BreakBlockListPrompt(context);
        case 2:
            return new DamageBlockListPrompt(context);
        case 3:
            return new PlaceBlockListPrompt(context);
        case 4:
            return new UseBlockListPrompt(context);
        case 5:
            return new CutBlockListPrompt(context);
        case 6:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new BlocksPrompt(stageNum, context);
        }
    }
    
    public class BreakBlockListPrompt extends QuestsEditorNumericPrompt {

        public BreakBlockListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorBreakBlocks");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockNames");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockDurability");
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
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) != null) {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_BREAK_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null) {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_BREAK_DURABILITY) != null) {
                    String text = "\n";
                    for (Short s : (List<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new BreakBlockNamesPrompt(context);
            case 2:
                return new BreakBlockAmountsPrompt(context);
            case 3:
                return new BreakBlockDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_BREAK_NAMES, null);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, null);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, null);
                return new BreakBlockListPrompt(context);
            case 5:
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
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new BreakBlockListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class BreakBlockNamesPrompt extends QuestsEditorStringPrompt {

        public BreakBlockNamesPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                                return new BreakBlockNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new BreakBlockNamesPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_NAMES, names);
            }
            return new BreakBlockListPrompt(context);
        }
    }

    public class BreakBlockAmountsPrompt extends QuestsEditorStringPrompt {

        public BreakBlockAmountsPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new BreakBlockAmountsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amounts);
            }
            return new BreakBlockListPrompt(context);
        }
    }

    public class BreakBlockDurabilityPrompt extends QuestsEditorStringPrompt {

        public BreakBlockDurabilityPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new BreakBlockDurabilityPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durability);
            }
            return new BreakBlockListPrompt(context);
        }
    }

    public class DamageBlockListPrompt extends QuestsEditorNumericPrompt {
        
        public DamageBlockListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorDamageBlocks");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockNames");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockDurability");
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
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null) {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null) {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) != null) {
                    String text = "\n";
                    for (Short s : (List<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new DamageBlockNamesPrompt(context);
            case 2:
                return new DamageBlockAmountsPrompt(context);
            case 3:
                return new DamageBlockDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, null);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, null);
                return new DamageBlockListPrompt(context);
            case 5:
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
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new DamageBlockListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class DamageBlockNamesPrompt extends QuestsEditorStringPrompt {

        public DamageBlockNamesPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                                return new DamageBlockNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new DamageBlockNamesPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
            }
            return new DamageBlockListPrompt(context);
        }
    }

    public class DamageBlockAmountsPrompt extends QuestsEditorStringPrompt {

        public DamageBlockAmountsPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new DamageBlockAmountsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amounts);
            }
            return new DamageBlockListPrompt(context);
        }
    }

    public class DamageBlockDurabilityPrompt extends QuestsEditorStringPrompt {

        public DamageBlockDurabilityPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new DamageBlockDurabilityPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durability);
            }
            return new DamageBlockListPrompt(context);
        }
    }

    public class PlaceBlockListPrompt extends QuestsEditorNumericPrompt {

        public PlaceBlockListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorPlaceBlocks");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockNames");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockDurability");
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
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) != null) {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_PLACE_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null) {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_PLACE_DURABILITY) != null) {
                    String text = "\n";
                    for (Short s : (List<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new PlaceBlockNamesPrompt(context);
            case 2:
                return new PlaceBlockAmountsPrompt(context);
            case 3:
                return new PlaceBlockDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_PLACE_NAMES, null);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, null);
                return new PlaceBlockListPrompt(context);
            case 5:
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
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new PlaceBlockListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class PlaceBlockNamesPrompt extends QuestsEditorStringPrompt {

        public PlaceBlockNamesPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                                return new PlaceBlockNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new PlaceBlockNamesPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_NAMES, names);
            }
            return new PlaceBlockListPrompt(context);
        }
    }

    public class PlaceBlockAmountsPrompt extends QuestsEditorStringPrompt {

        public PlaceBlockAmountsPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new PlaceBlockAmountsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amounts);
            }
            return new PlaceBlockListPrompt(context);
        }
    }

    public class PlaceBlockDurabilityPrompt extends QuestsEditorStringPrompt {

        public PlaceBlockDurabilityPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new PlaceBlockDurabilityPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durability);
            }
            return new PlaceBlockListPrompt(context);
        }
    }

    public class UseBlockListPrompt extends QuestsEditorNumericPrompt {

        public UseBlockListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorUseBlocks");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockNames");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockDurability");
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
                if (context.getSessionData(pref + CK.S_USE_NAMES) != null) {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_USE_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) != null) {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_USE_DURABILITY) != null) {
                    String text = "\n";
                    for (Short s : (List<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new UseBlockNamesPrompt(context);
            case 2:
                return new UseBlockAmountsPrompt(context);
            case 3:
                return new UseBlockDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_USE_NAMES, null);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_USE_DURABILITY, null);
                return new UseBlockListPrompt(context);
            case 5:
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
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new UseBlockListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class UseBlockNamesPrompt extends QuestsEditorStringPrompt {

        public UseBlockNamesPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                                return new UseBlockNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " " 
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new UseBlockNamesPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_USE_NAMES, names);
            }
            return new UseBlockListPrompt(context);
        }
    }

    public class UseBlockAmountsPrompt extends QuestsEditorStringPrompt {

        public UseBlockAmountsPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new UseBlockAmountsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amounts);
            }
            return new UseBlockListPrompt(context);
        }
    }

    public class UseBlockDurabilityPrompt extends QuestsEditorStringPrompt {

        public UseBlockDurabilityPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new UseBlockDurabilityPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_USE_DURABILITY, durability);
            }
            return new UseBlockListPrompt(context);
        }
    }

    public class CutBlockListPrompt extends QuestsEditorNumericPrompt {

        public CutBlockListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorCutBlocks");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockNames");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockAmounts");
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetBlockDurability");
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
                if (context.getSessionData(pref + CK.S_CUT_NAMES) != null) {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_CUT_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + ItemUtil.getPrettyItemName(s) + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_CUT_AMOUNTS) != null) {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_CUT_DURABILITY) != null) {
                    String text = "\n";
                    for (Short s : (List<Short>) context.getSessionData(pref + CK.S_CUT_DURABILITY)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                } else {
                    return "";
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.GOLD + "- " + getTitle(context) + " -\n";
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
                return new CutBlockNamesPrompt(context);
            case 2:
                return new CutBlockAmountsPrompt(context);
            case 3:
                return new CutBlockDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_CUT_NAMES, null);
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, null);
                context.setSessionData(pref + CK.S_CUT_DURABILITY, null);
                return new CutBlockListPrompt(context);
            case 5:
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
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new CutBlockListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class CutBlockNamesPrompt extends QuestsEditorStringPrompt {
        
        public CutBlockNamesPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                                return new CutBlockNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + ChatColor.RED + " "
                                    + Lang.get("stageEditorInvalidBlockName"));
                            return new CutBlockNamesPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_CUT_NAMES, names);
            }
            return new CutBlockListPrompt(context);
        }
    }

    public class CutBlockAmountsPrompt extends QuestsEditorStringPrompt {

        public CutBlockAmountsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new CutBlockAmountsPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, amounts);
            }
            return new CutBlockListPrompt(context);
        }
    }

    public class CutBlockDurabilityPrompt extends QuestsEditorStringPrompt {

        public CutBlockDurabilityPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new CutBlockDurabilityPrompt(context);
                        }
                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_CUT_DURABILITY, durability);
            }
            return new CutBlockListPrompt(context);
        }
    }
}
