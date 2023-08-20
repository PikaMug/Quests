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

package me.pikamug.quests.convo.quests.planner;

import me.pikamug.quests.quests.components.BukkitPlanner;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.Language;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class QuestPlannerPrompt extends QuestsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;

    public QuestPlannerPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Language.get("plannerTitle").replace("<quest>", (String) Objects
                .requireNonNull(context.getSessionData(Key.Q_NAME)));
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
        case 4:
        case 5:
            return ChatColor.BLUE;
        case 3:
            if (context.getSessionData(Key.PLN_START_DATE) == null || context.getSessionData(Key.PLN_END_DATE) == null) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Language.get("plnStart");
        case 2:
            return ChatColor.YELLOW + Language.get("plnEnd");
        case 3:
            if (context.getSessionData(Key.PLN_START_DATE) == null || context.getSessionData(Key.PLN_END_DATE) == null) {
                return ChatColor.GRAY + Language.get("plnRepeat");
            } else {
                return ChatColor.YELLOW + Language.get("plnRepeat");
            }
        case 4:
            return ChatColor.YELLOW + Language.get("plnCooldown");
        case 5:
            return ChatColor.YELLOW + Language.get("plnOverride");
        case 6:
            return ChatColor.YELLOW + Language.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            final String start = (String) context.getSessionData(Key.PLN_START_DATE);
            if (start == null) {
                return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
            } else {
                return "\n" + ChatColor.YELLOW + "     - " + getPrettyDate(start);
            }
        case 2:
            final String end = (String) context.getSessionData(Key.PLN_END_DATE);
            if (end == null) {
                return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
            } else {
                return "\n" + ChatColor.YELLOW +  "     - " 
                        + getPrettyDate(end);
            }
        case 3:
            final Long repeat = (Long) context.getSessionData(Key.PLN_REPEAT_CYCLE);
            if (context.getSessionData(Key.PLN_START_DATE) == null || context.getSessionData(Key.PLN_END_DATE) == null) {
                return ChatColor.GRAY + "(" + Language.get("stageEditorOptional") + ")";
            } else {
                if (repeat == null) {
                    return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + BukkitMiscUtil.getTime(repeat) + ChatColor.RESET + ChatColor.YELLOW + ")";
                }
            }
        case 4:
            final Long cooldown = (Long) context.getSessionData(Key.PLN_COOLDOWN);
            if (cooldown == null) {
                return ChatColor.GRAY + "(" + Language.get("noneSet") + ")";
            } else {
                return ChatColor.YELLOW + "(" + BukkitMiscUtil.getTime(cooldown) + ChatColor.RESET + ChatColor.YELLOW + ")";
            }
        case 5:
            final Boolean override = (Boolean) context.getSessionData(Key.PLN_OVERRIDE);
            if (override == null) {
                final boolean defaultOpt = new BukkitPlanner().getOverride();
                return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                        + Language.get(String.valueOf(defaultOpt)) : ChatColor.RED
                        + Language.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
            } else {
                return ChatColor.GRAY + "(" + (override ? ChatColor.GREEN
                        + Language.get(String.valueOf(override)) : ChatColor.RED
                        + Language.get(String.valueOf(override))) + ChatColor.GRAY + ")";
            }
        case 6:
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

        final String name = Objects.requireNonNull((String) context.getSessionData(Key.Q_NAME));
        final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + "- "  + getTitle(context)
                .replace(name, ChatColor.AQUA + (String) context.getSessionData(Key.Q_NAME) + ChatColor.DARK_AQUA)
                + " -");
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
            return new QuestDateTimePrompt(context, QuestPlannerPrompt.this, "start");
        case 2:
            return new QuestDateTimePrompt(context, QuestPlannerPrompt.this, "end");
        case 3:
            if (context.getSessionData(Key.PLN_START_DATE) != null && context.getSessionData(Key.PLN_END_DATE) != null) {
                return new QuestPlannerRepeatPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("invalidOption"));
                return new QuestPlannerPrompt(context);
            }
        case 4:
            return new QuestPlannerCooldownPrompt(context);
        case 5:
            return new QuestPlannerOverridePrompt(context);
        case 6:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new QuestPlannerPrompt(context);
        }
    }

    public class QuestPlannerRepeatPrompt extends QuestsEditorStringPrompt {
        
        public QuestPlannerRepeatPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("timePrompt");
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
            if (input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                return new QuestPlannerPrompt(context);
            }
            if (input.equalsIgnoreCase(Language.get("cmdClear"))) {
                context.setSessionData(Key.PLN_REPEAT_CYCLE, null);
                return new QuestPlannerPrompt(context);
            }
            final long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questEditorPositiveAmount"));
                } else {
                    context.setSessionData(Key.PLN_REPEAT_CYCLE, delay);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("reqNotANumber")
                        .replace("<input>", input));
                return new QuestPlannerRepeatPrompt(context);
            }
            return new QuestPlannerPrompt(context);
        }
    }
    
    public class QuestPlannerCooldownPrompt extends QuestsEditorStringPrompt {
        
        public QuestPlannerCooldownPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Language.get("timePrompt");
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
            if (input.equalsIgnoreCase(Language.get("cmdCancel"))) {
                return new QuestPlannerPrompt(context);
            }
            if (input.equalsIgnoreCase(Language.get("cmdClear"))) {
                context.setSessionData(Key.PLN_COOLDOWN, null);
                return new QuestPlannerPrompt(context);
            }
            final long delay;
            try {
                final long l = Long.parseLong(input);
                delay = l * 1000;
                if (delay < 1) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("questEditorPositiveAmount"));
                } else {
                    context.setSessionData(Key.PLN_COOLDOWN, delay);
                }
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("reqNotANumber")
                        .replace("<input>", input));
                return new QuestPlannerCooldownPrompt(context);
            }
            return new QuestPlannerPrompt(context);
        }
    }
    
    public class QuestPlannerOverridePrompt extends QuestsEditorStringPrompt {
        public QuestPlannerOverridePrompt(final ConversationContext context) {
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
            text = text.replace("<true>", Language.get("true"));
            text = text.replace("<false>", Language.get("false"));
            return text;
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Language.get("true");
            case 2:
                return ChatColor.YELLOW + Language.get("false");
            case 3:
                return ChatColor.RED + Language.get("cmdClear");
            case 4:
                return ChatColor.RED + Language.get("cmdCancel");
            default:
                return null;
            }
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = Language.get("optBooleanPrompt");
            text = text.replace("<true>", Language.get("true"));
            text = text.replace("<false>", Language.get("false"));
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Language.get("cmdCancel")) && !input.equalsIgnoreCase(Language.get("cmdClear"))) {
                if (input.startsWith("t") || input.equalsIgnoreCase(Language.get("true"))
                        || input.equalsIgnoreCase(Language.get("yesWord"))) {
                    context.setSessionData(Key.PLN_OVERRIDE, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(Language.get("false"))
                        || input.equalsIgnoreCase(Language.get("noWord"))) {
                    context.setSessionData(Key.PLN_OVERRIDE, false);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Language.get("itemCreateInvalidInput"));
                    return new QuestPlannerOverridePrompt(context);
                }
            } else if (input.equalsIgnoreCase(Language.get("cmdClear"))) {
                context.setSessionData(Key.PLN_OVERRIDE, null);
                return new QuestPlannerPrompt(context);
            }
            return new QuestPlannerPrompt(context);
        }
    }
    
    private String getPrettyDate(final String formattedDate) {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/dd/MM");
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
