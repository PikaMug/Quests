/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.menu;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
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

public class ActionMenuPrompt extends ActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ActionMenuPrompt(final @NotNull UUID uuid) {
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
        final String title = BukkitLang.get("eventEditorTitle");
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
            return ChatColor.YELLOW + BukkitLang.get("eventEditorCreate");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorEdit");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorDelete");
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
        final BukkitActionsEditorPostOpenNumericPromptEvent event
                = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            if (sender.hasPermission("quests.editor.actions.create")
                    || sender.hasPermission("quests.editor.events.create")) {
                SessionData.set(uuid, Key.A_OLD_ACTION, "");
                new ActionSelectCreatePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ActionMenuPrompt(uuid).start();
            }
            break;
        case 2:
            if (sender.hasPermission("quests.editor.actions.edit")
                    || sender.hasPermission("quests.editor.events.edit")) {
                if (plugin.getLoadedActions().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW 
                            + BukkitLang.get("eventEditorNoneToEdit"));
                    new ActionMenuPrompt(uuid).start();
                } else {
                    new ActionSelectEditPrompt(uuid).start();
                }
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ActionMenuPrompt(uuid).start();
            }
            break;
        case 3:
            if (sender.hasPermission("quests.editor.actions.delete")
                    || sender.hasPermission("quests.editor.events.delete")) {
                if (plugin.getLoadedActions().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW 
                            + BukkitLang.get("eventEditorNoneToDelete"));
                    new ActionMenuPrompt(uuid).start();
                } else {
                    new ActionSelectDeletePrompt(uuid).start();
                }
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ActionMenuPrompt(uuid).start();
            }
            break;
        case 4:
            sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("exited"));
            return;
        default:
            new ActionMenuPrompt(uuid).start();
            break;
        }
    }

    public class ActionSelectCreatePrompt extends ActionsEditorStringPrompt {
        public ActionSelectCreatePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("eventCreateTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.GOLD + getTitle() + "\n" + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                new ActionSelectCreatePrompt(uuid).start();
                return;
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Action action : plugin.getLoadedActions()) {
                    if (action.getName().equalsIgnoreCase(input)) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorExists"));
                        new ActionSelectCreatePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    new ActionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.contains(".") || input.contains(",")) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    new ActionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new ActionSelectCreatePrompt(uuid).start();
                    return;
                }
                SessionData.set(uuid, Key.A_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionMenuPrompt(uuid).start();
            }
        }
    }

    public class ActionSelectEditPrompt extends ActionsEditorStringPrompt {
        
        public ActionSelectEditPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedActions().stream().map(Action::getName).collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Action action = plugin.getAction(input);
                if (action != null) {
                    SessionData.set(uuid, Key.A_OLD_ACTION, action.getName());
                    SessionData.set(uuid, Key.A_NAME, action.getName());
                    plugin.getActionFactory().loadData(uuid, action);
                    new ActionMainPrompt(uuid).start();
                    return;
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorNotFound").replace("<input>", input));
                new ActionSelectEditPrompt(uuid).start();
            } else {
                new ActionMenuPrompt(uuid).start();
            }
        }
    }
    
    public class ActionSelectDeletePrompt extends ActionsEditorStringPrompt {

        public ActionSelectDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventDeleteTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterEventName");
        }
        
        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedActions().stream().map(Action::getName).collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> used = new LinkedList<>();
                final Action action = plugin.getAction(input);
                if (action != null) {
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        for (final Stage stage : quest.getStages()) {
                            if (stage.getFinishAction() != null 
                                    && stage.getFinishAction().getName().equalsIgnoreCase(action.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        SessionData.set(uuid, Key.ED_EVENT_DELETE, action.getName());
                        new ActionConfirmDeletePrompt(uuid).start();
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorEventInUse")
                        + " \"" + ChatColor.DARK_PURPLE + action.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            sender.sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        sender.sendMessage(ChatColor.RED 
                                + BukkitLang.get("eventEditorMustModifyQuests"));
                        new ActionSelectDeletePrompt(uuid).start();
                    }
                    return;
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorNotFound")
                        .replace("<input>", input));
                new ActionSelectDeletePrompt(uuid).start();
            } else {
                new ActionMenuPrompt(uuid).start();
            }
        }
    }

    public class ActionConfirmDeletePrompt extends ActionsEditorStringPrompt {
        
        public ActionConfirmDeletePrompt(final @NotNull UUID uuid) {
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
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.RED + getQueryText() + " (" + ChatColor.YELLOW
                    + SessionData.get(uuid, Key.ED_EVENT_DELETE) + ChatColor.RED + ")\n");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return QuestsIntegerPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                plugin.getActionFactory().deleteAction(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new ActionMenuPrompt(uuid).start();
            } else {
                new ActionConfirmDeletePrompt(uuid).start();
            }
        }
    }
}
