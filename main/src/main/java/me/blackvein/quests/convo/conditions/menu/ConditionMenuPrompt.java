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

package me.blackvein.quests.convo.conditions.menu;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.blackvein.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class ConditionMenuPrompt extends ConditionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public ConditionMenuPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("conditionEditorTitle");
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
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("conditionEditorCreate");
        case 2:
            return ChatColor.YELLOW + Lang.get("conditionEditorEdit");
        case 3:
            return ChatColor.YELLOW + Lang.get("conditionEditorDelete");
        case 4:
            return ChatColor.RED + Lang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final ConditionsEditorPostOpenNumericPromptEvent event = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        String text = ChatColor.GOLD + getTitle(context) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final Player player = (Player) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (player.hasPermission("quests.conditions.create")) {
                context.setSessionData(CK.C_OLD_CONDITION, "");
                return new ConditionSelectCreatePrompt(context);
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 2:
            if (player.hasPermission("quests.conditions.edit")) {
                if (plugin.getConditions().isEmpty()) {
                    ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW 
                            + Lang.get("conditionEditorNoneToEdit"));
                    return new ConditionMenuPrompt(context);
                } else {
                    return new ConditionSelectEditPrompt();
                }
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 3:
            if (player.hasPermission("quests.conditions.delete")) {
                if (plugin.getConditions().isEmpty()) {
                    ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW 
                            + Lang.get("conditionEditorNoneToDelete"));
                    return new ConditionMenuPrompt(context);
                } else {
                    return new ConditionSelectDeletePrompt();
                }
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new ConditionMenuPrompt(context);
            }
        case 4:
            ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("exited"));
            return Prompt.END_OF_CONVERSATION;
        default:
            return new ConditionMenuPrompt(context);
        }
    }
    
    public class ConditionSelectCreatePrompt extends ConditionsEditorStringPrompt {
        public ConditionSelectCreatePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorCreate");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorEnterName");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String text = ChatColor.GOLD + getTitle(context) + "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new ConditionSelectCreatePrompt(context);
            }
            input = input.trim();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (final Condition c : plugin.getConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("conditionEditorExists"));
                        return new ConditionSelectCreatePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new ConditionSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new ConditionSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new ConditionSelectCreatePrompt(context);
                }
                context.setSessionData(CK.C_NAME, input);
                actionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(actionNames);
                return new ConditionMainPrompt(context);
            } else {
                return new ConditionMenuPrompt(context);
            }
        }
    }

    private class ConditionSelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("conditionEditorEdit") + " -\n";
            for (final Condition a : plugin.getConditions()) {
                text += ChatColor.AQUA + a.getName() + ChatColor.GRAY + ", ";
            }
            text = text.substring(0, text.length() - 2) + "\n";
            text += ChatColor.YELLOW + Lang.get("conditionEditorEnterName");
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    context.setSessionData(CK.C_OLD_CONDITION, c.getName());
                    context.setSessionData(CK.C_NAME, c.getName());
                    plugin.getConditionFactory().loadData(c, context);
                    return new ConditionMainPrompt(context);
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("conditionEditorNotFound"));
                return new ConditionSelectEditPrompt();
            } else {
                return new ConditionMenuPrompt(context);
            }
        }
    }
    
    private class ConditionSelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("conditionEditorDelete") + " -\n";
            for (final Condition c : plugin.getConditions()) {
                text += ChatColor.AQUA + c.getName() + ChatColor.GRAY + ",";
            }
            text = text.substring(0, text.length() - 1) + "\n";
            text += ChatColor.YELLOW + Lang.get("conditionEditorEnterName");
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final LinkedList<String> used = new LinkedList<String>();
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    for (final Quest quest : plugin.getQuests()) {
                        for (final Stage stage : quest.getStages()) {
                            if (stage.getCondition() != null 
                                    && stage.getCondition().getName().equalsIgnoreCase(c.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_CONDITION_DELETE, c.getName());
                        return new ConditionConfirmDeletePrompt();
                    } else {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("conditionEditorInUse") 
                        + " \"" + ChatColor.DARK_PURPLE + c.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED 
                                + Lang.get("eventEditorMustModifyQuests"));
                        return new ConditionSelectDeletePrompt();
                    }
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("conditionEditorNotFound"));
                return new ConditionSelectDeletePrompt();
            } else {
                return new ConditionMenuPrompt(context);
            }
        }
    }

    private class ConditionConfirmDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
        + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
        + Lang.get("noWord");
            return ChatColor.RED + Lang.get("confirmDelete") + " (" + ChatColor.YELLOW 
                    + (String) context.getSessionData(CK.ED_CONDITION_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                plugin.getConditionFactory().deleteCondition(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ConditionMenuPrompt(context);
            } else {
                return new ConditionConfirmDeletePrompt();
            }
        }
    }
}
