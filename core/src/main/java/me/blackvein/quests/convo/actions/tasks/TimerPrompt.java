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

package me.blackvein.quests.convo.actions.tasks;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.ActionsEditorNumericPrompt;
import me.blackvein.quests.convo.actions.ActionsEditorStringPrompt;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.jetbrains.annotations.NotNull;

public class TimerPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public TimerPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("eventEditorTimer");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("eventEditorSetTimer");
        case 2:
            return ChatColor.YELLOW + Lang.get("eventEditorCancelTimer");
        case 3:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(CK.E_TIMER) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer timer = (Integer)context.getSessionData(CK.E_TIMER);
                if (timer != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + MiscUtil.getTime(timer * 1000L) + ChatColor.GRAY
                            + ")";
                }
            }
        case 2:
            return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_CANCEL_TIMER) + ChatColor.GRAY
                    + ")";
        case 3:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        if (context.getSessionData(CK.E_CANCEL_TIMER) == null) {
            context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("noWord"));
        }
        
        final ActionsEditorPostOpenNumericPromptEvent event
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }
    
    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new TimerFailPrompt(context);
        case 2:
            final String s = (String) context.getSessionData(CK.E_CANCEL_TIMER);
            if (s != null && s.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("noWord"));
            } else {
                context.setSessionData(CK.E_CANCEL_TIMER, Lang.get("yesWord"));
            }
            return new TimerPrompt(context);
        case 3:
            return new ActionMainPrompt(context);
        default:
            return new TimerPrompt(context);
        }
    }
    
    public class TimerFailPrompt extends ActionsEditorStringPrompt {
        
        public TimerFailPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterTimerSeconds");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
                } else {
                    context.setSessionData(CK.E_TIMER, i);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED
                        + Lang.get("reqNotANumber").replace("<input>", input));
                return new TimerFailPrompt(context);
            }
            return new TimerPrompt(context);
        }
    }
}
