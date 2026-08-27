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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class FabricQuestDateTimePrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final FabricQuestsEditorIntegerPrompt oldPrompt;
    private final String source;

    public FabricQuestDateTimePrompt(final @NotNull UUID uuid, final FabricQuestsEditorIntegerPrompt old, final String origin) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
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
        return FabricLang.get("dateTimeTitle");
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
        dateData += ChatFormatting.DARK_AQUA + dateFormat.format(cal.getTime()) + " ";
        dateData += ChatFormatting.AQUA + timeFormat.format(cal.getTime()) + " ";

        cal.setTimeZone(TimeZone.getTimeZone((String) SessionData.get(uuid, "tempZone")));
        final String[] iso = plugin.getConfigSettings().getLanguage().split("-");
        final Locale loc = iso.length > 1 ? new Locale(iso[0], iso[1]) : new Locale(iso[0]);
        final Double zonedHour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        final String[] sep = String.valueOf(zonedHour).replace("-", "").split("\\.");
        final DecimalFormat zoneFormat = new DecimalFormat("00");
        dateData += ChatFormatting.LIGHT_PURPLE + "UTC" + (zonedHour < 0 ? "-" : "+")
                + zoneFormat.format(Integer.valueOf(sep[0])) + ":" + zoneFormat.format(Integer.valueOf(sep[1]))
                + ChatFormatting.GREEN + " (" + cal.getTimeZone().getDisplayName(loc) + ")";
        return dateData;
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 0:
            return ChatFormatting.YELLOW;
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
            return ChatFormatting.BLUE;
        case 8:
            return ChatFormatting.RED;
        case 9:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 0:
            return ChatFormatting.GOLD + FabricLang.get("dateCreateLoadTime");
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("timeYear");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("timeMonth");
        case 3:
            return ChatFormatting.YELLOW + FabricLang.get("timeDay");
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("timeHour");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("timeMinute");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("timeSecond");
        case 7:
            return ChatFormatting.YELLOW + FabricLang.get("timeZone");
        case 8:
            return ChatFormatting.RED + FabricLang.get("cancel");
        case 9:
            return ChatFormatting.GREEN + FabricLang.get("done");
        default:
            return null;
        }
    }

    @Override
    public String getAdditionalText(final int number) {
        switch (number) {
        case 0:
            return "";
        case 1:
            if (SessionData.get(uuid, "tempYear") != null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, "tempYear")
                        + ChatFormatting.GRAY + ")";
            }
            break;
        case 2:
            final Integer month = (Integer) SessionData.get(uuid, "tempMonth");
            if (month != null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + (month + 1) + ChatFormatting.GRAY + ")";
            }
            break;
        case 3:
            if (SessionData.get(uuid, "tempDay") != null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, "tempDay")
                        + ChatFormatting.GRAY + ")";
            }
            break;
        case 4:
            if (SessionData.get(uuid, "tempHour") != null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, "tempHour")
                        + ChatFormatting.GRAY + ")";
            }
            break;
        case 5:
            if (SessionData.get(uuid, "tempMinute") != null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, "tempMinute")
                        + ChatFormatting.GRAY + ")";
            }
            break;
        case 6:
            if (SessionData.get(uuid, "tempSecond") != null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + SessionData.get(uuid, "tempSecond")
                        + ChatFormatting.GRAY + ")";
            }
            break;
        case 7:
            if (SessionData.get(uuid, "tempZone") != null) {
                final TimeZone tz = TimeZone.getTimeZone((String) SessionData.get(uuid, "tempZone"));
                return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + tz.getDisplayName(false, TimeZone.SHORT)
                        + ChatFormatting.GRAY + ")";
            }
            break;
        case 8:
        case 9:
            return "";
        default:
            return null;
        }
        return "";
    }

    @Override
    public @NotNull String getPromptText() {
        final StringBuilder text = new StringBuilder(ChatFormatting.AQUA + getTitle() + "\n");
        if (SessionData.get(uuid, "tempYear") != null && SessionData.get(uuid, "tempMonth") != null
                && SessionData.get(uuid, "tempDay") != null && SessionData.get(uuid, "tempHour") != null
                && SessionData.get(uuid, "tempMinute") != null && SessionData.get(uuid, "tempSecond") != null
                && SessionData.get(uuid, "tempZone") != null) {
            text.append(getDataText());
        }
        for (int i = 0; i <= size - 1; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 0:
            final Calendar cal = Calendar.getInstance();
            SessionData.set(uuid, "tempYear", cal.get(Calendar.YEAR));
            SessionData.set(uuid, "tempMonth", cal.get(Calendar.MONTH));
            SessionData.set(uuid, "tempDay", cal.get(Calendar.DAY_OF_MONTH));
            SessionData.set(uuid, "tempHour", cal.get(Calendar.HOUR_OF_DAY));
            SessionData.set(uuid, "tempMinute", cal.get(Calendar.MINUTE));
            SessionData.set(uuid, "tempSecond", cal.get(Calendar.SECOND));
            SessionData.set(uuid, "tempZone", cal.getTimeZone().getID());
            new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            break;
        case 1:
            new FabricQuestYearPrompt(uuid).start();
            break;
        case 2:
            new FabricQuestMonthPrompt(uuid).start();
            break;
        case 3:
            new FabricQuestDayPrompt(uuid).start();
            break;
        case 4:
            new FabricQuestHourPrompt(uuid).start();
            break;
        case 5:
            new FabricQuestMinutePrompt(uuid).start();
            break;
        case 6:
            new FabricQuestSecondPrompt(uuid).start();
            break;
        case 7:
            new FabricQuestOffsetPrompt(uuid).start();
            break;
        case 8:
            SessionData.set(uuid, "tempYear", null);
            SessionData.set(uuid, "tempMonth", null);
            SessionData.set(uuid, "tempDay", null);
            SessionData.set(uuid, "tempHour", null);
            SessionData.set(uuid, "tempMinute", null);
            SessionData.set(uuid, "tempSecond", null);
            SessionData.set(uuid, "tempZone", null);
            oldPrompt.start();
            break;
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
                final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("listsNotSameSize")));
                }
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            } else {
                oldPrompt.start();
            }
            break;
        default:
            new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            break;
        }
    }

    public class FabricQuestYearPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestYearPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterYear");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1000 || amt > 9999) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "1000").replace("<greatest>", "9999")));
                        new FabricQuestYearPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempYear", Integer.parseInt(input));
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestYearPrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestMonthPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestMonthPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterMonth");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 12) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "12")));
                        new FabricQuestMonthPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempMonth", Integer.parseInt(input) - 1);
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestMonthPrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestDayPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestDayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterDay");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 1 || amt > 31) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "1").replace("<greatest>", "31")));
                        new FabricQuestDayPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempDay", Integer.parseInt(input));
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestDayPrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestHourPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestHourPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterHour");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 23) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "23")));
                        new FabricQuestHourPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempHour", Integer.parseInt(input));
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestHourPrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestMinutePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestMinutePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterMinute");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59")));
                        new FabricQuestMinutePrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempMinute", Integer.parseInt(input));
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestMinutePrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestSecondPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestSecondPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterSecond");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final int amt = Integer.parseInt(input);
                    if (amt < 0 || amt > 59) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "0").replace("<greatest>", "59")));
                        new FabricQuestSecondPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, "tempSecond", Integer.parseInt(input));
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestSecondPrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestOffsetPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestOffsetPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterOffset");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                try {
                    final double amt = Double.parseDouble(input.replace("UTC", "").replace(":", "."));
                    if (amt < -12.0 || amt > 14.0) {
                        sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidRange")
                                .replace("<least>", "-12:00").replace("<greatest>", "14:00")));
                        new FabricQuestOffsetPrompt(uuid).start();
                    } else {
                        final String[] t = TimeZone.getAvailableIDs((int) Math.round(amt * 60.0 * 60.0 * 1000.0));
                        if (t.length > 1) {
                            new FabricQuestZonePrompt(uuid, t).start();
                        } else if (t.length > 0) {
                            SessionData.set(uuid, "tempZone", t[0]);
                        } else {
                            sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                        }
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                    }
                } catch (final NumberFormatException e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestOffsetPrompt(uuid).start();
                }
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }

    public class FabricQuestZonePrompt extends FabricQuestsEditorStringPrompt {

        String[] zones;

        public FabricQuestZonePrompt(final @NotNull UUID uuid, final String[] timezones) {
            super(uuid);
            zones = timezones;
        }

        @Override
        public String getTitle() {
            return FabricLang.get("timeZoneTitle");
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("dateCreateEnterZone");
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.LIGHT_PURPLE + getTitle() + "\n");
            for (final String z : zones) {
                text.append(ChatFormatting.GREEN).append(z).append(", ");
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            return text + "\n" + ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                for (final String z : zones) {
                    if (z.toLowerCase().startsWith(input.toLowerCase())) {
                        SessionData.set(uuid, "tempZone", z);
                        new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
                        return;
                    }
                }
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                new FabricQuestZonePrompt(uuid, zones).start();
            } else {
                new FabricQuestDateTimePrompt(uuid, oldPrompt, source).start();
            }
        }
    }
}
