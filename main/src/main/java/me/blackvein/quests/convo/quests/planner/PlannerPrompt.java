/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.quests.planner;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.Planner;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class PlannerPrompt extends QuestsEditorNumericPrompt {
    
    private final Quests plugin;

    public PlannerPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("plannerTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
            return ChatColor.BLUE;
        case 3:
            if (context.getSessionData(CK.PLN_START_DATE) == null || context.getSessionData(CK.PLN_END_DATE) == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 4:
        case 5:
            return ChatColor.BLUE;
        case 6:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("plnStart");
        case 2:
            return ChatColor.YELLOW + Lang.get("plnEnd");
        case 3:
            if (context.getSessionData(CK.PLN_START_DATE) == null || context.getSessionData(CK.PLN_END_DATE) == null) {
                return ChatColor.GRAY + Lang.get("plnRepeat");
            } else {
                return ChatColor.YELLOW + Lang.get("plnRepeat");
            }
        case 4:
            return ChatColor.YELLOW + Lang.get("plnCooldown");
        case 5:
            return ChatColor.YELLOW + Lang.get("plnOverride");
        case 6:
            return ChatColor.YELLOW + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(CK.PLN_START_DATE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "\n" + ChatColor.YELLOW + "     - " + getPrettyDate((String) context.getSessionData(CK.PLN_START_DATE));
            }
        case 2:
            if (context.getSessionData(CK.PLN_END_DATE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return "\n" + ChatColor.YELLOW +  "     - " 
                        + getPrettyDate((String) context.getSessionData(CK.PLN_END_DATE));
            }
        case 3:
            if (context.getSessionData(CK.PLN_START_DATE) == null || context.getSessionData(CK.PLN_END_DATE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
            } else {
                if (context.getSessionData(CK.PLN_REPEAT_CYCLE) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" 
                            + MiscUtil.getTime((Long) context.getSessionData(CK.PLN_REPEAT_CYCLE)) 
                            + ChatColor.RESET + ChatColor.YELLOW + ")";
                }
            }
        case 4:
            if (context.getSessionData(CK.PLN_COOLDOWN) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.YELLOW + "(" + MiscUtil.getTime((Long) context.getSessionData(CK.PLN_COOLDOWN)) 
                        + ChatColor.RESET + ChatColor.YELLOW + ")";
            }
        case 5:
            if (context.getSessionData(CK.PLN_OVERRIDE) == null) {
                final boolean defaultOpt = new Planner().getOverride();
                return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                        + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                        + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
            } else {
               final boolean quittingOpt = (Boolean) context.getSessionData(CK.PLN_OVERRIDE);
                return ChatColor.GRAY + "(" + (quittingOpt ? ChatColor.GREEN
                        + Lang.get(String.valueOf(quittingOpt)) : ChatColor.RED 
                        + Lang.get(String.valueOf(quittingOpt))) + ChatColor.GRAY + ")";
            }
        case 6:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.DARK_AQUA + getTitle(context).replace((String) context
                .getSessionData(CK.Q_NAME), ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) 
                + ChatColor.DARK_AQUA);
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new DateTimePrompt(context, PlannerPrompt.this, "start");
        case 2:
            return new DateTimePrompt(context, PlannerPrompt.this, "end");
        case 3:
            if (context.getSessionData(CK.PLN_START_DATE) != null && context.getSessionData(CK.PLN_END_DATE) != null) {
                return new PlannerRepeatPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new PlannerPrompt(context);
            }
        case 4:
            return new PlannerCooldownPrompt(context);
        case 5:
            return new PlannerOverridePrompt(context);
        case 6:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new PlannerPrompt(context);
        }
    }

    public class PlannerRepeatPrompt extends QuestsEditorStringPrompt {
        
        public PlannerRepeatPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("timePrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new PlannerPrompt(context);
            }
            if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.PLN_REPEAT_CYCLE, null);
                return new PlannerPrompt(context);
            }
            long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
                } else {
                    context.setSessionData(CK.PLN_REPEAT_CYCLE, delay);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                        .replace("<input>", input));
                return new PlannerRepeatPrompt(context);
            }
            return new PlannerPrompt(context);
        }
    }
    
    public class PlannerCooldownPrompt extends QuestsEditorStringPrompt {
        
        public PlannerCooldownPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("timePrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new PlannerPrompt(context);
            }
            if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.PLN_COOLDOWN, null);
                return new PlannerPrompt(context);
            }
            long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorPositiveAmount"));
                } else {
                    context.setSessionData(CK.PLN_COOLDOWN, delay);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                        .replace("<input>", input));
                return new PlannerCooldownPrompt(context);
            }
            return new PlannerPrompt(context);
        }
    }
    
    public class PlannerOverridePrompt extends QuestsEditorStringPrompt {
        public PlannerOverridePrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            String text = "Select '<true>' or '<false>'";
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return text;
        }
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("true");
            case 2:
                return ChatColor.YELLOW + Lang.get("false");
            case 3:
                return ChatColor.RED + Lang.get("cmdClear");
            case 4:
                return ChatColor.RED + Lang.get("cmdCancel");
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = Lang.get("optBooleanPrompt");
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                if (input.startsWith("t") || input.equalsIgnoreCase(Lang.get("true")) 
                        || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                    context.setSessionData(CK.PLN_OVERRIDE, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(Lang.get("false")) 
                        || input.equalsIgnoreCase(Lang.get("noWord"))) {
                    context.setSessionData(CK.PLN_OVERRIDE, false);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new PlannerOverridePrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.PLN_OVERRIDE, null);
                return new PlannerPrompt(context);
            }
            return new PlannerPrompt(context);
        }
    }
    
    private String getPrettyDate(final String formattedDate) {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/dd/MM");
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        final String[] date = formattedDate.split(":");
        final int day = Integer.valueOf(date[0]);
        final int month = Integer.valueOf(date[1]);
        final int year = Integer.valueOf(date[2]);
        final int hour = Integer.valueOf(date[3]);
        final int minute = Integer.valueOf(date[4]);
        final int second = Integer.valueOf(date[5]);
        
        cal.set(year, month, day, hour, minute, second);
        String output = ChatColor.DARK_AQUA + dateFormat.format(cal.getTime());
        output += ChatColor.AQUA + " " + timeFormat.format(cal.getTime());
        
        final TimeZone tz = TimeZone.getTimeZone(date[6]);
        cal.setTimeZone(tz);
        final String[] iso = Lang.getISO().split("-");
        final Locale loc = iso.length > 1 ? new Locale(iso[0], iso[1]) : new Locale(iso[0]);
        final Double zhour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        final String[] sep = String.valueOf(zhour).replace("-", "").split("\\.");
        final DecimalFormat zoneFormat = new DecimalFormat("00");
        output += ChatColor.LIGHT_PURPLE + " UTC" + (zhour < 0 ? "-":"+") + zoneFormat.format(Integer.valueOf(sep[0])) 
                + ":" + zoneFormat.format(Integer.valueOf(sep[1])) + ChatColor.GREEN + " (" 
                + cal.getTimeZone().getDisplayName(loc) + ")";
        return output;
    }
}
