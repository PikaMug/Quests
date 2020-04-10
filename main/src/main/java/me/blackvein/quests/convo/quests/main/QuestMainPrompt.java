package me.blackvein.quests.convo.quests.main;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.managers.RegionManager;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.Action;
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

public class QuestMainPrompt extends QuestsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public QuestMainPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 15;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("quest") + ": " + context.getSessionData(CK.Q_NAME) + "" + ChatColor.GRAY 
                + (context.getSessionData(CK.Q_ID) != null ? " (" + Lang.get("id") + ":" 
                + context.getSessionData(CK.Q_ID) + ")": "");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            return ChatColor.BLUE;
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 7:
            return ChatColor.BLUE;
        case 8:
            if (plugin.getDependencies().getCitizens() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
            return ChatColor.BLUE;
        case 14:
            return ChatColor.GREEN;
        case 15:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("questEditorName");
        case 2:
            if (context.getSessionData(CK.Q_ASK_MESSAGE) == null) {
                return ChatColor.RED + Lang.get("questEditorAskMessage");
            } else {
                return ChatColor.YELLOW + Lang.get("questEditorAskMessage");
            }
        case 3:
            if (context.getSessionData(CK.Q_FINISH_MESSAGE) == null) {
                return ChatColor.RED + Lang.get("questEditorFinishMessage");
            } else {
                return ChatColor.YELLOW + Lang.get("questEditorFinishMessage");
            }
        case 4:
            if (context.getSessionData(CK.Q_START_NPC) == null && plugin.getDependencies().getCitizens() 
                    != null) {
                return ChatColor.YELLOW + Lang.get("questEditorNPCStart");
            } else if (plugin.getDependencies().getCitizens() != null) {
                return ChatColor.YELLOW + Lang.get("questEditorNPCStart");
            } else {
                return ChatColor.GRAY + Lang.get("questEditorNPCStart");
            }
        case 5:
            return ChatColor.YELLOW + Lang.get("questEditorBlockStart");
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (context.getSessionData(CK.Q_REGION) == null) {
                    return ChatColor.YELLOW + Lang.get("questWGSetRegion");
                } else {
                    return ChatColor.YELLOW + Lang.get("questWGSetRegion");
                }
            } else {
                return ChatColor.GRAY + Lang.get("questWGSetRegion");
            }
        case 7:
            return ChatColor.YELLOW + Lang.get("questEditorInitialEvent");
        case 8:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(CK.Q_GUIDISPLAY) == null) {
                    return ChatColor.YELLOW + Lang.get("questEditorSetGUI");
                } else {
                    return ChatColor.YELLOW + Lang.get("questEditorSetGUI");
                }
            } else {
                return ChatColor.GRAY + Lang.get("questEditorSetGUI");
            }
        case 9:
            return ChatColor.DARK_AQUA + Lang.get("questEditorReqs");
        case 10:
            return ChatColor.AQUA + Lang.get("questEditorPln");
        case 11:
            return ChatColor.LIGHT_PURPLE + Lang.get("questEditorStages");
        case 12:
            return ChatColor.DARK_PURPLE + Lang.get("questEditorRews");
        case 13:
            return ChatColor.DARK_GREEN + Lang.get("questEditorOpts");
        case 14:
            return ChatColor.GREEN + Lang.get("save");
        case 15:
            return ChatColor.RED + Lang.get("exit");
        default:
            return null;
        }
    }
    
    public String getAdditionalText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            return "";
        case 2:
            if (context.getSessionData(CK.Q_ASK_MESSAGE) == null) {
                return ChatColor.DARK_RED + "(" + Lang.get("questRequiredNoneSet") + ")";
            } else {
                return ChatColor.YELLOW + "(" + context.getSessionData(CK.Q_ASK_MESSAGE) + ChatColor.RESET 
                        + ChatColor.YELLOW + ")";
            }
        case 3:
            if (context.getSessionData(CK.Q_FINISH_MESSAGE) == null) {
                return ChatColor.DARK_RED + "(" + Lang.get("questRequiredNoneSet") + ")";
            } else {
                return ChatColor.YELLOW + "(" + context.getSessionData(CK.Q_FINISH_MESSAGE) + ChatColor.RESET 
                        + ChatColor.YELLOW + ")";
            }
        case 4:
            if (context.getSessionData(CK.Q_START_NPC) == null && plugin.getDependencies().getCitizens() 
                    != null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else if (plugin.getDependencies().getCitizens() != null) {
                return ChatColor.YELLOW + "(" + CitizensAPI.getNPCRegistry().getById((Integer) context
                        .getSessionData(CK.Q_START_NPC)).getName() + ChatColor.RESET + ChatColor.YELLOW + ")";
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 5:
            if (context.getSessionData(CK.Q_START_BLOCK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                Location l = (Location) context.getSessionData(CK.Q_START_BLOCK);
                return ChatColor.YELLOW + "(" + l.getWorld().getName() + ", " + l.getBlockX() + ", " 
                        + l.getBlockY() + ", " + l.getBlockZ() + ")";
            }
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                if (context.getSessionData(CK.Q_REGION) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ChatColor.GREEN 
                            + (String) context.getSessionData(CK.Q_REGION) + ChatColor.YELLOW + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 7:
            if (context.getSessionData(CK.Q_INITIAL_EVENT) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.YELLOW + "(" + (String) context.getSessionData(CK.Q_INITIAL_EVENT) + ")";
            }
        case 8:
            if (plugin.getDependencies().getCitizens() != null) {
                if (context.getSessionData(CK.Q_GUIDISPLAY) == null) {
                    return ChatColor.GRAY +  "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.YELLOW + "(" + ItemUtil.getDisplayString((ItemStack) context
                            .getSessionData(CK.Q_GUIDISPLAY)) + ChatColor.RESET + ChatColor.YELLOW + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
        case 15:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": " + ChatColor.AQUA)
                + ChatColor.GOLD + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            return new QuestNamePrompt();
        case 2:
            return new QuestAskMessagePrompt();
        case 3:
            return new QuestFinishMessagePrompt();
        case 4:
            if (plugin.getDependencies().getCitizens() != null) {
                return new QuestNPCStartPrompt();
            } else {
                return new QuestMainPrompt(context);
            }
        case 5:
            Map<UUID, Block> blockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
            blockStarts.put(((Player) context.getForWhom()).getUniqueId(), null);
            plugin.getQuestFactory().setSelectedBlockStarts(blockStarts);
            return new QuestBlockStartPrompt();
        case 6:
            if (plugin.getDependencies().getWorldGuardApi() != null) {
                return new QuestRegionPrompt();
            } else {
                return new QuestMainPrompt(context);
            }
        case 7:
            return new QuestInitialActionPrompt();
        case 8:
            if (plugin.getDependencies().getCitizens() != null) {
                return new QuestGuiDisplayPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        case 9:
            return new RequirementsPrompt(context);
        case 10:
            return new PlannerPrompt(context);
        case 11:
            return new StageMenuPrompt(context);
        case 12:
            return new RewardsPrompt(context);
        case 13:
            return new OptionsPrompt(context);
        case 14:
            return new QuestSavePrompt(context);
        case 15:
            return new QuestExitPrompt(context);
        default:
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("questEditorEnterQuestName");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        String s = null;
                        if (context.getSessionData(CK.ED_QUEST_EDIT) != null) {
                            s = (String) context.getSessionData(CK.ED_QUEST_EDIT);
                        }
                        if (s != null && s.equalsIgnoreCase(input) == false) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNameExists"));
                            return new QuestNamePrompt();
                        }
                    }
                }
                List<String> questNames = plugin.getQuestFactory().getNamesOfQuestsBeingEdited();
                if (questNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestNamePrompt();
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestNamePrompt();
                }
                questNames.remove((String) context.getSessionData(CK.Q_NAME));
                context.setSessionData(CK.Q_NAME, input);
                questNames.add(input);
                plugin.getQuestFactory().setNamesOfQuestsBeingEdited(questNames);
            }
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestAskMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("questEditorEnterAskMessage");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
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
    
    public class QuestFinishMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("questEditorEnterFinishMessage");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
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
    
    public class QuestNPCStartPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
            selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            return ChatColor.YELLOW + Lang.get("questEditorEnterNPCStart") + "\n" 
                    + ChatColor.GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > -1) {
                        if (CitizensAPI.getNPCRegistry().getById(i) == null) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidNPC"));
                            return new QuestNPCStartPrompt();
                        }
                        context.setSessionData(CK.Q_START_NPC, i);
                        Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                        selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                        plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                        return new QuestMainPrompt(context);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new QuestNPCStartPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_START_NPC, null);
            }
            Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
            selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
            plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            return new QuestMainPrompt(context);
        }
    }
    
    public class QuestBlockStartPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("questEditorEnterBlockStart");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdDone")) || input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {
                    Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    Block block = selectedBlockStarts.get(player.getUniqueId());
                    if (block != null) {
                        Location loc = block.getLocation();
                        context.setSessionData(CK.Q_START_BLOCK, loc);
                        selectedBlockStarts.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get("questEditorNoStartBlockSelected"));
                        return new QuestBlockStartPrompt();
                    }
                } else {
                    Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                    selectedBlockStarts.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                }
                return new QuestMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                Map<UUID, Block> selectedBlockStarts = plugin.getQuestFactory().getSelectedBlockStarts();
                selectedBlockStarts.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedBlockStarts(selectedBlockStarts);
                context.setSessionData(CK.Q_START_BLOCK, null);
                return new QuestMainPrompt(context);
            }
            return new QuestBlockStartPrompt();
        }
    }
    
    public class QuestRegionPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.AQUA + Lang.get("questRegionTitle") + "\n";
            boolean any = false;
            for (World world : plugin.getServer().getWorlds()) {
                WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                RegionManager rm = api.getRegionManager(world);
                for (String region : rm.getRegions().keySet()) {
                    any = true;
                    text += ChatColor.GREEN + region + ", ";
                }
            }
            if (any) {
                text = text.substring(0, text.length() - 2);
                text += "\n\n";
            } else {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n\n";
            }
            return text + ChatColor.YELLOW + Lang.get("questWGPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String found = null;
                boolean done = false;
                for (World world : plugin.getServer().getWorlds()) {
                    WorldGuardAPI api = plugin.getDependencies().getWorldGuardApi();
                    RegionManager rm = api.getRegionManager(world);
                    for (String region : rm.getRegions().keySet()) {
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
                    player.sendMessage(ChatColor.YELLOW + error);
                    return new QuestRegionPrompt();
                } else {
                    context.setSessionData(CK.Q_REGION, found);
                    return new QuestMainPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_REGION, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("questWGRegionCleared"));
                return new QuestMainPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        }
    }
    
    public class QuestInitialActionPrompt extends StringPrompt {
        
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.AQUA + Lang.get("eventTitle") + "\n";
            if (plugin.getActions().isEmpty()) {
                text += ChatColor.RED + "- " + Lang.get("none");
            } else {
                for (Action e : plugin.getActions()) {
                    text += ChatColor.GREEN + "- " + e.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("questEditorEnterInitialEvent");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action a = plugin.getAction(input);
                if (a != null) {
                    context.setSessionData(CK.Q_INITIAL_EVENT, a.getName());
                    return new QuestMainPrompt(context);
                }
                player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " 
                        + Lang.get("questEditorInvalidEventName"));
                return new QuestInitialActionPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_INITIAL_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("questEditorEventCleared"));
                return new QuestMainPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        }
    }
    
    public class QuestGuiDisplayPrompt extends QuestsEditorNumericPrompt {
        
        private final Quests plugin;
        
        public QuestGuiDisplayPrompt(ConversationContext context) {
            super(context);
            this.plugin = (Quests)context.getPlugin();
        }
        
        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("questGUITitle");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
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
        
        public String getSelectionText(ConversationContext context, int number) {
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
        
        public String getAdditionalText(ConversationContext context, int number) {
            return null;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            if (context.getSessionData("tempStack") != null) {
                ItemStack stack = (ItemStack) context.getSessionData("tempStack");
                boolean failed = false;
                for (Quest quest : plugin.getQuests()) {
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
                context.setSessionData("tempStack", null);
            }
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            if (context.getSessionData(CK.Q_GUIDISPLAY) != null) {
                ItemStack stack = (ItemStack) context.getSessionData(CK.Q_GUIDISPLAY);
                text += " " + ChatColor.RESET + ItemUtil.getDisplayString(stack) + "\n";
            } else {
                text += " " + ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
            }
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
            case 1:
                return new ItemStackPrompt(QuestGuiDisplayPrompt.this);
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
        public QuestSavePrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return null;
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }
        
        public String getQueryText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("questEditorSave") + " \"" + ChatColor.AQUA 
                    + context.getSessionData(CK.Q_NAME) + ChatColor.YELLOW + "\"?";
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText(context) + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
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
                FileConfiguration data = new YamlConfiguration();
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
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidConfigurationException e) {
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
        public QuestExitPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return null;
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }
        
        public String getQueryText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("confirmDelete");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText(context) + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
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
