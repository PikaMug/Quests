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

import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BlocksPrompt extends QuestsEditorNumericPrompt {
    private final int stageNum;
    private final String pref;

    public BlocksPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 5;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("stageEditorBlocks");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatColor.BLUE;
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
            return ChatColor.YELLOW + Lang.get("stageEditorBreakBlocks");
        case 2:
            return ChatColor.YELLOW + Lang.get("stageEditorDamageBlocks");
        case 3:
            return ChatColor.YELLOW + Lang.get("stageEditorPlaceBlocks");
        case 4:
            return ChatColor.YELLOW + Lang.get("stageEditorUseBlocks");
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
            if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(ItemUtil.getPrettyItemName(names.get(i))).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 2:
            if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(ItemUtil.getPrettyItemName(names.get(i))).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 3:
            if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(ItemUtil.getPrettyItemName(names.get(i))).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 4:
            if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_USE_NAMES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
                if (names != null && amounts != null) {
                    for (int i = 0; i < names.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(ItemUtil.getPrettyItemName(names.get(i))).append(ChatColor.GRAY).append(" x ")
                                .append(ChatColor.DARK_AQUA).append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 5:
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
            return new BlocksBreakListPrompt(context);
        case 2:
            return new BlocksDamageListPrompt(context);
        case 3:
            return new BlocksPlaceListPrompt(context);
        case 4:
            return new BlocksUseListPrompt(context);
        case 5:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new BlocksPrompt(stageNum, context);
        }
    }
    
    public class BlocksBreakListPrompt extends QuestsEditorNumericPrompt {

        public BlocksBreakListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorBreakBlocks");
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> breakNames = (List<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
                    if (breakNames != null) {
                        for (final String s : breakNames) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(ItemUtil.getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> breakAmounts
                            = (List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
                    if (breakAmounts != null) {
                        for (final Integer i : breakAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_BREAK_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> breakDurability
                            = (List<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY);
                    if (breakDurability != null) {
                        for (final Short s : breakDurability) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }


            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
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
                return new BlockBreakNamesPrompt(context);
            case 2:
                return new BlockBreakAmountsPrompt(context);
            case 3:
                return new BlockBreakDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_BREAK_NAMES, null);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, null);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, null);
                return new BlocksBreakListPrompt(context);
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) context.getSessionData(pref + CK.S_BREAK_NAMES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_BREAK_DURABILITY, durability);
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new BlocksBreakListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class BlockBreakNamesPrompt extends QuestsEditorStringPrompt {

        public BlockBreakNamesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(m.name());
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotSolid")
                                        .replace("<input>", s));
                                return new BlockBreakNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorInvalidBlockName")
                                    .replace("<input>", s));
                            return new BlockBreakNamesPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockBreakNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_NAMES, names);
                
                LinkedList<Integer> amounts = new LinkedList<>();
                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amounts);
            }
            return new BlocksBreakListPrompt(context);
        }
    }

    public class BlockBreakAmountsPrompt extends QuestsEditorStringPrompt {

        public BlockBreakAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new BlockBreakAmountsPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockBreakAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amounts);
            }
            return new BlocksBreakListPrompt(context);
        }
    }

    public class BlockBreakDurabilityPrompt extends QuestsEditorStringPrompt {

        public BlockBreakDurabilityPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new BlockBreakDurabilityPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockBreakDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durability);
            }
            return new BlocksBreakListPrompt(context);
        }
    }

    public class BlocksDamageListPrompt extends QuestsEditorNumericPrompt {
        
        public BlocksDamageListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorDamageBlocks");
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> damageNames = (List<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
                    if (damageNames != null) {
                        for (final String s : damageNames) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(ItemUtil.getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> damageAmounts
                            = (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
                    if (damageAmounts != null) {
                        for (final Integer i : damageAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> damageDurability
                            = (List<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY);
                    if (damageDurability != null) {
                        for (final Short s : damageDurability) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
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
                return new BlockDamageNamesPrompt(context);
            case 2:
                return new BlockDamageAmountsPrompt(context);
            case 3:
                return new BlockDamageDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, null);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, null);
                return new BlocksDamageListPrompt(context);
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durability);
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new BlocksDamageListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class BlockDamageNamesPrompt extends QuestsEditorStringPrompt {

        public BlockDamageNamesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(m.name());
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotSolid")
                                        .replace("<input>", s));
                                return new BlockDamageNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorInvalidBlockName")
                                    .replace("<input>", s));
                            return new BlockDamageNamesPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockDamageNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
                
                LinkedList<Integer> amounts = new LinkedList<>();
                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amounts);
            }
            return new BlocksDamageListPrompt(context);
        }
    }

    public class BlockDamageAmountsPrompt extends QuestsEditorStringPrompt {

        public BlockDamageAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new BlockDamageAmountsPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockDamageAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amounts);
            }
            return new BlocksDamageListPrompt(context);
        }
    }

    public class BlockDamageDurabilityPrompt extends QuestsEditorStringPrompt {

        public BlockDamageDurabilityPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new BlockDamageDurabilityPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockDamageDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durability);
            }
            return new BlocksDamageListPrompt(context);
        }
    }

    public class BlocksPlaceListPrompt extends QuestsEditorNumericPrompt {

        public BlocksPlaceListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorPlaceBlocks");
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> placeNames  = (List<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
                    if (placeNames != null) {
                        for (final String s : placeNames) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(ItemUtil.getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> placeAmounts
                            = (List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
                    if (placeAmounts != null) {
                        for (final Integer i : placeAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_PLACE_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> placeDurability
                            = (List<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY);
                    if (placeDurability != null) {
                        for (final Short s : placeDurability) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
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
                return new BlockPlaceNamesPrompt(context);
            case 2:
                return new BlockPlaceAmountsPrompt(context);
            case 3:
                return new BlockPlaceDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_PLACE_NAMES, null);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, null);
                return new BlocksPlaceListPrompt(context);
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) context.getSessionData(pref + CK.S_PLACE_NAMES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_PLACE_DURABILITY, durability);
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new BlocksPlaceListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class BlockPlaceNamesPrompt extends QuestsEditorStringPrompt {

        public BlockPlaceNamesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(m.name());
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotSolid")
                                        .replace("<input>", s));
                                return new BlockPlaceNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorInvalidBlockName")
                                    .replace("<input>", s));
                            return new BlockPlaceNamesPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockPlaceNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_NAMES, names);
                
                LinkedList<Integer> amounts = new LinkedList<>();
                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amounts);
            }
            return new BlocksPlaceListPrompt(context);
        }
    }

    public class BlockPlaceAmountsPrompt extends QuestsEditorStringPrompt {

        public BlockPlaceAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new BlockPlaceAmountsPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockPlaceAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amounts);
            }
            return new BlocksPlaceListPrompt(context);
        }
    }

    public class BlockPlaceDurabilityPrompt extends QuestsEditorStringPrompt {

        public BlockPlaceDurabilityPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new BlockPlaceDurabilityPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockPlaceDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durability);
            }
            return new BlocksPlaceListPrompt(context);
        }
    }

    public class BlocksUseListPrompt extends QuestsEditorNumericPrompt {

        public BlocksUseListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorUseBlocks");
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
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_USE_NAMES) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<String> useNames = (List<String>) context.getSessionData(pref + CK.S_USE_NAMES);
                    if (useNames != null) {
                        for (final String s : useNames) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(ItemUtil.getPrettyItemName(s));
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 2:
                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> useAmounts = (List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
                    if (useAmounts != null) {
                        for (final Integer i : useAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                } else {
                    return "";
                }
            case 3:
                if (context.getSessionData(pref + CK.S_USE_DURABILITY) != null) {
                    final StringBuilder text = new StringBuilder();
                    final List<Short> useDurability = (List<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY);
                    if (useDurability != null) {
                        for (final Short s : useDurability) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
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
                return new BlockUseNamesPrompt(context);
            case 2:
                return new BlockUseAmountsPrompt(context);
            case 3:
                return new BlockUseDurabilityPrompt(context);
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_USE_NAMES, null);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_USE_DURABILITY, null);
                return new BlocksUseListPrompt(context);
            case 5:
                final int one;
                final int two;
                final List<Integer> names = (List<Integer>) context.getSessionData(pref + CK.S_USE_NAMES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
                if (names != null) {
                    one = names.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    final int missing;
                    LinkedList<Short> durability
                            = (LinkedList<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY);
                    if (durability != null) {
                        missing = one - durability.size();
                    } else {
                        missing = one;
                        durability = new LinkedList<>();
                    }
                    for (int i = 0; i < missing; i++) {
                        durability.add((short) 0);
                    }
                    context.setSessionData(pref + CK.S_USE_DURABILITY, durability);
                    return new BlocksPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new BlocksUseListPrompt(context);
                }
            default:
                return new BlocksPrompt(stageNum, context);
            }
        }
    }

    public class BlockUseNamesPrompt extends QuestsEditorStringPrompt {

        public BlockUseNamesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<String> names = new LinkedList<>();
                for (final String s : args) {
                    try {
                        final Material m = Material.matchMaterial(s);
                        if (m != null) {
                            if (m.isBlock()) {
                                names.add(m.name());
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotSolid")
                                        .replace("<input>", s));
                                return new BlockUseNamesPrompt(context);
                            }
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorInvalidBlockName")
                                    .replace("<input>", s));
                            return new BlockUseNamesPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockUseNamesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_USE_NAMES, names);
                
                LinkedList<Integer> amounts = new LinkedList<>();
                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
                }
                for (int i = 0; i < names.size(); i++) {
                    if (amounts != null) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amounts);
            }
            return new BlocksUseListPrompt(context);
        }
    }

    public class BlockUseAmountsPrompt extends QuestsEditorStringPrompt {

        public BlockUseAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockAmounts");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new BlockUseAmountsPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockUseAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amounts);
            }
            return new BlocksUseListPrompt(context);
        }
    }

    public class BlockUseDurabilityPrompt extends QuestsEditorStringPrompt {

        public BlockUseDurabilityPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorEnterBlockDurability");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final String[] args = input.split(" ");
                final LinkedList<Short> durability = new LinkedList<>();
                for (final String s : args) {
                    try {
                        if (Short.parseShort(s) >= 0) {
                            durability.add(Short.parseShort(s));
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED 
                                    + Lang.get("invalidMinimum").replace("<number>", "0"));
                            return new BlockUseDurabilityPrompt(context);
                        }
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNotListOfNumbers")
                                .replace("<data>", s));
                        return new BlockUseDurabilityPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_USE_DURABILITY, durability);
            }
            return new BlocksUseListPrompt(context);
        }
    }
}
