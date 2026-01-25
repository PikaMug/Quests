/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.main;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.options.QuestOptionsPrompt;
import me.pikamug.quests.convo.quests.planner.QuestPlannerPrompt;
import me.pikamug.quests.convo.quests.requirements.QuestRequirementsPrompt;
import me.pikamug.quests.convo.quests.rewards.QuestRewardsPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMenuPrompt;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class QuestMainPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public QuestMainPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }

    private final int size = 14;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        final StringBuilder title = new StringBuilder(BukkitLang.get("quest") + ": " + SessionData.get(uuid, Key.Q_NAME));

        if (plugin.hasLimitedAccess(uuid)) {
            title.append(ChatColor.RED).append(" (").append(BukkitLang.get("trialMode")).append(")");
        } else if (SessionData.get(uuid, Key.Q_ID) != null) {
            title.append(ChatColor.GRAY).append(" (").append(BukkitLang.get("id")).append(":")
                    .append(SessionData.get(uuid, Key.Q_ID)).append(")");
        }
        return title.toString();
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            return ChatColor.BLUE;
        case 5:
            if (Bukkit.getEntity(uuid) instanceof Player) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 13:
            return ChatColor.GREEN;
        case 14:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("questEditorName");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("questEditorAskMessage");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("questEditorFinishMessage");
        case 4:
            if (SessionData.get(uuid, Key.Q_START_NPC) == null || plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatColor.YELLOW + BukkitLang.get("questEditorNPCStart");
            } else {
                return ChatColor.GRAY + BukkitLang.get("questEditorNPCStart");
            }
        case 5:
            if (Bukkit.getEntity(uuid) instanceof Player) {
                return ChatColor.YELLOW + BukkitLang.get("questEditorBlockStart");
            } else {
                return ChatColor.GRAY + BukkitLang.get("questEditorBlockStart");
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.YELLOW + BukkitLang.get("questWGSetRegion");
            } else {
                return ChatColor.GRAY + BukkitLang.get("questWGSetRegion");
            }
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatColor.YELLOW + BukkitLang.get("questEditorSetGUI");
            } else {
                return ChatColor.GRAY + BukkitLang.get("questEditorSetGUI");
            }
        case 8:
            return ChatColor.DARK_AQUA + BukkitLang.get("questEditorReqs");
        case 9:
            return ChatColor.AQUA + BukkitLang.get("questEditorPln");
        case 10:
            return ChatColor.LIGHT_PURPLE + BukkitLang.get("questEditorStages");
        case 11:
            return ChatColor.DARK_PURPLE + BukkitLang.get("questEditorRews");
        case 12:
            return ChatColor.DARK_GREEN + BukkitLang.get("questEditorOpts");
        case 13:
            return ChatColor.GREEN + BukkitLang.get("save");
        case 14:
            return ChatColor.RED + BukkitLang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
            return "";
        case 2:
            return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.Q_ASK_MESSAGE) + ChatColor.RESET
                    + ChatColor.GRAY + ")";
        case 3:
            return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.Q_FINISH_MESSAGE)
                    + ChatColor.RESET + ChatColor.GRAY + ")";
        case 4:
            if (SessionData.get(uuid, Key.Q_START_NPC) == null && plugin.getDependencies().hasAnyNpcDependencies()) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else if (plugin.getDependencies().hasAnyNpcDependencies()) {
                final UUID u = UUID.fromString((String) Objects.requireNonNull(SessionData
                        .get(uuid, Key.Q_START_NPC)));
                return ChatColor.GRAY + "(" + ChatColor.AQUA + plugin.getDependencies().getNpcName(u)
                        + ChatColor.RESET + ChatColor.GRAY + ")";
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.Q_START_BLOCK) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final Location l = (Location) SessionData.get(uuid, Key.Q_START_BLOCK);
                if (l != null && l.getWorld() != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + l.getWorld().getName() + ", " + l.getBlockX() + ", "
                            + l.getBlockY() + ", " + l.getBlockZ() + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (SessionData.get(uuid, Key.Q_REGION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.Q_REGION)
                            + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, Key.Q_GUIDISPLAY) == null) {
                    return ChatColor.GRAY +  "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitItemUtil.getDisplayString((ItemStack)
                            SessionData.get(uuid, Key.Q_GUIDISPLAY)) + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenNumericPromptEvent event = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle().replaceFirst(": ", ": "
                + ChatColor.AQUA) + ChatColor.GOLD + " -");
        try {
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch (input.intValue()) {
        case 1:
            new QuestNamePrompt(uuid).start();
        case 2:
            new QuestAskMessagePrompt(uuid).start();
        case 3:
            new QuestFinishMessagePrompt(uuid).start();
        case 4:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestNPCStartPrompt(uuid).start();
            } else {
                new QuestMainPrompt(uuid).start();
            }
        case 5:
            if (sender instanceof Player) {
                final ConcurrentHashMap<UUID, Block> blockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                if (BukkitMiscUtil.getWorlds().isEmpty()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                    new QuestBlockStartPrompt(uuid).start();
                }
                blockStarts.put(((Player) sender).getUniqueId(),
                        Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                plugin.getQuestFactory().setSelectedBlockStarts(blockStarts);
                new QuestBlockStartPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                new QuestMainPrompt(uuid).start();
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                new QuestRegionPrompt(uuid).start();
            } else {
                new QuestMainPrompt(uuid).start();
            }
        case 7:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                new QuestGuiDisplayPrompt(uuid).start();
            } else {
                new QuestMainPrompt(uuid).start();
            }
        case 8:
            new QuestRequirementsPrompt(uuid).start();
        case 9:
            new QuestPlannerPrompt(uuid).start();
        case 10:
            new QuestStageMenuPrompt(uuid).start();
        case 11:
            new QuestRewardsPrompt(uuid).start();
        case 12:
            new QuestOptionsPrompt(uuid).start();
        case 13:
            new QuestSavePrompt(uuid).start();
        case 14:
            new QuestExitPrompt(uuid).start();
        default:
            new QuestMainPrompt(uuid).start();
        }
    }

    public class QuestNamePrompt extends QuestsEditorStringPrompt {

        public QuestNamePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
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
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        String s = null;
                        if (SessionData.get(uuid, Key.ED_QUEST_EDIT) != null) {
                            s = (String) SessionData.get(uuid, Key.ED_QUEST_EDIT);
                        }
                        if (s != null && !s.equalsIgnoreCase(input)) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorNameExists"));
                            new QuestNamePrompt(uuid).start();
                        }
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorBeingEdited"));
                    new QuestNamePrompt(uuid).start();
                }
                if (input.contains(",")) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorInvalidQuestName"));
                    new QuestNamePrompt(uuid).start();
                }
                questNames.remove((String) SessionData.get(uuid, Key.Q_NAME));
                SessionData.set(uuid, Key.Q_NAME, input);
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
            }
            new QuestMainPrompt(uuid).start();
        }
    }
    
    public class QuestAskMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestAskMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterAskMessage");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (input.startsWith("++")) {
                    if (SessionData.get(uuid, Key.Q_ASK_MESSAGE) != null) {
                        SessionData.set(uuid, Key.Q_ASK_MESSAGE, SessionData.get(uuid, Key.Q_ASK_MESSAGE) + " "
                                + input.substring(2));
                        new QuestMainPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.Q_ASK_MESSAGE, input);
            }
            new QuestMainPrompt(uuid).start();
        }
    }
    
    public class QuestFinishMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestFinishMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterFinishMessage");
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
                if (input.startsWith("++")) {
                    if (SessionData.get(uuid, Key.Q_FINISH_MESSAGE) != null) {
                        SessionData.set(uuid, Key.Q_FINISH_MESSAGE, SessionData.get(uuid, Key.Q_FINISH_MESSAGE) + " "
                                + input.substring(2));
                        new QuestMainPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.Q_FINISH_MESSAGE, input);
            }
            new QuestMainPrompt(uuid).start();
        }
    }
    
    public class QuestNPCStartPrompt extends QuestsEditorStringPrompt {
        
        public QuestNPCStartPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterNPCStart");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            if (Bukkit.getEntity(uuid) instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText();
            }
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final UUID uuid = UUID.fromString(input);
                    if (!plugin.getDependencies().isNpc(uuid)) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                .replace("<input>", input));
                        new QuestNPCStartPrompt(uuid).start();
                    }
                    SessionData.set(uuid, Key.Q_START_NPC, uuid.toString());
                    if (sender instanceof Player) {
                        final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                        selectingNpcs.remove(((Player) sender).getUniqueId());
                        plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                    }
                    new QuestMainPrompt(uuid).start();
                } catch (final IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED 
                            + BukkitLang.get("reqNotAUniqueId").replace("<input>", input));
                    new QuestNPCStartPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.Q_START_NPC, null);
            }
            if (sender instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) sender).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new QuestMainPrompt(uuid).start();
        }
    }
    
    public class QuestBlockStartPrompt extends QuestsEditorStringPrompt {
        
        public QuestBlockStartPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questEditorEnterBlockStart");
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
            final Player player = (Player) sender;
            if (input.equalsIgnoreCase(BukkitLang.get("cmdDone")) || input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                if (input.equalsIgnoreCase(BukkitLang.get("cmdDone"))) {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    final Block block = selectedBlockStarts.get(player.getUniqueId());
                    if (block != null) {
                        final Location loc = block.getLocation();
                        SessionData.set(uuid, Key.Q_START_BLOCK, loc);
                        selectedBlockStarts.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + BukkitLang.get("questEditorNoStartBlockSelected"));
                        new QuestBlockStartPrompt(uuid).start();
                    }
                } else {
                    final ConcurrentHashMap<UUID, Block> selectedBlockStarts
                            = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                new QuestMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (sender instanceof Player) {
                    final ConcurrentHashMap<UUID, Block> selectedBlockStarts
                            = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                SessionData.set(uuid, Key.Q_START_BLOCK, null);
                new QuestMainPrompt(uuid).start();
            }
            new QuestBlockStartPrompt(uuid).start();
        }
    }
    
    public class QuestRegionPrompt extends QuestsEditorStringPrompt {
        
        public QuestRegionPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("questRegionTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("questWGPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle() + "\n");
            boolean any = false;
            for (final World world : plugin.getServer().getWorlds()) {
                final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                final RegionManager regionManager = api.getRegionManager(world);
                if (regionManager != null) {
                    for (final String region : regionManager.getRegions().keySet()) {
                        any = true;
                        text.append(ChatColor.GREEN).append(region).append(", ");
                    }
                }
            }
            if (any) {
                text = new StringBuilder(text.substring(0, text.length() - 2) + "\n");
            } else {
                text.append(ChatColor.GRAY).append("(").append(BukkitLang.get("none")).append(")\n");
            }
            return text.toString() + ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                String found = null;
                boolean done = false;
                for (final World world : plugin.getServer().getWorlds()) {
                    final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                    final RegionManager regionManager = api.getRegionManager(world);
                    if (regionManager != null) {
                        for (final String region : regionManager.getRegions().keySet()) {
                            if (region.equalsIgnoreCase(input)) {
                                found = region;
                                done = true;
                                break;
                            }
                        }
                    }
                    if (done) {
                        break;
                    }
                }
                if (found == null) {
                    String error = BukkitLang.get("questWGInvalidRegion");
                    error = error.replace("<region>", ChatColor.RED + input + ChatColor.YELLOW);
                    sender.sendMessage(ChatColor.YELLOW + error);
                    new QuestRegionPrompt(uuid).start();
                } else {
                    SessionData.set(uuid, Key.Q_REGION, found);
                    new QuestMainPrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.Q_REGION, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("questWGRegionCleared"));
                new QuestMainPrompt(uuid).start();
            } else {
                new QuestMainPrompt(uuid).start();
            }
        }
    }
    
    public class QuestGuiDisplayPrompt extends QuestsEditorIntegerPrompt {
        
        public QuestGuiDisplayPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("questGUITitle");
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
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("clear");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final int number) {
            return null;
        }

        @Override
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                final ItemStack stack = (ItemStack) SessionData.get(uuid, "tempStack");
                if (stack != null) {
                    SessionData.set(uuid, Key.Q_GUIDISPLAY, stack.clone());
                }
                ItemStackPrompt.clearSessionData(uuid);
            }

            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle() + "\n");
            if (SessionData.get(uuid, Key.Q_GUIDISPLAY) != null) {
                final ItemStack stack = (ItemStack) SessionData.get(uuid, Key.Q_GUIDISPLAY);
                text.append(" ").append(ChatColor.RESET).append(BukkitItemUtil.getDisplayString(stack)).append("\n");
            } else {
                text.append(" ").append(ChatColor.GRAY).append("(").append(BukkitLang.get("noneSet")).append(")\n");
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, QuestGuiDisplayPrompt.this).start();
            case 2:
                SessionData.set(uuid, Key.Q_GUIDISPLAY, null);
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("questGUICleared"));
                new QuestGuiDisplayPrompt(uuid).start();
            case 3:
                plugin.getQuestFactory().returnToMenu(uuid);
            default:
                new QuestGuiDisplayPrompt(uuid).start();
            }
        }
    }
    
    public class QuestSavePrompt extends QuestsEditorStringPrompt {
        
        public QuestSavePrompt(final @NotNull UUID uuid) {
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
            return BukkitLang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
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
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(uuid) && !plugin.getConfigSettings().canTrialSave()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("modeDeny")
                            .replace("<mode>", BukkitLang.get("trialMode")));
                    new QuestMainPrompt(uuid).start();
                }
                if (SessionData.get(uuid, Key.Q_ASK_MESSAGE) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorNeedAskMessage"));
                    new QuestMainPrompt(uuid).start();
                } else if (SessionData.get(uuid, Key.Q_FINISH_MESSAGE) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorNeedFinishMessage"));
                    new QuestMainPrompt(uuid).start();
                } else if (new QuestStageMenuPrompt(uuid).getStages() == 0) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("questEditorNeedStages"));
                    new QuestMainPrompt(uuid).start();
                }
                final FileConfiguration data = new YamlConfiguration();
                try {
                    data.load(new File(plugin.getDataFolder(), "storage" + File.separatorChar + "quests.yml"));
                    ConfigurationSection questSection = data.getConfigurationSection("quests");
                    if (questSection == null) {
                        questSection = data.createSection("quests");
                    }
                    ConfigurationSection newSection = null;
                    if (SessionData.get(uuid, Key.Q_ID) == null) {
                        // Creating
                        final Locale locale = Locale.US;
                        final int padding = 6;
                        String format = "%0" + padding + "d";
                        int num = 1;
                        String customNum = String.format(locale, format, num);
                        while (questSection.contains(customNum)) {
                            num++;
                            customNum = String.format(locale, format, num);
                        }
                        newSection = questSection.createSection(customNum);
                    } else {
                        // Editing
                        final String qid = (String)SessionData.get(uuid, Key.Q_ID);
                        if (qid != null) {
                            newSection = questSection.createSection(qid);
                        }
                    }
                    if (newSection != null) {
                        plugin.getQuestFactory().saveQuest(uuid, newSection);
                        data.save(new File(plugin.getDataFolder(), "storage" + File.separatorChar + "quests.yml"));
                        sender.sendMessage(ChatColor.GREEN
                                + BukkitLang.get("questEditorSaved").replace("<command>", "/questadmin "
                                + BukkitLang.get("COMMAND_QUESTADMIN_RELOAD")));
                    }
                } catch (final IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                return;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new QuestMainPrompt(uuid).start();
            } else {
                new QuestSavePrompt(uuid).start();
            }
        }
    }

    public class QuestExitPrompt extends QuestsEditorStringPrompt {
        
        public QuestExitPrompt(final @NotNull UUID uuid) {
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
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
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
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                sender.sendMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + BukkitLang.get("exited"));
                return;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                new QuestMainPrompt(uuid).start();
            } else {
                new QuestExitPrompt(uuid).start();
            }
        }
    }
}
