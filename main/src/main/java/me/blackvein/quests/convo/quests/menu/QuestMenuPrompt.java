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

package me.blackvein.quests.convo.quests.menu;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.main.QuestMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class QuestMenuPrompt extends QuestsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public QuestMenuPrompt(final ConversationContext context) {
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
        return Lang.get("questEditorTitle");
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
            return ChatColor.YELLOW + Lang.get("questEditorCreate");
        case 2:
            return ChatColor.YELLOW + Lang.get("questEditorEdit");
        case 3:
            return ChatColor.YELLOW + Lang.get("questEditorDelete");
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
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        String text = ChatColor.GOLD + getTitle(context);
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final CommandSender cs = (CommandSender) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.create")) {
                return new QuestSelectCreatePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 2:
            if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.edit")) {
                return new QuestSelectEditPrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 3:
            if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.delete")) {
                return new QuestSelectDeletePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("exited"));
            return Prompt.END_OF_CONVERSATION;
        default:
            return new QuestMenuPrompt(context);
        }
    }
    
    public class QuestSelectCreatePrompt extends QuestsEditorStringPrompt {
        
        public QuestSelectCreatePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("questCreateTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questEditorEnterQuestName");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final String text = ChatColor.GOLD + getTitle(context)+ "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new QuestSelectCreatePrompt(context);
            }
            input = input.trim();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (final Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNameExists"));
                        return new QuestSelectCreatePrompt(context);
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new QuestSelectCreatePrompt(context);
                }
                context.setSessionData(CK.Q_NAME, input);
                context.setSessionData(CK.Q_ASK_MESSAGE, Lang.get("questEditorDefaultAskMessage"));
                context.setSessionData(CK.Q_FINISH_MESSAGE, Lang.get("questEditorDefaultFinishMessage"));
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
                return new QuestMainPrompt(context);
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    public class QuestSelectEditPrompt extends QuestsEditorStringPrompt {
        
        public QuestSelectEditPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("questEditorEdit");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questEditorEnterQuestName");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + getTitle(context);
            for (final Quest q : plugin.getQuests()) {
                text += "\n" + ChatColor.GRAY + "- " + ChatColor.AQUA + q.getName();
            }
            return text + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final Quest q = plugin.getQuest(input);
                if (q != null) {
                    plugin.getQuestFactory().loadQuest(context, q);
                    return new QuestMainPrompt(context);
                }
                return new QuestSelectEditPrompt(context);
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    public class QuestSelectDeletePrompt extends QuestsEditorStringPrompt {

        public QuestSelectDeletePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("questDeleteTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questEditorEnterQuestName");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + getTitle(context);
            for (final Quest quest : plugin.getQuests()) {
                text += ChatColor.AQUA + quest.getName() + ChatColor.GRAY + ",";
            }
            text = text.substring(0, text.length() - 1) + "\n";
            text += ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final LinkedList<String> used = new LinkedList<String>();
                final Quest found = plugin.getQuest(input);
                if (found != null) {
                    for (final Quest q : plugin.getQuests()) {
                        if (q.getRequirements().getNeededQuests().contains(q) 
                                || q.getRequirements().getBlockQuests().contains(q)) {
                            used.add(q.getName());
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_QUEST_DELETE, found.getName());
                        return new QuestConfirmDeletePrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + Lang.get("questEditorQuestAsRequirement1") + " \"" + ChatColor.DARK_PURPLE 
                                + context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + "\" " 
                                + Lang.get("questEditorQuestAsRequirement2"));
                        for (final String s : used) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + Lang.get("questEditorQuestAsRequirement3"));
                        return new QuestSelectDeletePrompt(context);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorQuestNotFound"));
                return new QuestSelectDeletePrompt(context);
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    public class QuestConfirmDeletePrompt extends QuestsEditorStringPrompt {
        
        public QuestConfirmDeletePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("confirmDelete");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
                    + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
                    + Lang.get("noWord");
            return ChatColor.RED + getQueryText(context) + " (" + ChatColor.YELLOW 
                    + (String) context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                plugin.getQuestFactory().deleteQuest(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new QuestMenuPrompt(context);
            } else {
                return new QuestConfirmDeletePrompt(context);
            }
        }
    }
}
