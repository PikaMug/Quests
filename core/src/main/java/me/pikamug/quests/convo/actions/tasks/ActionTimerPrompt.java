/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.tasks;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.actions.ActionsEditorNumericPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ActionTimerPrompt extends ActionsEditorNumericPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ActionTimerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("eventEditorTimer");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
            return ChatColor.BLUE;
        case 3:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetTimer");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorCancelTimer");
        case 3:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, Key.A_TIMER) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer timer = (Integer)SessionData.get(uuid, Key.A_TIMER);
                if (timer != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitMiscUtil.getTime(timer * 1000L) + ChatColor.GRAY
                            + ")";
                }
            }
        case 2:
            if (SessionData.get(uuid, Key.A_CANCEL_TIMER) == null) {
                return ChatColor.GRAY + "(" + ChatColor.RED + BukkitLang.get("false") + ChatColor.GRAY + ")";
            } else {
                final Boolean timerOpt = (Boolean) SessionData.get(uuid, Key.A_CANCEL_TIMER);
                return ChatColor.GRAY + "(" + (Boolean.TRUE.equals(timerOpt) ? ChatColor.GREEN + BukkitLang.get("true")
                        : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
            }
        case 3:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        if (SessionData.get(uuid, Key.A_CANCEL_TIMER) == null) {
            SessionData.set(uuid, Key.A_CANCEL_TIMER, false);
        }
        
        final BukkitActionsEditorPostOpenNumericPromptEvent event
                = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new ActionTimerFailPrompt(uuid).start();
        case 2:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_CANCEL_TIMER);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.A_CANCEL_TIMER, false);
            } else {
                SessionData.set(uuid, Key.A_CANCEL_TIMER, true);
            }
            new ActionTimerPrompt(uuid).start();
        case 3:
            new ActionMainPrompt(uuid).start();
        default:
            new ActionTimerPrompt(uuid).start();
        }
    }
    
    public class ActionTimerFailPrompt extends ActionsEditorStringPrompt {
        
        public ActionTimerFailPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterTimerSeconds");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorPositiveAmount"));
                } else {
                    SessionData.set(uuid, Key.A_TIMER, i);
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED
                        + BukkitLang.get("reqNotANumber").replace("<input>", input));
                new ActionTimerFailPrompt(uuid).start();
            }
            new ActionTimerPrompt(uuid).start();
        }
    }
}
