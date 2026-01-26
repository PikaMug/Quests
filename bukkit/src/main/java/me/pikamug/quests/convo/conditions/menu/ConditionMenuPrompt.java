/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.menu;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenStringPromptEvent;
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

public class ConditionMenuPrompt extends ConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ConditionMenuPrompt(final @NotNull UUID uuid) {
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
        final String title = BukkitLang.get("conditionEditorTitle");
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
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorCreate");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorEdit");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorDelete");
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
        final BukkitConditionsEditorPostOpenNumericPromptEvent event
                = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
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
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            if (sender.hasPermission("quests.conditions.create")) {
                SessionData.set(uuid, Key.C_OLD_CONDITION, "");
                new ConditionSelectCreatePrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ConditionMenuPrompt(uuid).start();
            }
            break;
        case 2:
            if (sender.hasPermission("quests.conditions.edit")) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW 
                            + BukkitLang.get("conditionEditorNoneToEdit"));
                    new ConditionMenuPrompt(uuid).start();
                } else {
                    new ConditionSelectEditPrompt(uuid).start();
                }
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ConditionMenuPrompt(uuid).start();
            }
            break;
        case 3:
            if (sender.hasPermission("quests.conditions.delete")) {
                if (plugin.getLoadedConditions().isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW 
                            + BukkitLang.get("conditionEditorNoneToDelete"));
                    new ConditionMenuPrompt(uuid).start();
                } else {
                    new ConditionSelectDeletePrompt(uuid).start();
                }
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ConditionMenuPrompt(uuid).start();
            }
            break;
        case 4:
            sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("exited"));
            break;
        default:
            new ConditionMenuPrompt(uuid).start();
            break;
        }
    }
    
    public class ConditionSelectCreatePrompt extends ConditionsEditorStringPrompt {
        
        public ConditionSelectCreatePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionCreateTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.GOLD + getTitle() + "\n" + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                new ConditionSelectCreatePrompt(uuid).start();
                return;
            }
            input = input.trim();
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Condition c : plugin.getLoadedConditions()) {
                    if (c.getName().equalsIgnoreCase(input)) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorExists"));
                        new ConditionSelectCreatePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> actionNames = plugin.getConditionFactory().getNamesOfConditionsBeingEdited();
                if (actionNames.contains(input)) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    new ConditionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.contains(".") || input.contains(",")) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    new ConditionSelectCreatePrompt(uuid).start();
                    return;
                }
                if (input.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new ConditionSelectCreatePrompt(uuid).start();
                    return;
                }
                SessionData.set(uuid, Key.C_NAME, input);
                actionNames.add(input);
                plugin.getConditionFactory().setNamesOfConditionsBeingEdited(actionNames);
                new ConditionMainPrompt(uuid).start();
            } else {
                new ConditionMenuPrompt(uuid).start();
            }
        }
    }

    public class ConditionSelectEditPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionSelectEditPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedConditions().stream().map(Condition::getName)
                    .collect(Collectors.toList());
            return sendClickableMenu(getTitle(), names, getQueryText(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    SessionData.set(uuid, Key.C_OLD_CONDITION, c.getName());
                    SessionData.set(uuid, Key.C_NAME, c.getName());
                    plugin.getConditionFactory().loadData(uuid, c);
                    new ConditionMainPrompt(uuid).start();
                    return;
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorNotFound")
                        .replace("<input>", input));
                new ConditionSelectEditPrompt(uuid).start();
            } else {
                new ConditionMenuPrompt(uuid).start();
            }
        }
    }
    
    public class ConditionSelectDeletePrompt extends ConditionsEditorStringPrompt {

        public ConditionSelectDeletePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("conditionDeleteTitle");
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEnterName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            final List<String> names = plugin.getLoadedConditions().stream().map(Condition::getName)
                    .collect(Collectors.toList());
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
                final Condition c = plugin.getCondition(input);
                if (c != null) {
                    for (final Quest quest : plugin.getLoadedQuests()) {
                        for (final Stage stage : quest.getStages()) {
                            if (stage.getCondition() != null 
                                    && stage.getCondition().getName().equalsIgnoreCase(c.getName())) {
                                used.add(quest.getName());
                                break;
                            }
                        }
                    }
                    if (used.isEmpty()) {
                        SessionData.set(uuid, Key.ED_CONDITION_DELETE, c.getName());
                        new ConditionConfirmDeletePrompt(uuid).start();
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorInUse")
                        + " \"" + ChatColor.DARK_PURPLE + c.getName() + ChatColor.RED + "\":");
                        for (final String s : used) {
                            sender.sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        sender.sendMessage(ChatColor.RED 
                                + BukkitLang.get("eventEditorMustModifyQuests"));
                        new ConditionSelectDeletePrompt(uuid).start();
                    }
                    return;
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("conditionEditorNotFound")
                        .replace("<input>", input));
                new ConditionSelectDeletePrompt(uuid).start();
            } else {
                new ConditionMenuPrompt(uuid).start();
            }
        }
    }

    public class ConditionConfirmDeletePrompt extends ConditionsEditorStringPrompt {
        
        public ConditionConfirmDeletePrompt(final @NotNull UUID uuid) {
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
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.RED + getQueryText() + " (" + ChatColor.YELLOW
                    + SessionData.get(uuid, Key.ED_CONDITION_DELETE) + ChatColor.RED + ")\n");
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
                plugin.getConditionFactory().deleteCondition(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new ConditionMenuPrompt(uuid).start();
            } else {
                new ConditionConfirmDeletePrompt(uuid).start();
            }
        }
    }
}
