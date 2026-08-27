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
import me.pikamug.quests.quests.components.FabricPlanner;
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
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

public class FabricQuestPlannerPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricQuestPlannerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 6;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("plannerTitle").replace("<quest>", (String) Objects
                .requireNonNull(SessionData.get(uuid, Key.Q_NAME)));
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 4:
        case 5:
            return ChatFormatting.BLUE;
        case 3:
            if (SessionData.get(uuid, Key.PLN_START_DATE) == null || SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                return ChatFormatting.GRAY;
            } else {
                return ChatFormatting.BLUE;
            }
        case 6:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("plnStart");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("plnEnd");
        case 3:
            if (SessionData.get(uuid, Key.PLN_START_DATE) == null || SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                return ChatFormatting.GRAY + FabricLang.get("plnRepeat");
            } else {
                return ChatFormatting.YELLOW + FabricLang.get("plnRepeat");
            }
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("plnCooldown");
        case 5:
            return ChatFormatting.YELLOW + FabricLang.get("plnOverride");
        case 6:
            return ChatFormatting.YELLOW + FabricLang.get("done");
        default:
            return null;
        }
    }

    @Override
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            final String start = (String) SessionData.get(uuid, Key.PLN_START_DATE);
            if (start == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "\n" + ChatFormatting.YELLOW + "     - " + getPrettyDate(start);
            }
        case 2:
            final String end = (String) SessionData.get(uuid, Key.PLN_END_DATE);
            if (end == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return "\n" + ChatFormatting.YELLOW + "     - " + getPrettyDate(end);
            }
        case 3:
            final Long repeat = (Long) SessionData.get(uuid, Key.PLN_REPEAT_CYCLE);
            if (SessionData.get(uuid, Key.PLN_START_DATE) == null || SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("stageEditorOptional") + ")";
            } else {
                if (repeat == null) {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
                } else {
                    return ChatFormatting.YELLOW + "(" + FabricMiscUtil.formatTime(repeat) + ChatFormatting.RESET + ChatFormatting.YELLOW + ")";
                }
            }
        case 4:
            final Long cooldown = (Long) SessionData.get(uuid, Key.PLN_COOLDOWN);
            if (cooldown == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                return ChatFormatting.YELLOW + "(" + FabricMiscUtil.formatTime(cooldown) + ChatFormatting.RESET + ChatFormatting.YELLOW + ")";
            }
        case 5:
            final Boolean override = (Boolean) SessionData.get(uuid, Key.PLN_OVERRIDE);
            if (override == null) {
                final boolean defaultOpt = new FabricPlanner().getOverride();
                return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                        + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                        + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
            } else {
                return ChatFormatting.GRAY + "(" + (override ? ChatFormatting.GREEN
                        + FabricLang.get(String.valueOf(override)) : ChatFormatting.RED
                        + FabricLang.get(String.valueOf(override))) + ChatFormatting.GRAY + ")";
            }
        case 6:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final String name = Objects.requireNonNull((String) SessionData.get(uuid, Key.Q_NAME));
        final StringBuilder text = new StringBuilder(ChatFormatting.DARK_AQUA + "- " + getTitle()
                .replace(name, ChatFormatting.AQUA + (String) SessionData.get(uuid, Key.Q_NAME) + ChatFormatting.DARK_AQUA)
                + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        if (sender == null) {
            return;
        }
        switch (input.intValue()) {
        case 1:
            new FabricQuestDateTimePrompt(uuid, FabricQuestPlannerPrompt.this, "start").start();
            break;
        case 2:
            new FabricQuestDateTimePrompt(uuid, FabricQuestPlannerPrompt.this, "end").start();
            break;
        case 3:
            if (SessionData.get(uuid, Key.PLN_START_DATE) != null && SessionData.get(uuid, Key.PLN_END_DATE) != null) {
                new FabricQuestPlannerRepeatPrompt(uuid).start();
            } else {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("invalidOption")));
                new FabricQuestPlannerPrompt(uuid).start();
            }
            break;
        case 4:
            new FabricQuestPlannerCooldownPrompt(uuid).start();
            break;
        case 5:
            new FabricQuestPlannerOverridePrompt(uuid).start();
            break;
        case 6:
            plugin.getQuestFactory().returnToMenu(uuid);
            break;
        default:
            new FabricQuestPlannerPrompt(uuid).start();
            break;
        }
    }

    public class FabricQuestPlannerRepeatPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestPlannerRepeatPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("timePrompt");
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
            if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new FabricQuestPlannerPrompt(uuid).start();
                return;
            }
            if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.PLN_REPEAT_CYCLE, null);
                new FabricQuestPlannerPrompt(uuid).start();
                return;
            }
            final long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorPositiveAmount")));
                } else {
                    SessionData.set(uuid, Key.PLN_REPEAT_CYCLE, delay);
                }
            } catch (final NumberFormatException e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                        .replace("<input>", input)));
                new FabricQuestPlannerRepeatPrompt(uuid).start();
                return;
            }
            new FabricQuestPlannerPrompt(uuid).start();
        }
    }

    public class FabricQuestPlannerCooldownPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestPlannerCooldownPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("timePrompt");
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
            if (input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
                new FabricQuestPlannerPrompt(uuid).start();
                return;
            }
            if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.PLN_COOLDOWN, null);
                new FabricQuestPlannerPrompt(uuid).start();
                return;
            }
            final long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorPositiveAmount")));
                } else {
                    SessionData.set(uuid, Key.PLN_COOLDOWN, delay);
                }
            } catch (final NumberFormatException e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                        .replace("<input>", input)));
                new FabricQuestPlannerCooldownPrompt(uuid).start();
                return;
            }
            new FabricQuestPlannerPrompt(uuid).start();
        }
    }

    public class FabricQuestPlannerOverridePrompt extends FabricQuestsEditorStringPrompt {
        public FabricQuestPlannerOverridePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;

        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            String text = "Select '<true>' or '<false>'";
            text = text.replace("<true>", FabricLang.get("true"));
            text = text.replace("<false>", FabricLang.get("false"));
            return text;
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("true");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("false");
            case 3:
                return ChatFormatting.RED + FabricLang.get("cmdClear");
            case 4:
                return ChatFormatting.RED + FabricLang.get("cmdCancel");
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            String text = FabricLang.get("optBooleanPrompt");
            text = text.replace("<true>", FabricLang.get("true"));
            text = text.replace("<false>", FabricLang.get("false"));
            return ChatFormatting.YELLOW + text;
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                if (input.startsWith("t") || input.equalsIgnoreCase(FabricLang.get("true"))
                        || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                    SessionData.set(uuid, Key.PLN_OVERRIDE, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(FabricLang.get("false"))
                        || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                    SessionData.set(uuid, Key.PLN_OVERRIDE, false);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestPlannerOverridePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.PLN_OVERRIDE, null);
                new FabricQuestPlannerPrompt(uuid).start();
                return;
            }
            new FabricQuestPlannerPrompt(uuid).start();
        }
    }

    private String getPrettyDate(final String formattedDate) {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        final String[] date = formattedDate.split(":");
        final int day = Integer.parseInt(date[0]);
        final int month = Integer.parseInt(date[1]);
        final int year = Integer.parseInt(date[2]);
        final int hour = Integer.parseInt(date[3]);
        final int minute = Integer.parseInt(date[4]);
        final int second = Integer.parseInt(date[5]);

        cal.set(year, month, day, hour, minute, second);
        String output = ChatFormatting.DARK_AQUA + dateFormat.format(cal.getTime());
        output += ChatFormatting.AQUA + " " + timeFormat.format(cal.getTime());

        final TimeZone tz = TimeZone.getTimeZone(date[6]);
        cal.setTimeZone(tz);
        final String[] iso = plugin.getConfigSettings().getLanguage().split("-");
        final Locale loc = iso.length > 1 ? new Locale(iso[0], iso[1]) : new Locale(iso[0]);
        final Double zonehour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        final String[] sep = String.valueOf(zonehour).replace("-", "").split("\\.");
        final DecimalFormat zoneFormat = new DecimalFormat("00");
        output += ChatFormatting.LIGHT_PURPLE + " UTC" + (zonehour < 0 ? "-" : "+")
                + zoneFormat.format(Integer.valueOf(sep[0])) + ":" + zoneFormat.format(Integer.valueOf(sep[1]))
                + ChatFormatting.GREEN + " (" + cal.getTimeZone().getDisplayName(loc) + ")";
        return output;
    }
}
