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
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestMobsPrompt extends QuestsEditorNumericPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final int stageNum;
    private final String pref;

    public QuestMobsPrompt(final int stageNum, final UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 6;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("stageEditorMobs");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
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
    public String getAdditionalText(final int number) {
        switch(number) {
        case 1:
            if (SessionData.get(uuid, pref + Key.S_MOB_TYPES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_TYPES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                if (mobs != null && amounts != null) {
                    if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) == null) {
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
                                = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                        final LinkedList<Integer> radii
                                = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                        final LinkedList<String> names
                                = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
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
            if (SessionData.get(uuid, pref + Key.S_TAME_TYPES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> mobs = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_TAME_TYPES);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
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
            if (SessionData.get(uuid, pref + Key.S_FISH) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer fish = (Integer) SessionData.get(uuid, pref + Key.S_FISH);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + fish + " " + BukkitLang.get("stageEditorFish")
                        + ChatColor.GRAY + ")";
            }
        case 4:
            if (SessionData.get(uuid, pref + Key.S_COW_MILK) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Integer cows = (Integer) SessionData.get(uuid, pref + Key.S_COW_MILK);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + cows + " " + BukkitLang.get("stageEditorCows")
                        + ChatColor.GRAY + ")";
            }
        case 5:
            if (SessionData.get(uuid, pref + Key.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> colors = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_SHEAR_COLORS);
                final LinkedList<Integer> amounts
                        = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
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
    public @NotNull String getPromptText() {
        SessionData.set(uuid, pref, Boolean.TRUE);

        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = Bukkit.getEntity(uuid);
        switch(input.intValue()) {
        case 1:
            new QuestMobsKillListPrompt(uuid).start();
        case 2:
            new QuestMobsTameListPrompt(uuid).start();
        case 3:
            new QuestMobsFishPrompt(uuid).start();
        case 4:
            new QuestMobsCowsPrompt(uuid).start();
        case 5:
            new QuestMobsShearListPrompt(uuid).start();
        case 6:
            try {
                new QuestStageMainPrompt(stageNum, uuid).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return;
            }
        default:
            new QuestMobsPrompt(stageNum, uuid).start();
        }
    }

    public class QuestMobsKillListPrompt extends QuestsEditorNumericPrompt {
        
        public QuestMobsKillListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorKillMobs");
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
                    if (Bukkit.getEntity(uuid) instanceof Player) {
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
        public String getSelectionText(final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorSetMobAmounts");
            case 3:
                if (Bukkit.getEntity(uuid) instanceof Player) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, pref + Key.S_MOB_TYPES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobTypes = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_TYPES);
                    if (mobTypes != null) {
                        for (final String s : mobTypes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobAmounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                    if (mobAmounts != null) {
                        for (final Integer i : mobAmounts) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobsKillLocations
                            = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                    if (mobsKillLocations != null) {
                        for (final String s : mobsKillLocations) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 4:
                if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> mobKillLocationsRadius
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                    if (mobKillLocationsRadius != null) {
                        for (final int i : mobKillLocationsRadius) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(i);
                        }
                    }
                    return text.toString();
                }
            case 5:
                if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> mobKillLocationsNames
                            = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new QuestMobsTypesPrompt(uuid).start();
            case 2:
                new QuestMobsAmountsPrompt(uuid).start();
            case 3:
                if (sender instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    if (BukkitMiscUtil.getWorlds().isEmpty()) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                        new QuestMobsKillListPrompt(uuid).start();
                    }
                    temp.put(((Player) sender).getUniqueId(),
                            Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                    new QuestMobsLocationPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                    new QuestMobsKillListPrompt(uuid).start();
                }
            case 4:
                new QuestMobsRadiiPrompt(uuid).start();
            case 5:
                new QuestMobsLocationNamesPrompt(uuid).start();
            case 6:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_MOB_TYPES, null);
                SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, null);
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS, null);
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, null);
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES, null);
                new QuestMobsKillListPrompt(uuid).start();
            case 7:
                final int one;
                final int two;
                final int three;
                final int four;
                final int five;
                final List<String> types = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_TYPES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                final List<String> locations = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                final List<Integer> radii
                        = (List<Integer>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS);
                final List<String> names = (List<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES);
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
                            new QuestMobsPrompt(stageNum, uuid).start();
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                            new QuestMobsKillListPrompt(uuid).start();
                        }
                    } else {
                        new QuestMobsPrompt(stageNum, uuid).start();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestMobsKillListPrompt(uuid).start();
                }
            default:
                new QuestMobsPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestMobsTypesPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsTypesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMobsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return mobs.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                .replace("<input>", s));
                        new QuestMobsTypesPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_TYPES, mobTypes);

                LinkedList<Integer> amounts = new LinkedList<>();
                if (SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS) != null) {
                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_MOB_AMOUNTS);
                }
                if (amounts != null) {
                    for (int i = 0; i < mobTypes.size(); i++) {
                        if (i >= amounts.size()) {
                            amounts.add(1);
                        }
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, amounts);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMobAmountsPrompt");
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
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new QuestMobsAmountsPrompt(uuid).start();
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        new QuestMobsAmountsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_AMOUNTS, mobAmounts);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsLocationPrompt extends QuestsEditorStringPrompt {

        public QuestMobsLocationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMobLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            final Player player = (Player) sender;
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final Block block = plugin.getQuestFactory().getSelectedKillLocations().get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    final LinkedList<String> locations;
                    if (SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS) != null) {
                        locations = (LinkedList<String>) SessionData.get(uuid, pref + Key.S_MOB_KILL_LOCATIONS);
                    } else {
                        locations = new LinkedList<>();
                    }
                    if (locations != null) {
                        locations.add(BukkitConfigUtil.getLocationInfo(loc));
                    }
                    SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS, locations);
                    final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNoBlock"));
                    new QuestMobsLocationPrompt(uuid).start();
                }
                new QuestMobsKillListPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedKillLocations(temp);
                new QuestMobsKillListPrompt(uuid).start();
            } else {
                new QuestMobsLocationPrompt(uuid).start();
            }
        }
    }

    public class QuestMobsRadiiPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsRadiiPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMobLocationRadiiPrompt");
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
                final LinkedList<Integer> radii = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new QuestMobsRadiiPrompt(uuid).start();
                        }
                        radii.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidItemName")
                                .replace("<input>", s));
                        new QuestMobsRadiiPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_RADIUS, radii);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }

    public class QuestMobsLocationNamesPrompt extends QuestsEditorStringPrompt {

        public QuestMobsLocationNamesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMobLocationNamesPrompt");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> locNames = new LinkedList<>(Arrays.asList(input.split(BukkitLang.get("charSemi"))));
                SessionData.set(uuid, pref + Key.S_MOB_KILL_LOCATIONS_NAMES, locNames);
            }
            new QuestMobsKillListPrompt(uuid).start();
        }
    }
    
    public class QuestMobsTameListPrompt extends QuestsEditorNumericPrompt {

        public QuestMobsTameListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorTameMobs");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, pref + Key.S_TAME_TYPES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> tameTypes = (List<String>) SessionData.get(uuid, pref + Key.S_TAME_TYPES);
                    if (tameTypes != null) {
                        for (final String s : tameTypes) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> tameAmounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -\n");
            for (int i = 1; i <= size; i++) {
                text.append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(ChatColor.RESET)
                        .append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i)).append("\n");
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new QuestMobsTameTypesPrompt(uuid).start();
            case 2:
                new QuestMobsTameAmountsPrompt(uuid).start();
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_TAME_TYPES, null);
                SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, null);
                new QuestMobsTameListPrompt(uuid).start();
            case 4:
                final int one;
                final int two;
                final List<String> types = (List<String>) SessionData.get(uuid, pref + Key.S_TAME_TYPES);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
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
                    new QuestMobsPrompt(stageNum, uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestMobsTameListPrompt(uuid).start();
                }
            default:
                new QuestMobsPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestMobsTameTypesPrompt extends QuestsEditorStringPrompt {

        public QuestMobsTameTypesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMobsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
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
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return mobs.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperMobType(s) != null) {
                        final EntityType type = BukkitMiscUtil.getProperMobType(s);
                        if (type != null) {
                            final Class<? extends Entity> ec = type.getEntityClass();
                            if (type.isAlive() && (ec != null && Tameable.class.isAssignableFrom(ec))) {
                                mobTypes.add(s);
                                SessionData.set(uuid, pref + Key.S_TAME_TYPES, mobTypes);

                                LinkedList<Integer> amounts = new LinkedList<>();
                                if (SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS) != null) {
                                    amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_TAME_AMOUNTS);
                                }
                                if (amounts != null) {
                                    for (int i = 0; i < mobTypes.size(); i++) {
                                        if (i >= amounts.size()) {
                                            amounts.add(1);
                                        }
                                    }
                                }
                                SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, amounts);
                            } else {
                                sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                        .replace("<input>", s));
                                new QuestMobsTameTypesPrompt(uuid).start();
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                .replace("<input>", s));
                        new QuestMobsTameTypesPrompt(uuid).start();
                    }
                }
            }
            new QuestMobsTameListPrompt(uuid).start();
        }
    }

    public class QuestMobsTameAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsTameAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorTameAmountsPrompt");
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
                final LinkedList<Integer> mobAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new QuestMobsTameAmountsPrompt(uuid).start();
                        }
                        mobAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        new QuestMobsTameAmountsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_TAME_AMOUNTS, mobAmounts);
            }
            new QuestMobsTameListPrompt(uuid).start();
        }
    }

    public class QuestMobsFishPrompt extends QuestsEditorStringPrompt {

        public QuestMobsFishPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorCatchFishPrompt");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorPositiveAmount"));
                        new QuestMobsFishPrompt(uuid).start();
                    } else if (i > 0) {
                        SessionData.set(uuid, pref + Key.S_FISH, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestMobsFishPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, pref + Key.S_FISH, null);
            }
            new QuestMobsPrompt(stageNum, uuid).start();
        }
    }

    public class QuestMobsCowsPrompt extends QuestsEditorStringPrompt {

        public QuestMobsCowsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorMilkCowsPrompt");
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
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorPositiveAmount"));
                        new QuestMobsCowsPrompt(uuid).start();
                    } else if (i > 0) {
                        SessionData.set(uuid, pref + Key.S_COW_MILK, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new QuestMobsCowsPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, pref + Key.S_COW_MILK, null);
            }
            new QuestMobsPrompt(stageNum, uuid).start();
        }
    }

    public class QuestMobsShearListPrompt extends QuestsEditorNumericPrompt {

        public QuestMobsShearListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorShearSheep");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, pref + Key.S_SHEAR_COLORS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<String> shearColors = (List<String>) SessionData.get(uuid, pref + Key.S_SHEAR_COLORS);
                    if (shearColors != null) {
                        for (final String s : shearColors) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                        }
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<Integer> shearAmounts
                            = (List<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = Bukkit.getEntity(uuid);
            switch(input.intValue()) {
            case 1:
                new QuestMobsShearColorsPrompt(uuid).start();
            case 2:
                new QuestMobsShearAmountsPrompt(uuid).start();
            case 3:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorObjectiveCleared"));
                SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, null);
                SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, null);
                new QuestMobsShearListPrompt(uuid).start();
            case 4:
                final int one;
                final int two;
                final List<String> colors = (List<String>) SessionData.get(uuid, pref + Key.S_SHEAR_COLORS);
                final List<Integer> amounts = (List<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
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
                    new QuestMobsPrompt(stageNum, uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("listsNotSameSize"));
                    new QuestMobsShearListPrompt(uuid).start();
                }
            default:
                new QuestMobsPrompt(stageNum, uuid).start();
            }
        }
    }

    public class QuestMobsShearColorsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsShearColorsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorColors");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorShearColorsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder cols = new StringBuilder(ChatColor.LIGHT_PURPLE + "- " + getTitle() + " - \n");
            final DyeColor[] colArr = DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {
                if (i < (colArr.length - 1)) {
                    cols.append(BukkitMiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append(", ");
                } else {
                    cols.append(BukkitMiscUtil.snakeCaseToUpperCamelCase(colArr[i].name())).append("\n");
                }
            }
            return cols.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = Bukkit.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> colors = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperDyeColor(s) != null) {
                        colors.add(s);
                        SessionData.set(uuid, pref + Key.S_SHEAR_COLORS, colors);

                        LinkedList<Integer> amounts = new LinkedList<>();
                        if (SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS) != null) {
                            amounts = (LinkedList<Integer>) SessionData.get(uuid, pref + Key.S_SHEAR_AMOUNTS);
                        }
                        if (amounts != null) {
                            for (int i = 0; i < colors.size(); i++) {
                                if (i >= amounts.size()) {
                                    amounts.add(1);
                                }
                            }
                        }
                        SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, amounts);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidDye")
                                .replace("<input>", s));
                        new QuestMobsShearColorsPrompt(uuid).start();
                    }
                }
            }
            new QuestMobsShearListPrompt(uuid).start();
        }
    }

    public class QuestMobsShearAmountsPrompt extends QuestsEditorStringPrompt {
        
        public QuestMobsShearAmountsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorShearAmountsPrompt");
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
                final LinkedList<Integer> shearAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new QuestMobsShearAmountsPrompt(uuid).start();
                        }
                        shearAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", input));
                        new QuestMobsShearAmountsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, pref + Key.S_SHEAR_AMOUNTS, shearAmounts);
            }
            new QuestMobsShearListPrompt(uuid).start();
        }
    }
}
