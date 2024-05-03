/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.objectives;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMainPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class QuestMobsPrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public QuestMobsPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("stageEditorMobs");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
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
        switch(number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorKillMobs");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorTameMobs");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorCatchFish");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorMilkCows");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("stageEditorShearSheep");
        case 6:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(pref + Key.S_MOB_TYPES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + Key.S_MOB_TYPES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + Key.S_MOB_AMOUNTS);
                if (mobs != null && amounts != null) {
                    if (context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS) == null) {
                        for (int i = 0; i < mobs.size(); i++) {
                            if (BukkitMiscUtil.getProperMobType(mobs.get(i)) != null) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                        .append(BukkitMiscUtil.getPrettyMobName(Objects.requireNonNull(BukkitMiscUtil
                                                .getProperMobType(mobs.get(i))))).append(ChatColor.GRAY).append(" x ")
                                        .append(ChatColor.DARK_AQUA).append(amounts.get(i));
                            }
                        }
                    } else {
                        final LinkedList<String> locations
                                = (LinkedList<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS);
                        final LinkedList<Integer> radii
                                = (LinkedList<Integer>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                        final LinkedList<String> names
                                = (LinkedList<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
                        if (locations != null && radii != null && names != null) {
                            for (int i = 0; i < mobs.size(); i++) {
                                String msg = BukkitLang.get("blocksWithin");
                                msg = msg.replace("<amount>", String.valueOf(radii.get(i)));
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                        .append(BukkitMiscUtil.getPrettyMobName(Objects.requireNonNull(BukkitMiscUtil
                                                .getProperMobType(mobs.get(i))))).append(ChatColor.GRAY).append(" x ")
                                        .append(ChatColor.DARK_AQUA).append(amounts.get(i)).append(ChatColor.GRAY)
                                        .append(msg).append(ChatColor.YELLOW).append(names.get(i)).append(" (")
                                        .append(locations.get(i)).append(")");
                            }
                        }
                    }
                }
                return text.toString();
            }
        case 2:
            if (context.getSessionData(pref + Key.S_TAME_TYPES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + Key.S_TAME_TYPES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + Key.S_TAME_AMOUNTS);
                if (mobs != null && amounts != null) {
                    for (int i = 0; i < mobs.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(mobs.get(i)).append(ChatColor.GRAY).append(" x ").append(ChatColor.AQUA)
                                .append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 3:
            if (context.getSessionData(pref + Key.S_FISH) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer fish = (Integer) context.getSessionData(pref + Key.S_FISH);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + fish + " " + BukkitLang.get("stageEditorFish")
                        + ChatColor.GRAY + ")";
            }
        case 4:
            if (context.getSessionData(pref + Key.S_COW_MILK) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer cows = (Integer) context.getSessionData(pref + Key.S_COW_MILK);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + cows + " " + BukkitLang.get("stageEditorCows")
                        + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData(pref + Key.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> colors = (LinkedList<String>) context.getSessionData(pref + Key.S_SHEAR_COLORS);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + Key.S_SHEAR_AMOUNTS);
                if (colors != null && amounts != null) {
                    for (int i = 0; i < colors.size(); i++) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(colors.get(i)).append(ChatColor.GRAY).append(" x ").append(ChatColor.AQUA)
                                .append(amounts.get(i));
                    }
                }
                return text.toString();
            }
        case 6:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);

        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            return new QuestMobsKillListPrompt(context);
        case 2:
            return new QuestMobsTameListPrompt(context);
        case 3:
            return new QuestMobsFishPrompt(context);
        case 4:
            return new QuestMobsCowsPrompt(context);
        case 5:
            return new QuestMobsShearListPrompt(context);
        case 6:
            try {
                return new QuestStageMainPrompt(stageNum, context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new QuestMobsPrompt(stageNum, context);
        }
    }

    public class QuestMobsKillListPrompt extends QuestsEditorNumericPrompt {
        
        public QuestMobsKillListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorKillMobs");
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
                    if (context.getForWhom() instanceof Player) {
                        return ChatColor.BLUE;
                    } else {
                        return ChatColor.GRAY;
                    }
                case 6:
                    return ChatColor.RED;
                case 7:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetMobAmounts");
            case 3:
                if (context.getForWhom() instanceof Player) {
                    return ChatColor.YELLOW + BukkitLang.get("stageEditorSetKillLocations");
                } else {
                    return ChatColor.GRAY + BukkitLang.get("stageEditorSetKillLocations");
                }
            case 4:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetKillLocationRadii");
            case 5:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetKillLocationNames");
            case 6:
                return ChatColor.RED + BukkitLang.get("clear");
            case 7:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + Key.S_MOB_TYPES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobTypes = (List<String>) context.getSessionData(pref + Key.S_MOB_TYPES);
                    if (mobTypes != null) {
                        for (final String s : mobTypes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + Key.S_MOB_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobAmounts = (List<Integer>) context.getSessionData(pref + Key.S_MOB_AMOUNTS);
                    if (mobAmounts != null) {
                        for (final Integer i : mobAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobsKillLocations
                            = (List<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS);
                    if (mobsKillLocations != null) {
                        for (final String s : mobsKillLocations) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 4:
                if (context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobKillLocationsRadius
                            = (List<Integer>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                    if (mobKillLocationsRadius != null) {
                        for (final int i : mobKillLocationsRadius) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 5:
                if (context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobKillLocationsNames
                            = (List<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
                    if (mobKillLocationsNames != null) {
                        for (final String s : mobKillLocationsNames) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 6:
            case 7:
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

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestMobsTypesPrompt(context);
            case 2:
                return new QuestMobsAmountsPrompt(context);
            case 3:
                if (context.getForWhom() instanceof Player) {
                    final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.put(((Player) context.getForWhom()).getUniqueId(), null);
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                    return new QuestMobsLocationPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                    return new QuestMobsKillListPrompt(context);
                }
            case 4:
                return new QuestMobsRadiiPrompt(context);
            case 5:
                return new QuestMobsLocationNamesPrompt(context);
            case 6:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + Key.S_MOB_TYPES, null);
                context.setSessionData(pref + Key.S_MOB_AMOUNTS, null);
                context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS, null);
                context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, null);
                context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES, null);
                return new QuestMobsKillListPrompt(context);
            case 7:
                final int one;
                final int two;
                final int three;
                final int four;
                final int five;
                final List<String> types = (List<String>) context.getSessionData(pref + Key.S_MOB_TYPES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + Key.S_MOB_AMOUNTS);
                final List<String> locations = (List<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS);
                final List<Integer> radii
                        = (List<Integer>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                final List<String> names = (List<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
                if (types != null) {
                    one = types.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (locations != null) {
                    three = locations.size();
                } else {
                    three = 0;
                }
                if (radii != null) {
                    four = radii.size();
                } else {
                    four = 0;
                }
                if (names != null) {
                    five = names.size();
                } else {
                    five = 0;
                }
                if (one == two) {
                    if (three != 0 || four != 0 || five != 0) {
                        if (two == three && three == four && four == five) {
                            return new QuestMobsPrompt(stageNum, context);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                            return new QuestMobsKillListPrompt(context);
                        }
                    } else {
                        return new QuestMobsPrompt(stageNum, context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestMobsKillListPrompt(context);
                }
            default:
                return new QuestMobsPrompt(stageNum, context);
            }
        }
    }

    public class QuestMobsTypesPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsTypesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMobsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<EntityType> mobArr = new LinkedList<>(Arrays.asList(EntityType.values()));
            final List<EntityType> toRemove = new LinkedList<>();
            for (final EntityType type : mobArr) {
                if (!type.isAlive() || type.name().equals("PLAYER")) {
                    toRemove.add(type);
                }
            }
            mobArr.removeAll(toRemove);
            mobArr.sort(Comparator.comparing(EntityType::name));
            for (int i = 0; i < mobArr.size(); i++) {
                mobs.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()));
                if (i < (mobArr.size() - 1)) {
                     mobs.append(ChatColor.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return mobs.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                .replace("<input>", s));
                        return new QuestMobsTypesPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_MOB_TYPES, mobTypes);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (context.getSessionData(pref + Key.S_MOB_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) context.getSessionData(pref + Key.S_MOB_AMOUNTS);
                }
                if (amounts != null) {
                    for (int i = 0; i < mobTypes.size(); i++) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                context.setSessionData(pref + Key.S_MOB_AMOUNTS, amounts);
            }
            return new QuestMobsKillListPrompt(context);
        }
    }

    public class QuestMobsAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsAmountsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMobAmountsPrompt");
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
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new QuestMobsAmountsPrompt(context);
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new QuestMobsAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_MOB_AMOUNTS, mobAmounts);
            }
            return new QuestMobsKillListPrompt(context);
        }
    }

    public class QuestMobsLocationPrompt extends QuestsEditorStringPrompt {

        public QuestMobsLocationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMobLocationPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final Block block = plugin.getQuestFactory().getSelectedKillLocations().get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(pref + Key.S_MOB_KILL_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS, locations);
                    final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoBlock"));
                    return new QuestMobsLocationPrompt(context);
                }
                return new QuestMobsKillListPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedKillLocations(temp);
                return new QuestMobsKillListPrompt(context);
            } else {
                return new QuestMobsLocationPrompt(context);
            }
        }
    }

    public class QuestMobsRadiiPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsRadiiPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMobLocationRadiiPrompt");
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
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new QuestMobsRadiiPrompt(context);
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidItemName")
                                .replace("<input>", s));
                        return new QuestMobsRadiiPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, radii);
            }
            return new QuestMobsKillListPrompt(context);
        }
    }

    public class QuestMobsLocationNamesPrompt extends QuestsEditorStringPrompt {

        public QuestMobsLocationNamesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMobLocationNamesPrompt");
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
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                context.setSessionData(pref + Key.S_MOB_KILL_LOCATIONS_NAMES, locNames);
            }
            return new QuestMobsKillListPrompt(context);
        }
    }
    
    public class QuestMobsTameListPrompt extends QuestsEditorNumericPrompt {

        public QuestMobsTameListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorTameMobs");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetTameAmounts");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + Key.S_TAME_TYPES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> tameTypes = (List<String>) context.getSessionData(pref + Key.S_TAME_TYPES);
                    if (tameTypes != null) {
                        for (final String s : tameTypes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + Key.S_TAME_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> tameAmounts = (List<Integer>) context.getSessionData(pref + Key.S_TAME_AMOUNTS);
                    if (tameAmounts != null) {
                        for (final Integer i : tameAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
            case 4:
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

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i)).append("\n");
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestMobsTameTypesPrompt(context);
            case 2:
                return new QuestMobsTameAmountsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + Key.S_TAME_TYPES, null);
                context.setSessionData(pref + Key.S_TAME_AMOUNTS, null);
                return new QuestMobsTameListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> types = (List<String>) context.getSessionData(pref + Key.S_TAME_TYPES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + Key.S_TAME_AMOUNTS);
                if (types != null) {
                    one = types.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new QuestMobsPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestMobsTameListPrompt(context);
                }
            default:
                return new QuestMobsPrompt(stageNum, context);
            }
        }
    }

    public class QuestMobsTameTypesPrompt extends QuestsEditorStringPrompt {

        public QuestMobsTameTypesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMobsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final List<EntityType> mobArr = new LinkedList<>(Arrays.asList(EntityType.values()));
            final List<EntityType> toRemove = new LinkedList<>();
            for (final EntityType type : mobArr) {
                final Class<? extends Entity> ec = type.getEntityClass();
                if (!type.isAlive() || (ec != null && !Tameable.class.isAssignableFrom(ec))) {
                    toRemove.add(type);
                }
            }
            mobArr.removeAll(toRemove);
            mobArr.sort(Comparator.comparing(EntityType::name));
            for (int i = 0; i < mobArr.size(); i++) {
                mobs.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()));
                if (i < (mobArr.size() - 1)) {
                     mobs.append(ChatColor.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return mobs.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperMobType(s) != null) {
                        final EntityType type = BukkitMiscUtil.getProperMobType(s);
                        if (type != null) {
                            final Class<? extends Entity> ec = type.getEntityClass();
                            if (type.isAlive() && (ec != null && Tameable.class.isAssignableFrom(ec))) {
                                mobTypes.add(s);
                                context.setSessionData(pref + Key.S_TAME_TYPES, mobTypes);

                                LinkedList<Integer> amounts = new LinkedList<>();
                                if (context.getSessionData(pref + Key.S_TAME_AMOUNTS) != null) {
                                    amounts = (LinkedList<Integer>) context.getSessionData(pref + Key.S_TAME_AMOUNTS);
                                }
                                if (amounts != null) {
                                    for (int i = 0; i < mobTypes.size(); i++) {
                                        if (i >= amounts.size()) {
                                            amounts.add(1);
                                        }
                                    }
                                }
                                context.setSessionData(pref + Key.S_TAME_AMOUNTS, amounts);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                        .replace("<input>", s));
                                return new QuestMobsTameTypesPrompt(context);
                            }
                        }
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                .replace("<input>", s));
                        return new QuestMobsTameTypesPrompt(context);
                    }
                }
            }
            return new QuestMobsTameListPrompt(context);
        }
    }

    public class QuestMobsTameAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsTameAmountsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorTameAmountsPrompt");
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
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new QuestMobsTameAmountsPrompt(context);
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new QuestMobsTameAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_TAME_AMOUNTS, mobAmounts);
            }
            return new QuestMobsTameListPrompt(context);
        }
    }

    public class QuestMobsFishPrompt extends QuestsEditorStringPrompt {

        public QuestMobsFishPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorCatchFishPrompt");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorPositiveAmount"));
                        return new QuestMobsFishPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(pref + Key.S_FISH, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestMobsFishPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(pref + Key.S_FISH, null);
            }
            return new QuestMobsPrompt(stageNum, context);
        }
    }

    public class QuestMobsCowsPrompt extends QuestsEditorStringPrompt {

        public QuestMobsCowsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorMilkCowsPrompt");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorPositiveAmount"));
                        return new QuestMobsCowsPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(pref + Key.S_COW_MILK, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestMobsCowsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(pref + Key.S_COW_MILK, null);
            }
            return new QuestMobsPrompt(stageNum, context);
        }
    }

    public class QuestMobsShearListPrompt extends QuestsEditorNumericPrompt {

        public QuestMobsShearListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorShearSheep");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetShearColors");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetShearAmounts");
            case 3:
                return ChatColor.RED + BukkitLang.get("clear");
            case 4:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + Key.S_SHEAR_COLORS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> shearColors = (List<String>) context.getSessionData(pref + Key.S_SHEAR_COLORS);
                    if (shearColors != null) {
                        for (final String s : shearColors) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + Key.S_SHEAR_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> shearAmounts
                            = (List<Integer>) context.getSessionData(pref + Key.S_SHEAR_AMOUNTS);
                    if (shearAmounts != null) {
                        for (final Integer i : shearAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
            case 4:
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

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new QuestMobsShearColorsPrompt(context);
            case 2:
                return new QuestMobsShearAmountsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + Key.S_SHEAR_COLORS, null);
                context.setSessionData(pref + Key.S_SHEAR_AMOUNTS, null);
                return new QuestMobsShearListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> colors = (List<String>) context.getSessionData(pref + Key.S_SHEAR_COLORS);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + Key.S_SHEAR_AMOUNTS);
                if (colors != null) {
                    one = colors.size();
                } else {
                    one = 0;
                }
                if (amounts != null) {
                    two = amounts.size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new QuestMobsPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    return new QuestMobsShearListPrompt(context);
                }
            default:
                return new QuestMobsPrompt(stageNum, context);
            }
        }
    }

    public class QuestMobsShearColorsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsShearColorsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("stageEditorColors");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorShearColorsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder cols = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle(context) + " - \n");
            final DyeColor[] colArr = DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {
                if (i < (colArr.length - 1)) {
                    cols.append(BukkitMiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append(", ");
                } else {
                    cols.append(BukkitMiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append("\n");
                }
            }
            return cols.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> colors = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperDyeColor(s) != null) {
                        colors.add(s);
                        context.setSessionData(pref + Key.S_SHEAR_COLORS, colors);

                        LinkedList<Integer> amounts = new LinkedList<>();
                        if (context.getSessionData(pref + Key.S_SHEAR_AMOUNTS) != null) {
                            amounts = (LinkedList<Integer>) context.getSessionData(pref + Key.S_SHEAR_AMOUNTS);
                        }
                        if (amounts != null) {
                            for (int i = 0; i < colors.size(); i++) {
                                if (i >= amounts.size()) {
                                    amounts.add(1);
                                }
                            }
                        }
                        context.setSessionData(pref + Key.S_SHEAR_AMOUNTS, amounts);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidDye")
                                .replace("<input>", s));
                        return new QuestMobsShearColorsPrompt(context);
                    }
                }
            }
            return new QuestMobsShearListPrompt(context);
        }
    }

    public class QuestMobsShearAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsShearAmountsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("stageEditorShearAmountsPrompt");
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
                final LinkedList<Integer> shearAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new QuestMobsShearAmountsPrompt(context);
                        }
                        shearAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new QuestMobsShearAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + Key.S_SHEAR_AMOUNTS, shearAmounts);
            }
            return new QuestMobsShearListPrompt(context);
        }
    }
}
