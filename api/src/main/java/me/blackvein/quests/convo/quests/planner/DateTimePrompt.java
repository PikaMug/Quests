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

package me.blackvein.quests.convo.quests.planner;

import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimePrompt extends QuestsEditorNumericPrompt {
    private final Prompt oldPrompt;
    private final String source;

    public DateTimePrompt(final ConversationContext context, final Prompt old, final String origin) {
        super(context);
        oldPrompt = old;
        source = origin;
    }
    
    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("dateTimeTitle");
    }
    
    public String getDataText(final ConversationContext context) {
        String dateData = "";
        final Integer year = (Integer) context.getSessionData("tempYear");
        final Integer month = (Integer) context.getSessionData("tempMonth");
        final Integer day = (Integer) context.getSessionData("tempDay");
        final Integer hour = (Integer) context.getSessionData("tempHour");
        final Integer minute = (Integer) context.getSessionData("tempMinute");
        final Integer second = (Integer) context.getSessionData("tempSecond");
        if (year == null || month == null || day == null || hour == null || minute == null || second == null) {
            return dateData;
        }
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/dd/MM");
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        
        cal.set(year, month, day, hour, minute, second);
        dateData += ChatColor.DARK_AQUA + dateFormat.format(cal.getTime()) + " ";
        dateData += ChatColor.AQUA + timeFormat.format(cal.getTime()) + " ";
        
        cal.setTimeZone(TimeZone.getTimeZone((String) context.getSessionData("tempZone")));
        final String[] iso = Lang.getISO().split("-");
        final Locale loc = new Locale(iso[0], iso[1]);
        final Double zonedHour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        final String[] sep = String.valueOf(zonedHour).replace("-", "").split("\\.");
        final DecimalFormat zoneFormat = new DecimalFormat("00");
        dateData += ChatColor.LIGHT_PURPLE + "UTC" + (zonedHour < 0 ? "-":"+") + zoneFormat.format(Integer.valueOf(sep[0]))
                + ":" + zoneFormat.format(Integer.valueOf(sep[1])) + ChatColor.GREEN + " (" 
                + cal.getTimeZone().getDisplayName(loc) + ")";
        return dateData;
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 0:
            return ChatColor.YELLOW;
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
            return ChatColor.BLUE;
        case 8:
            return ChatColor.RED;
        case 9:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch(number) {
        case 0:
            return ChatColor.GOLD + Lang.get("dateCreateLoadTime");
        case 1:
            return ChatColor.YELLOW + Lang.get("timeYear");
        case 2:
            return ChatColor.YELLOW + Lang.get("timeMonth");
        case 3:
            return ChatColor.YELLOW + Lang.get("timeDay");
        case 4:
            return ChatColor.YELLOW + Lang.get("timeHour");
        case 5:
            return ChatColor.YELLOW + Lang.get("timeMinute");
        case 6:
            return ChatColor.YELLOW + Lang.get("timeSecond");
        case 7:
            return ChatColor.YELLOW + Lang.get("timeZone");
        case 8:
            return ChatColor.RED + Lang.get("cancel");
        case 9:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 0:
            return "";
        case 1:
            if (context.getSessionData("tempYear") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData("tempYear") 
                        + ChatColor.GRAY + ")";
            }
        case 2:
            final Integer month = (Integer) context.getSessionData("tempMonth");
            if (month != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + (month + 1) + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData("tempDay") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData("tempDay") 
                        + ChatColor.GRAY + ")";
            }
        case 4:
            if (context.getSessionData("tempHour") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData("tempHour") 
                        + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData("tempMinute") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData("tempMinute") 
                        + ChatColor.GRAY + ")";
            }
        case 6:
            if (context.getSessionData("tempSecond") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData("tempSecond") 
                        + ChatColor.GRAY + ")";
            }
        case 7:
            if (context.getSessionData("tempZone") != null) {
                final TimeZone tz = TimeZone.getTimeZone((String) context.getSessionData("tempZone"));
                return ChatColor.GRAY + "(" + ChatColor.AQUA + tz.getDisplayName(false, TimeZone.SHORT) 
                        + ChatColor.GRAY + ")";
            }
        case 8:
        case 9:
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
    
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle(context) + "\n");
        if (context.getSessionData("tempYear") != null  && context.getSessionData("tempMonth") != null
                && context.getSessionData("tempDay") != null && context.getSessionData("tempHour") != null
                && context.getSessionData("tempMinute") != null && context.getSessionData("tempSecond") != null
                && context.getSessionData("tempZone") != null) {
            text.append(getDataText(context));
        }
        for (int i = 0; i <= size - 1; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ").append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 0:
            final Calendar cal = Calendar.getInstance();
            context.setSessionData("tempYear", cal.get(Calendar.YEAR));
            context.setSessionData("tempMonth", cal.get(Calendar.MONTH));
            context.setSessionData("tempDay", cal.get(Calendar.DAY_OF_MONTH));
            context.setSessionData("tempHour", cal.get(Calendar.HOUR_OF_DAY));
            context.setSessionData("tempMinute", cal.get(Calendar.MINUTE));
            context.setSessionData("tempSecond", cal.get(Calendar.SECOND));
            context.setSessionData("tempZone", cal.getTimeZone().getID());
            return new DateTimePrompt(context, oldPrompt, source);
        case 1:
            return new YearPrompt(context);
        case 2:
            return new MonthPrompt(context);
        case 3:
            return new DayPrompt(context);
        case 4:
            return new HourPrompt(context);
        case 5:
            return new MinutePrompt(context);
        case 6:
            return new SecondPrompt(context);
        case 7:
            return new OffsetPrompt(context);
        case 8:
            context.setSessionData("tempYear", null);
            context.setSessionData("tempMonth", null);
            context.setSessionData("tempDay", null);
            context.setSessionData("tempHour", null);
            context.setSessionData("tempMinute", null);
            context.setSessionData("tempSecond", null);
            context.setSessionData("tempZone", null);
            return oldPrompt;
        case 9:
            if (context.getSessionData("tempYear") != null && context.getSessionData("tempMonth") != null
                    && context.getSessionData("tempDay") != null && context.getSessionData("tempHour") != null
                    && context.getSessionData("tempMinute") != null && context.getSessionData("tempSecond") != null
                    && context.getSessionData("tempZone") != null) {
                final Integer year = (Integer) context.getSessionData("tempYear");
                final Integer month = (Integer) context.getSessionData("tempMonth");
                final Integer day = (Integer) context.getSessionData("tempDay");
                final Integer hour = (Integer) context.getSessionData("tempHour");
                final Integer minute = (Integer) context.getSessionData("tempMinute");
                final Integer second = (Integer) context.getSessionData("tempSecond");
                final String zone = (String) context.getSessionData("tempZone");
                final String date = day + ":" + month + ":" + year + ":"
                        + hour + ":" + minute + ":" + second + ":" + zone;
                if (source != null) {
                    if (source.equals("start")) {
                        context.setSessionData(CK.PLN_START_DATE, date);
                    } else if (source.equals("end")) {
                        context.setSessionData(CK.PLN_END_DATE, date);
                    }
                }
                context.setSessionData("tempYear", null);
                context.setSessionData("tempMonth", null);
                context.setSessionData("tempDay", null);
                context.setSessionData("tempHour", null);
                context.setSessionData("tempMinute", null);
                context.setSessionData("tempSecond", null);
                context.setSessionData("tempZone", null);
                return oldPrompt;
            } else if (context.getSessionData("tempYear") != null || context.getSessionData("tempMonth") != null
                    || context.getSessionData("tempDay") != null || context.getSessionData("tempHour") != null
                    || context.getSessionData("tempMinute") != null || context.getSessionData("tempSecond") != null
                    || context.getSessionData("tempZone") != null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                return new DateTimePrompt(context, oldPrompt, source);
            } else {
                return oldPrompt;
            }
        default:
            return new DateTimePrompt(context, oldPrompt, source);
        }
    }
    
    public class YearPrompt extends QuestsEditorStringPrompt {
        
        public YearPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterYear");
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
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1000 || amt > 9999) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1000").replace("<greatest>", "9999"));
                        return new YearPrompt(context);
                    } else {
                        context.setSessionData("tempYear", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new YearPrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class MonthPrompt extends QuestsEditorStringPrompt {
        
        public MonthPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterMonth");
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
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 12) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "12"));
                        return new MonthPrompt(context);
                    } else {
                        context.setSessionData("tempMonth", Integer.parseInt(input) - 1);
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new MonthPrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class DayPrompt extends QuestsEditorStringPrompt {
        
        public DayPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterDay");
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
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 31) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "31"));
                        return new DayPrompt(context);
                    } else {
                        context.setSessionData("tempDay", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new DayPrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class HourPrompt extends QuestsEditorStringPrompt {
        
        public HourPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterHour");
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
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 23) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "23"));
                        return new HourPrompt(context);
                    } else {
                        context.setSessionData("tempHour", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new HourPrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class MinutePrompt extends QuestsEditorStringPrompt {
        
        public MinutePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterMinute");
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
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        return new MinutePrompt(context);
                    } else {
                        context.setSessionData("tempMinute", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new MinutePrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class SecondPrompt extends QuestsEditorStringPrompt {
        
        public SecondPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterSecond");
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
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        return new SecondPrompt(context);
                    } else {
                        context.setSessionData("tempSecond", Integer.parseInt(input));
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new SecondPrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class OffsetPrompt extends QuestsEditorStringPrompt {
        
        public OffsetPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterOffset");
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
                try {
                    final double amt = Double.parseDouble(input.replace("UTC", "").replace(":", "."));
                    if (amt < -12.0 || amt > 14.0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                            .replace("<least>", "-12:00").replace("<greatest>", "14:00"));
                        return new OffsetPrompt(context);
                    } else {
                        final String[] t = TimeZone.getAvailableIDs((int) Math.round(amt * 60.0 * 60.0 * 1000.0));
                        if (t.length > 1) {
                            return new ZonePrompt(context, t);
                        } else if (t.length > 0) {
                            context.setSessionData("tempZone", t[0]);
                        }  else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                        }    
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new OffsetPrompt(context);
                }
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class ZonePrompt extends QuestsEditorStringPrompt {
        
        String[] zones;
        
        public ZonePrompt(final ConversationContext context, final String[] timezones) {
            super(context);
            zones = timezones;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("timeZoneTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("dateCreateEnterZone");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            for (final String z : zones) {
                text.append(ChatColor.GREEN).append(z).append(", ");
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            return text + "\n" + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                for (final String z : zones) {
                    if (z.toLowerCase().startsWith(input.toLowerCase())) {
                        context.setSessionData("tempZone", z);
                        return new DateTimePrompt(context, oldPrompt, source);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new ZonePrompt(context, zones);
            } else {
                return new DateTimePrompt(context, oldPrompt, source);
            }
        }
    }
}
