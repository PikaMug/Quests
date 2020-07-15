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

package me.blackvein.quests.conditions;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.conditions.menu.ConditionMenuPrompt;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class ConditionFactory implements ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory convoCreator;
    private List<String> editingConditionNames = new LinkedList<String>();

    public ConditionFactory(Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize convoCreator last so that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new ConditionMenuPrompt(new ConversationContext(plugin, null, null))).withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);
    }

    public ConversationFactory getConversationFactory() {
        return convoCreator;
    }
    
    public List<String> getNamesOfConditionsBeingEdited() {
        return editingConditionNames;
    }
    
    public void setNamesOfConditionsBeingEdited(List<String> conditionNames) {
        this.editingConditionNames = conditionNames;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
    }
    
    public Prompt returnToMenu(ConversationContext context) {
        return new ConditionMainPrompt(context);
    }
    
    public void loadData(Condition condition, ConversationContext context) {
        if (condition.isFailQuest() == true) {
            context.setSessionData(CK.C_FAIL_QUEST, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.C_FAIL_QUEST, Lang.get("noWord"));
        }
        if (condition.getItemsWhileHoldingMainHand() != null 
                && condition.getItemsWhileHoldingMainHand().isEmpty() == false) {
            LinkedList<ItemStack> items = new LinkedList<ItemStack>();
            items.addAll(condition.getItemsWhileHoldingMainHand());
            context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
        }
        if (condition.getWorldsWhileStayingWithin() != null 
                && condition.getWorldsWhileStayingWithin().isEmpty() == false) {
            LinkedList<String> worlds = new LinkedList<String>();
            worlds.addAll(condition.getBiomesWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_WORLD, worlds);
        }
        if (condition.getBiomesWhileStayingWithin() != null 
                && condition.getBiomesWhileStayingWithin().isEmpty() == false) {
            LinkedList<String> biomes = new LinkedList<String>();
            biomes.addAll(condition.getBiomesWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_BIOME, biomes);
        }
    }

    public void clearData(ConversationContext context) {
        context.setSessionData(CK.C_OLD_CONDITION, null);
        context.setSessionData(CK.C_NAME, null);
        context.setSessionData(CK.C_FAIL_QUEST, null);
        context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, null);
        context.setSessionData(CK.C_WHILE_WITHIN_WORLD, null);
        context.setSessionData(CK.C_WHILE_WITHIN_BIOME, null);
    }

    public void deleteCondition(ConversationContext context) {
        YamlConfiguration data = new YamlConfiguration();
        File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        try {
            data.load(conditionsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        String condition = (String) context.getSessionData(CK.ED_CONDITION_DELETE);
        ConfigurationSection sec = data.getConfigurationSection("conditions");
        sec.set(condition, null);
        try {
            data.save(conditionsFile);
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
        ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("conditionEditorDeleted"));
        for (Quester q : plugin.getQuesters()) {
            for (Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }

    @SuppressWarnings("unchecked")
    public void saveCondition(ConversationContext context) {
        YamlConfiguration data = new YamlConfiguration();
        File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        try {
            data.load(conditionsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        if (((String) context.getSessionData(CK.C_OLD_CONDITION)).isEmpty() == false) {
            data.set("conditions." + (String) context.getSessionData(CK.C_OLD_CONDITION), null);
            LinkedList<Condition> temp = plugin.getConditions();
            temp.remove(plugin.getCondition((String) context.getSessionData(CK.C_OLD_CONDITION)));
            plugin.setConditions(temp);
        }
        ConfigurationSection section = data.createSection("conditions." + (String) context.getSessionData(CK.C_NAME));
        editingConditionNames.remove((String) context.getSessionData(CK.C_NAME));
        if (context.getSessionData(CK.C_FAIL_QUEST) != null) {
            String s = (String) context.getSessionData(CK.C_FAIL_QUEST);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("fail-quest", true);
            }
        }
        if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) != null) {
            section.set("hold-main-hand", 
                    (LinkedList<ItemStack>) context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND));
        }
        if (context.getSessionData(CK.C_WHILE_WITHIN_WORLD) != null) {
            section.set("stay-within-world", 
                    (LinkedList<ItemStack>) context.getSessionData(CK.C_WHILE_WITHIN_WORLD));
        }
        if (context.getSessionData(CK.C_WHILE_WITHIN_BIOME) != null) {
            section.set("stay-within-biome", 
                    (LinkedList<ItemStack>) context.getSessionData(CK.C_WHILE_WITHIN_BIOME));
        }
        try {
            data.save(conditionsFile);
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
        ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("conditionEditorSaved"));
        for (Quester q : plugin.getQuesters()) {
            for (Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}