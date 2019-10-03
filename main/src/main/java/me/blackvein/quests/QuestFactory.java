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
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.managers.RegionManager;

import me.blackvein.quests.actions.Action;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenCreatePromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenExitPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenMainPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenSavePromptEvent;
import me.blackvein.quests.prompts.GUIDisplayPrompt;
import me.blackvein.quests.prompts.OptionsPrompt;
import me.blackvein.quests.prompts.RequirementsPrompt;
import me.blackvein.quests.prompts.RewardsPrompt;
import me.blackvein.quests.prompts.PlannerPrompt;
import me.blackvein.quests.prompts.StagesPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.WorldGuardAPI;
import net.citizensnpcs.api.CitizensAPI;

public class QuestFactory implements ConversationAbandonedListener {

    private final Quests plugin;
    private Map<UUID, Block> selectedBlockStarts = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedKillLocations = new HashMap<UUID, Block>();
    private Map<UUID, Block> selectedReachLocations = new HashMap<UUID, Block>();
    private HashSet<Player> selectingNpcs = new HashSet<Player>();
    private List<String> names = new LinkedList<String>();
    private ConversationFactory convoCreator;
    private File questsFile;
    
    public QuestFactory(Quests plugin) {
        this.plugin = plugin;
        questsFile = new File(plugin.getDataFolder(), "quests.yml");
        // Ensure to initialize convoCreator last so that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin).withModality(false).withLocalEcho(false).withFirstPrompt(new MainMenuPrompt()).withTimeout(3600).thatExcludesNonPlayersWithMessage("Console may not perform this operation!").addConversationAbandonedListener(this);
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

    public HashSet<Player> getSelectingNpcs() {
        return selectingNpcs;
    }

    public void setSelectingNpcs(HashSet<Player> selectingNpcs) {
        this.selectingNpcs = selectingNpcs;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
    
    public ConversationFactory getConversationFactory() {
        return convoCreator;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.getContext().getSessionData(CK.Q_NAME) != null) {
            names.remove((String) abandonedEvent.getContext().getSessionData(CK.Q_NAME));
        }
        Player player = (Player) abandonedEvent.getContext().getForWhom();
        selectedBlockStarts.remove(player.getUniqueId());
        selectedKillLocations.remove(player.getUniqueId());
        selectedReachLocations.remove(player.getUniqueId());
    }
    
    public class MainMenuPrompt extends NumericPrompt {
        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle() {
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

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenMainPromptEvent event = new QuestsEditorPostOpenMainPromptEvent(context);
            plugin.getServer().getPluginManager().callEvent(event);
            String text = ChatColor.GOLD + getTitle() + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            final Player player = (Player) context.getForWhom();
            switch (input.intValue()) {
                case 1:
                    if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.create")) {
                        return new QuestNamePrompt();
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                        return new MainMenuPrompt();
                    }
                case 2:
                    if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.edit")) {
                        return new SelectEditPrompt();
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                        return new MainMenuPrompt();
                    }
                case 3:
                    if (player.hasPermission("quests.editor.*") || player.hasPermission("quests.editor.delete")) {
                        return new SelectDeletePrompt();
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get("noPermission"));
                        return new MainMenuPrompt();
                    }
                case 4:
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("exited"));
                    return Prompt.END_OF_CONVERSATION;
                default:
                    return null;
            }
        }
    }

    public Prompt returnToMenu() {
        return new CreateMenuPrompt();
    }

    public class CreateMenuPrompt extends NumericPrompt {
        private final int size = 15;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("quest") + ": " + context.getSessionData(CK.Q_NAME);
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
                    if (context.getSessionData(CK.Q_START_NPC) == null && plugin.getDependencies().getCitizens() != null) {
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
                        return ChatColor.YELLOW + "(" + context.getSessionData(CK.Q_ASK_MESSAGE) + ChatColor.RESET + ChatColor.YELLOW + ")";
                    }
                case 3:
                    if (context.getSessionData(CK.Q_FINISH_MESSAGE) == null) {
                        return ChatColor.DARK_RED + "(" + Lang.get("questRequiredNoneSet") + ")";
                    } else {
                        return ChatColor.YELLOW + "(" + context.getSessionData(CK.Q_FINISH_MESSAGE) + ChatColor.RESET + ChatColor.YELLOW + ")";
                    }
                case 4:
                    if (context.getSessionData(CK.Q_START_NPC) == null && plugin.getDependencies().getCitizens() != null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else if (plugin.getDependencies().getCitizens() != null) {
                        return ChatColor.YELLOW + "(" + CitizensAPI.getNPCRegistry().getById((Integer) context.getSessionData(CK.Q_START_NPC)).getName() + ChatColor.RESET + ChatColor.YELLOW + ")";
                    } else {
                        return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
                    }
                case 5:
                    if (context.getSessionData(CK.Q_START_BLOCK) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        Location l = (Location) context.getSessionData(CK.Q_START_BLOCK);
                        return ChatColor.YELLOW + "(" + l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")";
                    }
                case 6:
                    if (plugin.getDependencies().getWorldGuardApi() != null) {
                        if (context.getSessionData(CK.Q_REGION) == null) {
                            return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                        } else {
                            return ChatColor.YELLOW + "(" + ChatColor.GREEN + (String) context.getSessionData(CK.Q_REGION) + ChatColor.YELLOW + ")";
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
                            return ChatColor.YELLOW + "(" + ItemUtil.getDisplayString((ItemStack) context.getSessionData(CK.Q_GUIDISPLAY)) + ChatColor.RESET + ChatColor.YELLOW + ")";
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
            QuestsEditorPostOpenCreatePromptEvent event = new QuestsEditorPostOpenCreatePromptEvent(context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": " + ChatColor.AQUA) + ChatColor.GOLD + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
                case 1:
                    return new SetNamePrompt();
                case 2:
                    return new AskMessagePrompt();
                case 3:
                    return new FinishMessagePrompt();
                case 4:
                    if (plugin.getDependencies().getCitizens() != null) {
                        return new NPCStartPrompt();
                    } else {
                        return new CreateMenuPrompt();
                    }
                case 5:
                    selectedBlockStarts.put(((Player) context.getForWhom()).getUniqueId(), null);
                    return new BlockStartPrompt();
                case 6:
                    if (plugin.getDependencies().getWorldGuardApi() != null) {
                        return new RegionPrompt();
                    } else {
                        return new CreateMenuPrompt();
                    }
                case 7:
                    return new InitialActionPrompt();
                case 8:
                    if (plugin.getDependencies().getCitizens() != null) {
                        return new GUIDisplayPrompt(plugin, QuestFactory.this);
                    } else {
                        return new CreateMenuPrompt();
                    }
                case 9:
                    return new RequirementsPrompt(plugin, QuestFactory.this);
                case 10:
                    return new PlannerPrompt(plugin, QuestFactory.this);
                case 11:
                    return new StagesPrompt(plugin, QuestFactory.this);
                case 12:
                    return new RewardsPrompt(plugin, QuestFactory.this);
                case 13:
                    return new OptionsPrompt(plugin, QuestFactory.this);
                case 14:
                    return new SavePrompt();
                case 15:
                    return new ExitPrompt();
                default:
                    return null;
            }
        }
    }

    private class SelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String s = ChatColor.GOLD + Lang.get("questEditTitle") + "\n";
            for (Quest q : plugin.getQuests()) {
                s += ChatColor.GRAY + "- " + ChatColor.YELLOW + q.getName() + "\n";
            }
            return s + ChatColor.GOLD + Lang.get("questEditorEditEnterQuestName");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                Quest q = plugin.getQuest(input);
                if (q != null) {
                    loadQuest(context, q);
                    return new CreateMenuPrompt();
                }
                return new SelectEditPrompt();
            } else {
                return new MainMenuPrompt();
            }
        }
    }

    private class QuestNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("questCreateTitle") + "\n";
            text += ChatColor.AQUA + Lang.get("questEditorCreate") + " " + ChatColor.GOLD + "- " 
                    + Lang.get("questEditorEnterQuestName");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                for (Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNameExists"));
                        return new QuestNamePrompt();
                    }
                }
                if (names.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new QuestNamePrompt();
                }
                if (input.contains(".") || input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestNamePrompt();
                }
                context.setSessionData(CK.Q_NAME, input);
                names.add(input);
                return new CreateMenuPrompt();
            } else {
                return new MainMenuPrompt();
            }
        }
    }

    private class SetNamePrompt extends StringPrompt {

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
                            return new SetNamePrompt();
                        }
                    }
                }
                if (names.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new SetNamePrompt();
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new QuestNamePrompt();
                }
                names.remove((String) context.getSessionData(CK.Q_NAME));
                context.setSessionData(CK.Q_NAME, input);
                names.add(input);
            }
            return new CreateMenuPrompt();
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
                        context.setSessionData(CK.Q_ASK_MESSAGE, context.getSessionData(CK.Q_ASK_MESSAGE) + " " + input.substring(2));
                        return new CreateMenuPrompt();
                    }
                }
                context.setSessionData(CK.Q_ASK_MESSAGE, input);
            }
            return new CreateMenuPrompt();
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
                        context.setSessionData(CK.Q_FINISH_MESSAGE, context.getSessionData(CK.Q_FINISH_MESSAGE) + " " + input.substring(2));
                        return new CreateMenuPrompt();
                    }
                }
                context.setSessionData(CK.Q_FINISH_MESSAGE, input);
            }
            return new CreateMenuPrompt();
        }
    }

    private class NPCStartPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            selectingNpcs.add((Player) context.getForWhom());
            return ChatColor.YELLOW + Lang.get("questEditorEnterNPCStart") + "\n" + ChatColor.GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > -1) {
                        if (CitizensAPI.getNPCRegistry().getById(i) == null) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidNPC"));
                            return new NPCStartPrompt();
                        }
                        context.setSessionData(CK.Q_START_NPC, i);
                        selectingNpcs.remove((Player) context.getForWhom());
                        return new CreateMenuPrompt();
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                    return new NPCStartPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_START_NPC, null);
            }
            selectingNpcs.remove((Player) context.getForWhom());
            return new CreateMenuPrompt();
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
                return new CreateMenuPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                selectedBlockStarts.remove(player.getUniqueId());
                context.setSessionData(CK.Q_START_BLOCK, null);
                return new CreateMenuPrompt();
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
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
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
                    error = error.replaceAll("<region>", ChatColor.RED + input + ChatColor.YELLOW);
                    player.sendMessage(ChatColor.YELLOW + error);
                    return new RegionPrompt();
                } else {
                    context.setSessionData(CK.Q_REGION, found);
                    return new CreateMenuPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_REGION, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("questWGRegionCleared"));
                return new CreateMenuPrompt();
            } else {
                return new CreateMenuPrompt();
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
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                Action a = plugin.getAction(input);
                if (a != null) {
                    context.setSessionData(CK.Q_INITIAL_EVENT, a.getName());
                    return new CreateMenuPrompt();
                }
                player.sendMessage(ChatColor.RED + input + ChatColor.YELLOW + " " + Lang.get("questEditorInvalidEventName"));
                return new InitialActionPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.Q_INITIAL_EVENT, null);
                player.sendMessage(ChatColor.YELLOW + Lang.get("questEditorEventCleared"));
                return new CreateMenuPrompt();
            } else {
                return new CreateMenuPrompt();
            }
        }
    }

    public class SavePrompt extends StringPrompt {
        private final int size = 2;
        
        public int getSize() {
            return size;
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
            return ChatColor.YELLOW + Lang.get("questEditorSave") + " \"" + ChatColor.AQUA + context.getSessionData(CK.Q_NAME) + ChatColor.YELLOW + "\"?\n";
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenSavePromptEvent event = new QuestsEditorPostOpenSavePromptEvent(QuestFactory.this, context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText(context);
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                if (context.getSessionData(CK.Q_ASK_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNeedAskMessage"));
                    return new CreateMenuPrompt();
                } else if (context.getSessionData(CK.Q_FINISH_MESSAGE) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNeedFinishMessage"));
                    return new CreateMenuPrompt();
                } else if (new StagesPrompt(plugin, QuestFactory.this).getStages(context) == 0) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorNeedStages"));
                    return new CreateMenuPrompt();
                }
                FileConfiguration data = new YamlConfiguration();
                try {
                    data.load(new File(plugin.getDataFolder(), "quests.yml"));
                    ConfigurationSection questSection = data.getConfigurationSection("quests");
                    if (questSection == null) {
                        questSection = data.createSection("quests");
                    }
                    int customNum = 1;
                    while (true) {
                        if (questSection.contains("custom" + customNum)) {
                            customNum++;
                        } else {
                            break;
                        }
                    }
                    ConfigurationSection newSection = questSection.createSection("custom" + customNum);
                    saveQuest(context, newSection);
                    data.save(new File(plugin.getDataFolder(), "quests.yml"));
                    context.getForWhom().sendRawMessage(ChatColor.GREEN
                            + Lang.get("questEditorSaved").replaceAll("<command>", "/questadmin " + Lang.get("COMMAND_QUESTADMIN_RELOAD")));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new CreateMenuPrompt();
            } else {
                return new SavePrompt();
            }
        }
    }

    public class ExitPrompt extends StringPrompt {
        private final int size = 2;
        
        public int getSize() {
            return size;
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
            return ChatColor.YELLOW + Lang.get("confirmDelete") + "\n";
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenExitPromptEvent event = new QuestsEditorPostOpenExitPromptEvent(QuestFactory.this, context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText(context);
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + Lang.get("exited"));
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new CreateMenuPrompt();
            } else {
                return new ExitPrompt();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void saveQuest(ConversationContext cc, ConfigurationSection cs) {
        String edit = null;
        if (cc.getSessionData(CK.ED_QUEST_EDIT) != null) {
            edit = (String) cc.getSessionData(CK.ED_QUEST_EDIT);
        }
        if (edit != null) {
            ConfigurationSection questList = cs.getParent();
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
        String name = (String) cc.getSessionData(CK.Q_NAME);
        String desc = (String) cc.getSessionData(CK.Q_ASK_MESSAGE);
        String finish = (String) cc.getSessionData(CK.Q_FINISH_MESSAGE);
        Integer npcStart = null;
        String blockStart = null;
        String initialEvent = null;
        String region = null;
        ItemStack guiDisplay = null;
        Integer moneyReq = null;
        Integer questPointsReq = null;
        List<ItemStack> itemReqs = null;
        List<Boolean> removeItemReqs = null;
        List<String> permReqs = null;
        List<String> questReqs = null;
        List<String> questBlocks = null;
        List<String> mcMMOSkillReqs = null;
        List<Integer> mcMMOAmountReqs = null;
        String heroesPrimaryReq = null;
        String heroesSecondaryReq = null;
        LinkedList<String> customReqs = null;
        LinkedList<Map<String, Object>> customReqsData = null;
        String failMessage = null;
        Integer moneyRew = null;
        Integer questPointsRew = null;
        List<ItemStack> itemRews = null;
        List<Integer> RPGItemRews = null;
        List<Integer> RPGItemAmounts = null;
        Integer expRew = null;
        List<String> commandRews = null;
        List<String> commandDisplayOverrideRews = null;
        List<String> permRews = null;
        List<String> mcMMOSkillRews = null;
        List<Integer> mcMMOSkillAmounts = null;
        List<String> heroesClassRews = null;
        List<Double> heroesExpRews = null;
        List<String> phatLootRews = null;
        LinkedList<String> customRews = null;
        LinkedList<Map<String, Object>> customRewsData = null;
        String startDatePln = null;
        String endDatePln = null;
        Long repeatCyclePln = null;
        Long cooldownPln = null;
        boolean allowCommandsOpt = true;
        boolean allowQuittingOpt = true;
        boolean useDungeonsXLPluginOpt = false;
        boolean usePartiesPluginOpt = true;
        Integer shareProgressLevelOpt = 1;
        boolean requireSameQuestOpt = true;
        if (cc.getSessionData(CK.Q_START_NPC) != null) {
            npcStart = (Integer) cc.getSessionData(CK.Q_START_NPC);
        }
        if (cc.getSessionData(CK.Q_START_BLOCK) != null) {
            blockStart = Quests.getLocationInfo((Location) cc.getSessionData(CK.Q_START_BLOCK));
        }
        if (cc.getSessionData(CK.REQ_MONEY) != null) {
            moneyReq = (Integer) cc.getSessionData(CK.REQ_MONEY);
        }
        if (cc.getSessionData(CK.REQ_QUEST_POINTS) != null) {
            questPointsReq = (Integer) cc.getSessionData(CK.REQ_QUEST_POINTS);
        }
        if (cc.getSessionData(CK.REQ_ITEMS) != null) {
            itemReqs = new LinkedList<ItemStack>();
            removeItemReqs = new LinkedList<Boolean>();
            itemReqs.addAll((List<ItemStack>) cc.getSessionData(CK.REQ_ITEMS));
            removeItemReqs.addAll((List<Boolean>) cc.getSessionData(CK.REQ_ITEMS_REMOVE));
        }
        if (cc.getSessionData(CK.REQ_PERMISSION) != null) {
            permReqs = new LinkedList<String>();
            permReqs.addAll((List<String>) cc.getSessionData(CK.REQ_PERMISSION));
        }
        if (cc.getSessionData(CK.REQ_QUEST) != null) {
            questReqs = new LinkedList<String>();
            questReqs.addAll((List<String>) cc.getSessionData(CK.REQ_QUEST));
        }
        if (cc.getSessionData(CK.REQ_QUEST_BLOCK) != null) {
            questBlocks = new LinkedList<String>();
            questBlocks.addAll((List<String>) cc.getSessionData(CK.REQ_QUEST_BLOCK));
        }
        if (cc.getSessionData(CK.REQ_MCMMO_SKILLS) != null) {
            mcMMOSkillReqs = new LinkedList<String>();
            mcMMOAmountReqs = new LinkedList<Integer>();
            mcMMOSkillReqs.addAll((List<String>) cc.getSessionData(CK.REQ_MCMMO_SKILLS));
            mcMMOAmountReqs.addAll((List<Integer>) cc.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS));
        }
        if (cc.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null) {
            heroesPrimaryReq = (String) cc.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS);
        }
        if (cc.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null) {
            heroesSecondaryReq = (String) cc.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS);
        }
        if (cc.getSessionData(CK.REQ_CUSTOM) != null) {
            customReqs = (LinkedList<String>) cc.getSessionData(CK.REQ_CUSTOM);
            customReqsData = (LinkedList<Map<String, Object>>) cc.getSessionData(CK.REQ_CUSTOM_DATA);
        }
        if (cc.getSessionData(CK.Q_FAIL_MESSAGE) != null) {
            failMessage = (String) cc.getSessionData(CK.Q_FAIL_MESSAGE);
        }
        if (cc.getSessionData(CK.Q_INITIAL_EVENT) != null) {
            initialEvent = (String) cc.getSessionData(CK.Q_INITIAL_EVENT);
        }
        if (cc.getSessionData(CK.Q_REGION) != null) {
            region = (String) cc.getSessionData(CK.Q_REGION);
        }
        if (cc.getSessionData(CK.Q_GUIDISPLAY) != null) {
            guiDisplay = (ItemStack) cc.getSessionData(CK.Q_GUIDISPLAY);
        }
        if (cc.getSessionData(CK.REW_MONEY) != null) {
            moneyRew = (Integer) cc.getSessionData(CK.REW_MONEY);
        }
        if (cc.getSessionData(CK.REW_QUEST_POINTS) != null) {
            questPointsRew = (Integer) cc.getSessionData(CK.REW_QUEST_POINTS);
        }
        if (cc.getSessionData(CK.REW_ITEMS) != null) {
            itemRews = (List<ItemStack>) cc.getSessionData(CK.REW_ITEMS);
        }
        if (cc.getSessionData(CK.REW_EXP) != null) {
            expRew = (Integer) cc.getSessionData(CK.REW_EXP);
        }
        if (cc.getSessionData(CK.REW_COMMAND) != null) {
            commandRews = new LinkedList<String>();
            commandRews.addAll((List<String>)cc.getSessionData(CK.REW_COMMAND));
        }
        if (cc.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY) != null) {
            commandDisplayOverrideRews = new LinkedList<String>();
            commandDisplayOverrideRews.addAll((List<String>)cc.getSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY));
        }
        if (cc.getSessionData(CK.REW_PERMISSION) != null) {
            permRews = new LinkedList<String>();
            permRews.addAll((List<String>) cc.getSessionData(CK.REW_PERMISSION));
        }
        if (cc.getSessionData(CK.REW_MCMMO_SKILLS) != null) {
            mcMMOSkillRews = new LinkedList<String>();
            mcMMOSkillAmounts = new LinkedList<Integer>();
            mcMMOSkillRews.addAll((List<String>) cc.getSessionData(CK.REW_MCMMO_SKILLS));
            mcMMOSkillAmounts.addAll((List<Integer>) cc.getSessionData(CK.REW_MCMMO_AMOUNTS));
        }
        if (cc.getSessionData(CK.REW_HEROES_CLASSES) != null) {
            heroesClassRews = new LinkedList<String>();
            heroesExpRews = new LinkedList<Double>();
            heroesClassRews.addAll((List<String>) cc.getSessionData(CK.REW_HEROES_CLASSES));
            heroesExpRews.addAll((List<Double>) cc.getSessionData(CK.REW_HEROES_AMOUNTS));
        }
        if (cc.getSessionData(CK.REW_PHAT_LOOTS) != null) {
            phatLootRews = new LinkedList<String>();
            phatLootRews.addAll((List<String>) cc.getSessionData(CK.REW_PHAT_LOOTS));
        }
        if (cc.getSessionData(CK.REW_CUSTOM) != null) {
            customRews = (LinkedList<String>) cc.getSessionData(CK.REW_CUSTOM);
            customRewsData = (LinkedList<Map<String, Object>>) cc.getSessionData(CK.REW_CUSTOM_DATA);
        }
        if (cc.getSessionData(CK.PLN_START_DATE) != null) {
            startDatePln = (String) cc.getSessionData(CK.PLN_START_DATE);
        }
        if (cc.getSessionData(CK.PLN_END_DATE) != null) {
            endDatePln = (String) cc.getSessionData(CK.PLN_END_DATE);
        }
        if (cc.getSessionData(CK.PLN_REPEAT_CYCLE) != null) {
            repeatCyclePln = (Long) cc.getSessionData(CK.PLN_REPEAT_CYCLE);
        }
        if (cc.getSessionData(CK.PLN_COOLDOWN) != null) {
            cooldownPln = (Long) cc.getSessionData(CK.PLN_COOLDOWN);
        }
        if (cc.getSessionData(CK.OPT_ALLOW_COMMANDS) != null) {
            allowCommandsOpt = (Boolean) cc.getSessionData(CK.OPT_ALLOW_COMMANDS);
        }
        if (cc.getSessionData(CK.OPT_ALLOW_QUITTING) != null) {
            allowQuittingOpt = (Boolean) cc.getSessionData(CK.OPT_ALLOW_QUITTING);
        }
        if (cc.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) != null) {
            useDungeonsXLPluginOpt = (Boolean) cc.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN);
        }
        if (cc.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) != null) {
            usePartiesPluginOpt = (Boolean) cc.getSessionData(CK.OPT_USE_PARTIES_PLUGIN);
        }
        if (cc.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) != null) {
            shareProgressLevelOpt = (Integer) cc.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL);
        }
        if (cc.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) != null) {
            requireSameQuestOpt = (Boolean) cc.getSessionData(CK.OPT_REQUIRE_SAME_QUEST);
        }
        cs.set("name", name);
        cs.set("npc-giver-id", npcStart);
        cs.set("block-start", blockStart);
        cs.set("ask-message", desc);
        cs.set("finish-message", finish);
        cs.set("event", initialEvent);
        cs.set("region", region);
        cs.set("gui-display", guiDisplay);
        if (moneyReq != null || questPointsReq != null || itemReqs != null && itemReqs.isEmpty() == false || permReqs != null && permReqs.isEmpty() == false || (questReqs != null && questReqs.isEmpty() == false) || (questBlocks != null && questBlocks.isEmpty() == false) || (mcMMOSkillReqs != null && mcMMOSkillReqs.isEmpty() == false) || heroesPrimaryReq != null || heroesSecondaryReq != null || customReqs != null) {
            ConfigurationSection reqs = cs.createSection("requirements");
            reqs.set("items", itemReqs);
            reqs.set("remove-items", removeItemReqs);
            reqs.set("money", moneyReq);
            reqs.set("quest-points", questPointsReq);
            reqs.set("permissions", permReqs);
            reqs.set("quests", questReqs);
            reqs.set("quest-blocks", questBlocks);
            reqs.set("mcmmo-skills", mcMMOSkillReqs);
            reqs.set("mcmmo-amounts", mcMMOAmountReqs);
            reqs.set("heroes-primary-class", heroesPrimaryReq);
            reqs.set("heroes-secondary-class", heroesSecondaryReq);
            if (customReqs != null) {
                ConfigurationSection customReqsSec = reqs.createSection("custom-requirements");
                for (int i = 0; i < customReqs.size(); i++) {
                    ConfigurationSection customReqSec = customReqsSec.createSection("req" + (i + 1));
                    customReqSec.set("name", customReqs.get(i));
                    customReqSec.set("data", customReqsData.get(i));
                }
            }
            reqs.set("fail-requirement-message", failMessage);
        } else {
            cs.set("requirements", null);
        }
        ConfigurationSection stages = cs.createSection("stages");
        ConfigurationSection ordered = stages.createSection("ordered");
        String pref;
        LinkedList<Integer> breakNames;
        LinkedList<Integer> breakAmounts;
        LinkedList<Short> breakDurability;
        LinkedList<Integer> damageNames;
        LinkedList<Integer> damageAmounts;
        LinkedList<Short> damageDurability;
        LinkedList<Integer> placeNames;
        LinkedList<Integer> placeAmounts;
        LinkedList<Short> placeDurability;
        LinkedList<Integer> useNames;
        LinkedList<Integer> useAmounts;
        LinkedList<Short> useDurability;
        LinkedList<Integer> cutNames;
        LinkedList<Integer> cutAmounts;
        LinkedList<Short> cutDurability;
        LinkedList<ItemStack> craftItems;
        LinkedList<ItemStack> smeltItems;
        LinkedList<String> enchantments;
        LinkedList<Integer> enchantmentIds;
        LinkedList<Integer> enchantmentAmounts;
        LinkedList<ItemStack> brewItems;
        Integer fish;
        Integer players;
        LinkedList<ItemStack> deliveryItems;
        LinkedList<Integer> deliveryNPCIds;
        LinkedList<String> deliveryMessages;
        LinkedList<Integer> npcTalkIds;
        LinkedList<Integer> npcKillIds;
        LinkedList<Integer> npcKillAmounts;
        LinkedList<String> mobs;
        LinkedList<Integer> mobAmounts;
        LinkedList<String> mobLocs;
        LinkedList<Integer> mobRadii;
        LinkedList<String> mobLocNames;
        LinkedList<String> reachLocs;
        LinkedList<Integer> reachRadii;
        LinkedList<String> reachNames;
        LinkedList<String> tames;
        LinkedList<Integer> tameAmounts;
        LinkedList<String> shearColors;
        LinkedList<Integer> shearAmounts;
        LinkedList<String> passDisplays;
        LinkedList<LinkedList<String>> passPhrases;
        LinkedList<String> customObjs;
        LinkedList<Integer> customObjCounts;
        LinkedList<Entry<String, Object>> customObjsData;
        String script;
        String startEvent;
        String finishEvent;
        String deathEvent;
        String disconnectEvent;
        LinkedList<String> chatEvents;
        LinkedList<String> chatEventTriggers;
        LinkedList<String> commandEvents;
        LinkedList<String> commandEventTriggers;
        Long delay;
        String overrideDisplay;
        String delayMessage;
        String startMessage;
        String completeMessage;
        for (int i = 1; i <= new StagesPrompt(plugin, this).getStages(cc); i++) {
            pref = "stage" + i;
            ConfigurationSection stage = ordered.createSection("" + i);
            breakNames = null;
            breakAmounts = null;
            breakDurability = null;
            damageNames = null;
            damageAmounts = null;
            damageDurability = null;
            placeNames = null;
            placeAmounts = null;
            placeDurability = null;
            useNames = null;
            useAmounts = null;
            useDurability = null;
            cutNames = null;
            cutAmounts = null;
            cutDurability = null;
            craftItems = null;
            smeltItems = null;
            enchantments = null;
            enchantmentIds = null;
            enchantmentAmounts = null;
            brewItems = null;
            fish = null;
            players = null;
            deliveryItems = null;
            deliveryNPCIds = null;
            deliveryMessages = null;
            npcTalkIds = null;
            npcKillIds = null;
            npcKillAmounts = null;
            mobs = null;
            mobAmounts = null;
            mobLocs = null;
            mobRadii = null;
            mobLocNames = null;
            reachLocs = null;
            reachRadii = null;
            reachNames = null;
            tames = null;
            tameAmounts = null;
            shearColors = null;
            shearAmounts = null;
            passDisplays = null;
            passPhrases = null;
            customObjs = null;
            customObjCounts = null;
            customObjsData = null;
            script = null;
            startEvent = null;
            finishEvent = null;
            deathEvent = null;
            disconnectEvent = null;
            chatEvents = null;
            chatEventTriggers = null;
            commandEvents = null;
            commandEventTriggers = null;
            delay = null;
            overrideDisplay = null;
            delayMessage = null;
            startMessage = null;
            completeMessage = null;
            if (cc.getSessionData(pref + CK.S_BREAK_NAMES) != null) {
                breakNames = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_BREAK_NAMES);
                breakAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_BREAK_AMOUNTS);
                breakDurability = (LinkedList<Short>) cc.getSessionData(pref + CK.S_BREAK_DURABILITY);
            }
            if (cc.getSessionData(pref + CK.S_DAMAGE_NAMES) != null) {
                damageNames = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_DAMAGE_NAMES);
                damageAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
                damageDurability = (LinkedList<Short>) cc.getSessionData(pref + CK.S_DAMAGE_DURABILITY);
            }
            if (cc.getSessionData(pref + CK.S_PLACE_NAMES) != null) {
                placeNames = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_PLACE_NAMES);
                placeAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_PLACE_AMOUNTS);
                placeDurability = (LinkedList<Short>) cc.getSessionData(pref + CK.S_PLACE_DURABILITY);
            }
            if (cc.getSessionData(pref + CK.S_USE_NAMES) != null) {
                useNames = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_USE_NAMES);
                useAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_USE_AMOUNTS);
                useDurability = (LinkedList<Short>) cc.getSessionData(pref + CK.S_USE_DURABILITY);
            }
            if (cc.getSessionData(pref + CK.S_CUT_NAMES) != null) {
                cutNames = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_CUT_NAMES);
                cutAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_CUT_AMOUNTS);
                cutDurability = (LinkedList<Short>) cc.getSessionData(pref + CK.S_CUT_DURABILITY);
            }
            if (cc.getSessionData(pref + CK.S_CRAFT_ITEMS) != null) {
                craftItems = (LinkedList<ItemStack>) cc.getSessionData(pref + CK.S_CRAFT_ITEMS);
            }
            if (cc.getSessionData(pref + CK.S_SMELT_ITEMS) != null) {
                smeltItems = (LinkedList<ItemStack>) cc.getSessionData(pref + CK.S_SMELT_ITEMS);
            }
            if (cc.getSessionData(pref + CK.S_ENCHANT_TYPES) != null) {
                enchantments = (LinkedList<String>) cc.getSessionData(pref + CK.S_ENCHANT_TYPES);
                enchantmentIds = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_ENCHANT_NAMES);
                enchantmentAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_ENCHANT_AMOUNTS);
            }
            if (cc.getSessionData(pref + CK.S_BREW_ITEMS) != null) {
                brewItems = (LinkedList<ItemStack>) cc.getSessionData(pref + CK.S_BREW_ITEMS);
            }
            if (cc.getSessionData(pref + CK.S_FISH) != null) {
                fish = (Integer) cc.getSessionData(pref + CK.S_FISH);
            }
            if (cc.getSessionData(pref + CK.S_PLAYER_KILL) != null) {
                players = (Integer) cc.getSessionData(pref + CK.S_PLAYER_KILL);
            }
            if (cc.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                deliveryItems = (LinkedList<ItemStack>) cc.getSessionData(pref + CK.S_DELIVERY_ITEMS);
                deliveryNPCIds = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_DELIVERY_NPCS);
                deliveryMessages = (LinkedList<String>) cc.getSessionData(pref + CK.S_DELIVERY_MESSAGES);
            }
            if (cc.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) != null) {
                npcTalkIds = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_NPCS_TO_TALK_TO);
            }
            if (cc.getSessionData(pref + CK.S_NPCS_TO_KILL) != null) {
                npcKillIds = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_NPCS_TO_KILL);
                npcKillAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
            }
            if (cc.getSessionData(pref + CK.S_MOB_TYPES) != null) {
                mobs = (LinkedList<String>) cc.getSessionData(pref + CK.S_MOB_TYPES);
                mobAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_MOB_AMOUNTS);
                if (cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null) {
                    mobLocs = (LinkedList<String>) cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    mobRadii = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                    mobLocNames = (LinkedList<String>) cc.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
                }
            }
            if (cc.getSessionData(pref + CK.S_REACH_LOCATIONS) != null) {
                reachLocs = (LinkedList<String>) cc.getSessionData(pref + CK.S_REACH_LOCATIONS);
                reachRadii = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS);
                reachNames = (LinkedList<String>) cc.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES);
            }
            if (cc.getSessionData(pref + CK.S_TAME_TYPES) != null) {
                tames = (LinkedList<String>) cc.getSessionData(pref + CK.S_TAME_TYPES);
                tameAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_TAME_AMOUNTS);
            }
            if (cc.getSessionData(pref + CK.S_SHEAR_COLORS) != null) {
                shearColors = (LinkedList<String>) cc.getSessionData(pref + CK.S_SHEAR_COLORS);
                shearAmounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
            }
            if (cc.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null) {
                passDisplays = (LinkedList<String>) cc.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
                passPhrases = (LinkedList<LinkedList<String>>) cc.getSessionData(pref + CK.S_PASSWORD_PHRASES);
            }
            if (cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) != null) {
                customObjs = (LinkedList<String>) cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
                customObjCounts = (LinkedList<Integer>) cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
                customObjsData = (LinkedList<Entry<String, Object>>) cc.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
            }
            if (cc.getSessionData(pref + CK.S_START_EVENT) != null) {
                startEvent = (String) cc.getSessionData(pref + CK.S_START_EVENT);
            }
            if (cc.getSessionData(pref + CK.S_FINISH_EVENT) != null) {
                finishEvent = (String) cc.getSessionData(pref + CK.S_FINISH_EVENT);
            }
            if (cc.getSessionData(pref + CK.S_DEATH_EVENT) != null) {
                deathEvent = (String) cc.getSessionData(pref + CK.S_DEATH_EVENT);
            }
            if (cc.getSessionData(pref + CK.S_DISCONNECT_EVENT) != null) {
                disconnectEvent = (String) cc.getSessionData(pref + CK.S_DISCONNECT_EVENT);
            }
            if (cc.getSessionData(pref + CK.S_CHAT_EVENTS) != null) {
                chatEvents = (LinkedList<String>) cc.getSessionData(pref + CK.S_CHAT_EVENTS);
                chatEventTriggers = (LinkedList<String>) cc.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS);
            }
            if (cc.getSessionData(pref + CK.S_COMMAND_EVENTS) != null) {
                commandEvents = (LinkedList<String>) cc.getSessionData(pref + CK.S_COMMAND_EVENTS);
                commandEventTriggers = (LinkedList<String>) cc.getSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS);
            }
            if (cc.getSessionData(pref + CK.S_DELAY) != null) {
                delay = (Long) cc.getSessionData(pref + CK.S_DELAY);
                delayMessage = (String) cc.getSessionData(pref + CK.S_DELAY_MESSAGE);
            }
            if (cc.getSessionData(pref + CK.S_DENIZEN) != null) {
                script = (String) cc.getSessionData(pref + CK.S_DENIZEN);
            }
            if (cc.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) != null) {
                overrideDisplay = (String) cc.getSessionData(pref + CK.S_OVERRIDE_DISPLAY);
            }
            if (cc.getSessionData(pref + CK.S_START_MESSAGE) != null) {
                startMessage = (String) cc.getSessionData(pref + CK.S_START_MESSAGE);
            }
            if (cc.getSessionData(pref + CK.S_COMPLETE_MESSAGE) != null) {
                completeMessage = (String) cc.getSessionData(pref + CK.S_COMPLETE_MESSAGE);
            }
            if (breakNames != null && breakNames.isEmpty() == false) {
                stage.set("break-block-names", breakNames);
                stage.set("break-block-amounts", breakAmounts);
                stage.set("break-block-durability", breakDurability);
            }
            if (damageNames != null && damageNames.isEmpty() == false) {
                stage.set("damage-block-names", damageNames);
                stage.set("damage-block-amounts", damageAmounts);
                stage.set("damage-block-durability", damageDurability);
            }
            if (placeNames != null && placeNames.isEmpty() == false) {
                stage.set("place-block-names", placeNames);
                stage.set("place-block-amounts", placeAmounts);
                stage.set("place-block-durability", placeDurability);
            }
            if (useNames != null && useNames.isEmpty() == false) {
                stage.set("use-block-names", useNames);
                stage.set("use-block-amounts", useAmounts);
                stage.set("use-block-durability", useDurability);
            }
            if (cutNames != null && cutNames.isEmpty() == false) {
                stage.set("cut-block-names", cutNames);
                stage.set("cut-block-amounts", cutAmounts);
                stage.set("cut-block-durability", cutDurability);
            }
            if (craftItems != null && craftItems.isEmpty() == false) {
                stage.set("items-to-craft", craftItems);
            } else {
                stage.set("items-to-craft", null);
            }
            if (smeltItems != null && smeltItems.isEmpty() == false) {
                stage.set("items-to-smelt", smeltItems);
            } else {
                stage.set("items-to-smelt", null);
            }
            stage.set("enchantments", enchantments);
            stage.set("enchantment-item-names", enchantmentIds);
            stage.set("enchantment-amounts", enchantmentAmounts);
            if (brewItems != null && brewItems.isEmpty() == false) {
                stage.set("items-to-brew", brewItems);
            } else {
                stage.set("items-to-brew", null);
            }
            stage.set("fish-to-catch", fish);
            stage.set("players-to-kill", players);
            if (deliveryItems != null && deliveryItems.isEmpty() == false) {
                stage.set("items-to-deliver", deliveryItems);
            } else {
                stage.set("items-to-deliver", null);
            }
            stage.set("npc-delivery-ids", deliveryNPCIds);
            stage.set("delivery-messages", deliveryMessages);
            stage.set("npc-ids-to-talk-to", npcTalkIds);
            stage.set("npc-ids-to-kill", npcKillIds);
            stage.set("npc-kill-amounts", npcKillAmounts);
            stage.set("mobs-to-kill", mobs);
            stage.set("mob-amounts", mobAmounts);
            stage.set("locations-to-kill", mobLocs);
            stage.set("kill-location-radii", mobRadii);
            stage.set("kill-location-names", mobLocNames);
            stage.set("locations-to-reach", reachLocs);
            stage.set("reach-location-radii", reachRadii);
            stage.set("reach-location-names", reachNames);
            stage.set("mobs-to-tame", tames);
            stage.set("mob-tame-amounts", tameAmounts);
            stage.set("sheep-to-shear", shearColors);
            stage.set("sheep-amounts", shearAmounts);
            stage.set("password-displays", passDisplays);
            if (passPhrases != null) {
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
            if (customObjs != null && customObjs.isEmpty() == false) {
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
            stage.set("script-to-run", script);
            stage.set("start-event", startEvent);
            stage.set("finish-event", finishEvent);
            stage.set("death-event", deathEvent);
            stage.set("disconnect-event", disconnectEvent);
            if (chatEvents != null && chatEvents.isEmpty() == false) {
                stage.set("chat-events", chatEvents);
                stage.set("chat-event-triggers", chatEventTriggers);
            }
            if (commandEvents != null && commandEvents.isEmpty() == false) {
                stage.set("command-events", commandEvents);
                stage.set("command-event-triggers", commandEventTriggers);
            }
            if (delay != null) {
                stage.set("delay", delay.intValue() / 1000);
            }
            stage.set("delay-message", delayMessage == null ? delayMessage : delayMessage.replace("\\n", "\n"));
            stage.set("objective-override", overrideDisplay == null ? overrideDisplay : overrideDisplay.replace("\\n", "\n"));
            stage.set("start-message", startMessage == null ? startMessage : startMessage.replace("\\n", "\n"));
            stage.set("complete-message", completeMessage == null ? completeMessage : completeMessage.replace("\\n", "\n"));
        }
        if (moneyRew != null || questPointsRew != null || itemRews != null && itemRews.isEmpty() == false 
                || permRews != null && permRews.isEmpty() == false || expRew != null || commandRews != null && commandRews.isEmpty() == false 
                || commandDisplayOverrideRews != null && commandDisplayOverrideRews.isEmpty() == false || mcMMOSkillRews != null 
                || RPGItemRews != null || heroesClassRews != null && heroesClassRews.isEmpty() == false 
                || phatLootRews != null && phatLootRews.isEmpty() == false || customRews != null && customRews.isEmpty() == false) {
            ConfigurationSection rews = cs.createSection("rewards");
            rews.set("items", (itemRews != null && itemRews.isEmpty() == false) ? itemRews : null);
            rews.set("money", moneyRew);
            rews.set("quest-points", questPointsRew);
            rews.set("exp", expRew);
            rews.set("permissions", permRews);
            rews.set("commands", commandRews);
            rews.set("commands-override-display", commandDisplayOverrideRews);
            rews.set("mcmmo-skills", mcMMOSkillRews);
            rews.set("mcmmo-levels", mcMMOSkillAmounts);
            rews.set("rpgitem-names", RPGItemRews);
            rews.set("rpgitem-amounts", RPGItemAmounts);
            rews.set("heroes-exp-classes", heroesClassRews);
            rews.set("heroes-exp-amounts", heroesExpRews);
            rews.set("phat-loots", phatLootRews);
            if (customRews != null) {
                ConfigurationSection customRewsSec = rews.createSection("custom-rewards");
                for (int i = 0; i < customRews.size(); i++) {
                    ConfigurationSection customRewSec = customRewsSec.createSection("req" + (i + 1));
                    customRewSec.set("name", customRews.get(i));
                    customRewSec.set("data", customRewsData.get(i));
                }
            }
        } else {
            cs.set("rewards", null);
        }
        if (startDatePln != null || endDatePln != null || repeatCyclePln != null || cooldownPln != null) {
            ConfigurationSection sch = cs.createSection("planner");
            if (startDatePln != null) {
                sch.set("start", startDatePln);
            }
            if (endDatePln != null) {
                sch.set("end", endDatePln);
            }
            if (repeatCyclePln != null) {
                sch.set("repeat", repeatCyclePln.intValue() / 1000);
            }
            if (cooldownPln != null) {
                sch.set("cooldown", cooldownPln.intValue() / 1000);
            }
        } else {
            cs.set("planner", null);
        }
        ConfigurationSection sch = cs.createSection("options");
        sch.set("allow-commands", allowCommandsOpt);
        sch.set("allow-quitting", allowQuittingOpt);
        sch.set("use-dungeonsxl-plugin", useDungeonsXLPluginOpt);
        sch.set("use-parties-plugin", usePartiesPluginOpt);
        sch.set("share-progress-level", shareProgressLevelOpt);
        sch.set("require-same-quest", requireSameQuestOpt);
    }

    @SuppressWarnings("deprecation")
    public static void loadQuest(ConversationContext cc, Quest q) {
        cc.setSessionData(CK.ED_QUEST_EDIT, q.getName());
        cc.setSessionData(CK.Q_NAME, q.getName());
        if (q.npcStart != null) {
            cc.setSessionData(CK.Q_START_NPC, q.npcStart.getId());
        }
        cc.setSessionData(CK.Q_START_BLOCK, q.blockStart);
        cc.setSessionData(CK.Q_ASK_MESSAGE, q.description);
        cc.setSessionData(CK.Q_FINISH_MESSAGE, q.finished);
        if (q.initialAction != null) {
            cc.setSessionData(CK.Q_INITIAL_EVENT, q.initialAction.getName());
        }
        if (q.region != null) {
            cc.setSessionData(CK.Q_REGION, q.region);
        }
        if (q.guiDisplay != null) {
            cc.setSessionData(CK.Q_GUIDISPLAY, q.guiDisplay);
        }
        Requirements reqs = q.getRequirements();
        if (reqs.getMoney() != 0) {
            cc.setSessionData(CK.REQ_MONEY, reqs.getMoney());
        }
        if (reqs.getQuestPoints() != 0) {
            cc.setSessionData(CK.REQ_QUEST_POINTS, reqs.getQuestPoints());
        }
        if (reqs.getItems().isEmpty() == false) {
            cc.setSessionData(CK.REQ_ITEMS, reqs.getItems());
            cc.setSessionData(CK.REQ_ITEMS_REMOVE, reqs.getRemoveItems());
        }
        if (reqs.getNeededQuests().isEmpty() == false) {
            cc.setSessionData(CK.REQ_QUEST, reqs.getNeededQuests());
        }
        if (reqs.getBlockQuests().isEmpty() == false) {
            cc.setSessionData(CK.REQ_QUEST_BLOCK, reqs.getBlockQuests());
        }
        if (reqs.getMcmmoSkills().isEmpty() == false) {
            cc.setSessionData(CK.REQ_MCMMO_SKILLS, reqs.getMcmmoAmounts());
            cc.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, reqs.getMcmmoAmounts());
        }
        if (reqs.getPermissions().isEmpty() == false) {
            cc.setSessionData(CK.REQ_PERMISSION, reqs.getPermissions());
        }
        if (reqs.getHeroesPrimaryClass() != null) {
            cc.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, reqs.getHeroesPrimaryClass());
        }
        if (reqs.getHeroesSecondaryClass() != null) {
            cc.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, reqs.getHeroesSecondaryClass());
        }
        if (reqs.getFailRequirements() != null) {
            cc.setSessionData(CK.Q_FAIL_MESSAGE, reqs.getFailRequirements());
        }
        if (reqs.getCustomRequirements().isEmpty() == false) {
            LinkedList<String> list = new LinkedList<String>();
            LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
            for (Entry<String, Map<String, Object>> entry : reqs.getCustomRequirements().entrySet()) {
                list.add(entry.getKey());
                datamapList.add(entry.getValue());
            }
            cc.setSessionData(CK.REQ_CUSTOM, list);
            cc.setSessionData(CK.REQ_CUSTOM_DATA, datamapList);
        }
        Rewards rews = q.getRewards();
        if (rews.getMoney() != 0) {
            cc.setSessionData(CK.REW_MONEY, rews.getMoney());
        }
        if (rews.getQuestPoints() != 0) {
            cc.setSessionData(CK.REW_QUEST_POINTS, rews.getQuestPoints());
        }
        if (rews.getExp() != 0) {
            cc.setSessionData(CK.REW_EXP, rews.getExp());
        }
        if (rews.getItems().isEmpty() == false) {
            cc.setSessionData(CK.REW_ITEMS, rews.getItems());
        }
        if (rews.getCommands().isEmpty() == false) {
            cc.setSessionData(CK.REW_COMMAND, rews.getCommands());
        }
        if (rews.getCommandsOverrideDisplay().isEmpty() == false) {
            cc.setSessionData(CK.REW_COMMAND_OVERRIDE_DISPLAY, rews.getCommandsOverrideDisplay());
        }
        if (rews.getPermissions().isEmpty() == false) {
            cc.setSessionData(CK.REW_PERMISSION, rews.getPermissions());
        }
        if (rews.getMcmmoSkills().isEmpty() == false) {
            cc.setSessionData(CK.REW_MCMMO_SKILLS, rews.getMcmmoSkills());
            cc.setSessionData(CK.REW_MCMMO_AMOUNTS, rews.getMcmmoAmounts());
        }
        if (rews.getHeroesClasses().isEmpty() == false) {
            cc.setSessionData(CK.REW_HEROES_CLASSES, rews.getHeroesClasses());
            cc.setSessionData(CK.REW_HEROES_AMOUNTS, rews.getHeroesAmounts());
        }
        if (rews.getPhatLoots().isEmpty() == false) {
            cc.setSessionData(CK.REW_PHAT_LOOTS, rews.getPhatLoots());
        }
        if (rews.getCustomRewards().isEmpty() == false) {
            cc.setSessionData(CK.REW_CUSTOM, new LinkedList<String>(rews.getCustomRewards().keySet()));
            cc.setSessionData(CK.REW_CUSTOM_DATA, new LinkedList<Object>(rews.getCustomRewards().values()));
        }
        Planner pln = q.getPlanner();
        if (pln.getStart() != null) {
            cc.setSessionData(CK.PLN_START_DATE, pln.getStart());
        }
        if (pln.getEnd() != null) {
            cc.setSessionData(CK.PLN_END_DATE, pln.getEnd());
        }
        if (pln.getRepeat() != -1) {
            cc.setSessionData(CK.PLN_REPEAT_CYCLE, pln.getRepeat());
        }
        if (pln.getCooldown() != -1) {
            cc.setSessionData(CK.PLN_COOLDOWN, pln.getCooldown());
        }
        Options opt = q.getOptions();
        cc.setSessionData(CK.OPT_ALLOW_COMMANDS, opt.getAllowCommands());
        cc.setSessionData(CK.OPT_ALLOW_QUITTING, opt.getAllowQuitting());
        cc.setSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN, opt.getUseDungeonsXLPlugin());
        cc.setSessionData(CK.OPT_USE_PARTIES_PLUGIN, opt.getUsePartiesPlugin());
        cc.setSessionData(CK.OPT_SHARE_PROGRESS_LEVEL, opt.getShareProgressLevel());
        cc.setSessionData(CK.OPT_REQUIRE_SAME_QUEST, opt.getRequireSameQuest());
        // Stages (Objectives)
        int index = 1;
        for (Stage stage : q.getStages()) {
            final String pref = "stage" + index;
            index++;
            cc.setSessionData(pref, Boolean.TRUE);
            if (stage.blocksToBreak != null) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToBreak) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                cc.setSessionData(pref + CK.S_BREAK_NAMES, names);
                cc.setSessionData(pref + CK.S_BREAK_AMOUNTS, amnts);
                cc.setSessionData(pref + CK.S_BREAK_DURABILITY, durab);
            }
            if (stage.blocksToDamage != null) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToDamage) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                cc.setSessionData(pref + CK.S_DAMAGE_NAMES, names);
                cc.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amnts);
                cc.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durab);
            }
            if (stage.blocksToPlace != null) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToPlace) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                cc.setSessionData(pref + CK.S_PLACE_NAMES, names);
                cc.setSessionData(pref + CK.S_PLACE_AMOUNTS, amnts);
                cc.setSessionData(pref + CK.S_PLACE_DURABILITY, durab);
            }
            if (stage.blocksToUse != null) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToUse) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                cc.setSessionData(pref + CK.S_USE_NAMES, names);
                cc.setSessionData(pref + CK.S_USE_AMOUNTS, amnts);
                cc.setSessionData(pref + CK.S_USE_DURABILITY, durab);
            }
            if (stage.blocksToCut != null) {
                LinkedList<String> names = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                LinkedList<Short> durab = new LinkedList<Short>();
                for (ItemStack e : stage.blocksToCut) {
                    names.add(e.getType().name());
                    amnts.add(e.getAmount());
                    durab.add(e.getDurability());
                }
                cc.setSessionData(pref + CK.S_CUT_NAMES, names);
                cc.setSessionData(pref + CK.S_CUT_AMOUNTS, amnts);
                cc.setSessionData(pref + CK.S_CUT_DURABILITY, durab);
            }
            if (stage.getItemsToCraft().isEmpty() == false) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (ItemStack is : stage.getItemsToCraft()) {
                    items.add(is);
                }
                cc.setSessionData(pref + CK.S_CRAFT_ITEMS, items);
            }
            if (stage.getItemsToSmelt().isEmpty() == false) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (ItemStack is : stage.getItemsToSmelt()) {
                    items.add(is);
                }
                cc.setSessionData(pref + CK.S_SMELT_ITEMS, items);
            }
            if (stage.itemsToEnchant.isEmpty() == false) {
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
                cc.setSessionData(pref + CK.S_ENCHANT_TYPES, enchants);
                cc.setSessionData(pref + CK.S_ENCHANT_NAMES, names);
                cc.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, amounts);
            }
            if (stage.getItemsToBrew().isEmpty() == false) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>();
                for (ItemStack is : stage.getItemsToBrew()) {
                    items.add(is);
                }
                cc.setSessionData(pref + CK.S_BREW_ITEMS, items);
            }
            if (stage.fishToCatch != null) {
                cc.setSessionData(pref + CK.S_FISH, stage.fishToCatch);
            }
            if (stage.playersToKill != null) {
                cc.setSessionData(pref + CK.S_PLAYER_KILL, stage.playersToKill);
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
                cc.setSessionData(pref + CK.S_DELIVERY_ITEMS, items);
                cc.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);
                cc.setSessionData(pref + CK.S_DELIVERY_MESSAGES, stage.deliverMessages);
            }
            if (stage.citizensToInteract.isEmpty() == false) {
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (Integer n : stage.citizensToInteract) {
                    npcs.add(n);
                }
                cc.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);
            }
            if (stage.citizensToKill.isEmpty() == false) {
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (Integer n : stage.citizensToKill) {
                    npcs.add(n);
                }
                cc.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);
                cc.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, stage.citizenNumToKill);
            }
            if (stage.mobsToKill.isEmpty() == false) {
                LinkedList<String> mobs = new LinkedList<String>();
                for (EntityType et : stage.mobsToKill) {
                    mobs.add(MiscUtil.getPrettyMobName(et));
                }
                cc.setSessionData(pref + CK.S_MOB_TYPES, mobs);
                cc.setSessionData(pref + CK.S_MOB_AMOUNTS, stage.mobNumToKill);
                if (stage.locationsToKillWithin.isEmpty() == false) {
                    LinkedList<String> locs = new LinkedList<String>();
                    for (Location l : stage.locationsToKillWithin) {
                        locs.add(Quests.getLocationInfo(l));
                    }
                    cc.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locs);
                    cc.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, stage.radiiToKillWithin);
                    cc.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, stage.killNames);
                }
            }
            if (stage.locationsToReach.isEmpty() == false) {
                LinkedList<String> locs = new LinkedList<String>();
                for (Location l : stage.locationsToReach) {
                    locs.add(Quests.getLocationInfo(l));
                }
                cc.setSessionData(pref + CK.S_REACH_LOCATIONS, locs);
                cc.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, stage.radiiToReachWithin);
                cc.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, stage.locationNames);
            }
            if (stage.mobsToTame.isEmpty() == false) {
                LinkedList<String> mobs = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                for (Entry<EntityType, Integer> e : stage.mobsToTame.entrySet()) {
                    mobs.add(MiscUtil.getPrettyMobName(e.getKey()));
                    amnts.add(e.getValue());
                }
                cc.setSessionData(pref + CK.S_TAME_TYPES, mobs);
                cc.setSessionData(pref + CK.S_TAME_AMOUNTS, amnts);
            }
            if (stage.sheepToShear.isEmpty() == false) {
                LinkedList<String> colors = new LinkedList<String>();
                LinkedList<Integer> amnts = new LinkedList<Integer>();
                for (Entry<DyeColor, Integer> e : stage.sheepToShear.entrySet()) {
                    colors.add(MiscUtil.getPrettyDyeColorName(e.getKey()));
                    amnts.add(e.getValue());
                }
                cc.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                cc.setSessionData(pref + CK.S_SHEAR_AMOUNTS, amnts);
            }
            if (stage.passwordDisplays.isEmpty() == false) {
                cc.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, stage.passwordDisplays);
                cc.setSessionData(pref + CK.S_PASSWORD_PHRASES, stage.passwordPhrases);
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
                cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
                cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                cc.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
            }
            if (stage.startEvent != null) {
                cc.setSessionData(pref + CK.S_START_EVENT, stage.startEvent.getName());
            }
            if (stage.finishEvent != null) {
                cc.setSessionData(pref + CK.S_FINISH_EVENT, stage.finishEvent.getName());
            }
            if (stage.deathEvent != null) {
                cc.setSessionData(pref + CK.S_DEATH_EVENT, stage.deathEvent.getName());
            }
            if (stage.disconnectEvent != null) {
                cc.setSessionData(pref + CK.S_DISCONNECT_EVENT, stage.disconnectEvent.getName());
            }
            if (stage.chatEvents != null) {
                LinkedList<String> chatEvents = new LinkedList<String>();
                LinkedList<String> chatEventTriggers = new LinkedList<String>();
                for (String s : stage.chatEvents.keySet()) {
                    chatEventTriggers.add(s);
                    chatEvents.add(stage.chatEvents.get(s).getName());
                }
                cc.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
                cc.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);
            }
            if (stage.commandEvents != null) {
                LinkedList<String> commandEvents = new LinkedList<String>();
                LinkedList<String> commandEventTriggers = new LinkedList<String>();
                for (String s : stage.commandEvents.keySet()) {
                    commandEventTriggers.add(s);
                    commandEvents.add(stage.commandEvents.get(s).getName());
                }
                cc.setSessionData(pref + CK.S_COMMAND_EVENTS, commandEvents);
                cc.setSessionData(pref + CK.S_COMMAND_EVENT_TRIGGERS, commandEventTriggers);
            }
            if (stage.delay != -1) {
                cc.setSessionData(pref + CK.S_DELAY, stage.delay);
                if (stage.delayMessage != null) {
                    cc.setSessionData(pref + CK.S_DELAY_MESSAGE, stage.delayMessage);
                }
            }
            if (stage.script != null) {
                cc.setSessionData(pref + CK.S_DENIZEN, stage.script);
            }
            if (stage.objectiveOverride != null) {
                cc.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, stage.objectiveOverride);
            }
            if (stage.completeMessage != null) {
                cc.setSessionData(pref + CK.S_COMPLETE_MESSAGE, stage.completeMessage);
            }
            if (stage.startMessage != null) {
                cc.setSessionData(pref + CK.S_START_MESSAGE, stage.startMessage);
            }
        }
    }

    private class SelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("questDeleteTitle") + "\n";
            for (Quest quest : plugin.getQuests()) {
                text += ChatColor.AQUA + quest.getName() + ChatColor.YELLOW + ",";
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
                        if (q.getRequirements().getNeededQuests().contains(q.getName()) || q.getRequirements().getBlockQuests().contains(q.getName())) {
                            used.add(q.getName());
                        }
                    }
                    if (used.isEmpty()) {
                        context.setSessionData(CK.ED_QUEST_DELETE, found.getName());
                        return new DeletePrompt();
                    } else {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questEditorQuestAsRequirement1") + " \"" + ChatColor.DARK_PURPLE + context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + "\" " + Lang.get("questEditorQuestAsRequirement2"));
                        for (String s : used) {
                            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + "- " + ChatColor.DARK_RED + s);
                        }
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questEditorQuestAsRequirement3"));
                        return new SelectDeletePrompt();
                    }
                }
                ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questEditorQuestNotFound"));
                return new SelectDeletePrompt();
            } else {
                return new MainMenuPrompt();
            }
        }
    }

    private class DeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GREEN + "" + ChatColor.BOLD + "1" + ChatColor.RESET + "" + ChatColor.GREEN + " - " + Lang.get("yesWord") + "\n";
            text += ChatColor.RED + "" + ChatColor.BOLD + "2" + ChatColor.RESET + "" + ChatColor.RED + " - " + Lang.get("noWord");
            return ChatColor.RED + Lang.get("confirmDelete") + " (" + ChatColor.YELLOW + (String) context.getSessionData(CK.ED_QUEST_DELETE) + ChatColor.RED + ")\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                deleteQuest(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new MainMenuPrompt();
            } else {
                return new DeletePrompt();
            }
        }
    }

    private void deleteQuest(ConversationContext context) {
        YamlConfiguration data = new YamlConfiguration();
        try {
            data.load(questsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile"));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile"));
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
        plugin.reloadQuests();
        context.getForWhom().sendRawMessage(ChatColor.GREEN + Lang.get("questDeleted"));
    }
}
