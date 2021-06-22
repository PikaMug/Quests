/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.quests.main;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.options.OptionsPrompt;
import me.blackvein.quests.convo.quests.planner.PlannerPrompt;
import me.blackvein.quests.convo.quests.requirements.RequirementsPrompt;
import me.blackvein.quests.convo.quests.rewards.RewardsPrompt;
import me.blackvein.quests.convo.quests.stages.StageMenuPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.citizensnpcs.api.CitizensAPI;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QuestMainPrompt extends QuestsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public QuestMainPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 14;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("quest") + ": " + context.getSessionData(CK.Q_NAME) + "" + ChatColor.GRAY 
                + (context.getSessionData(CK.Q_ID) != null ? " (" + Lang.get("id") + ":" 
                + context.getSessionData(CK.Q_ID) + ")": "");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
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
            if (plugin.getDependencies().getCitizens() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
            return ChatColor.BLUE;
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
            return ChatColor.YELLOW + Lang.get("questEditorName");
        case 2:
            return ChatColor.YELLOW + Lang.get("questEditorAskMessage");
        case 3:
            return ChatColor.YELLOW + Lang.get("questEditorFinishMessage");
        case 4:
            if (context.getSessionData(CK.Q_START_NPC) == null || plugin.getDependencies().getCitizens() != null) {
                return ChatColor.YELLOW + Lang.get("questEditorNPCStart");
            } else {
                return ChatColor.GRAY + Lang.get("questEditorNPCStart");
            }
        case 5:
            if (context.getForWhom() instanceof Player) {
                return ChatColor.YELLOW + Lang.get("questEditorBlockStart");
            } else {
                return ChatColor.GRAY + Lang.get("questEditorBlockStart");
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.YELLOW + Lang.get("questWGSetRegion");
            } else {
                return ChatColor.GRAY + Lang.get("questWGSetRegion");
            }
        case 7:
            if (plugin.getDependencies().getCitizens() != null) {
                return ChatColor.YELLOW + Lang.get("questEditorSetGUI");
            } else {
                return ChatColor.GRAY + Lang.get("questEditorSetGUI");
            }
        case 8:
            return ChatColor.DARK_AQUA + Lang.get("questEditorReqs");
        case 9:
            return ChatColor.AQUA + Lang.get("questEditorPln");
        case 10:
            return ChatColor.LIGHT_PURPLE + Lang.get("questEditorStages");
        case 11:
            return ChatColor.DARK_PURPLE + Lang.get("questEditorRews");
        case 12:
            return ChatColor.DARK_GREEN + Lang.get("questEditorOpts");
        case 13:
            return ChatColor.GREEN + Lang.get("save");
        case 14:
            return ChatColor.RED + Lang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return "";
        case 2:
            return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.Q_ASK_MESSAGE) + ChatColor.RESET 
                    + ChatColor.GRAY + ")";
        case 3:
            return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.Q_FINISH_MESSAGE) 
                    + ChatColor.RESET + ChatColor.GRAY + ")";
        case 4:
            if (context.getSessionData(CK.Q_START_NPC) == null && plugin.getDependencies().getCitizens() 
                    != null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else if (plugin.getDependencies().getCitizens() != null) {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + CitizensAPI.getNPCRegistry().getById((Integer) context
                        .getSessionData(CK.Q_START_NPC)).getName() + ChatColor.RESET + ChatColor.GRAY + ")";
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 5:
            if (context.getSessionData(CK.Q_START_BLOCK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final Location l = (Location) context.getSessionData(CK.Q_START_BLOCK);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + l.getWorld().getName() + ", " + l.getBlockX() + ", " 
                        + l.getBlockY() + ", " + l.getBlockZ() + ChatColor.RESET + ChatColor.GRAY + ")";
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (context.getSessionData(CK.Q_REGION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + (String) context.getSessionData(CK.Q_REGION) 
                            + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 7:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(CK.Q_GUIDISPLAY) == null) {
                    return ChatColor.GRAY +  "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + ItemUtil.getDisplayString((ItemStack) context
                            .getSessionData(CK.Q_GUIDISPLAY)) + ChatColor.RESET + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 8:
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": " + ChatColor.AQUA)
                + ChatColor.GOLD + " -";
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new QuestNamePrompt(context);
        case 2:
            return new QuestAskMessagePrompt(context);
        case 3:
            return new QuestFinishMessagePrompt(context);
        case 4:
            if (plugin.getDependencies().getCitizens() != null) {
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
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return new QuestMainPrompt(context);
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return new QuestRegionPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        case 7:
            if (plugin.getDependencies().getCitizens() != null) {
                return new QuestGuiDisplayPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        case 8:
            return new RequirementsPrompt(context);
        case 9:
            return new PlannerPrompt(context);
        case 10:
            return new StageMenuPrompt(context);
        case 11:
            return new RewardsPrompt(context);
        case 12:
            return new OptionsPrompt(context);
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
            return Lang.get("questEditorEnterQuestName");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (final Quest q : plugin.getLoadedQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        String s = null;
                        if (context.getSessionData(CK.ED_QUEST_EDIT) != null) {
                            s = (String) context.getSessionData(CK.ED_QUEST_EDIT);
                        }
                        if (s != null && s.equalsIgnoreCase(input) == false) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNameExists"));
                            return new QuestNamePrompt(context);
                        }
                    }
                }
                final List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestNamePrompt(context);
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestNamePrompt(context);
                }
                questNames.remove(context.getSessionData(CK.Q_NAME));
                context.setSessionData(CK.Q_NAME, input);
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
            return Lang.get("questEditorEnterAskMessage");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (input.startsWith("++")) {
                    if (context.getSessionData(CK.Q_ASK_MESSAGE) != null) {
                        context.setSessionData(CK.Q_ASK_MESSAGE, context.getSessionData(CK.Q_ASK_MESSAGE) + " " 
                                + input.substring(2));
                        return new QuestMainPrompt(context);
                    }
                }
                context.setSessionData(CK.Q_ASK_MESSAGE, input);
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
            return Lang.get("questEditorEnterFinishMessage");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                if (input.startsWith("++")) {
                    if (context.getSessionData(CK.Q_FINISH_MESSAGE) != null) {
                        context.setSessionData(CK.Q_FINISH_MESSAGE, context.getSessionData(CK.Q_FINISH_MESSAGE) + " " 
                                + input.substring(2));
                        return new QuestMainPrompt(context);
                    }
                }
                context.setSessionData(CK.Q_FINISH_MESSAGE, input);
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
            return Lang.get("questEditorEnterNPCStart");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + getQueryText(context) + "\n" 
                        + ChatColor.GOLD + Lang.get("npcHint");
            } else {
                return ChatColor.YELLOW + getQueryText(context);
            }
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > -1) {
                        if (CitizensAPI.getNPCRegistry().getById(i) == null) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidNPC"));
                            return new QuestNPCStartPrompt(context);
                        }
                        context.setSessionData(CK.Q_START_NPC, i);
                        if (context.getForWhom() instanceof Player) {
                            final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                            selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                        }
                        return new QuestMainPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new QuestNPCStartPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_START_NPC, null);
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
            return Lang.get("questEditorEnterBlockStart");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdDone")) || input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    final Block block = selectedBlockStarts.get(player.getUniqueId());
                    if (block != null) {
                        final Location loc = block.getLocation();
                        context.setSessionData(CK.Q_START_BLOCK, loc);
                        selectedBlockStarts.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get("questEditorNoStartBlockSelected"));
                        return new QuestBlockStartPrompt(context);
                    }
                } else {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                return new QuestMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                if (context.getForWhom() instanceof Player) {
                    final Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                context.setSessionData(CK.Q_START_BLOCK, null);
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
            return Lang.get("questRegionTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questWGPrompt");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.AQUA + getTitle(context) + "\n";
            boolean any = false;
            for (final World world : plugin.getServer().getWorlds()) {
                final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                final RegionManager rm = api.getRegionManager(world);
                for (final String region : rm.getRegions().keySet()) {
                    any = true;
                    text += ChatColor.GREEN + region + ", ";
                }
            }
            if (any) {
                text = text.substring(0, text.length() - 2) + "\n";
            } else {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n";
            }
            return text + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String found = null;
                boolean done = false;
                for (final World world : plugin.getServer().getWorlds()) {
                    final WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                    final RegionManager rm = api.getRegionManager(world);
                    for (final String region : rm.getRegions().keySet()) {
                        if (region.equalsIgnoreCase(input)) {
                            found = region;
                            done = true;
                            break;
                        }
                    }
                    if (done) {
                        break;
                    }
                }
                if (found == null) {
                    String error = Lang.get("questWGInvalidRegion");
                    error = error.replace("<region>", ChatColor.RED + input + ChatColor.YELLOW);
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + error);
                    return new QuestRegionPrompt(context);
                } else {
                    context.setSessionData(CK.Q_REGION, found);
                    return new QuestMainPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_REGION, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("questWGRegionCleared"));
                return new QuestMainPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        }
    }
    
    public class QuestGuiDisplayPrompt extends QuestsEditorNumericPrompt {
        
        private final Quests plugin;
        
        public QuestGuiDisplayPrompt(final ConversationContext context) {
            super(context);
            this.plugin = (Quests)context.getPlugin();
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("questGUITitle");
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
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.YELLOW + Lang.get("clear");
            case 3:
                return ChatColor.YELLOW + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            return null;
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                final ItemStack stack = (ItemStack) context.getSessionData("tempStack");
                boolean failed = false;
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (quest.getGUIDisplay() != null) {
                        if (ItemUtil.compareItems(stack, quest.getGUIDisplay(), false) == 0) {
                            String error = Lang.get("questGUIError");
                            error = error.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED);
                            context.getForWhom().sendRawMessage(ChatColor.RED + error);
                            failed = true;
                            break;
                        }
                    }
                }
                if (!failed) {
                    context.setSessionData(CK.Q_GUIDISPLAY, context.getSessionData("tempStack"));
                }
                ItemStackPrompt.clearSessionData(context);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            if (context.getSessionData(CK.Q_GUIDISPLAY) != null) {
                final ItemStack stack = (ItemStack) context.getSessionData(CK.Q_GUIDISPLAY);
                text += " " + ChatColor.RESET + ItemUtil.getDisplayString(stack) + "\n";
            } else {
                text += " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
            }
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i);
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, QuestGuiDisplayPrompt.this);
            case 2:
                context.setSessionData(CK.Q_GUIDISPLAY, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("questGUICleared"));
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
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questEditorSave");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.YELLOW + getQueryText(context);
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i);
            }
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(context.getForWhom()) && !plugin.getSettings().canTrialSave()) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("noPermission"));
                    return new QuestMainPrompt(context);
                }
                if (context.getSessionData(CK.Q_ASK_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNeedAskMessage"));
                    return new QuestMainPrompt(context);
                } else if (context.getSessionData(CK.Q_FINISH_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNeedFinishMessage"));
                    return new QuestMainPrompt(context);
                } else if (new StageMenuPrompt(context).getStages(context) == 0) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNeedStages"));
                    return new QuestMainPrompt(context);
                }
                final FileConfiguration data = new YamlConfiguration();
                try {
                    data.load(new File(plugin.getDataFolder(), "quests.yml"));
                    ConfigurationSection questSection = data.getConfigurationSection("quests");
                    if (questSection == null) {
                        questSection = data.createSection("quests");
                    }
                    ConfigurationSection newSection;
                    if (context.getSessionData(CK.Q_ID) == null) {
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
                        newSection = questSection.createSection((String)context.getSessionData(CK.Q_ID));
                    }
                    plugin.getQuestFactory().saveQuest(context, newSection);
                    data.save(new File(plugin.getDataFolder(), "quests.yml"));
                    context.getForWhom().sendRawMessage(ChatColor.GREEN
                            + Lang.get("questEditorSaved").replace("<command>", "/questadmin " 
                            + Lang.get("COMMAND_QUESTADMIN_RELOAD")));
                } catch (final IOException e) {
                    e.printStackTrace();
                } catch (final InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
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
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("confirmDelete");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event 
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.YELLOW + getQueryText(context);
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i);
            }
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + Lang.get("exited"));
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new QuestMainPrompt(context);
            } else {
                return new QuestExitPrompt(context);
            }
        }
    }
}
