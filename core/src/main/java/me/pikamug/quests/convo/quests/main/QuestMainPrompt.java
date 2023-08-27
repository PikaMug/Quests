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

package me.pikamug.quests.convo.quests.main;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.convo.quests.options.QuestOptionsPrompt;
import me.pikamug.quests.convo.quests.planner.QuestPlannerPrompt;
import me.pikamug.quests.convo.quests.requirements.QuestRequirementsPrompt;
import me.pikamug.quests.convo.quests.rewards.QuestRewardsPrompt;
import me.pikamug.quests.convo.quests.stages.QuestStageMenuPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLanguage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class QuestMainPrompt extends QuestsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public QuestMainPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    private final int size = 14;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        final StringBuilder title = new StringBuilder(BukkitLanguage.get("quest") + ": " + context.getSessionData(Key.Q_NAME));

        if (plugin.hasLimitedAccess(context.getForWhom())) {
            title.append(ChatColor.RED).append(" (").append(BukkitLanguage.get("trialMode")).append(")");
        } else if (context.getSessionData(Key.Q_ID) != null) {
            title.append(ChatColor.GRAY).append(" (").append(BukkitLanguage.get("id")).append(":")
                    .append(context.getSessionData(Key.Q_ID)).append(")");
        }
        return title.toString();
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
            if (context.getForWhom() instanceof Player) {
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
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLanguage.get("questEditorName");
        case 2:
            return ChatColor.YELLOW + BukkitLanguage.get("questEditorAskMessage");
        case 3:
            return ChatColor.YELLOW + BukkitLanguage.get("questEditorFinishMessage");
        case 4:
            if (context.getSessionData(Key.Q_START_NPC) == null || plugin.getDependencies().getCitizens() != null
                    || plugin.getDependencies().getZnpcsPlus() != null) {
                return ChatColor.YELLOW + BukkitLanguage.get("questEditorNPCStart");
            } else {
                return ChatColor.GRAY + BukkitLanguage.get("questEditorNPCStart");
            }
        case 5:
            if (context.getForWhom() instanceof Player) {
                return ChatColor.YELLOW + BukkitLanguage.get("questEditorBlockStart");
            } else {
                return ChatColor.GRAY + BukkitLanguage.get("questEditorBlockStart");
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.YELLOW + BukkitLanguage.get("questWGSetRegion");
            } else {
                return ChatColor.GRAY + BukkitLanguage.get("questWGSetRegion");
            }
        case 7:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                return ChatColor.YELLOW + BukkitLanguage.get("questEditorSetGUI");
            } else {
                return ChatColor.GRAY + BukkitLanguage.get("questEditorSetGUI");
            }
        case 8:
            return ChatColor.DARK_AQUA + BukkitLanguage.get("questEditorReqs");
        case 9:
            return ChatColor.AQUA + BukkitLanguage.get("questEditorPln");
        case 10:
            return ChatColor.LIGHT_PURPLE + BukkitLanguage.get("questEditorStages");
        case 11:
            return ChatColor.DARK_PURPLE + BukkitLanguage.get("questEditorRews");
        case 12:
            return ChatColor.DARK_GREEN + BukkitLanguage.get("questEditorOpts");
        case 13:
            return ChatColor.GREEN + BukkitLanguage.get("save");
        case 14:
            return ChatColor.RED + BukkitLanguage.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
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
            return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.Q_ASK_MESSAGE) + ChatColor.RESET
                    + ChatColor.GRAY + ")";
        case 3:
            return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.Q_FINISH_MESSAGE)
                    + ChatColor.RESET + ChatColor.GRAY + ")";
        case 4:
            if (context.getSessionData(Key.Q_START_NPC) == null && (plugin.getDependencies().getCitizens() != null
                    || plugin.getDependencies().getZnpcsPlus() != null)) {
                return ChatColor.GRAY + "(" + BukkitLanguage.get("noneSet") + ")";
            } else if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                final UUID uuid = UUID.fromString((String) Objects.requireNonNull(context
                        .getSessionData(Key.Q_START_NPC)));
                return ChatColor.GRAY + "(" + ChatColor.AQUA + plugin.getDependencies().getNpcName(uuid)
                        + ChatColor.RESET + ChatColor.GRAY + ")";
            } else {
                return ChatColor.GRAY + "(" + BukkitLanguage.get("notInstalled") + ")";
            }
        case 5:
            if (context.getSessionData(Key.Q_START_BLOCK) == null) {
                return ChatColor.GRAY + "(" + BukkitLanguage.get("noneSet") + ")";
            } else {
                final Location l = (Location) context.getSessionData(Key.Q_START_BLOCK);
                if (l != null && l.getWorld() != null) {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + l.getWorld().getName() + ", " + l.getBlockX() + ", "
                            + l.getBlockY() + ", " + l.getBlockZ() + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (context.getSessionData(Key.Q_REGION) == null) {
                    return ChatColor.GRAY + "(" + BukkitLanguage.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(Key.Q_REGION)
                            + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLanguage.get("notInstalled") + ")";
            }
        case 7:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                if (context.getSessionData(Key.Q_GUIDISPLAY) == null) {
                    return ChatColor.GRAY +  "(" + BukkitLanguage.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + BukkitItemUtil.getDisplayString((ItemStack) context
                            .getSessionData(Key.Q_GUIDISPLAY)) + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLanguage.get("notInstalled") + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": "
                + ChatColor.AQUA) + ChatColor.GOLD + " -");
        try {
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new QuestNamePrompt(context);
        case 2:
            return new QuestAskMessagePrompt(context);
        case 3:
            return new QuestFinishMessagePrompt(context);
        case 4:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                return new QuestNPCStartPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        case 5:
            if (context.getForWhom() instanceof Player) {
                final Map<UUID, Block> blockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                blockStarts.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getQuestFactory().setSelectedBlockStarts(blockStarts);
                return new QuestBlockStartPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLanguage.get("consoleError"));
                return new QuestMainPrompt(context);
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return new QuestRegionPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        case 7:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null) {
                return new QuestGuiDisplayPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        case 8:
            return new QuestRequirementsPrompt(context);
        case 9:
            return new QuestPlannerPrompt(context);
        case 10:
            return new QuestStageMenuPrompt(context);
        case 11:
            return new QuestRewardsPrompt(context);
        case 12:
            return new QuestOptionsPrompt(context);
        case 13:
            return new QuestSavePrompt(context);
        case 14:
            return new QuestExitPrompt(context);
        default:
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestNamePrompt extends QuestsEditorStringPrompt {

        public QuestNamePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterQuestName");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        String s = null;
                        if (context.getSessionData(Key.ED_QUEST_EDIT) != null) {
                            s = (String) context.getSessionData(Key.ED_QUEST_EDIT);
                        }
                        if (s != null && !s.equalsIgnoreCase(input)) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorNameExists"));
                            return new QuestNamePrompt(context);
                        }
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorBeingEdited"));
                    return new QuestNamePrompt(context);
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorInvalidQuestName"));
                    return new QuestNamePrompt(context);
                }
                questNames.remove((String) context.getSessionData(Key.Q_NAME));
                context.setSessionData(Key.Q_NAME, input);
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
            }
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestAskMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestAskMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterAskMessage");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                if (input.startsWith("++")) {
                    if (context.getSessionData(Key.Q_ASK_MESSAGE) != null) {
                        context.setSessionData(Key.Q_ASK_MESSAGE, context.getSessionData(Key.Q_ASK_MESSAGE) + " "
                                + input.substring(2));
                        return new QuestMainPrompt(context);
                    }
                }
                context.setSessionData(Key.Q_ASK_MESSAGE, input);
            }
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestFinishMessagePrompt extends QuestsEditorStringPrompt {
        
        public QuestFinishMessagePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterFinishMessage");
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
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                if (input.startsWith("++")) {
                    if (context.getSessionData(Key.Q_FINISH_MESSAGE) != null) {
                        context.setSessionData(Key.Q_FINISH_MESSAGE, context.getSessionData(Key.Q_FINISH_MESSAGE) + " "
                                + input.substring(2));
                        return new QuestMainPrompt(context);
                    }
                }
                context.setSessionData(Key.Q_FINISH_MESSAGE, input);
            }
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestNPCStartPrompt extends QuestsEditorStringPrompt {
        
        public QuestNPCStartPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterNPCStart");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLanguage.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText(context);
            }
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLanguage.get("cmdClear"))) {
                try {
                    final UUID uuid = UUID.fromString(input);
                    if (plugin.getDependencies().getNpcEntity(uuid) == null) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("stageEditorInvalidNPC")
                                .replace("<input>", input));
                        return new QuestNPCStartPrompt(context);
                    }
                    context.setSessionData(Key.Q_START_NPC, uuid.toString());
                    if (context.getForWhom() instanceof Player) {
                        final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                        selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                        plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                    }
                    return new QuestMainPrompt(context);
                } catch (final IllegalArgumentException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + BukkitLanguage.get("reqNotAUniqueId").replace("<input>", input));
                    return new QuestNPCStartPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLanguage.get("cmdClear"))) {
                context.setSessionData(Key.Q_START_NPC, null);
            }
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestBlockStartPrompt extends QuestsEditorStringPrompt {
        
        public QuestBlockStartPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorEnterBlockStart");
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
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(BukkitLanguage.get("cmdDone")) || input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel"))) {
                if (input.equalsIgnoreCase(BukkitLanguage.get("cmdDone"))) {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    final Block block = selectedBlockStarts.get(player.getUniqueId());
                    if (block != null) {
                        final Location loc = block.getLocation();
                        context.setSessionData(Key.Q_START_BLOCK, loc);
                        selectedBlockStarts.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + BukkitLanguage.get("questEditorNoStartBlockSelected"));
                        return new QuestBlockStartPrompt(context);
                    }
                } else {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                return new QuestMainPrompt(context);
            } else if (input.equalsIgnoreCase(BukkitLanguage.get("cmdClear"))) {
                if (context.getForWhom() instanceof Player) {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                context.setSessionData(Key.Q_START_BLOCK, null);
                return new QuestMainPrompt(context);
            }
            return new QuestBlockStartPrompt(context);
        }
    }
    
    public class QuestRegionPrompt extends QuestsEditorStringPrompt {
        
        public QuestRegionPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLanguage.get("questRegionTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questWGPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            StringBuilder text = new StringBuilder(ChatColor.AQUA + getTitle(context) + "\n");
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
                text.append(ChatColor.GRAY).append("(").append(BukkitLanguage.get("none")).append(")\n");
            }
            return text.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLanguage.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLanguage.get("cmdClear"))) {
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
                    String error = BukkitLanguage.get("questWGInvalidRegion");
                    error = error.replace("<region>", ChatColor.RED + input + ChatColor.YELLOW);
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + error);
                    return new QuestRegionPrompt(context);
                } else {
                    context.setSessionData(Key.Q_REGION, found);
                    return new QuestMainPrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLanguage.get("cmdClear"))) {
                context.setSessionData(Key.Q_REGION, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLanguage.get("questWGRegionCleared"));
                return new QuestMainPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        }
    }
    
    public class QuestGuiDisplayPrompt extends QuestsEditorNumericPrompt {
        
        public QuestGuiDisplayPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLanguage.get("questGUITitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLanguage.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.YELLOW + BukkitLanguage.get("clear");
            case 3:
                return ChatColor.YELLOW + BukkitLanguage.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            return null;
        }

        @Override
        public @NotNull String getBasicPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                final ItemStack stack = (ItemStack) context.getSessionData("tempStack");
                if (stack != null) {
                    context.setSessionData(Key.Q_GUIDISPLAY, stack.clone());
                }
                ItemStackPrompt.clearSessionData(context);
            }

            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context) + "\n");
            if (context.getSessionData(Key.Q_GUIDISPLAY) != null) {
                final ItemStack stack = (ItemStack) context.getSessionData(Key.Q_GUIDISPLAY);
                text.append(" ").append(ChatColor.RESET).append(BukkitItemUtil.getDisplayString(stack)).append("\n");
            } else {
                text.append(" ").append(ChatColor.GRAY).append("(").append(BukkitLanguage.get("noneSet")).append(")\n");
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return text.toString();
        }

        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, QuestGuiDisplayPrompt.this);
            case 2:
                context.setSessionData(Key.Q_GUIDISPLAY, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLanguage.get("questGUICleared"));
                return new QuestGuiDisplayPrompt(context);
            case 3:
                return plugin.getQuestFactory().returnToMenu(context);
            default:
                return new QuestGuiDisplayPrompt(context);
            }
        }
    }
    
    public class QuestSavePrompt extends QuestsEditorStringPrompt {
        
        public QuestSavePrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLanguage.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLanguage.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLanguage.get("yesWord"))) {
                if (plugin.hasLimitedAccess(context.getForWhom()) && !plugin.getConfigSettings().canTrialSave()) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("modeDeny")
                            .replace("<mode>", BukkitLanguage.get("trialMode")));
                    return new QuestMainPrompt(context);
                }
                if (context.getSessionData(Key.Q_ASK_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorNeedAskMessage"));
                    return new QuestMainPrompt(context);
                } else if (context.getSessionData(Key.Q_FINISH_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorNeedFinishMessage"));
                    return new QuestMainPrompt(context);
                } else if (new QuestStageMenuPrompt(context).getStages(context) == 0) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLanguage.get("questEditorNeedStages"));
                    return new QuestMainPrompt(context);
                }
                final FileConfiguration data = new YamlConfiguration();
                try {
                    data.load(new File(plugin.getDataFolder(), "quests.yml"));
                    ConfigurationSection questSection = data.getConfigurationSection("quests");
                    if (questSection == null) {
                        questSection = data.createSection("quests");
                    }
                    ConfigurationSection newSection = null;
                    if (context.getSessionData(Key.Q_ID) == null) {
                        // Creating
                        int customNum = 1;
                        while (true) {
                            if (questSection.contains("custom" + customNum)) {
                                customNum++;
                            } else {
                                break;
                            }
                        }
                        newSection = questSection.createSection("custom" + customNum);
                    } else {
                        // Editing
                        final String qid = (String)context.getSessionData(Key.Q_ID);
                        if (qid != null) {
                            newSection = questSection.createSection(qid);
                        }
                    }
                    if (newSection != null) {
                        plugin.getQuestFactory().saveQuest(context, newSection);
                        data.save(new File(plugin.getDataFolder(), "quests.yml"));
                        context.getForWhom().sendRawMessage(ChatColor.GREEN
                                + BukkitLanguage.get("questEditorSaved").replace("<command>", "/questadmin "
                                + BukkitLanguage.get("COMMAND_QUESTADMIN_RELOAD")));
                    }
                } catch (final IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLanguage.get("noWord"))) {
                return new QuestMainPrompt(context);
            } else {
                return new QuestSavePrompt(context);
            }
        }
    }

    public class QuestExitPrompt extends QuestsEditorStringPrompt {
        
        public QuestExitPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @SuppressWarnings("unused")
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLanguage.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLanguage.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLanguage.get("confirmDelete");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(BukkitLanguage.get("yesWord"))) {
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + BukkitLanguage.get("exited"));
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(BukkitLanguage.get("noWord"))) {
                return new QuestMainPrompt(context);
            } else {
                return new QuestExitPrompt(context);
            }
        }
    }
}
