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
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.components.BukkitPlanner;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
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
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

public class QuestPlannerPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;

    public QuestPlannerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("plannerTitle").replace("<quest>", (String) Objects
                .requireNonNull(SessionData.get(uuid, Key.Q_NAME)));
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 4:
        case 5:
            return ChatColor.BLUE;
        case 3:
            if (SessionData.get(uuid, Key.PLN_START_DATE) == null || SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 6:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("plnStart");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("plnEnd");
        case 3:
            if (SessionData.get(uuid, Key.PLN_START_DATE) == null || SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                return ChatColor.GRAY + BukkitLang.get("plnRepeat");
            } else {
                return ChatColor.YELLOW + BukkitLang.get("plnRepeat");
            }
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("plnCooldown");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("plnOverride");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("done");
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
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "\n" + ChatColor.YELLOW + "     - " + getPrettyDate(start);
            }
        case 2:
            final String end = (String) SessionData.get(uuid, Key.PLN_END_DATE);
            if (end == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return "\n" + ChatColor.YELLOW +  "     - " 
                        + getPrettyDate(end);
            }
        case 3:
            final Long repeat = (Long) SessionData.get(uuid, Key.PLN_REPEAT_CYCLE);
            if (SessionData.get(uuid, Key.PLN_START_DATE) == null || SessionData.get(uuid, Key.PLN_END_DATE) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("stageEditorOptional") + ")";
            } else {
                if (repeat == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + BukkitMiscUtil.getTime(repeat) + ChatColor.RESET + ChatColor.YELLOW + ")";
                }
            }
        case 4:
            final Long cooldown = (Long) SessionData.get(uuid, Key.PLN_COOLDOWN);
            if (cooldown == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.YELLOW + "(" + BukkitMiscUtil.getTime(cooldown) + ChatColor.RESET + ChatColor.YELLOW + ")";
            }
        case 5:
            final Boolean override = (Boolean) SessionData.get(uuid, Key.PLN_OVERRIDE);
            if (override == null) {
                final boolean defaultOpt = new BukkitPlanner().getOverride();
                return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                        + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                        + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
            } else {
                return ChatColor.GRAY + "(" + (override ? ChatColor.GREEN
                        + BukkitLang.get(String.valueOf(override)) : ChatColor.RED
                        + BukkitLang.get(String.valueOf(override))) + ChatColor.GRAY + ")";
            }
        case 6:
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

        final String name = Objects.requireNonNull((String) SessionData.get(uuid, Key.Q_NAME));
        final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + "- "  + getTitle()
                .replace(name, ChatColor.AQUA + (String) SessionData.get(uuid, Key.Q_NAME) + ChatColor.DARK_AQUA)
                + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            new QuestDateTimePrompt(uuid, QuestPlannerPrompt.this, "start");
            break;
        case 2:
            new QuestDateTimePrompt(uuid, QuestPlannerPrompt.this, "end");
            break;
        case 3:
            if (SessionData.get(uuid, Key.PLN_START_DATE) != null && SessionData.get(uuid, Key.PLN_END_DATE) != null) {
                new QuestPlannerRepeatPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new QuestPlannerPrompt(uuid).start();
            }
            break;
        case 4:
            new QuestPlannerCooldownPrompt(uuid).start();
            break;
        case 5:
            new QuestPlannerOverridePrompt(uuid).start();
            break;
        case 6:
            plugin.getQuestFactory().returnToMenu(uuid);
            break;
        default:
            new QuestPlannerPrompt(uuid).start();
            break;
        }
    }

    public class QuestPlannerRepeatPrompt extends QuestsEditorStringPrompt {
        
        public QuestPlannerRepeatPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("timePrompt");
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
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestPlannerPrompt(uuid).start();
                return;
            }
            if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.PLN_REPEAT_CYCLE, null);
                new QuestPlannerPrompt(uuid).start();
                return;
            }
            final long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorPositiveAmount"));
                } else {
                    SessionData.set(uuid, Key.PLN_REPEAT_CYCLE, delay);
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                        .replace("<input>", input));
                new QuestPlannerRepeatPrompt(uuid).start();
                return;
            }
            new QuestPlannerPrompt(uuid).start();
        }
    }
    
    public class QuestPlannerCooldownPrompt extends QuestsEditorStringPrompt {
        
        public QuestPlannerCooldownPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("timePrompt");
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
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new QuestPlannerPrompt(uuid).start();
                return;
            }
            if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.PLN_COOLDOWN, null);
                new QuestPlannerPrompt(uuid).start();
                return;
            }
            final long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorPositiveAmount"));
                } else {
                    SessionData.set(uuid, Key.PLN_COOLDOWN, delay);
                }
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                        .replace("<input>", input));
                new QuestPlannerCooldownPrompt(uuid).start();
                return;
            }
            new QuestPlannerPrompt(uuid).start();
        }
    }
    
    public class QuestPlannerOverridePrompt extends QuestsEditorStringPrompt {
        public QuestPlannerOverridePrompt(final @NotNull UUID uuid) {
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
            text = text.replace("<true>", BukkitLang.get("true"));
            text = text.replace("<false>", BukkitLang.get("false"));
            return text;
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("true");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("false");
            case 3:
                return ChatColor.RED + BukkitLang.get("cmdClear");
            case 4:
                return ChatColor.RED + BukkitLang.get("cmdCancel");
            default:
                return null;
            }
        }
        
        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = BukkitLang.get("optBooleanPrompt");
            text = text.replace("<true>", BukkitLang.get("true"));
            text = text.replace("<false>", BukkitLang.get("false"));
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (input.startsWith("t") || input.equalsIgnoreCase(BukkitLang.get("true"))
                        || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                    SessionData.set(uuid, Key.PLN_OVERRIDE, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(BukkitLang.get("false"))
                        || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                    SessionData.set(uuid, Key.PLN_OVERRIDE, false);
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestPlannerOverridePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.PLN_OVERRIDE, null);
                new QuestPlannerPrompt(uuid).start();
                return;
            }
            new QuestPlannerPrompt(uuid).start();
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
        String output = ChatColor.DARK_AQUA + dateFormat.format(cal.getTime());
        output += ChatColor.AQUA + " " + timeFormat.format(cal.getTime());
        
        final TimeZone tz = TimeZone.getTimeZone(date[6]);
        cal.setTimeZone(tz);
        final String[] iso = plugin.getConfigSettings().getLanguage().split("-");
        final Locale loc = iso.length > 1 ? new Locale(iso[0], iso[1]) : new Locale(iso[0]);
        final Double zonehour = (double) (cal.getTimeZone().getRawOffset() / 60 / 60 / 1000);
        final String[] sep = String.valueOf(zonehour).replace("-", "").split("\\.");
        final DecimalFormat zoneFormat = new DecimalFormat("00");
        output += ChatColor.LIGHT_PURPLE + " UTC" + (zonehour < 0 ? "-":"+")
                + zoneFormat.format(Integer.valueOf(sep[0])) + ":" + zoneFormat.format(Integer.valueOf(sep[1]))
                + ChatColor.GREEN + " (" + cal.getTimeZone().getDisplayName(loc) + ")";
        return output;
    }
}
