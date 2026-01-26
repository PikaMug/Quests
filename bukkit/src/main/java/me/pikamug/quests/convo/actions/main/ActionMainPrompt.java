/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.main;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.tasks.ActionEffectPrompt;
import me.pikamug.quests.convo.actions.tasks.ActionPlayerPrompt;
import me.pikamug.quests.convo.actions.tasks.ActionTimerPrompt;
import me.pikamug.quests.convo.actions.tasks.ActionWeatherPrompt;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.entity.BukkitQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionMainPrompt extends ActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ActionMainPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }

    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return BukkitLang.get("event") + ": " + SessionData.get(uuid, Key.A_NAME);
    }

    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 8:
            return ChatColor.BLUE;
        case 7:
            if (plugin.getDependencies().getDenizenApi() == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 9:
            return ChatColor.GREEN;
        case 10:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetName");
        case 2:
            return ChatColor.GOLD + BukkitLang.get("eventEditorPlayer");
        case 3:
            return ChatColor.GOLD + BukkitLang.get("eventEditorTimer");
        case 4:
            return ChatColor.GOLD + BukkitLang.get("eventEditorEffect");
        case 5:
            return ChatColor.GOLD + BukkitLang.get("eventEditorWeather");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobSpawns");
        case 7:
            if (plugin.getDependencies().getDenizenApi() == null) {
                return ChatColor.GRAY + BukkitLang.get("stageEditorDenizenScript");
            } else {
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDenizenScript");
            }
        case 8:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorFailQuest");
        case 9:
            return ChatColor.GREEN + BukkitLang.get("save");
        case 10:
            return ChatColor.RED + BukkitLang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 9:
        case 10:
            return "";
        case 6:
            if (SessionData.get(uuid, Key.A_MOBS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final LinkedList<QuestMob> mobs = (LinkedList<QuestMob>) SessionData.get(uuid, Key.A_MOBS);
                final StringBuilder text = new StringBuilder();
                if (mobs != null) {
                    for (final QuestMob mob : mobs) {
                        final BukkitQuestMob qm = (BukkitQuestMob) mob;
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                .append(qm.getType().name())
                                .append((qm.getName() != null) ? " (" + qm.getName() + ")" : "").append(ChatColor.GRAY)
                                .append(" x ").append(ChatColor.DARK_AQUA).append(qm.getSpawnAmounts())
                                .append("\n").append(ChatColor.GRAY).append("        \u2515 ").append(ChatColor.GREEN)
                                .append(BukkitConfigUtil.getLocationInfo(qm.getSpawnLocation()));
                    }
                }
                return text.toString();
            }
        case 7:
            if (plugin.getDependencies().getDenizenApi() == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            } else {
                if (SessionData.get(uuid, Key.A_DENIZEN) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_DENIZEN)
                            + ChatColor.GRAY + ")";
                }
            }
        case 8:
            if (SessionData.get(uuid, Key.A_FAIL_QUEST) == null) {
                return ChatColor.GRAY + "(" + ChatColor.RED + BukkitLang.get("false") + ChatColor.GRAY + ")";
            } else {
                final Boolean failOpt = (Boolean) SessionData.get(uuid, Key.A_FAIL_QUEST);
                return ChatColor.GRAY + "(" + (Boolean.TRUE.equals(failOpt) ? ChatColor.GREEN + BukkitLang.get("true")
                        : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitActionsEditorPostOpenNumericPromptEvent event
                = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle().replaceFirst(": ", ": "
                + ChatColor.AQUA) + ChatColor.GOLD + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new ActionNamePrompt(uuid).start();
            break;
        case 2:
            new ActionPlayerPrompt(uuid).start();
            break;
        case 3:
            new ActionTimerPrompt(uuid).start();
            break;
        case 4:
            new ActionEffectPrompt(uuid).start();
            break;
        case 5:
            new ActionWeatherPrompt(uuid).start();
            break;
        case 6:
            new ActionMobListPrompt(uuid).start();
            break;
        case 7:
            if (plugin.getDependencies().getDenizenApi() != null) {
                if (!plugin.hasLimitedAccess(uuid)) {
                    new ActionDenizenPrompt(uuid).start();
                } else {
                    Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + BukkitLang.get("modeDeny")
                            .replace("<mode>", BukkitLang.get("trialMode")));
                    new ActionMainPrompt(uuid).start();
                }
            } else {
                new ActionMainPrompt(uuid).start();
            }
            break;
        case 8:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_FAIL_QUEST);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.A_FAIL_QUEST, false);
            } else {
                SessionData.set(uuid, Key.A_FAIL_QUEST, true);
            }
            new ActionMainPrompt(uuid).start();
            break;
        case 9:
            if (SessionData.get(uuid, Key.A_OLD_ACTION) != null) {
                new ActionSavePrompt(uuid, (String) SessionData.get(uuid, Key.A_OLD_ACTION)).start();
            } else {
                new ActionSavePrompt(uuid, null).start();
            }
            break;
        case 10:
            new ActionExitPrompt(uuid).start();
            break;
        default:
            new ActionMainPrompt(uuid).start();
            break;
        }
    }
    
    public class ActionNamePrompt extends ActionsEditorStringPrompt {

        public ActionNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorEnterEventName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                for (final Action a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorExists"));
                        new ActionNamePrompt(uuid).start();
                        return;
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    new ActionNamePrompt(uuid).start();
                    return;
                }
                if (input.contains(",")) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    new ActionNamePrompt(uuid).start();
                    return;
                }
                actionNames.remove((String) SessionData.get(uuid, Key.A_NAME));
                SessionData.set(uuid, Key.A_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
            }
            new ActionMainPrompt(uuid).start();
        }
    }

    public class ActionMobListPrompt extends ActionsEditorIntegerPrompt {
        
        public ActionMobListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorMobSpawnsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                return ChatColor.RED;
            case 3:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }
        
        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorAddMobTypes");
            case 2:
                return ChatColor.RED + BukkitLang.get("clear");
            case 3:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.A_MOBS) != null) {
                    @SuppressWarnings("unchecked")
                    final
                    LinkedList<QuestMob> mobs = (LinkedList<QuestMob>) SessionData.get(uuid, Key.A_MOBS);
                    final StringBuilder text = new StringBuilder();
                    if (mobs != null) {
                        for (final QuestMob mob : mobs) {
                            final BukkitQuestMob qm = (BukkitQuestMob) mob;
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(qm.getType().name())
                                    .append((qm.getName() != null) ? " (" + qm.getName() + ")" : "")
                                    .append(" x ").append(qm.getSpawnAmounts()).append("\n").append(ChatColor.GRAY)
                                    .append("        \u2515 ").append(ChatColor.GREEN)
                                    .append(BukkitConfigUtil.getLocationInfo(qm.getSpawnLocation()));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenNumericPromptEvent event
                    = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
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
                new ActionMobPrompt(uuid, null).start();
                break;
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorMobSpawnsCleared"));
                SessionData.set(uuid, Key.A_MOBS, null);
                new ActionMobListPrompt(uuid).start();
                break;
            case 3:
                new ActionMainPrompt(uuid).start();
                break;
            default:
                new ActionMobListPrompt(uuid).start();
                break;
            }
        }
    }

    public class ActionMobPrompt extends ActionsEditorIntegerPrompt {

        private BukkitQuestMob questMob;

        public ActionMobPrompt(final @NotNull UUID uuid, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }
        
        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorAddMobTypesTitle");
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
                return ChatColor.RED;
            case 7:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }
        
        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobName");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobType");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobSpawnAmount");
            case 4:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorAddSpawnLocation");
            case 5:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetEquipment");
            case 6:
                return ChatColor.RED + BukkitLang.get("cancel");
            case 7:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.GRAY + "(" + (questMob.getName() == null ? BukkitLang.get("noneSet") : ChatColor.AQUA
                        + questMob.getName()) + ChatColor.GRAY + ")";
            case 2:
                return ChatColor.GRAY + "(" + (questMob.getType() == null ? BukkitLang.get("noneSet") : ChatColor.AQUA
                        + questMob.getType().name()) + ChatColor.GRAY + ")";
            case 3:
                return ChatColor.GRAY + "(" + (questMob.getSpawnAmounts() == null ? ChatColor.GRAY
                        + BukkitLang.get("noneSet") : ChatColor.AQUA + "" + questMob.getSpawnAmounts()) + ChatColor.GRAY
                        + ")";
            case 4:
                return ChatColor.GRAY + "(" + (questMob.getSpawnLocation() == null ? ChatColor.GRAY
                        + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitConfigUtil.getLocationInfo(questMob
                        .getSpawnLocation())) + ChatColor.GRAY + ")";
            case 5:
                return ChatColor.GRAY + "(" + (questMob.getInventory()[0] == null ? ChatColor.GRAY
                        + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitItemUtil.getDisplayString(questMob.getInventory()[0]))
                        + ChatColor.GRAY + ")";
            case 6:
            case 7:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            if (questMob == null) {
                questMob = new BukkitQuestMob();
            }
            
            final BukkitActionsEditorPostOpenNumericPromptEvent event
                    = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i))
                        .append(ChatColor.GRAY).append(" ").append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ActionMobNamePrompt(uuid, questMob).start();
                break;
            case 2:
                new ActionMobTypePrompt(uuid, questMob).start();
                break;
            case 3:
                new ActionMobAmountPrompt(uuid, questMob).start();
                break;
            case 4:
                if (sender instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> selectedMobLocations
                            = plugin.getActionFactory().getSelectedMobLocations();
                    if (BukkitMiscUtil.getWorlds().isEmpty()) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                        new ActionMobLocationPrompt(uuid, questMob).start();
                    }
                    selectedMobLocations.put(uuid, Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                    plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                    new ActionMobLocationPrompt(uuid, questMob).start();
                } else {
                    sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                    new ActionMainPrompt(uuid).start();
                }
                break;
            case 5:
                new ActionMobEquipmentPrompt(uuid, questMob).start();
                break;
            case 6:
                new ActionMobListPrompt(uuid).start();
                break;
            case 7:
                if (questMob.getType() == null) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("eventEditorMustSetMobTypesFirst"));
                    new ActionMobPrompt(uuid, questMob).start();
                } else if (questMob.getSpawnLocation() == null) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("eventEditorMustSetMobLocationFirst"));
                    new ActionMobPrompt(uuid, questMob).start();
                } else if (questMob.getSpawnAmounts() == null) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("eventEditorMustSetMobAmountsFirst"));
                    new ActionMobPrompt(uuid, questMob).start();
                }
                final LinkedList<QuestMob> list = SessionData.get(uuid, Key.A_MOBS) == null ? new LinkedList<>()
                        : (LinkedList<QuestMob>) SessionData.get(uuid, Key.A_MOBS);
                if (list != null) {
                    list.add(questMob);
                    SessionData.set(uuid, Key.A_MOBS, list);
                }
                new ActionMobListPrompt(uuid).start();
                break;
            default:
                new ActionMobPrompt(uuid, questMob).start();
                break;
            }
        }
    }

    public class ActionMobEquipmentPrompt extends ActionsEditorIntegerPrompt {

        private BukkitQuestMob questMob;
        private Integer itemIndex = -1;

        public ActionMobEquipmentPrompt(final @NotNull UUID uuid, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }

        private final int size = 12;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorAddEquipmentTitle");
        }

        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    return ChatColor.BLUE;
                case 11:
                    return ChatColor.RED;
                case 12:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
                case 1:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobItemInHand");
                case 2:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobItemInHandDrop");
                case 3:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobBoots");
                case 4:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobBootsDrop");
                case 5:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobLeggings");
                case 6:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobLeggingsDrop");
                case 7:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobChestPlate");
                case 8:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobChestPlateDrop");
                case 9:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobHelmet");
                case 10:
                    return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMobHelmetDrop");
                case 11:
                    return ChatColor.RED + BukkitLang.get("cancel");
                case 12:
                    return ChatColor.GREEN + BukkitLang.get("done");
                default:
                    return null;
            }
        }

        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
                case 1:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[0] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitItemUtil.getDisplayString(questMob
                            .getInventory()[0])) + ChatColor.GRAY + ")";
                case 2:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[0] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[0])
                            + ChatColor.GRAY + ")";
                case 3:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[1] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitItemUtil.getDisplayString(questMob
                            .getInventory()[1])) + ChatColor.GRAY + ")";
                case 4:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[1] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[1])
                            + ChatColor.GRAY + ")";
                case 5:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[2] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitItemUtil.getDisplayString(questMob
                            .getInventory()[2])) + ChatColor.GRAY + ")";
                case 6:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[2] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[2])
                            + ChatColor.GRAY + ")";
                case 7:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[3] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitItemUtil.getDisplayString(questMob
                            .getInventory()[3])) + ChatColor.GRAY + ")";
                case 8:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[3] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[3])
                            + ChatColor.GRAY + ")";
                case 9:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[4] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + BukkitItemUtil.getDisplayString(questMob
                            .getInventory()[4])) + ChatColor.GRAY + ")";
                case 10:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[4] == null ? ChatColor.GRAY
                            + BukkitLang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[4])
                            + ChatColor.GRAY + ")";
                case 11:
                case 12:
                    return "";
                default:
                    return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            if (questMob == null) {
                questMob = new BukkitQuestMob();
            }
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (itemIndex >= 0) {
                    questMob.getInventory()[itemIndex] = ((ItemStack) SessionData.get(uuid, "tempStack"));
                    try {
                        if (questMob.getDropChances()[itemIndex] == null) {
                            final Float[] chances = questMob.getDropChances();
                            chances[itemIndex] = 1.0f;
                            questMob.setDropChances(chances);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    itemIndex = -1;
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitActionsEditorPostOpenNumericPromptEvent event
                    = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i))
                        .append(ChatColor.GRAY).append(" ").append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            switch (input.intValue()) {
                case 1:
                    itemIndex = 0;
                    new ItemStackPrompt(uuid, ActionMobEquipmentPrompt.this).start();
                    break;
                case 2:
                    new ActionMobDropPrompt(uuid, 0, questMob).start();
                    break;
                case 3:
                    itemIndex = 1;
                    new ItemStackPrompt(uuid, ActionMobEquipmentPrompt.this).start();
                    break;
                case 4:
                    new ActionMobDropPrompt(uuid, 1, questMob).start();
                    break;
                case 5:
                    itemIndex = 2;
                    new ItemStackPrompt(uuid, ActionMobEquipmentPrompt.this).start();
                    break;
                case 6:
                    new ActionMobDropPrompt(uuid, 2, questMob).start();
                    break;
                case 7:
                    itemIndex = 3;
                    new ItemStackPrompt(uuid, ActionMobEquipmentPrompt.this).start();
                    break;
                case 8:
                    new ActionMobDropPrompt(uuid, 3, questMob).start();
                    break;
                case 9:
                    itemIndex = 4;
                    new ItemStackPrompt(uuid, ActionMobEquipmentPrompt.this).start();
                    break;
                case 10:
                    new ActionMobDropPrompt(uuid, 4, questMob).start();
                    break;
                case 11:
                    new ActionMobListPrompt(uuid).start();
                    break;
                default:
                    new ActionMobPrompt(uuid, questMob).start();
                    break;
            }
        }
    }

    public class ActionMobNamePrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobNamePrompt(final @NotNull UUID uuid, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetMobNamePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(String input) {
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new ActionMobPrompt(uuid, questMob).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                questMob.setName(null);
                new ActionMobPrompt(uuid, questMob).start();
            } else {
                input = ChatColor.translateAlternateColorCodes('&', input);
                questMob.setName(input);
                new ActionMobPrompt(uuid, questMob).start();
            }
        }
    }

    public class ActionMobTypePrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobTypePrompt(final @NotNull UUID uuid, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetMobTypesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
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
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (BukkitMiscUtil.getProperMobType(input) != null) {
                    questMob.setType(BukkitMiscUtil.getProperMobType(input));
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                            .replace("<input>", input));
                    new ActionMobTypePrompt(uuid, questMob).start();
                    return;
                }
            }
            new ActionMobPrompt(uuid, questMob).start();
        }
    }

    public class ActionMobAmountPrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobAmountPrompt(final @NotNull UUID uuid, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetMobAmountsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 1) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                .replace("<number>", "1"));
                        new ActionMobAmountPrompt(uuid, questMob).start();
                        return;
                    }
                    questMob.setSpawnAmounts(i);
                    new ActionMobPrompt(uuid, questMob).start();
                    return;
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                    new ActionMobAmountPrompt(uuid, questMob).start();
                    return;
                }
            }
            new ActionMobPrompt(uuid, questMob).start();
        }
    }

    public class ActionMobLocationPrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobLocationPrompt(final @NotNull UUID uuid, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetMobLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase(BukkitLang.get("cmdAdd"))) {
                final ConcurrentHashMap<UUID, Block> selectedMobLocations
                        = plugin.getActionFactory().getSelectedMobLocations();
                final Block block = selectedMobLocations.get(uuid);
                if (block != null) {
                    final Location loc = block.getLocation();
                    questMob.setSpawnLocation(loc);
                    selectedMobLocations.remove(uuid);
                    plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    new ActionMobLocationPrompt(uuid, questMob).start();
                    return;
                }
                new ActionMobPrompt(uuid, questMob).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedMobLocations
                        = plugin.getActionFactory().getSelectedMobLocations();
                selectedMobLocations.remove(uuid);
                plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                new ActionMobPrompt(uuid, questMob).start();
            } else {
                new ActionMobLocationPrompt(uuid, questMob).start();
            }
        }
    }

    public class ActionMobDropPrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;
        private final Integer invIndex;

        public ActionMobDropPrompt(final @NotNull UUID uuid, final int invIndex, final BukkitQuestMob questMob) {
            super(uuid);
            this.questMob = questMob;
            this.invIndex = invIndex;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetDropChance").replace("<least>", "0.0").replaceFirst("<greatest>", "1.0");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                new ActionMobPrompt(uuid, questMob).start();
                return;
            }
            float chance = 0.0f;
            try {
                chance = Float.parseFloat(input);
            } catch (final NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                        .replace("<least>", "0.0").replace("<greatest>", "1.0"));
                new ActionMobDropPrompt(uuid, invIndex, questMob).start();
                return;
            }
            if (chance > 1 || chance < 0) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidRange")
                        .replace("<least>", "0.0").replace("<greatest>", "1.0"));
                new ActionMobDropPrompt(uuid, invIndex, questMob).start();
                return;
            }
            final Float[] temp = questMob.getDropChances();
            temp[invIndex] = chance;
            questMob.setDropChances(temp);
            new ActionMobEquipmentPrompt(uuid, questMob).start();
        }
    }
    
    public class ActionDenizenPrompt extends ActionsEditorStringPrompt {
        
        public ActionDenizenPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("stageEditorDenizenScript");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("stageEditorScriptPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + "- " + getTitle() + " -");
            if (plugin.getDependencies().getDenizenApi() != null
                    && plugin.getDependencies().getDenizenApi().getScriptNames() != null) {
                for (final String s : plugin.getDependencies().getDenizenApi().getScriptNames()) {
                    text.append("\n").append(ChatColor.AQUA).append("- ").append(s);
                }
            }
            return text + "\n" + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (plugin.getDependencies().getDenizenApi().containsScript(input)) {
                    SessionData.set(uuid, Key.A_DENIZEN, input.toUpperCase());
                    new ActionMainPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidScript"));
                    new ActionDenizenPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_DENIZEN, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("stageEditorDenizenCleared"));
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionMainPrompt(uuid).start();
            }
        }
    }

    public class ActionSavePrompt extends ActionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<>();

        public ActionSavePrompt(final @NotNull UUID uuid, final String modifiedName) {
            super(uuid);
            if (modifiedName != null) {
                modName = modifiedName;
                for (final Quest q : plugin.getLoadedQuests()) {
                    for (final Stage s : q.getStages()) {
                        if (s.getFinishAction() != null && s.getFinishAction().getName() != null) {
                            if (s.getFinishAction().getName().equalsIgnoreCase(modifiedName)) {
                                modified.add(q.getName());
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText());
            if (!modified.isEmpty()) {
                text.append("\n").append(ChatColor.RED).append(" ").append(BukkitLang.get("eventEditorModifiedNote"));
                for (final String s : modified) {
                    text.append("\n").append(ChatColor.GRAY).append("    - ").append(ChatColor.DARK_RED).append(s);
                }
                text.append("\n").append(ChatColor.RED).append(" ").append(BukkitLang.get("eventEditorForcedToQuit"));
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return QuestsIntegerPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(uuid) && !plugin.getConfigSettings().canTrialSave()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("modeDeny")
                            .replace("<mode>", BukkitLang.get("trialMode")));
                    new ActionMainPrompt(uuid).start();
                }
                plugin.getActionFactory().saveAction(uuid);
                return;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionSavePrompt(uuid, modName).start();
            }
        }
    }
    
    public class ActionExitPrompt extends ActionsEditorStringPrompt {
        
        public ActionExitPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return QuestsIntegerPrompt.sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final String input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input == null) {
                return;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                sender.sendMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + BukkitLang.get("exited"));
                plugin.getActionFactory().clearData(uuid);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionExitPrompt(uuid).start();
            }
        }
    }
}
