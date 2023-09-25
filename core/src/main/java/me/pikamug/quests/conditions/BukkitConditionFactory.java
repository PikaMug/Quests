/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.conditions;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.convo.conditions.menu.ConditionMenuPrompt;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitFakeConversable;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
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
import java.util.UUID;

public class BukkitConditionFactory implements ConditionFactory, ConversationAbandonedListener {

    private final BukkitQuestsPlugin plugin;
    private final ConversationFactory conversationFactory;
    private List<String> editingConditionNames = new LinkedList<>();

    public BukkitConditionFactory(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
        // Ensure to initialize factory last so that 'this' is fully initialized before it is passed
        this.conversationFactory = new ConversationFactory(plugin).withModality(false).withLocalEcho(false)
                .withFirstPrompt(new ConditionMenuPrompt(new ConversationContext(plugin, new BukkitFakeConversable(),
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
    
    public void loadData(final ConversationContext context, final Condition condition) {
        BukkitCondition bukkitCondition = (BukkitCondition) condition;
        if (bukkitCondition.isFailQuest()) {
            context.setSessionData(Key.C_FAIL_QUEST, true);
        } else {
            context.setSessionData(Key.C_FAIL_QUEST, false);
        }
        if (bukkitCondition.getEntitiesWhileRiding() != null && !bukkitCondition.getEntitiesWhileRiding().isEmpty()) {
            final LinkedList<String> entities = new LinkedList<>(bukkitCondition.getEntitiesWhileRiding());
            context.setSessionData(Key.C_WHILE_RIDING_ENTITY, entities);
        }
        if (bukkitCondition.getNpcsWhileRiding() != null && !bukkitCondition.getNpcsWhileRiding().isEmpty()) {
            final LinkedList<UUID> npcs = new LinkedList<>(bukkitCondition.getNpcsWhileRiding());
            context.setSessionData(Key.C_WHILE_RIDING_NPC, npcs);
        }
        if (bukkitCondition.getPermissions() != null && !bukkitCondition.getPermissions().isEmpty()) {
            final LinkedList<String> permissions = new LinkedList<>(bukkitCondition.getPermissions());
            context.setSessionData(Key.C_WHILE_PERMISSION, permissions);
        }
        if (bukkitCondition.getItemsWhileHoldingMainHand() != null && !bukkitCondition.getItemsWhileHoldingMainHand().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(bukkitCondition.getItemsWhileHoldingMainHand());
            context.setSessionData(Key.C_WHILE_HOLDING_MAIN_HAND, items);
        }
        if (bukkitCondition.getItemsWhileWearing() != null && !bukkitCondition.getItemsWhileWearing().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(bukkitCondition.getItemsWhileWearing());
            context.setSessionData(Key.C_WHILE_WEARING, items);
        }
        if (bukkitCondition.getWorldsWhileStayingWithin() != null && !bukkitCondition.getWorldsWhileStayingWithin().isEmpty()) {
            final LinkedList<String> worlds = new LinkedList<>(bukkitCondition.getBiomesWhileStayingWithin());
            context.setSessionData(Key.C_WHILE_WITHIN_WORLD, worlds);
        }
        if (bukkitCondition.getTickStartWhileStayingWithin() > -1) {
            final int tick = bukkitCondition.getTickStartWhileStayingWithin();
            context.setSessionData(Key.C_WHILE_WITHIN_TICKS_START, tick);
        }
        if (bukkitCondition.getTickEndWhileStayingWithin() > -1) {
            final int tick = bukkitCondition.getTickEndWhileStayingWithin();
            context.setSessionData(Key.C_WHILE_WITHIN_TICKS_END, tick);
        }
        if (bukkitCondition.getBiomesWhileStayingWithin() != null && !bukkitCondition.getBiomesWhileStayingWithin().isEmpty()) {
            final LinkedList<String> biomes = new LinkedList<>(bukkitCondition.getBiomesWhileStayingWithin());
            context.setSessionData(Key.C_WHILE_WITHIN_BIOME, biomes);
        }
        if (bukkitCondition.getRegionsWhileStayingWithin() != null && !bukkitCondition.getRegionsWhileStayingWithin().isEmpty()) {
            final LinkedList<String> regions = new LinkedList<>(bukkitCondition.getRegionsWhileStayingWithin());
            context.setSessionData(Key.C_WHILE_WITHIN_REGION, regions);
        }
        if (bukkitCondition.getPlaceholdersCheckIdentifier() != null
                && !bukkitCondition.getPlaceholdersCheckIdentifier().isEmpty()) {
            final LinkedList<String> identifiers = new LinkedList<>(bukkitCondition.getPlaceholdersCheckIdentifier());
            context.setSessionData(Key.C_WHILE_PLACEHOLDER_ID, identifiers);
        }
        if (bukkitCondition.getPlaceholdersCheckValue() != null && !bukkitCondition.getPlaceholdersCheckValue().isEmpty()) {
            final LinkedList<String> values = new LinkedList<>(bukkitCondition.getPlaceholdersCheckValue());
            context.setSessionData(Key.C_WHILE_PLACEHOLDER_VAL, values);
        }
    }

    public void clearData(final ConversationContext context) {
        context.setSessionData(Key.C_OLD_CONDITION, null);
        context.setSessionData(Key.C_NAME, null);
        context.setSessionData(Key.C_FAIL_QUEST, null);
        context.setSessionData(Key.C_WHILE_RIDING_ENTITY, null);
        context.setSessionData(Key.C_WHILE_RIDING_NPC, null);
        context.setSessionData(Key.C_WHILE_PERMISSION, null);
        context.setSessionData(Key.C_WHILE_HOLDING_MAIN_HAND, null);
        context.setSessionData(Key.C_WHILE_WEARING, null);
        context.setSessionData(Key.C_WHILE_WITHIN_WORLD, null);
        context.setSessionData(Key.C_WHILE_WITHIN_TICKS_START, null);
        context.setSessionData(Key.C_WHILE_WITHIN_TICKS_END, null);
        context.setSessionData(Key.C_WHILE_WITHIN_BIOME, null);
        context.setSessionData(Key.C_WHILE_WITHIN_REGION, null);
        context.setSessionData(Key.C_WHILE_PLACEHOLDER_ID, null);
        context.setSessionData(Key.C_WHILE_PLACEHOLDER_VAL, null);
    }

    public void deleteCondition(final ConversationContext context) {
        final YamlConfiguration data = new YamlConfiguration();
        final File conditionsFile = new File(plugin.getDataFolder(), "conditions.yml");
        try {
            data.load(conditionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        final String condition = (String) context.getSessionData(Key.ED_CONDITION_DELETE);
        final ConfigurationSection sec = data.getConfigurationSection("conditions");
        if (sec != null && condition != null) {
            sec.set(condition, null);
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorDeleted"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted condition " + condition);
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
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
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        if (context.getSessionData(Key.C_OLD_CONDITION) != null
                && !((String) Objects.requireNonNull(context.getSessionData(Key.C_OLD_CONDITION))).isEmpty()) {
            data.set("conditions." + context.getSessionData(Key.C_OLD_CONDITION), null);
            final Collection<Condition> temp = plugin.getLoadedConditions();
            temp.remove(plugin.getCondition((String) context.getSessionData(Key.C_OLD_CONDITION)));
            plugin.setLoadedConditions(temp);
        }
        final ConfigurationSection section = data.createSection("conditions." + context.getSessionData(Key.C_NAME));
        editingConditionNames.remove((String) context.getSessionData(Key.C_NAME));
        if (context.getSessionData(Key.C_FAIL_QUEST) != null) {
            final Boolean b = (Boolean) context.getSessionData(Key.C_FAIL_QUEST);
            if (b != null) {
                section.set("fail-quest", b);
            }
        }
        if (context.getSessionData(Key.C_WHILE_RIDING_ENTITY) != null) {
            section.set("ride-entity", context.getSessionData(Key.C_WHILE_RIDING_ENTITY));
        }
        if (context.getSessionData(Key.C_WHILE_RIDING_NPC) != null) {
            section.set("ride-npc-uuid", context.getSessionData(Key.C_WHILE_RIDING_NPC));
        }
        if (context.getSessionData(Key.C_WHILE_PERMISSION) != null) {
            section.set("permission", context.getSessionData(Key.C_WHILE_PERMISSION));
        }
        if (context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
            section.set("hold-main-hand", context.getSessionData(Key.C_WHILE_HOLDING_MAIN_HAND));
        }
        if (context.getSessionData(Key.C_WHILE_WEARING) != null) {
            section.set("wear", context.getSessionData(Key.C_WHILE_WEARING));
        }
        if (context.getSessionData(Key.C_WHILE_WITHIN_WORLD) != null) {
            section.set("stay-within-world", context.getSessionData(Key.C_WHILE_WITHIN_WORLD));
        }
        if (context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START) != null) {
            section.set("stay-within-ticks.start", context.getSessionData(Key.C_WHILE_WITHIN_TICKS_START));
        }
        if (context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END) != null) {
            section.set("stay-within-ticks.end", context.getSessionData(Key.C_WHILE_WITHIN_TICKS_END));
        }
        if (context.getSessionData(Key.C_WHILE_WITHIN_BIOME) != null) {
            section.set("stay-within-biome", context.getSessionData(Key.C_WHILE_WITHIN_BIOME));
        }
        if (context.getSessionData(Key.C_WHILE_WITHIN_REGION) != null) {
            section.set("stay-within-region", context.getSessionData(Key.C_WHILE_WITHIN_REGION));
        }
        if (context.getSessionData(Key.C_WHILE_PLACEHOLDER_ID) != null) {
            section.set("check-placeholder-id", context.getSessionData(Key.C_WHILE_PLACEHOLDER_ID));
        }
        if (context.getSessionData(Key.C_WHILE_PLACEHOLDER_VAL) != null) {
            section.set("check-placeholder-value", context.getSessionData(Key.C_WHILE_PLACEHOLDER_VAL));
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorSaved"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = context.getForWhom() instanceof Player ? 
                    "Player " + ((Player)context.getForWhom()).getUniqueId() : "CONSOLE";
            plugin.getLogger().info(identifier + " saved condition " + context.getSessionData(Key.C_NAME));
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(context);
    }
}