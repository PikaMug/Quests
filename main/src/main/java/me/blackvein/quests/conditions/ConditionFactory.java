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

    public ConditionFactory(final Quests plugin) {
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
    
    public void setNamesOfConditionsBeingEdited(final List<String> conditionNames) {
        this.editingConditionNames = conditionNames;
    }

    @Override
    public void conversationAbandoned(final ConversationAbandonedEvent abandonedEvent) {
    }
    
    public Prompt returnToMenu(final ConversationContext context) {
        return new ConditionMainPrompt(context);
    }
    
    public void loadData(final Condition condition, final ConversationContext context) {
        if (condition.isFailQuest() == true) {
            context.setSessionData(CK.C_FAIL_QUEST, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.C_FAIL_QUEST, Lang.get("noWord"));
        }
        if (condition.getItemsWhileHoldingMainHand() != null 
                && condition.getItemsWhileHoldingMainHand().isEmpty() == false) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>();
            items.addAll(condition.getItemsWhileHoldingMainHand());
            context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
        }
        if (condition.getWorldsWhileStayingWithin() != null 
                && condition.getWorldsWhileStayingWithin().isEmpty() == false) {
            final LinkedList<String> worlds = new LinkedList<String>();
            worlds.addAll(condition.getBiomesWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_WORLD, worlds);
        }
        if (condition.getBiomesWhileStayingWithin() != null 
                && condition.getBiomesWhileStayingWithin().isEmpty() == false) {
            final LinkedList<String> biomes = new LinkedList<String>();
            biomes.addAll(condition.getBiomesWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_BIOME, biomes);
        }
    }

    public void clearData(final ConversationContext context) {
        context.setSessionData(CK.C_OLD_CONDITION, null);
        context.setSessionData(CK.C_NAME, null);
        context.setSessionData(CK.C_FAIL_QUEST, null);
        context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, null);
        context.setSessionData(CK.C_WHILE_WITHIN_WORLD, null);
        context.setSessionData(CK.C_WHILE_WITHIN_BIOME, null);
    }

    public void deleteCondition(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        try {
            data.load(conditionsFile);
        } catch (final IOException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        final String condition = (String) context.getSessionData(CK.ED_CONDITION_DELETE);
        final ConfigurationSection sec = data.getConfigurationSection("conditions");
        sec.set(condition, null);
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
            @Override
            public void execute(final Boolean response) {
                if (!response) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorDeleted"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String name = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(name + " deleted condition " + condition);
        }
        for (final Quester q : plugin.getQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }

    public void saveCondition(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        try {
            data.load(conditionsFile);
        } catch (final IOException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        if (((String) context.getSessionData(CK.C_OLD_CONDITION)).isEmpty() == false) {
            data.set("conditions." + (String) context.getSessionData(CK.C_OLD_CONDITION), null);
            final LinkedList<Condition> temp = plugin.getConditions();
            temp.remove(plugin.getCondition((String) context.getSessionData(CK.C_OLD_CONDITION)));
            plugin.setConditions(temp);
        }
        final ConfigurationSection section = data.createSection("conditions." + (String) context.getSessionData(CK.C_NAME));
        editingConditionNames.remove(context.getSessionData(CK.C_NAME));
        if (context.getSessionData(CK.C_FAIL_QUEST) != null) {
            final String s = (String) context.getSessionData(CK.C_FAIL_QUEST);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("fail-quest", true);
            }
        }
        if (context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND) != null) {
            section.set("hold-main-hand", 
                    context.getSessionData(CK.C_WHILE_HOLDING_MAIN_HAND));
        }
        if (context.getSessionData(CK.C_WHILE_WITHIN_WORLD) != null) {
            section.set("stay-within-world", 
                    context.getSessionData(CK.C_WHILE_WITHIN_WORLD));
        }
        if (context.getSessionData(CK.C_WHILE_WITHIN_BIOME) != null) {
            section.set("stay-within-biome", 
                    context.getSessionData(CK.C_WHILE_WITHIN_BIOME));
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = new ReloadCallback<Boolean>() {
            @Override
            public void execute(final Boolean response) {
                if (!response) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
                }
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorSaved"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String name = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(name + " saved condition " + (String) context.getSessionData(CK.C_NAME));
        }
        for (final Quester q : plugin.getQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}