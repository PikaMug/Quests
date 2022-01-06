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

package me.blackvein.quests.convo.quests.objectives;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
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

public class MobsPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;

    public MobsPrompt(final int stageNum, final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
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
        return Lang.get("stageEditorMobs");
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
            return ChatColor.YELLOW + Lang.get("stageEditorKillMobs");
        case 2:
            return ChatColor.YELLOW + Lang.get("stageEditorTameMobs"); 
        case 3:
            return ChatColor.YELLOW + Lang.get("stageEditorCatchFish");
        case 4:
            return ChatColor.YELLOW + Lang.get("stageEditorMilkCows");
        case 5:
            return ChatColor.YELLOW + Lang.get("stageEditorShearSheep");
        case 6:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
                final LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);
                if (mobs != null && amounts != null) {
                    if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                        for (int i = 0; i < mobs.size(); i++) {
                            if (MiscUtil.getProperMobType(mobs.get(i)) != null) {
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                        .append(MiscUtil.getPrettyMobName(Objects.requireNonNull(MiscUtil
                                                .getProperMobType(mobs.get(i))))).append(ChatColor.GRAY).append(" x ")
                                        .append(ChatColor.DARK_AQUA).append(amounts.get(i));
                            }
                        }
                    } else {
                        final LinkedList<String> locations
                                = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                        final LinkedList<Integer> radii
                                = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                        final LinkedList<String> names
                                = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
                        if (locations != null && radii != null && names != null) {
                            for (int i = 0; i < mobs.size(); i++) {
                                String msg = Lang.get("blocksWithin");
                                msg = msg.replace("<amount>", ChatColor.DARK_PURPLE + "" + radii.get(i) + ChatColor.GRAY);
                                text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                        .append(MiscUtil.getPrettyMobName(Objects.requireNonNull(MiscUtil
                                                .getProperMobType(mobs.get(i))))).append(ChatColor.GRAY).append(" x ")
                                        .append(ChatColor.DARK_AQUA).append(amounts.get(i)).append(ChatColor.GRAY).append(msg)
                                        .append(ChatColor.YELLOW).append(names.get(i)).append(" (").append(locations.get(i))
                                        .append(")");
                            }
                        }
                    }
                }
                return text.toString();
            }
        case 2:
            if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
                final LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);
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
            if (context.getSessionData(pref + CK.S_FISH) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer fish = (Integer) context.getSessionData(pref + CK.S_FISH);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + fish + " " + Lang.get("stageEditorFish") 
                        + ChatColor.GRAY + ")";
            }
        case 4:
            if (context.getSessionData(pref + CK.S_COW_MILK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Integer cows = (Integer) context.getSessionData(pref + CK.S_COW_MILK);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + cows + " " + Lang.get("stageEditorCows") 
                        + ChatColor.GRAY + ")";
            }
        case 5:
            if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> colors = (LinkedList<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
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

        if (context.getPlugin() != null) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
        }

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
            return new MobsKillListPrompt(context);
        case 2:
            return new MobsTameListPrompt(context); 
        case 3:
            return new MobsFishPrompt(context);
        case 4:
            return new MobsCowsPrompt(context);
        case 5:
            return new MobsShearListPrompt(context);
        case 6:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new MobsPrompt(stageNum, context);
        }
    }

    public class MobsKillListPrompt extends QuestsEditorNumericPrompt {
        
        public MobsKillListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorKillMobs");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetMobAmounts"); 
            case 3:
                if (context.getForWhom() instanceof Player) {
                    return ChatColor.YELLOW + Lang.get("stageEditorSetKillLocations");
                } else {
                    return ChatColor.GRAY + Lang.get("stageEditorSetKillLocations");
                }
            case 4:
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillLocationRadii");
            case 5:
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillLocationNames");
            case 6:
                return ChatColor.RED + Lang.get("clear");
            case 7:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobTypes = (List<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
                    if (mobTypes != null) {
                        for (final String s : mobTypes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + CK.S_MOB_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobAmounts = (List<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);
                    if (mobAmounts != null) {
                        for (final Integer i : mobAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobsKillLocations
                            = (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    if (mobsKillLocations != null) {
                        for (final String s : mobsKillLocations) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 4:
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobKillLocationsRadius
                            = (List<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                    if (mobKillLocationsRadius != null) {
                        for (final int i : mobKillLocationsRadius) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 5:
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobKillLocationsNames
                            = (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new MobsTypesPrompt(context);
            case 2:
                return new MobsAmountsPrompt(context);
            case 3:
                if (context.getForWhom() instanceof Player) {
                    final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.put(((Player) context.getForWhom()).getUniqueId(), null);
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                    return new MobsLocationPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                    return new MobsKillListPrompt(context);
                }
            case 4:
                return new MobsRadiiPrompt(context);
            case 5:
                return new MobsLocationNamesPrompt(context);
            case 6:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_MOB_TYPES, null);
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, null);
                return new MobsKillListPrompt(context);
            case 7:
                final int one;
                final int two;
                final int three;
                final int four;
                final int five;
                final List<String> types = (List<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);
                final List<String> locations = (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                final List<Integer> radii
                        = (List<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                final List<String> names = (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
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
                            return new MobsPrompt(stageNum, context);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                            return new MobsKillListPrompt(context);
                        }
                    } else {
                        return new MobsPrompt(stageNum, context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobsKillListPrompt(context);
                }
            default:
                return new MobsPrompt(stageNum, context);
            }
        }
    }

    public class MobsTypesPrompt extends QuestsEditorStringPrompt {
        
        public MobsTypesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMobsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                mobs.append(ChatColor.AQUA).append(MiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()));
                if (i < (mobArr.size() - 1)) {
                     mobs.append(ChatColor.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return mobs.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (MiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new MobsTypesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_MOB_TYPES, mobTypes);
            }
            return new MobsKillListPrompt(context);
        }
    }

    public class MobsAmountsPrompt extends QuestsEditorStringPrompt {
        
        public MobsAmountsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMobAmountsPrompt");
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
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new MobsAmountsPrompt(context);
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new MobsAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, mobAmounts);
            }
            return new MobsKillListPrompt(context);
        }
    }

    public class MobsLocationPrompt extends QuestsEditorStringPrompt {

        public MobsLocationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMobLocationPrompt");
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

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Block block = plugin.getQuestFactory().getSelectedKillLocations().get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null) {
                        locations = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(ConfigUtil.getLocationInfo(loc));
                    }
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locations);
                    final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("stageEditorNoBlock"));
                    return new MobsLocationPrompt(context);
                }
                return new MobsKillListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedKillLocations(temp);
                return new MobsKillListPrompt(context);
            } else {
                return new MobsLocationPrompt(context);
            }
        }
    }

    public class MobsRadiiPrompt extends QuestsEditorStringPrompt {
        
        public MobsRadiiPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMobLocationRadiiPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
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
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new MobsRadiiPrompt(context);
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidItemName"));
                        return new MobsRadiiPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, radii);
            }
            return new MobsKillListPrompt(context);
        }
    }

    public class MobsLocationNamesPrompt extends QuestsEditorStringPrompt {

        public MobsLocationNamesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMobLocationNamesPrompt");
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
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(Lang.get("charSemi"))));
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, locNames);
            }
            return new MobsKillListPrompt(context);
        }
    }
    
    public class MobsFishPrompt extends QuestsEditorStringPrompt {

        public MobsFishPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorCatchFishPrompt");
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new MobsFishPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(pref + CK.S_FISH, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new MobsFishPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_FISH, null);
            }
            return new MobsPrompt(stageNum, context);
        }
    }
    
    public class MobsCowsPrompt extends QuestsEditorStringPrompt {

        public MobsCowsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMilkCowsPrompt");
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
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new MobsCowsPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(pref + CK.S_COW_MILK, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new MobsCowsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_COW_MILK, null);
            }
            return new MobsPrompt(stageNum, context);
        }
    }
    
    public class MobsTameListPrompt extends QuestsEditorNumericPrompt {

        public MobsTameListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorTameMobs");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetTameAmounts"); 
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> tameTypes = (List<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
                    if (tameTypes != null) {
                        for (final String s : tameTypes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + CK.S_TAME_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> tameAmounts = (List<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new MobsTameTypesPrompt(context);
            case 2:
                return new MobsTameAmountsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_TAME_TYPES, null);
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, null);
                return new MobsTameListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> types = (List<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);
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
                    return new MobsPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobsTameListPrompt(context);
                }
            default:
                return new MobsPrompt(stageNum, context);
            }
        }
    }

    public class MobsTameTypesPrompt extends QuestsEditorStringPrompt {

        public MobsTameTypesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorMobsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
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
                mobs.append(ChatColor.AQUA).append(MiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()));
                if (i < (mobArr.size() - 1)) {
                     mobs.append(ChatColor.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return mobs.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (MiscUtil.getProperMobType(s) != null) {
                        final EntityType type = MiscUtil.getProperMobType(s);
                        if (type != null) {
                            final Class<? extends Entity> ec = type.getEntityClass();
                            if (type.isAlive() || (ec != null && Tameable.class.isAssignableFrom(ec))) {
                                mobTypes.add(s);
                                context.setSessionData(pref + CK.S_TAME_TYPES, mobTypes);
                            } else {
                                context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED
                                        + Lang.get("stageEditorInvalidMob"));
                                return new MobsTameTypesPrompt(context);
                            }
                        }
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new MobsTameTypesPrompt(context);
                    }
                }
            }
            return new MobsTameListPrompt(context);
        }
    }

    public class MobsTameAmountsPrompt extends QuestsEditorStringPrompt {
        
        public MobsTameAmountsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorTameAmountsPrompt");
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
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new MobsTameAmountsPrompt(context);
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new MobsTameAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, mobAmounts);
            }
            return new MobsTameListPrompt(context);
        }
    }

    public class MobsShearListPrompt extends QuestsEditorNumericPrompt {

        public MobsShearListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorShearSheep");
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
                return ChatColor.YELLOW + Lang.get("stageEditorSetShearColors");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetShearAmounts"); 
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> shearColors = (List<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
                    if (shearColors != null) {
                        for (final String s : shearColors) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> shearAmounts = (List<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
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
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenNumericPromptEvent event
                        = new QuestsEditorPostOpenNumericPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

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
                return new MobsShearColorsPrompt(context);
            case 2:
                return new MobsShearAmountsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_SHEAR_COLORS, null);
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, null);
                return new MobsShearListPrompt(context);
            case 4:
                final int one;
                final int two;
                final List<String> colors = (List<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
                final List<Integer> amounts = (List<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
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
                    return new MobsPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobsShearListPrompt(context);
                }
            default:
                return new MobsPrompt(stageNum, context);
            }
        }
    }

    public class MobsShearColorsPrompt extends QuestsEditorStringPrompt {
        
        public MobsShearColorsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorColors");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorShearColorsPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final QuestsEditorPostOpenStringPromptEvent event
                        = new QuestsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder cols = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle(context) + " - \n");
            final DyeColor[] colArr = DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {
                if (i < (colArr.length - 1)) {
                    cols.append(MiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append(", ");
                } else {
                    cols.append(MiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append("\n");
                }
            }
            return cols.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> colors = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (MiscUtil.getProperDyeColor(s) != null) {
                        colors.add(s);
                        context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidDye"));
                        return new MobsShearColorsPrompt(context);
                    }
                }
            }
            return new MobsShearListPrompt(context);
        }
    }

    public class MobsShearAmountsPrompt extends QuestsEditorStringPrompt {
        
        public MobsShearAmountsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorShearAmountsPrompt");
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
                final LinkedList<Integer> shearAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new MobsShearAmountsPrompt(context);
                        }
                        shearAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new MobsShearAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, shearAmounts);
            }
            return new MobsShearListPrompt(context);
        }
    }
}
