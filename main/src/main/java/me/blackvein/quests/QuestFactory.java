/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.managers.RegionManager;

import me.blackvein.quests.Quests.ReloadCallback;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.prompts.GUIDisplayPrompt;
import me.blackvein.quests.convo.quests.prompts.OptionsPrompt;
import me.blackvein.quests.convo.quests.prompts.PlannerPrompt;
import me.blackvein.quests.convo.quests.prompts.RequirementsPrompt;
import me.blackvein.quests.convo.quests.prompts.RewardsPrompt;
import me.blackvein.quests.convo.quests.prompts.StageMenuPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.citizensnpcs.api.CitizensAPI;

public class QuestFactory implements ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory convoCreator;
    private Map<UUID, Block> selectedBlockStarts = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedKillLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedReachLocations = new HashMap<UUID, Block>();
    private Set<UUID> selectingNpcs = new HashSet<UUID>();
    private List<String> editingQuestNames = new LinkedList<String>();
    
    public QuestFactory(Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize convoCreator last so that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new QuestMenuPrompt(null)).withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);
    }

    public Map<UUID, Block> getSelectedBlockStarts() {
        return selectedBlockStarts;
    }

    public void setSelectedBlockStarts(Map<UUID, Block> selectedBlockStarts) {
        this.selectedBlockStarts = selectedBlockStarts;
    }

    public Map<UUID, Block> getSelectedKillLocations() {
        return selectedKillLocations;
    }

    public void setSelectedKillLocations(Map<UUID, Block> selectedKillLocations) {
        this.selectedKillLocations = selectedKillLocations;
    }

    public Map<UUID, Block> getSelectedReachLocations() {
        return selectedReachLocations;
    }

    public void setSelectedReachLocations(Map<UUID, Block> selectedReachLocations) {
        this.selectedReachLocations = selectedReachLocations;
    }
    
    public Set<UUID> getSelectingNpcs() {
        return selectingNpcs;
    }

    public void setSelectingNpcs(Set<UUID> selectingNpcs) {
        this.selectingNpcs = selectingNpcs;
    }

    /**
     * @deprecated Use {@link#getNamesOfQuestsBeingEdited}
     */
    public List<String> getNames() {
        return editingQuestNames;
    }

    /**
     * @deprecated Use {@link#setNamesOfQuestsBeingEdited}
     */
    public void setNames(List<String> names) {
        this.editingQuestNames = names;
    }
    
    public List<String> getNamesOfQuestsBeingEdited() {
        return editingQuestNames;
    }
    
    public void setNamesOfQuestsBeingEdited(List<String> questNames) {
        this.editingQuestNames = questNames;
    }
    
    public ConversationFactory getConversationFactory() {
        return convoCreator;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.getContext().getSessionData(CK.Q_NAME) != null) {
            editingQuestNames.remove((String) abandonedEvent.getContext().getSessionData(CK.Q_NAME));
        }
        Player player = (Player) abandonedEvent.getContext().getForWhom();
        selectedBlockStarts.remove(player.getUniqueId());
        selectedKillLocations.remove(player.getUniqueId());
        selectedReachLocations.remove(player.getUniqueId());
    }
    
    public class QuestMenuPrompt extends QuestsEditorNumericPrompt {
        public QuestMenuPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("questEditorTitle");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.BLUE;
            case 4:
                return ChatColor.RED;
            default:
                return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("questEditorCreate");
            case 2:
                return ChatColor.YELLOW + Lang.get("questEditorEdit");
            case 3:
                return ChatColor.YELLOW + Lang.get("questEditorDelete");
            case 4:
                return ChatColor.RED + Lang.get("exit");
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
            plugin.getServer().getPluginManager().callEvent(event);
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            final Player player = (Player) context.getForWhom();
            switch (input.intValue()) {
            case 1:
                if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.create")) {
                    return new QuestSelectCreatePrompt(plugin, context);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                    return new QuestMenuPrompt(context);
                }
            case 2:
                if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.edit")) {
                    return new QuestSelectEditPrompt();
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                    return new QuestMenuPrompt(context);
                }
            case 3:
                if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.delete")) {
                    return new QuestSelectDeletePrompt();
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                    return new QuestMenuPrompt(context);
                }
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("exited"));
                return Prompt.END_OF_CONVERSATION;
            default:
                return new QuestMenuPrompt(context);
            }
        }
    }

    public Prompt returnToMenu(ConversationContext context) {
        return new QuestMainPrompt(context);
    }

    public class QuestMainPrompt extends QuestsEditorNumericPrompt {
        public QuestMainPrompt(ConversationContext context) {
            super(context);
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
                return new QuestSetNamePrompt();
            case 2:
                return new AskMessagePrompt();
            case 3:
                return new FinishMessagePrompt();
            case 4:
                if (plugin.getDependencies().getCitizens() != null) {
                    return new NPCStartPrompt();
                } else {
                    return new QuestMainPrompt(context);
                }
            case 5:
                selectedBlockStarts.put(((Player) context.getForWhom()).getUniqueId(), null);
                return new BlockStartPrompt();
            case 6:
                if (plugin.getDependencies().getWorldGuardApi() != null) {
                    return new RegionPrompt();
                } else {
                    return new QuestMainPrompt(context);
                }
            case 7:
                return new InitialActionPrompt();
            case 8:
                if (plugin.getDependencies().getCitizens() != null) {
                    return new GUIDisplayPrompt(context);
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
                return new SavePrompt(context);
            case 15:
                return new ExitPrompt(context);
            default:
                return new QuestMainPrompt(context);
            }
        }
    }
    
    public class QuestSelectCreatePrompt extends QuestsEditorStringPrompt {
        public QuestSelectCreatePrompt(Quests plugin, ConversationContext context) {
            super(context);
        }

        public String getTitle(ConversationContext context) {
            return Lang.get("questCreateTitle");
        }
        
        public String getQueryText(ConversationContext context) {
            return Lang.get("questEditorEnterQuestName");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context)+ "\n" + ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                return new QuestSelectCreatePrompt(plugin, context);
            }
            input = input.trim();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNameExists"));
                        return new QuestSelectCreatePrompt(plugin, context);
                    }
                }
                if (editingQuestNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestSelectCreatePrompt(plugin, context);
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestSelectCreatePrompt(plugin, context);
                }
                if (input.equals("")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new QuestSelectCreatePrompt(plugin, context);
                }
                context.setSessionData(CK.Q_NAME, input);
                editingQuestNames.add(input);
                return new QuestMainPrompt(context);
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }

    private class QuestSelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String s = ChatColor.GOLD + Lang.get("questEditTitle") + "\n";
            for (Quest q : plugin.getQuests()) {
                s += ChatColor.GRAY + "- " + ChatColor.AQUA + q.getName() + "\n";
            }
            return s + ChatColor.YELLOW + Lang.get("questEditorEnterQuestName");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                Quest q = plugin.getQuest(input);
                if (q != null) {
                    loadQuest(context, q);
                    return new QuestMainPrompt(context);
                }
                return new QuestSelectEditPrompt();
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void loadQuest(ConversationContext context, Quest q) {
        context.setSessionData(CK.ED_QUEST_EDIT, q.getName());
        context.setSessionData(CK.Q_ID, q.getId());
        context.setSessionData(CK.Q_NAME, q.getName());
        if (q.npcStart != null) {
            context.setSessionData(CK.Q_START_NPC, q.npcStart.getId());
        }
        context.setSessionData(CK.Q_START_BLOCK, q.blockStart);
        context.setSessionData(CK.Q_ASK_MESSAGE, q.description);
        context.setSessionData(CK.Q_FINISH_MESSAGE, q.finished);
        if (q.initialAction != null) {
            context.setSessionData(CK.Q_INITIAL_EVENT, q.initialAction.getName());
        }
        if (q.regionStart != null) {
            context.setSessionData(CK.Q_REGION, q.regionStart);
        }
        if (q.guiDisplay != null) {
            context.setSessionData(CK.Q_GUIDISPLAY, q.guiDisplay);
        }
        Requirements reqs = q.getRequirements();
        if (reqs.getMoney() != 0) {
            context.setSessionData(CK.REQ_MONEY, reqs.getMoney());
        }
        if (reqs.getQuestPoints() != 0) {
            context.setSessionData(CK.REQ_QUEST_POINTS, reqs.getQuestPoints());
        }
        if (reqs.getItems().isEmpty() == false) {
            context.setSessionData(CK.REQ_ITEMS, reqs.getItems());
            context.setSessionData(CK.REQ_ITEMS_REMOVE, reqs.getRemoveItems());
        }
        if (reqs.getNeededQuests().isEmpty() == false) {
            context.setSessionData(CK.REQ_QUEST, reqs.getNeededQuests());
        }
        if (reqs.getBlockQuests().isEmpty() == false) {
            context.setSessionData(CK.REQ_QUEST_BLOCK, reqs.getBlockQuests());
        }
        if (reqs.getMcmmoSkills().isEmpty() == false) {
            context.setSessionData(CK.REQ_MCMMO_SKILLS, reqs.getMcmmoAmounts());
            context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, reqs.getMcmmoAmounts());
        }
        if (reqs.getPermissions().isEmpty() == false) {
            context.setSessionData(CK.REQ_PERMISSION, reqs.getPermissions());
        }
        if (reqs.getHeroesPrimaryClass() != null) {
            context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, reqs.getHeroesPrimaryClass());
        }
        if (reqs.getHeroesSecondaryClass() != null) {
            context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, reqs.getHeroesSecondaryClass());
        }
        if (reqs.getCustomRequirements().isEmpty() == false) {
            LinkedList<String> list = new LinkedList<String>();
            LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
            for (Entry<String, Map<String, Object>> entry : reqs.getCustomRequirements().entrySet()) {
                list.add(entry.getKey());
                datamapList.add(entry.getValue());
            }
            context.setSessionData(CK.REQ_CUSTOM, list);
            context.setSessionData(CK.REQ_CUSTOM_DATA, datamapList);
        }
        if (reqs.getDetailsOverride().isEmpty() == false) {
            context.setSessionData(CK.REQ_FAIL_MESSAGE, reqs.getDetailsOverride());
        }
        Rewards rews = q.getRewards();
        if (rews.getMoney() != 0) {
            context.setSessionData(CK.REW_MONEY, rews.getMoney());
        }
        if (rews.getQuestPoints() != 0) {
            context.setSessionData(CK.REW_QUEST_POINTS, rews.getQuestPoints());
        }
        if (rews.getExp() != 0) {
            context.setSessionData(CK.REW_EXP, rews.getExp());
        }
        if (rews.getItems().isEmpty() == false) {
            context.setSessionData(CK.REW_ITEMS, rews.getItems());
        }
        if (rews.getCommands().isEmpty() == false) {
            context.setSessionData(CK.REW_COMMAND, rews.getCommands());
        }
        if (rews.getCommandsOverrideDisplay().isEmpty() == false) {
            context.setSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY, rews.getCommandsOverrideDisplay());
        }
        if (rews.getPermissions().isEmpty() == false) {
            context.setSessionData(CK.REW_PERMISSION, rews.getPermissions());
        }
        if (rews.getPermissions().isEmpty() == false) {
            context.setSessionData(CK.REW_PERMISSION_WORLDS, rews.getPermissionWorlds());
        }
        if (rews.getMcmmoSkills().isEmpty() == false) {
            context.setSessionData(CK.REW_MCMMO_SKILLS, rews.getMcmmoSkills());
            context.setSessionData(CK.REW_MCMMO_AMOUNTS, rews.getMcmmoAmounts());
        }
        if (rews.getHeroesClasses().isEmpty() == false) {
            context.setSessionData(CK.REW_HEROES_CLASSES, rews.getHeroesClasses());
            context.setSessionData(CK.REW_HEROES_AMOUNTS, rews.getHeroesAmounts());
        }
        if (rews.getPhatLoots().isEmpty() == false) {
            context.setSessionData(CK.REW_PHAT_LOOTS, rews.getPhatLoots());
        }
        if (rews.getCustomRewards().isEmpty() == false) {
            context.setSessionData(CK.REW_CUSTOM, new LinkedList<String>(rews.getCustomRewards().keySet()));
            context.setSessionData(CK.REW_CUSTOM_DATA, new LinkedList<Object>(rews.getCustomRewards().values()));
        }
        if (rews.getDetailsOverride().isEmpty() == false) {
            context.setSessionData(CK.REW_DETAILS_OVERRIDE, rews.getDetailsOverride());
        }
        Planner pln = q.getPlanner();
        if (pln.getStart() != null) {
            context.setSessionData(CK.PLN_START_DATE, pln.getStart());
        }
        if (pln.getEnd() != null) {
            context.setSessionData(CK.PLN_END_DATE, pln.getEnd());
        }
        if (pln.getRepeat() != -1) {
            context.setSessionData(CK.PLN_REPEAT_CYCLE, pln.getRepeat());
        }
        if (pln.getCooldown() != -1) {
            context.setSessionData(CK.PLN_COOLDOWN, pln.getCooldown());
        }
        Options opt = q.getOptions();
        context.setSessionData(CK.OPT_ALLOW_COMMANDS, opt.getAllowCommands());
        context.setSessionData(CK.OPT_ALLOW_QUITTING, opt.getAllowQuitting());
        context.setSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN, opt.getUseDungeonsXLPlugin());
        context.setSessionData(CK.OPT_USE_PARTIES_PLUGIN, opt.getUsePartiesPlugin());
        context.setSessionData(CK.OPT_SHARE_PROGRESS_LEVEL, opt.getShareProgressLevel());
        context.setSessionData(CK.OPT_REQUIRE_SAME_QUEST, opt.getRequireSameQuest());
        // Stages (Objectives)
        int index = 1;
        for (Stage stage : q.getStages()) {
            final String pref = "stage" + index;
            index++;
            context.setSessionData(pref, Boolean.TRUE);
            if (!stage.getBlocksToBreak().isEmpty()) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToBreak) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_BREAK_NAMES, names);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durab);
            }
            if (!stage.getBlocksToDamage().isEmpty()) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToDamage) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durab);
            }
            if (!stage.getBlocksToPlace().isEmpty()) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToPlace) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_PLACE_NAMES, names);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durab);
            }
            if (!stage.getBlocksToUse().isEmpty()) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToUse) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_USE_NAMES, names);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_USE_DURABILITY, durab);
            }
            if (!stage.getBlocksToCut().isEmpty()) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToCut) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                context.setSessionData(pref + CK.S_CUT_NAMES, names);
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, amnts);
                context.setSessionData(pref + CK.S_CUT_DURABILITY, durab);
            }
            if (!stage.getItemsToCraft().isEmpty()) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (ItemStack is : stage.getItemsToCraft()) {
                    items.add(is);
                }
                context.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
            }
            if (!stage.getItemsToSmelt().isEmpty()) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (ItemStack is : stage.getItemsToSmelt()) {
                    items.add(is);
                }
                context.setSessionData(pref + CK.S_SMELT_ITEMS, items);
            }
            if (!stage.getItemsToEnchant().isEmpty()) {
                LinkedList<String> enchants = new LinkedList<String>();
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (Entry<Map<Enchantment, Material>, Integer> e : stage.itemsToEnchant.entrySet()) {
                    amounts.add(e.getValue());
                    for (Entry<Enchantment, Material> e2 : e.getKey().entrySet()) {
                        names.add(e2.getValue().name());
                        enchants.add(ItemUtil.getPrettyEnchantmentName(e2.getKey()));
                    }
                }
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, enchants);
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, names);
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, amounts);
            }
            if (!stage.getItemsToBrew().isEmpty()) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (ItemStack is : stage.getItemsToBrew()) {
                    items.add(is);
                }
                context.setSessionData(pref + CK.S_BREW_ITEMS, items);
            }
            if (stage.cowsToMilk != null) {
                context.setSessionData(pref + CK.S_COW_MILK, stage.cowsToMilk);
            }
            if (stage.fishToCatch != null) {
                context.setSessionData(pref + CK.S_FISH, stage.fishToCatch);
            }
            if (stage.playersToKill != null) {
                context.setSessionData(pref + CK.S_PLAYER_KILL, stage.playersToKill);
            }
            if (stage.getItemsToDeliver().isEmpty() == false) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (ItemStack is : stage.getItemsToDeliver()) {
                    items.add(is);
                }
                for (Integer n : stage.getItemDeliveryTargets()) {
                    npcs.add(n);
                }
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, items);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, stage.deliverMessages);
            }
            if (stage.citizensToInteract.isEmpty() == false) {
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (Integer n : stage.citizensToInteract) {
                    npcs.add(n);
                }
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            }
            if (stage.citizensToKill.isEmpty() == false) {
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (Integer n : stage.citizensToKill) {
                    npcs.add(n);
                }
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, stage.citizenNumToKill);
            }
            if (stage.mobsToKill.isEmpty() == false) {
                LinkedList<String> mobs = new LinkedList<String>();
                for (EntityType et : stage.mobsToKill) {
                    mobs.add(MiscUtil.getPrettyMobName(et));
                }
                context.setSessionData(pref + CK.S_MOB_TYPES, mobs);
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, stage.mobNumToKill);
                if (stage.locationsToKillWithin.isEmpty() == false) {
                    LinkedList<String> locs = new LinkedList<String>();
                    for (Location l : stage.locationsToKillWithin) {
                        locs.add(ConfigUtil.getLocationInfo(l));
                    }
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locs);
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, stage.radiiToKillWithin);
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, stage.killNames);
                }
            }
            if (stage.locationsToReach.isEmpty() == false) {
                LinkedList<String> locs = new LinkedList<String>();
                for (Location l : stage.locationsToReach) {
                    locs.add(ConfigUtil.getLocationInfo(l));
                }
                context.setSessionData(pref + CK.S_REACH_LOCATIONS, locs);
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, stage.radiiToReachWithin);
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, stage.locationNames);
            }
            if (stage.mobsToTame.isEmpty() == false) {
                LinkedList<String> mobs = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                for (Entry<EntityType, Integer> e : stage.mobsToTame.entrySet()) {
                    mobs.add(MiscUtil.getPrettyMobName(e.getKey()));
                    amnts.add(e.getValue());
                }
                context.setSessionData(pref + CK.S_TAME_TYPES, mobs);
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, amnts);
            }
            if (stage.sheepToShear.isEmpty() == false) {
                LinkedList<String> colors = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                for (Entry<DyeColor, Integer> e : stage.sheepToShear.entrySet()) {
                    colors.add(MiscUtil.getPrettyDyeColorName(e.getKey()));
                    amnts.add(e.getValue());
                }
                context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, amnts);
            }
            if (stage.passwordDisplays.isEmpty() == false) {
                context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, stage.passwordDisplays);
                context.setSessionData(pref + CK.S_PASSWORD_PHRASES, stage.passwordPhrases);
            }
            if (stage.customObjectives.isEmpty() == false) {
                LinkedList<String> list = new LinkedList<String>();
                LinkedList<Integer> countList = new LinkedList<Integer>();
                LinkedList<Entry<String, Object>> datamapList = new LinkedList<Entry<String, Object>>();
                for (int i = 0; i < stage.customObjectives.size(); i++) {
                    list.add(stage.customObjectives.get(i).getName());
                    countList.add(stage.customObjectiveCounts.get(i));
                }
                datamapList.addAll(stage.customObjectiveData);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
            }
            if (stage.startAction != null) {
                context.setSessionData(pref + CK.S_START_EVENT, stage.startAction.getName());
            }
            if (stage.finishAction != null) {
                context.setSessionData(pref + CK.S_FINISH_EVENT, stage.finishAction.getName());
            }
            if (stage.deathAction != null) {
                context.setSessionData(pref + CK.S_DEATH_EVENT, stage.deathAction.getName());
            }
            if (stage.disconnectAction != null) {
                context.setSessionData(pref + CK.S_DISCONNECT_EVENT, stage.disconnectAction.getName());
            }
            if (!stage.getChatActions().isEmpty()) {
                LinkedList<String> chatEvents = new LinkedList<String>();
                LinkedList<String> chatEventTriggers = new LinkedList<String>();
                for (String s : stage.chatActions.keySet()) {
                    chatEventTriggers.add(s);
                    chatEvents.add(stage.chatActions.get(s).getName());
                }
                context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
                context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
            }
            if (!stage.getCommandActions().isEmpty()) {
                LinkedList<String> commandEvents = new LinkedList<String>();
                LinkedList<String> commandEventTriggers = new LinkedList<String>();
                for (String s : stage.commandActions.keySet()) {
                    commandEventTriggers.add(s);
                    commandEvents.add(stage.commandActions.get(s).getName());
                }
                context.setSessionData(pref + CK.S_COMMAND_EVENTS, commandEvents);
                context.setSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
            }
            if (stage.delay != -1) {
                context.setSessionData(pref + CK.S_DELAY, stage.delay);
                if (stage.delayMessage != null) {
                    context.setSessionData(pref + CK.S_DELAY_MESSAGE, stage.delayMessage);
                }
            }
            if (stage.script != null) {
                context.setSessionData(pref + CK.S_DENIZEN, stage.script);
            }
            if (stage.completeMessage != null) {
                context.setSessionData(pref + CK.S_COMPLETE_MESSAGE, stage.completeMessage);
            }
            if (stage.startMessage != null) {
                context.setSessionData(pref + CK.S_START_MESSAGE, stage.startMessage);
            }
            if (stage.objectiveOverrides.isEmpty() == false) {
                context.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, stage.objectiveOverrides);
            }
        }
    }
    
    private class QuestSelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("questDeleteTitle") + "\n";
            for (Quest quest : plugin.getQuests()) {
                text += ChatColor.AQUA + quest.getName() + ChatColor.GRAY + ",";
            }
            text = text.substring(0, text.length() - 1) + "\n";
            text += ChatColor.YELLOW + Lang.get("questEditorEnterQuestName");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> used = new LinkedList<String>();
                Quest found = plugin.getQuest(input);
                if (found != null) {
                    for (Quest q : plugin.getQuests()) {
                        if (q.getRequirements().getNeededQuests().contains(q.getName()) 
                                || q.getRequirements().getBlockQuests().contains(q.getName())) {
                            used.add(q.getName());
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_QUEST_DELETE, found.getName());
                        return new QuestConfirmDeletePrompt();
                    } else {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED 
                                + Lang.get("questEditorQuestAsRequirement1") + " \"" + ChatColor.DARK_PURPLE 
                                + context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + "\" " 
                                + Lang.get("questEditorQuestAsRequirement2"));
                        for (String s : used) {
                            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED 
                                + Lang.get("questEditorQuestAsRequirement3"));
                        return new QuestSelectDeletePrompt();
                    }
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questEditorQuestNotFound"));
                return new QuestSelectDeletePrompt();
            } else {
                return new QuestMenuPrompt(context);
            }
        }
    }

    private class QuestConfirmDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " 
                    + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " 
                    + Lang.get("noWord");
            return ChatColor.RED + Lang.get("confirmDelete") + " (" + ChatColor.YELLOW 
                    + (String) context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                deleteQuest(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new QuestMenuPrompt(context);
            } else {
                return new QuestConfirmDeletePrompt();
            }
        }
    }

    private void deleteQuest(ConversationContext context) {
        FileConfiguration data = new YamlConfiguration();
        File questsFile = new File(plugin.getDataFolder(), "quests.yml");
        try {
            data.load(questsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<quest>", questsFile.getName()));
            return;
        }
        String quest = (String) context.getSessionData(CK.ED_QUEST_DELETE);
        ConfigurationSection sec = data.getConfigurationSection("quests");
        for (String key : sec.getKeys(false)) {
            if (sec.getString(key + ".name").equalsIgnoreCase(quest)) {
                sec.set(key, null);
                break;
            }
        }
        try {
            data.save(questsFile);
        } catch (IOException e) {
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
            public void execute(Boolean response) {
                if (!response) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.GREEN + Lang.get("questDeleted"));
    }

    private class QuestSetNamePrompt extends StringPrompt {

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
                            return new QuestSetNamePrompt();
                        }
                    }
                }
                if (editingQuestNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestSetNamePrompt();
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestSelectCreatePrompt(plugin, context);
                }
                editingQuestNames.remove((String) context.getSessionData(CK.Q_NAME));
                context.setSessionData(CK.Q_NAME, input);
                editingQuestNames.add(input);
            }
            return new QuestMainPrompt(context);
        }
    }

    private class AskMessagePrompt extends StringPrompt {

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

    private class FinishMessagePrompt extends StringPrompt {

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

    private class NPCStartPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
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
                            return new NPCStartPrompt();
                        }
                        context.setSessionData(CK.Q_START_NPC, i);
                        selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                        return new QuestMainPrompt(context);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED 
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new NPCStartPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_START_NPC, null);
            }
            selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
            return new QuestMainPrompt(context);
        }
    }

    private class BlockStartPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("questEditorEnterBlockStart");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdDone")) || input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {
                    Block block = selectedBlockStarts.get(player.getUniqueId());
                    if (block != null) {
                        Location loc = block.getLocation();
                        context.setSessionData(CK.Q_START_BLOCK, loc);
                        selectedBlockStarts.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get("questEditorNoStartBlockSelected"));
                        return new BlockStartPrompt();
                    }
                } else {
                    selectedBlockStarts.remove(player.getUniqueId());
                }
                return new QuestMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                selectedBlockStarts.remove(player.getUniqueId());
                context.setSessionData(CK.Q_START_BLOCK, null);
                return new QuestMainPrompt(context);
            }
            return new BlockStartPrompt();
        }
    }

    private class RegionPrompt extends StringPrompt {

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
                    return new RegionPrompt();
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

    private class InitialActionPrompt extends StringPrompt {

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
                return new InitialActionPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_INITIAL_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("questEditorEventCleared"));
                return new QuestMainPrompt(context);
            } else {
                return new QuestMainPrompt(context);
            }
        }
    }

    public class SavePrompt extends QuestsEditorStringPrompt {
        public SavePrompt(ConversationContext context) {
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
                    saveQuest(context, newSection);
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
                return new SavePrompt(context);
            }
        }
    }

    public class ExitPrompt extends QuestsEditorStringPrompt {
        public ExitPrompt(ConversationContext context) {
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
                return new ExitPrompt(context);
            }
        }
    }

    public void saveQuest(ConversationContext context, ConfigurationSection section) {
        String edit = null;
        if (context.getSessionData(CK.ED_QUEST_EDIT) != null) {
            edit = (String) context.getSessionData(CK.ED_QUEST_EDIT);
        }
        if (edit != null) {
            ConfigurationSection questList = section.getParent();
            for (String key : questList.getKeys(false)) {
                String name = questList.getString(key + ".name");
                if (name != null) {
                    if (name.equalsIgnoreCase(edit)) {
                        questList.set(key, null);
                        break;
                    }
                }
            }
        }
        section.set("name", context.getSessionData(CK.Q_NAME) != null 
                ? (String) context.getSessionData(CK.Q_NAME) : null);
        section.set("ask-message", context.getSessionData(CK.Q_ASK_MESSAGE) != null 
                ? (String) context.getSessionData(CK.Q_ASK_MESSAGE) : null);
        section.set("finish-message", context.getSessionData(CK.Q_FINISH_MESSAGE) != null 
                ? (String) context.getSessionData(CK.Q_FINISH_MESSAGE) : null);
        section.set("npc-giver-id", context.getSessionData(CK.Q_START_NPC) != null 
                ? (Integer) context.getSessionData(CK.Q_START_NPC) : null);
        section.set("block-start", context.getSessionData(CK.Q_START_BLOCK) != null 
                ? ConfigUtil.getLocationInfo((Location) context.getSessionData(CK.Q_START_BLOCK)) : null);
        section.set("event", context.getSessionData(CK.Q_INITIAL_EVENT) != null 
                ? (String) context.getSessionData(CK.Q_INITIAL_EVENT) : null);
        section.set("region", context.getSessionData(CK.Q_REGION) != null 
                ? (String) context.getSessionData(CK.Q_REGION) : null);
        section.set("gui-display", context.getSessionData(CK.Q_GUIDISPLAY) != null 
                ? (ItemStack) context.getSessionData(CK.Q_GUIDISPLAY) : null);
        saveRequirements(context, section);
        saveStages(context, section);
        saveRewards(context, section);
        savePlanner(context, section);
        saveOptions(context, section);
    }
    
    @SuppressWarnings("unchecked")
    private void saveRequirements(ConversationContext context, ConfigurationSection section) {
        ConfigurationSection reqs = section.createSection("requirements");
        reqs.set("money", context.getSessionData(CK.REQ_MONEY) != null 
                ? (Integer) context.getSessionData(CK.REQ_MONEY) : null);
        reqs.set("quest-points", context.getSessionData(CK.REQ_QUEST_POINTS) != null 
                ? (Integer) context.getSessionData(CK.REQ_QUEST_POINTS) : null);
        reqs.set("items", context.getSessionData(CK.REQ_ITEMS) != null 
                ? (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS) : null);
        reqs.set("remove-items", context.getSessionData(CK.REQ_ITEMS_REMOVE) != null 
                ? (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE) : null);
        reqs.set("permissions", context.getSessionData(CK.REQ_PERMISSION) != null 
                ? (List<String>) context.getSessionData(CK.REQ_PERMISSION) : null);
        reqs.set("quests", context.getSessionData(CK.REQ_QUEST) != null 
                ? (List<String>) context.getSessionData(CK.REQ_QUEST) : null);
        reqs.set("quest-blocks", context.getSessionData(CK.REQ_QUEST_BLOCK) != null 
                ? (List<String>) context.getSessionData(CK.REQ_QUEST_BLOCK) : null);
        reqs.set("mcmmo-skills", context.getSessionData(CK.REQ_MCMMO_SKILLS) != null 
                ? (List<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS) : null);
        reqs.set("mcmmo-amounts", context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) != null 
                ? (List<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) : null);
        reqs.set("heroes-primary-class", context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null 
                ? (String) context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) : null);
        reqs.set("heroes-secondary-class", context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null 
                ? (String) context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) : null);
        LinkedList<String> customReqs = context.getSessionData(CK.REQ_CUSTOM) != null 
                ? (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM) : null;
        LinkedList<Map<String, Object>> customReqsData = context.getSessionData(CK.REQ_CUSTOM_DATA) != null 
                ? (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA) : null;
        if (customReqs != null) {
            ConfigurationSection customReqsSec = reqs.createSection("custom-requirements");
            for (int i = 0; i < customReqs.size(); i++) {
                ConfigurationSection customReqSec = customReqsSec.createSection("req" + (i + 1));
                customReqSec.set("name", customReqs.get(i));
                customReqSec.set("data", customReqsData.get(i));
            }
        }
        reqs.set("fail-requirement-message", context.getSessionData(CK.REQ_FAIL_MESSAGE) != null 
                ? (List<String>)context.getSessionData(CK.REQ_FAIL_MESSAGE) : null);
        if (reqs.getKeys(false).isEmpty()) {
            section.set("requirements", null);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void saveStages(ConversationContext context, ConfigurationSection section) {
        ConfigurationSection stages = section.createSection("stages");
        ConfigurationSection ordered = stages.createSection("ordered");
        String pref;
        for (int i = 1; i <= new StageMenuPrompt(context).getStages(context); i++) {
            pref = "stage" + i;
            ConfigurationSection stage = ordered.createSection("" + i);
            stage.set("break-block-names", context.getSessionData(pref + CK.S_BREAK_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_NAMES) : null);
            stage.set("break-block-amounts", context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS) : null);
            stage.set("break-block-durability", context.getSessionData(pref + CK.S_BREAK_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_BREAK_DURABILITY) : null);
            stage.set("damage-block-names", context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_NAMES) : null);
            stage.set("damage-block-amounts", context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) : null);
            stage.set("damage-block-durability", context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) : null);
            stage.set("place-block-names", context.getSessionData(pref + CK.S_PLACE_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_NAMES) : null);
            stage.set("place-block-amounts", context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS) : null);
            stage.set("place-block-durability", context.getSessionData(pref + CK.S_PLACE_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_PLACE_DURABILITY) : null);
            stage.set("use-block-names", context.getSessionData(pref + CK.S_USE_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_NAMES) : null);
            stage.set("use-block-amounts", context.getSessionData(pref + CK.S_USE_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS) : null);
            stage.set("use-block-durability", context.getSessionData(pref + CK.S_USE_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_USE_DURABILITY) : null);
            stage.set("cut-block-names", context.getSessionData(pref + CK.S_CUT_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_NAMES) : null);
            stage.set("cut-block-amounts", context.getSessionData(pref + CK.S_CUT_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS) : null);
            stage.set("cut-block-durability", context.getSessionData(pref + CK.S_CUT_DURABILITY) != null 
                    ? (LinkedList<Short>) context.getSessionData(pref + CK.S_CUT_DURABILITY) : null);
            stage.set("items-to-craft", context.getSessionData(pref + CK.S_CRAFT_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_CRAFT_ITEMS) : null);
            stage.set("items-to-smelt", context.getSessionData(pref + CK.S_SMELT_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_SMELT_ITEMS) : null);
            stage.set("enchantments", context.getSessionData(pref + CK.S_ENCHANT_TYPES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES) : null);
            stage.set("enchantment-item-names", context.getSessionData(pref + CK.S_ENCHANT_NAMES) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_NAMES) : null);
            stage.set("enchantment-amounts", context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) : null);
            stage.set("items-to-brew", context.getSessionData(pref + CK.S_BREW_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_BREW_ITEMS) : null);
            stage.set("cows-to-milk", context.getSessionData(pref + CK.S_COW_MILK) != null 
                    ? (Integer) context.getSessionData(pref + CK.S_COW_MILK) : null);
            stage.set("fish-to-catch", context.getSessionData(pref + CK.S_FISH) != null 
                    ? (Integer) context.getSessionData(pref + CK.S_FISH) : null);
            stage.set("players-to-kill", context.getSessionData(pref + CK.S_PLAYER_KILL) != null 
                    ? (Integer) context.getSessionData(pref + CK.S_PLAYER_KILL) : null);
            stage.set("items-to-deliver", context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null 
                    ? (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS) : null);
            stage.set("npc-delivery-ids", context.getSessionData(pref + CK.S_DELIVERY_NPCS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS) : null);
            stage.set("delivery-messages", context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) : null);
            stage.set("npc-ids-to-talk-to", context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) : null);
            stage.set("npc-ids-to-kill", context.getSessionData(pref + CK.S_NPCS_TO_KILL) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL) : null);
            stage.set("npc-kill-amounts", context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) : null);
            stage.set("mobs-to-kill", context.getSessionData(pref + CK.S_MOB_TYPES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_TYPES) : null);
            stage.set("mob-amounts", context.getSessionData(pref + CK.S_MOB_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS) : null);
            stage.set("locations-to-kill", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) : null);
            stage.set("kill-location-radii", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) : null);
            stage.set("kill-location-names", context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) : null);
            stage.set("locations-to-reach", context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS) : null);
            stage.set("reach-location-radii", context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) : null);
            stage.set("reach-location-names", context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) : null);
            stage.set("mobs-to-tame", context.getSessionData(pref + CK.S_TAME_TYPES) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_TAME_TYPES) : null);
            stage.set("mob-tame-amounts", context.getSessionData(pref + CK.S_TAME_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS) : null);
            stage.set("sheep-to-shear", context.getSessionData(pref + CK.S_SHEAR_COLORS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS) : null);
            stage.set("sheep-amounts", context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) != null 
                    ? (LinkedList<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) : null);
            stage.set("password-displays", context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null 
                    ? (LinkedList<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) : null);
            LinkedList<LinkedList<String>> passPhrases 
                    = (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
            if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null) {
                LinkedList<String> toPut = new LinkedList<String>();
                for (LinkedList<String> list : passPhrases) {
                    String combine = "";
                    for (String s : list) {
                        if (list.getLast().equals(s) == false) {
                            combine += s + "|";
                        } else {
                            combine += s;
                        }
                    }
                    toPut.add(combine);
                }
                stage.set("password-phrases", toPut);
            }
            LinkedList<String> customObjs = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
            LinkedList<Integer> customObjCounts 
                    = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
            LinkedList<Entry<String, Object>> customObjsData 
                    = (LinkedList<Entry<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
            if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) != null) {
                ConfigurationSection sec = stage.createSection("custom-objectives");
                for (int index = 0; index < customObjs.size(); index++) {
                    ConfigurationSection sec2 = sec.createSection("custom" + (index + 1));
                    sec2.set("name", customObjs.get(index));
                    sec2.set("count", customObjCounts.get(index));
                    CustomObjective found = null;
                    for (CustomObjective co : plugin.getCustomObjectives()) {
                        if (co.getName().equals(customObjs.get(index))) {
                            found = co;
                            break;
                        }
                    }
                    ConfigurationSection sec3 = sec2.createSection("data");
                    for (Entry<String, Object> datamap : found.getData()) {
                        for (Entry<String, Object> e : customObjsData) {
                            if (e.getKey().equals(datamap.getKey())) {
                                sec3.set(e.getKey(), e.getValue()); // if anything goes wrong it's probably here
                            }
                        }
                    }
                }
            }
            stage.set("script-to-run", context.getSessionData(pref + CK.S_DENIZEN) != null 
                    ? context.getSessionData(pref + CK.S_DENIZEN) : null);
            stage.set("start-event", context.getSessionData(pref + CK.S_START_EVENT) != null 
                    ? context.getSessionData(pref + CK.S_START_EVENT) : null);
            stage.set("finish-event", context.getSessionData(pref + CK.S_FINISH_EVENT) != null 
                    ? context.getSessionData(pref + CK.S_FINISH_EVENT) : null);
            stage.set("death-event", context.getSessionData(pref + CK.S_DEATH_EVENT) != null 
                    ? context.getSessionData(pref + CK.S_DEATH_EVENT) : null);
            stage.set("disconnect-event", context.getSessionData(pref + CK.S_DISCONNECT_EVENT) != null 
                    ? context.getSessionData(pref + CK.S_DISCONNECT_EVENT) : null);
            stage.set("chat-events", context.getSessionData(pref + CK.S_CHAT_EVENTS) != null 
                    ? context.getSessionData(pref + CK.S_CHAT_EVENTS) : null);
            stage.set("chat-event-triggers", context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS) != null 
                    ? context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS) : null);
            stage.set("command-events", context.getSessionData(pref + CK.S_COMMAND_EVENTS) != null 
                    ? context.getSessionData(pref + CK.S_COMMAND_EVENTS) : null);
            stage.set("command-event-triggers", context.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS) != null 
                    ? context.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS) : null);
            Long delay = (Long) context.getSessionData(pref + CK.S_DELAY);
            if (context.getSessionData(pref + CK.S_DELAY) != null) {
                stage.set("delay", delay.intValue() / 1000);
            }
            String delayMessage = (String) context.getSessionData(pref + CK.S_DELAY_MESSAGE);
            if (context.getSessionData(pref + CK.S_DELAY_MESSAGE) != null) {
                stage.set("delay-message", delayMessage == null ? delayMessage : delayMessage.replace("\\n", "\n"));
            }
            String startMessage = (String) context.getSessionData(pref + CK.S_START_MESSAGE);
            if (context.getSessionData(pref + CK.S_START_MESSAGE) != null) {
                stage.set("start-message", startMessage == null ? startMessage : startMessage.replace("\\n", "\n"));
            }
            String completeMessage = (String) context.getSessionData(pref + CK.S_COMPLETE_MESSAGE);
            if (context.getSessionData(pref + CK.S_COMPLETE_MESSAGE) != null) {
                stage.set("complete-message", completeMessage == null ? completeMessage 
                        : completeMessage.replace("\\n", "\n"));
            }
            stage.set("objective-override", context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) != null 
                    ? context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) : null);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void saveRewards(ConversationContext context, ConfigurationSection section) {
        ConfigurationSection rews = section.createSection("rewards");
        rews.set("items", context.getSessionData(CK.REW_ITEMS) != null 
                ? (List<ItemStack>) context.getSessionData(CK.REW_ITEMS) : null);
        rews.set("money", context.getSessionData(CK.REW_MONEY) != null 
                ? (Integer) context.getSessionData(CK.REW_MONEY) : null);
        rews.set("quest-points", context.getSessionData(CK.REW_QUEST_POINTS) != null 
                ? (Integer) context.getSessionData(CK.REW_QUEST_POINTS) : null);
        rews.set("exp", context.getSessionData(CK.REW_EXP) != null 
                ? (Integer) context.getSessionData(CK.REW_EXP) : null);
        rews.set("commands", context.getSessionData(CK.REW_COMMAND) != null 
                ? (List<String>) context.getSessionData(CK.REW_COMMAND) : null);
        rews.set("commands-override-display", context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) != null 
                ? (List<String>) context.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) : null);
        rews.set("permissions", context.getSessionData(CK.REW_PERMISSION) != null 
                ? (List<String>)context.getSessionData(CK.REW_PERMISSION) : null);
        rews.set("permission-worlds", context.getSessionData(CK.REW_PERMISSION_WORLDS) != null 
                ? (List<String>)context.getSessionData(CK.REW_PERMISSION_WORLDS) : null);
        rews.set("mcmmo-skills", context.getSessionData(CK.REW_MCMMO_SKILLS) != null 
                ? (List<String>) context.getSessionData(CK.REW_MCMMO_SKILLS) : null);
        rews.set("mcmmo-levels", context.getSessionData(CK.REW_MCMMO_AMOUNTS) != null 
                ? (List<Integer>) context.getSessionData(CK.REW_MCMMO_AMOUNTS) : null);
        rews.set("heroes-exp-classes", context.getSessionData(CK.REW_HEROES_CLASSES) != null 
                ? (List<String>) context.getSessionData(CK.REW_HEROES_CLASSES) : null);
        rews.set("heroes-exp-amounts", context.getSessionData(CK.REW_HEROES_AMOUNTS) != null 
                ? (List<Double>) context.getSessionData(CK.REW_HEROES_AMOUNTS) : null);
        rews.set("phat-loots", context.getSessionData(CK.REW_PHAT_LOOTS) != null 
                ? (List<String>) context.getSessionData(CK.REW_PHAT_LOOTS) : null);
        LinkedList<String> customRews = context.getSessionData(CK.REW_CUSTOM) != null 
                ? (LinkedList<String>) context.getSessionData(CK.REW_CUSTOM) : null;
        LinkedList<Map<String, Object>> customRewsData = context.getSessionData(CK.REW_CUSTOM_DATA) != null 
                ? (LinkedList<Map<String, Object>>) context.getSessionData(CK.REW_CUSTOM_DATA) : null;
        if (customRews != null) {
            ConfigurationSection customRewsSec = rews.createSection("custom-rewards");
            for (int i = 0; i < customRews.size(); i++) {
                ConfigurationSection customRewSec = customRewsSec.createSection("req" + (i + 1));
                customRewSec.set("name", customRews.get(i));
                customRewSec.set("data", customRewsData.get(i));
            }
        }
        rews.set("details-override", context.getSessionData(CK.REW_DETAILS_OVERRIDE) != null 
                ? (List<String>)context.getSessionData(CK.REW_DETAILS_OVERRIDE) : null);
        if (rews.getKeys(false).isEmpty()) {
            section.set("rewards", null);
        }
    }
    
    private void savePlanner(ConversationContext context, ConfigurationSection section) {
        ConfigurationSection pln = section.createSection("planner");
        pln.set("start", context.getSessionData(CK.PLN_START_DATE) != null 
                ? (String) context.getSessionData(CK.PLN_START_DATE) : null);
        pln.set("end", context.getSessionData(CK.PLN_END_DATE) != null 
                ? (String) context.getSessionData(CK.PLN_END_DATE) : null);
        pln.set("repeat", context.getSessionData(CK.PLN_REPEAT_CYCLE) != null 
                ? ((Long) context.getSessionData(CK.PLN_REPEAT_CYCLE) / 1000) : null);
        pln.set("cooldown", context.getSessionData(CK.PLN_COOLDOWN) != null 
                ? ((Long) context.getSessionData(CK.PLN_COOLDOWN) / 1000) : null);
        if (pln.getKeys(false).isEmpty()) {
            section.set("planner", null);
        }
    }
    
    private void saveOptions(ConversationContext context, ConfigurationSection section) {
        ConfigurationSection opts = section.createSection("options");
        opts.set("allow-commands", context.getSessionData(CK.OPT_ALLOW_COMMANDS) != null 
                ? (Boolean) context.getSessionData(CK.OPT_ALLOW_COMMANDS) : null);
        opts.set("allow-quitting", context.getSessionData(CK.OPT_ALLOW_QUITTING) != null 
                ? (Boolean) context.getSessionData(CK.OPT_ALLOW_QUITTING) : null);
        opts.set("use-dungeonsxl-plugin", context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) != null 
                ? (Boolean) context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) : null);
        opts.set("use-parties-plugin", context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) != null 
                ? (Boolean) context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) : null);
        opts.set("share-progress-level", context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) != null 
                ? (Integer) context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) : null);
        opts.set("require-same-quest", context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST) != null 
                ? (Boolean) context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST) : null);
        if (opts.getKeys(false).isEmpty()) {
            section.set("options", null);
        }
    }
}
