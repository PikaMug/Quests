/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.planner;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class QuestDateTimePrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private final Prompt oldPrompt;
    private final String source;

    public QuestDateTimePrompt(final ConversationContext context, final Prompt old, final String origin) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
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
        return BukkitLang.get("dateTimeTitle");
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
        final String[] iso = plugin.getConfigSettings().getLanguage().split("-");
        final Locale loc = new Locale(iso[0], iso[1]);
        final Double zonedHour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        final String[] sep = String.valueOf(zonedHour).replace("-", "").split("\\.");
        final DecimalFormat zoneFormat = new DecimalFormat("00");
        dateData += ChatColor.LIGHT_PURPLE + "UTC" + (zonedHour < 0 ? "-":"+")
                + zoneFormat.format(Integer.valueOf(sep[0])) + ":" + zoneFormat.format(Integer.valueOf(sep[1]))
                + ChatColor.GREEN + " (" + cal.getTimeZone().getDisplayName(loc) + ")";
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
            return ChatColor.GOLD + BukkitLang.get("dateCreateLoadTime");
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("timeYear");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("timeMonth");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("timeDay");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("timeHour");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("timeMinute");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("timeSecond");
        case 7:
            return ChatColor.YELLOW + BukkitLang.get("timeZone");
        case 8:
            return ChatColor.RED + BukkitLang.get("cancel");
        case 9:
            return ChatColor.GREEN + BukkitLang.get("done");
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
        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
    
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle(context) + "\n");
        if (context.getSessionData("tempYear") != null  && context.getSessionData("tempMonth") != null
                && context.getSessionData("tempDay") != null && context.getSessionData("tempHour") != null
                && context.getSessionData("tempMinute") != null && context.getSessionData("tempSecond") != null
                && context.getSessionData("tempZone") != null) {
            text.append(getDataText(context));
        }
        for (int i = 0; i <= size - 1; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
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
            return new QuestDateTimePrompt(context, oldPrompt, source);
        case 1:
            return new QuestYearPrompt(context);
        case 2:
            return new QuestMonthPrompt(context);
        case 3:
            return new QuestDayPrompt(context);
        case 4:
            return new QuestHourPrompt(context);
        case 5:
            return new QuestMinutePrompt(context);
        case 6:
            return new QuestSecondPrompt(context);
        case 7:
            return new QuestOffsetPrompt(context);
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
                        context.setSessionData(Key.PLN_START_DATE, date);
                    } else if (source.equals("end")) {
                        context.setSessionData(Key.PLN_END_DATE, date);
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
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                return new QuestDateTimePrompt(context, oldPrompt, source);
            } else {
                return oldPrompt;
            }
        default:
            return new QuestDateTimePrompt(context, oldPrompt, source);
        }
    }
    
    public class QuestYearPrompt extends QuestsEditorStringPrompt {
        
        public QuestYearPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterYear");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1000 || amt > 9999) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1000").replace("<greatest>", "9999"));
                        return new QuestYearPrompt(context);
                    } else {
                        context.setSessionData("tempYear", Integer.parseInt(input));
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestYearPrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestMonthPrompt extends QuestsEditorStringPrompt {
        
        public QuestMonthPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterMonth");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 12) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "12"));
                        return new QuestMonthPrompt(context);
                    } else {
                        context.setSessionData("tempMonth", Integer.parseInt(input) - 1);
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestMonthPrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestDayPrompt extends QuestsEditorStringPrompt {
        
        public QuestDayPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterDay");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 31) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "31"));
                        return new QuestDayPrompt(context);
                    } else {
                        context.setSessionData("tempDay", Integer.parseInt(input));
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestDayPrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestHourPrompt extends QuestsEditorStringPrompt {
        
        public QuestHourPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterHour");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 23) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "23"));
                        return new QuestHourPrompt(context);
                    } else {
                        context.setSessionData("tempHour", Integer.parseInt(input));
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestHourPrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestMinutePrompt extends QuestsEditorStringPrompt {
        
        public QuestMinutePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterMinute");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        return new QuestMinutePrompt(context);
                    } else {
                        context.setSessionData("tempMinute", Integer.parseInt(input));
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestMinutePrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestSecondPrompt extends QuestsEditorStringPrompt {
        
        public QuestSecondPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterSecond");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        return new QuestSecondPrompt(context);
                    } else {
                        context.setSessionData("tempSecond", Integer.parseInt(input));
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestSecondPrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestOffsetPrompt extends QuestsEditorStringPrompt {
        
        public QuestOffsetPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterOffset");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final double amt = Double.parseDouble(input.replace("UTC", "").replace(":", "."));
                    if (amt < -12.0 || amt > 14.0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                            .replace("<least>", "-12:00").replace("<greatest>", "14:00"));
                        return new QuestOffsetPrompt(context);
                    } else {
                        final String[] t = TimeZone.getAvailableIDs((int) Math.round(amt * 60.0 * 60.0 * 1000.0));
                        if (t.length > 1) {
                            return new QuestZonePrompt(context, t);
                        } else if (t.length > 0) {
                            context.setSessionData("tempZone", t[0]);
                        }  else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                        }    
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestOffsetPrompt(context);
                }
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
    
    public class QuestZonePrompt extends QuestsEditorStringPrompt {
        
        String[] zones;
        
        public QuestZonePrompt(final ConversationContext context, final String[] timezones) {
            super(context);
            zones = timezones;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("timeZoneTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("dateCreateEnterZone");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final String z : zones) {
                    if (z.toLowerCase().startsWith(input.toLowerCase())) {
                        context.setSessionData("tempZone", z);
                        return new QuestDateTimePrompt(context, oldPrompt, source);
                    }
                }
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                return new QuestZonePrompt(context, zones);
            } else {
                return new QuestDateTimePrompt(context, oldPrompt, source);
            }
        }
    }
}
