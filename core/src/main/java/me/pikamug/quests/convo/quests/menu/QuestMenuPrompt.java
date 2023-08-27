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

package me.pikamug.quests.convo.quests.menu;

import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.main.QuestMainPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLanguage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestMenuPrompt extends QuestsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public QuestMenuPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        final String title = BukkitLanguage.get("questEditorTitle");
        return title + (plugin.hasLimitedAccess(context.getForWhom()) ? ChatColor.RED + " (" + BukkitLanguage.get("trialMode")
                + ")" : "");
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
            return ChatColor.YELLOW + BukkitLanguage.get("questEditorCreate");
        case 2:
            return ChatColor.YELLOW + BukkitLanguage.get("questEditorEdit");
        case 3:
            return ChatColor.YELLOW + BukkitLanguage.get("questEditorDelete");
        case 4:
            return ChatColor.RED + BukkitLanguage.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event 
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final CommandSender cs = (CommandSender) context.getForWhom();
        switch (input.intValue()) {
        case 1:
            if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.create")) {
                return new QuestSelectCreatePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 2:
            if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.edit")) {
                return new QuestSelectEditPrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 3:
            if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.delete")) {
                return new QuestSelectDeletePrompt(context);
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLanguage.get("noPermission"));
                return new QuestMenuPrompt(context);
            }
        case 4:
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLanguage.get("exited"));
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
            return BukkitLanguage.get("questCreateTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterQuestName");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.GOLD + getTitle(context)+ "\n" + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("itemCreateInvalidInput"));
                return new QuestSelectCreatePrompt(context);
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorNameExists"));
                        return new QuestSelectCreatePrompt(context);
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorBeingEdited"));
                    return new QuestSelectCreatePrompt(context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorInvalidQuestName"));
                    return new QuestSelectCreatePrompt(context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("itemCreateInvalidInput"));
                    return new QuestSelectCreatePrompt(context);
                }
                context.setSessionData(Key.Q_NAME, input);
                context.setSessionData(Key.Q_ASK_MESSAGE, BukkitLanguage.get("questEditorDefaultAskMessage"));
                context.setSessionData(Key.Q_FINISH_MESSAGE, BukkitLanguage.get("questEditorDefaultFinishMessage"));
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
            return BukkitLanguage.get("questEditTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                final Quest q = plugin.getQuest(input);
                if (q != null) {
                    plugin.getQuestFactory().loadQuest(context, q);
                    return new QuestMainPrompt(context);
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorQuestNotFound"));
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
            return BukkitLanguage.get("questDeleteTitle");
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterQuestName");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(context), names, getQueryText(context), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                final LinkedList<String> used = new LinkedList<>();
                final Quest found = plugin.getQuest(input);
                if (found != null) {
                    for (final Quest q : plugin.getLoadedQuests()) {
                        if (q.getRequirements().getNeededQuestIds().contains(q.getId())
                                || q.getRequirements().getBlockQuestIds().contains(q.getId())) {
                            used.add(q.getName());
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(Key.ED_QUEST_DELETE, found.getName());
                        return new QuestConfirmDeletePrompt(context);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + BukkitLanguage.get("questEditorQuestAsRequirement1") + " \"" + ChatColor.DARK_PURPLE
                                + context.getSessionData(Key.ED_QUEST_DELETE) + ChatColor.RED + "\" "
                                + BukkitLanguage.get("questEditorQuestAsRequirement2"));
                        for (final String s : used) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        context.getForWhom().sendRawMessage(ChatColor.RED 
                                + BukkitLanguage.get("questEditorQuestAsRequirement3"));
                        return new QuestSelectDeletePrompt(context);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorQuestNotFound"));
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
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLanguage.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLanguage.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("confirmDelete");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.RED + getQueryText(context) + " (" + ChatColor.YELLOW
                    + context.getSessionData(Key.ED_QUEST_DELETE) + ChatColor.RED + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLanguage.get("yesWord"))) {
                plugin.getQuestFactory().deleteQuest(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLanguage.get("noWord"))) {
                return new QuestMenuPrompt(context);
            } else {
                return new QuestConfirmDeletePrompt(context);
            }
        }
    }
}
