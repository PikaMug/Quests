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

package me.blackvein.quests.conditions;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.conditions.menu.ConditionMenuPrompt;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.FakeConversable;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BukkitConditionFactory implements ConditionFactory, ConversationAbandonedListener {

    private final Quests plugin;
    private final ConversationFactory conversationFactory;
    private List<String> editingConditionNames = new LinkedList<>();

    public BukkitConditionFactory(final Quests plugin) {
        this.plugin = plugin;
        // Ensure to initialize factory last so that 'this' is fully initialized before it is passed
        this.conversationFactory = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new ConditionMenuPrompt(new ConversationContext(plugin, new FakeConversable(),
                        new HashMap<>()))).withTimeout(3600)
                .withPrefix(new LineBreakPrefix()).addConversationAbandonedListener(this);
    }
    
    public static class LineBreakPrefix implements ConversationPrefix {
        @Override
        public @NotNull String getPrefix(final @NotNull ConversationContext context) {
            return "\n";
        }
    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }
    
    public List<String> getNamesOfConditionsBeingEdited() {
        return editingConditionNames;
    }
    
    public void setNamesOfConditionsBeingEdited(final List<String> conditionNames) {
        this.editingConditionNames = conditionNames;
    }

    @Override
    public void conversationAbandoned(final @NotNull ConversationAbandonedEvent abandonedEvent) {
    }
    
    public Prompt returnToMenu(final ConversationContext context) {
        return new ConditionMainPrompt(context);
    }
    
    public void loadData(final ICondition condition, final ConversationContext context) {
        if (condition.isFailQuest()) {
            context.setSessionData(CK.C_FAIL_QUEST, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.C_FAIL_QUEST, Lang.get("noWord"));
        }
        if (condition.getEntitiesWhileRiding() != null && !condition.getEntitiesWhileRiding().isEmpty()) {
            final LinkedList<String> entities = new LinkedList<>(condition.getEntitiesWhileRiding());
            context.setSessionData(CK.C_WHILE_RIDING_ENTITY, entities);
        }
        if (condition.getNpcsWhileRiding() != null && !condition.getNpcsWhileRiding().isEmpty()) {
            final LinkedList<Integer> npcs = new LinkedList<>(condition.getNpcsWhileRiding());
            context.setSessionData(CK.C_WHILE_RIDING_NPC, npcs);
        }
        if (condition.getPermissions() != null && !condition.getPermissions().isEmpty()) {
            final LinkedList<String> permissions = new LinkedList<>(condition.getPermissions());
            context.setSessionData(CK.C_WHILE_PERMISSION, permissions);
        }
        if (condition.getItemsWhileHoldingMainHand() != null && !condition.getItemsWhileHoldingMainHand().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(condition.getItemsWhileHoldingMainHand());
            context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, items);
        }
        if (condition.getWorldsWhileStayingWithin() != null && !condition.getWorldsWhileStayingWithin().isEmpty()) {
            final LinkedList<String> worlds = new LinkedList<>(condition.getBiomesWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_WORLD, worlds);
        }
        if (condition.getBiomesWhileStayingWithin() != null && !condition.getBiomesWhileStayingWithin().isEmpty()) {
            final LinkedList<String> biomes = new LinkedList<>(condition.getBiomesWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_BIOME, biomes);
        }
        if (condition.getRegionsWhileStayingWithin() != null && !condition.getRegionsWhileStayingWithin().isEmpty()) {
            final LinkedList<String> regions = new LinkedList<>(condition.getRegionsWhileStayingWithin());
            context.setSessionData(CK.C_WHILE_WITHIN_REGION, regions);
        }
        if (condition.getPlaceholdersCheckIdentifier() != null
                && !condition.getPlaceholdersCheckIdentifier().isEmpty()) {
            final LinkedList<String> identifiers = new LinkedList<>(condition.getPlaceholdersCheckIdentifier());
            context.setSessionData(CK.C_WHILE_PLACEHOLDER_ID, identifiers);
        }
        if (condition.getPlaceholdersCheckValue() != null && !condition.getPlaceholdersCheckValue().isEmpty()) {
            final LinkedList<String> values = new LinkedList<>(condition.getPlaceholdersCheckValue());
            context.setSessionData(CK.C_WHILE_PLACEHOLDER_VAL, values);
        }
    }

    public void clearData(final ConversationContext context) {
        context.setSessionData(CK.C_OLD_CONDITION, null);
        context.setSessionData(CK.C_NAME, null);
        context.setSessionData(CK.C_FAIL_QUEST, null);
        context.setSessionData(CK.C_WHILE_RIDING_ENTITY, null);
        context.setSessionData(CK.C_WHILE_RIDING_NPC, null);
        context.setSessionData(CK.C_WHILE_PERMISSION, null);
        context.setSessionData(CK.C_WHILE_HOLDING_MAIN_HAND, null);
        context.setSessionData(CK.C_WHILE_WITHIN_WORLD, null);
        context.setSessionData(CK.C_WHILE_WITHIN_BIOME, null);
        context.setSessionData(CK.C_WHILE_WITHIN_REGION, null);
        context.setSessionData(CK.C_WHILE_PLACEHOLDER_ID, null);
        context.setSessionData(CK.C_WHILE_PLACEHOLDER_VAL, null);
    }

    public void deleteCondition(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        try {
            data.load(conditionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        final String condition = (String) context.getSessionData(CK.ED_CONDITION_DELETE);
        final ConfigurationSection sec = data.getConfigurationSection("conditions");
        if (sec != null && condition != null) {
            sec.set(condition, null);
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorDeleted"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted condition " + condition);
        }
        for (final IQuester q : plugin.getOfflineQuesters()) {
            for (final IQuest quest : q.getCurrentQuests().keySet()) {
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
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        if (context.getSessionData(CK.C_OLD_CONDITION) != null
                && !((String) Objects.requireNonNull(context.getSessionData(CK.C_OLD_CONDITION))).isEmpty()) {
            data.set("conditions." + context.getSessionData(CK.C_OLD_CONDITION), null);
            final Collection<ICondition> temp = plugin.getLoadedConditions();
            temp.remove(plugin.getCondition((String) context.getSessionData(CK.C_OLD_CONDITION)));
            plugin.setLoadedConditions(temp);
        }
        final ConfigurationSection section = data.createSection("conditions." + context.getSessionData(CK.C_NAME));
        editingConditionNames.remove((String) context.getSessionData(CK.C_NAME));
        if (context.getSessionData(CK.C_FAIL_QUEST) != null) {
            final String s = (String) context.getSessionData(CK.C_FAIL_QUEST);
            if (s != null && s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("fail-quest", true);
            }
        }
        if (context.getSessionData(CK.C_WHILE_RIDING_ENTITY) != null) {
            section.set("ride-entity", 
                    context.getSessionData(CK.C_WHILE_RIDING_ENTITY));
        }
        if (context.getSessionData(CK.C_WHILE_RIDING_NPC) != null) {
            section.set("ride-npc", 
                    context.getSessionData(CK.C_WHILE_RIDING_NPC));
        }
        if (context.getSessionData(CK.C_WHILE_PERMISSION) != null) {
            section.set("permission", 
                    context.getSessionData(CK.C_WHILE_PERMISSION));
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
        if (context.getSessionData(CK.C_WHILE_WITHIN_REGION) != null) {
            section.set("stay-within-region", 
                    context.getSessionData(CK.C_WHILE_WITHIN_REGION));
        }
        if (context.getSessionData(CK.C_WHILE_PLACEHOLDER_ID) != null) {
            section.set("check-placeholder-id", 
                    context.getSessionData(CK.C_WHILE_PLACEHOLDER_ID));
        }
        if (context.getSessionData(CK.C_WHILE_PLACEHOLDER_VAL) != null) {
            section.set("check-placeholder-value", 
                    context.getSessionData(CK.C_WHILE_PLACEHOLDER_VAL));
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("conditionEditorSaved"));
        if (plugin.getSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved condition " + context.getSessionData(CK.C_NAME));
        }
        for (final IQuester q : plugin.getOfflineQuesters()) {
            for (final IQuest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}