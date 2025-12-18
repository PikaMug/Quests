/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.menu;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.main.QuestMainPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestMenuPrompt extends QuestsEditorNumericPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public QuestMenuPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }

    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        final String title = BukkitLang.get("questEditorTitle");
        return title + (plugin.hasLimitedAccess(uuid) ? ChatColor.RED + " (" + BukkitLang.get("trialMode")
                + ")" : "");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("questEditorCreate");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("questEditorEdit");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("questEditorDelete");
        case 4:
            return ChatColor.RED + BukkitLang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final int number) {
        return null;
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = Bukkit.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            if (sender.hasPermission("quests.editor.*") || sender.hasPermission("quests.editor.create")) {
                new QuestSelectCreatePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new QuestMenuPrompt(uuid).start();
            }
        case 2:
            if (sender.hasPermission("quests.editor.*") || sender.hasPermission("quests.editor.edit")) {
                new QuestSelectEditPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new QuestMenuPrompt(uuid).start();
            }
        case 3:
            if (sender.hasPermission("quests.editor.*") || sender.hasPermission("quests.editor.delete")) {
                new QuestSelectDeletePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new QuestMenuPrompt(uuid).start();
            }
        case 4:
            sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("exited"));
            return;
        default:
            new QuestMenuPrompt(uuid).start();
        }
    }
    
    public class QuestSelectCreatePrompt extends QuestsEditorStringPrompt {
        
        public QuestSelectCreatePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("questCreateTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.GOLD + getTitle()+ "\n" + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(String input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (input == null) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                new QuestSelectCreatePrompt(uuid).start();
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorNameExists"));
                        new QuestSelectCreatePrompt(uuid).start();
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    new QuestSelectCreatePrompt(uuid).start();
                }
                if (input.contains(".") || input.contains(",")) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    new QuestSelectCreatePrompt(uuid).start();
                }
                if (input.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestSelectCreatePrompt(uuid).start();
                }
                SessionData.set(uuid, Key.Q_NAME, input);
                SessionData.set(uuid, Key.Q_ASK_MESSAGE, BukkitLang.get("questEditorDefaultAskMessage"));
                SessionData.set(uuid, Key.Q_FINISH_MESSAGE, BukkitLang.get("questEditorDefaultFinishMessage"));
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
                new QuestMainPrompt(uuid).start();
            } else {
                new QuestMenuPrompt(uuid).start();
            }
        }
    }
    
    public class QuestSelectEditPrompt extends QuestsEditorStringPrompt {
        
        public QuestSelectEditPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("questEditTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Quest q = plugin.getQuest(input);
                if (q != null) {
                    plugin.getQuestFactory().loadQuest(uuid, q);
                    new QuestMainPrompt(uuid).start();
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("questNotFound")
                        .replace("<input>", input));
                new QuestSelectEditPrompt(uuid).start();
            } else {
                new QuestMenuPrompt(uuid).start();
            }
        }
    }
    
    public class QuestSelectDeletePrompt extends QuestsEditorStringPrompt {

        public QuestSelectDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("questDeleteTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedQuests().stream().map(Quest::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
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
                        SessionData.set(uuid, Key.ED_QUEST_DELETE, found.getName());
                        new QuestConfirmDeletePrompt(uuid).start();
                    } else {
                        sender.sendMessage(ChatColor.RED 
                                + BukkitLang.get("questEditorQuestAsRequirement1") + " \"" + ChatColor.DARK_PURPLE
                                + SessionData.get(uuid, Key.ED_QUEST_DELETE) + ChatColor.RED + "\" "
                                + BukkitLang.get("questEditorQuestAsRequirement2"));
                        for (final String s : used) {
                            sender.sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        sender.sendMessage(ChatColor.RED 
                                + BukkitLang.get("questEditorQuestAsRequirement3"));
                        new QuestSelectDeletePrompt(uuid).start();
                    }
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("questNotFound")
                        .replace("<input>", input));
                new QuestSelectDeletePrompt(uuid).start();
            } else {
                new QuestMenuPrompt(uuid).start();
            }
        }
    }
    
    public class QuestConfirmDeletePrompt extends QuestsEditorStringPrompt {
        
        public QuestConfirmDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.RED + getQueryText() + " (" + ChatColor.YELLOW
                    + SessionData.get(uuid, Key.ED_QUEST_DELETE) + ChatColor.RED + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                plugin.getQuestFactory().deleteQuest(uuid);
                return;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new QuestMenuPrompt(uuid).start();
            } else {
                new QuestConfirmDeletePrompt(uuid).start();
            }
        }
    }
}
