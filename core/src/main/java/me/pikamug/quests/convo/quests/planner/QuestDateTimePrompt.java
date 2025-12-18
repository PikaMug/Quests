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
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class QuestDateTimePrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final QuestsIntegerPrompt oldPrompt;
    private final String source;

    public QuestDateTimePrompt(final @NotNull UUID uuid, final QuestsIntegerPrompt old, final String origin) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
        oldPrompt = old;
        source = origin;
    }
    
    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("dateTimeTitle");
    }
    
    public String getDataText() {
        String dateData = "";
        final Integer year = (Integer) SessionData.get(uuid, "tempYear");
        final Integer month = (Integer) SessionData.get(uuid, "tempMonth");
        final Integer day = (Integer) SessionData.get(uuid, "tempDay");
        final Integer hour = (Integer) SessionData.get(uuid, "tempHour");
        final Integer minute = (Integer) SessionData.get(uuid, "tempMinute");
        final Integer second = (Integer) SessionData.get(uuid, "tempSecond");
        if (year == null || month == null || day == null || hour == null || minute == null || second == null) {
            return dateData;
        }
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        
        cal.set(year, month, day, hour, minute, second);
        dateData += ChatColor.DARK_AQUA + dateFormat.format(cal.getTime()) + " ";
        dateData += ChatColor.AQUA + timeFormat.format(cal.getTime()) + " ";
        
        cal.setTimeZone(TimeZone.getTimeZone((String) SessionData.get(uuid, "tempZone")));
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
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
        switch(number) {
        case 0:
            return "";
        case 1:
            if (SessionData.get(uuid, "tempYear") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, "tempYear") 
                        + ChatColor.GRAY + ")";
            }
        case 2:
            final Integer month = (Integer) SessionData.get(uuid, "tempMonth");
            if (month != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + (month + 1) + ChatColor.GRAY + ")";
            }
        case 3:
            if (SessionData.get(uuid, "tempDay") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, "tempDay") 
                        + ChatColor.GRAY + ")";
            }
        case 4:
            if (SessionData.get(uuid, "tempHour") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, "tempHour") 
                        + ChatColor.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, "tempMinute") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, "tempMinute") 
                        + ChatColor.GRAY + ")";
            }
        case 6:
            if (SessionData.get(uuid, "tempSecond") != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, "tempSecond") 
                        + ChatColor.GRAY + ")";
            }
        case 7:
            if (SessionData.get(uuid, "tempZone") != null) {
                final TimeZone tz = TimeZone.getTimeZone((String) SessionData.get(uuid, "tempZone"));
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
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
    
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle() + "\n");
        if (SessionData.get(uuid, "tempYear") != null  && SessionData.get(uuid, "tempMonth") != null
                && SessionData.get(uuid, "tempDay") != null && SessionData.get(uuid, "tempHour") != null
                && SessionData.get(uuid, "tempMinute") != null && SessionData.get(uuid, "tempSecond") != null
                && SessionData.get(uuid, "tempZone") != null) {
            text.append(getDataText());
        }
        for (int i = 0; i <= size - 1; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        switch(input.intValue()) {
        case 0:
            final Calendar cal = Calendar.getInstance();
            SessionData.set(uuid, "tempYear", cal.get(Calendar.YEAR));
            SessionData.set(uuid, "tempMonth", cal.get(Calendar.MONTH));
            SessionData.set(uuid, "tempDay", cal.get(Calendar.DAY_OF_MONTH));
            SessionData.set(uuid, "tempHour", cal.get(Calendar.HOUR_OF_DAY));
            SessionData.set(uuid, "tempMinute", cal.get(Calendar.MINUTE));
            SessionData.set(uuid, "tempSecond", cal.get(Calendar.SECOND));
            SessionData.set(uuid, "tempZone", cal.getTimeZone().getID());
            new QuestDateTimePrompt(uuid, oldPrompt, source);
        case 1:
            new QuestYearPrompt(uuid).start();
        case 2:
            new QuestMonthPrompt(uuid).start();
        case 3:
            new QuestDayPrompt(uuid).start();
        case 4:
            new QuestHourPrompt(uuid).start();
        case 5:
            new QuestMinutePrompt(uuid).start();
        case 6:
            new QuestSecondPrompt(uuid).start();
        case 7:
            new QuestOffsetPrompt(uuid).start();
        case 8:
            SessionData.set(uuid, "tempYear", null);
            SessionData.set(uuid, "tempMonth", null);
            SessionData.set(uuid, "tempDay", null);
            SessionData.set(uuid, "tempHour", null);
            SessionData.set(uuid, "tempMinute", null);
            SessionData.set(uuid, "tempSecond", null);
            SessionData.set(uuid, "tempZone", null);
            oldPrompt.start();
        case 9:
            if (SessionData.get(uuid, "tempYear") != null && SessionData.get(uuid, "tempMonth") != null
                    && SessionData.get(uuid, "tempDay") != null && SessionData.get(uuid, "tempHour") != null
                    && SessionData.get(uuid, "tempMinute") != null && SessionData.get(uuid, "tempSecond") != null
                    && SessionData.get(uuid, "tempZone") != null) {
                final Integer year = (Integer) SessionData.get(uuid, "tempYear");
                final Integer month = (Integer) SessionData.get(uuid, "tempMonth");
                final Integer day = (Integer) SessionData.get(uuid, "tempDay");
                final Integer hour = (Integer) SessionData.get(uuid, "tempHour");
                final Integer minute = (Integer) SessionData.get(uuid, "tempMinute");
                final Integer second = (Integer) SessionData.get(uuid, "tempSecond");
                final String zone = (String) SessionData.get(uuid, "tempZone");
                final String date = day + ":" + month + ":" + year + ":"
                        + hour + ":" + minute + ":" + second + ":" + zone;
                if (source != null) {
                    if (source.equals("start")) {
                        SessionData.set(uuid, Key.PLN_START_DATE, date);
                        if (SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                            final String endDate = "31:11:2999:23:59:59:" + zone;
                            SessionData.set(uuid, Key.PLN_END_DATE, endDate);
                        }
                    } else if (source.equals("end")) {
                        SessionData.set(uuid, Key.PLN_END_DATE, date);
                    }
                }
                SessionData.set(uuid, "tempYear", null);
                SessionData.set(uuid, "tempMonth", null);
                SessionData.set(uuid, "tempDay", null);
                SessionData.set(uuid, "tempHour", null);
                SessionData.set(uuid, "tempMinute", null);
                SessionData.set(uuid, "tempSecond", null);
                SessionData.set(uuid, "tempZone", null);
                oldPrompt.start();
            } else if (SessionData.get(uuid, "tempYear") != null || SessionData.get(uuid, "tempMonth") != null
                    || SessionData.get(uuid, "tempDay") != null || SessionData.get(uuid, "tempHour") != null
                    || SessionData.get(uuid, "tempMinute") != null || SessionData.get(uuid, "tempSecond") != null
                    || SessionData.get(uuid, "tempZone") != null) {
                Bukkit.getEntity(uuid).sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            } else {
                oldPrompt.start();
            }
        default:
            new QuestDateTimePrompt(uuid, oldPrompt, source);
        }
    }
    
    public class QuestYearPrompt extends QuestsEditorStringPrompt {
        
        public QuestYearPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterYear");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1000 || amt > 9999) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1000").replace("<greatest>", "9999"));
                        new QuestYearPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempYear", Integer.parseInt(input));
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestYearPrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestMonthPrompt extends QuestsEditorStringPrompt {
        
        public QuestMonthPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterMonth");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 12) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "12"));
                        new QuestMonthPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempMonth", Integer.parseInt(input) - 1);
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestMonthPrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestDayPrompt extends QuestsEditorStringPrompt {
        
        public QuestDayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterDay");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 31) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "31"));
                        new QuestDayPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempDay", Integer.parseInt(input));
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestDayPrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestHourPrompt extends QuestsEditorStringPrompt {
        
        public QuestHourPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterHour");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 23) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "23"));
                        new QuestHourPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempHour", Integer.parseInt(input));
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestHourPrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestMinutePrompt extends QuestsEditorStringPrompt {
        
        public QuestMinutePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterMinute");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        new QuestMinutePrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempMinute", Integer.parseInt(input));
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestMinutePrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestSecondPrompt extends QuestsEditorStringPrompt {
        
        public QuestSecondPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterSecond");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59"));
                        new QuestSecondPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempSecond", Integer.parseInt(input));
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestSecondPrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestOffsetPrompt extends QuestsEditorStringPrompt {
        
        public QuestOffsetPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterOffset");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final double amt = Double.parseDouble(input.replace("UTC", "").replace(":", "."));
                    if (amt < -12.0 || amt > 14.0) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                            .replace("<least>", "-12:00").replace("<greatest>", "14:00"));
                        new QuestOffsetPrompt(uuid).start();
                    } else {
                        final String[] t = TimeZone.getAvailableIDs((int) Math.round(amt * 60.0 * 60.0 * 1000.0));
                        if (t.length > 1) {
                            new QuestZonePrompt(uuid, t);
                        } else if (t.length > 0) {
                            SessionData.set(uuid, "tempZone", t[0]);
                        }  else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                        }    
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestOffsetPrompt(uuid).start();
                }
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
    
    public class QuestZonePrompt extends QuestsEditorStringPrompt {
        
        String[] zones;
        
        public QuestZonePrompt(final @NotNull UUID uuid, final String[] timezones) {
            super(uuid);
            zones = timezones;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("timeZoneTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("dateCreateEnterZone");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            for (final String z : zones) {
                text.append(ChatColor.GREEN).append(z).append(", ");
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            return text + "\n" + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final String z : zones) {
                    if (z.toLowerCase().startsWith(input.toLowerCase())) {
                        SessionData.set(uuid, "tempZone", z);
                        new QuestDateTimePrompt(uuid, oldPrompt, source);
                    }
                }
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                new QuestZonePrompt(uuid, zones);
            } else {
                new QuestDateTimePrompt(uuid, oldPrompt, source);
            }
        }
    }
}
