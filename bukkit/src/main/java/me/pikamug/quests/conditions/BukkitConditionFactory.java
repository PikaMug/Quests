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
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BukkitConditionFactory implements ConditionFactory {

    private final BukkitQuestsPlugin plugin;
    private List<String> editingConditionNames = new LinkedList<>();

    public BukkitConditionFactory(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public List<String> getNamesOfConditionsBeingEdited() {
        return editingConditionNames;
    }
    
    public void setNamesOfConditionsBeingEdited(final List<String> conditionNames) {
        this.editingConditionNames = conditionNames;
    }
    
    public void returnToMenu(final UUID uuid) {
        new ConditionMainPrompt(uuid).start();
    }
    
    public void loadData(final UUID uuid, final Condition condition) {
        BukkitCondition bukkitCondition = (BukkitCondition) condition;
        if (bukkitCondition.isFailQuest()) {
            SessionData.set(uuid, Key.C_FAIL_QUEST, true);
        } else {
            SessionData.set(uuid, Key.C_FAIL_QUEST, false);
        }
        if (bukkitCondition.getEntitiesWhileRiding() != null && !bukkitCondition.getEntitiesWhileRiding().isEmpty()) {
            final LinkedList<String> entities = new LinkedList<>(bukkitCondition.getEntitiesWhileRiding());
            SessionData.set(uuid, Key.C_WHILE_RIDING_ENTITY, entities);
        }
        if (bukkitCondition.getNpcsWhileRiding() != null && !bukkitCondition.getNpcsWhileRiding().isEmpty()) {
            final LinkedList<UUID> npcs = new LinkedList<>(bukkitCondition.getNpcsWhileRiding());
            SessionData.set(uuid, Key.C_WHILE_RIDING_NPC, npcs);
        }
        if (bukkitCondition.getPermissions() != null && !bukkitCondition.getPermissions().isEmpty()) {
            final LinkedList<String> permissions = new LinkedList<>(bukkitCondition.getPermissions());
            SessionData.set(uuid, Key.C_WHILE_PERMISSION, permissions);
        }
        if (bukkitCondition.getItemsWhileHoldingMainHand() != null && !bukkitCondition.getItemsWhileHoldingMainHand().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(bukkitCondition.getItemsWhileHoldingMainHand());
            SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, items);
        }
        if (bukkitCondition.getItemsWhileWearing() != null && !bukkitCondition.getItemsWhileWearing().isEmpty()) {
            final LinkedList<ItemStack> items = new LinkedList<>(bukkitCondition.getItemsWhileWearing());
            SessionData.set(uuid, Key.C_WHILE_WEARING, items);
        }
        if (bukkitCondition.getWorldsWhileStayingWithin() != null && !bukkitCondition.getWorldsWhileStayingWithin().isEmpty()) {
            final LinkedList<String> worlds = new LinkedList<>(bukkitCondition.getBiomesWhileStayingWithin());
            SessionData.set(uuid, Key.C_WHILE_WITHIN_WORLD, worlds);
        }
        if (bukkitCondition.getTickStartWhileStayingWithin() > -1) {
            final int tick = bukkitCondition.getTickStartWhileStayingWithin();
            SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, tick);
        }
        if (bukkitCondition.getTickEndWhileStayingWithin() > -1) {
            final int tick = bukkitCondition.getTickEndWhileStayingWithin();
            SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, tick);
        }
        if (bukkitCondition.getBiomesWhileStayingWithin() != null && !bukkitCondition.getBiomesWhileStayingWithin().isEmpty()) {
            final LinkedList<String> biomes = new LinkedList<>(bukkitCondition.getBiomesWhileStayingWithin());
            SessionData.set(uuid, Key.C_WHILE_WITHIN_BIOME, biomes);
        }
        if (bukkitCondition.getRegionsWhileStayingWithin() != null && !bukkitCondition.getRegionsWhileStayingWithin().isEmpty()) {
            final LinkedList<String> regions = new LinkedList<>(bukkitCondition.getRegionsWhileStayingWithin());
            SessionData.set(uuid, Key.C_WHILE_WITHIN_REGION, regions);
        }
        if (bukkitCondition.getPlaceholdersCheckIdentifier() != null
                && !bukkitCondition.getPlaceholdersCheckIdentifier().isEmpty()) {
            final LinkedList<String> identifiers = new LinkedList<>(bukkitCondition.getPlaceholdersCheckIdentifier());
            SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, identifiers);
        }
        if (bukkitCondition.getPlaceholdersCheckValue() != null && !bukkitCondition.getPlaceholdersCheckValue().isEmpty()) {
            final LinkedList<String> values = new LinkedList<>(bukkitCondition.getPlaceholdersCheckValue());
            SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, values);
        }
    }

    public void clearData(final UUID uuid) {
        SessionData.set(uuid, Key.C_OLD_CONDITION, null);
        SessionData.set(uuid, Key.C_NAME, null);
        SessionData.set(uuid, Key.C_FAIL_QUEST, null);
        SessionData.set(uuid, Key.C_WHILE_RIDING_ENTITY, null);
        SessionData.set(uuid, Key.C_WHILE_RIDING_NPC, null);
        SessionData.set(uuid, Key.C_WHILE_PERMISSION, null);
        SessionData.set(uuid, Key.C_WHILE_HOLDING_MAIN_HAND, null);
        SessionData.set(uuid, Key.C_WHILE_WEARING, null);
        SessionData.set(uuid, Key.C_WHILE_WITHIN_WORLD, null);
        SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_START, null);
        SessionData.set(uuid, Key.C_WHILE_WITHIN_TICKS_END, null);
        SessionData.set(uuid, Key.C_WHILE_WITHIN_BIOME, null);
        SessionData.set(uuid, Key.C_WHILE_WITHIN_REGION, null);
        SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_ID, null);
        SessionData.set(uuid, Key.C_WHILE_PLACEHOLDER_VAL, null);
    }

    public void deleteCondition(final UUID uuid) {
        final YamlConfiguration data = new YamlConfiguration();
        final File conditionsFile = new File(plugin.getDataFolder(), "storage" + File.separatorChar + "conditions.yml");
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        try {
            data.load(conditionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        final String condition = (String) SessionData.get(uuid, Key.ED_CONDITION_DELETE);
        final ConfigurationSection sec = data.getConfigurationSection("conditions");
        if (sec != null && condition != null) {
            sec.set(condition, null);
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorDeleted"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = sender instanceof Player ? "Player " + uuid : "CONSOLE";
            plugin.getLogger().info(identifier + " deleted condition " + condition);
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(uuid);
    }

    public void saveCondition(final UUID uuid) {
        final YamlConfiguration data = new YamlConfiguration();
        final File conditionsFile = new File(plugin.getDataFolder(), "storage" + File.separatorChar + "conditions.yml");
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        try {
            data.load(conditionsFile);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questErrorReadingFile")
                    .replace("<file>", conditionsFile.getName()));
            return;
        }
        if (SessionData.get(uuid, Key.C_OLD_CONDITION) != null
                && !((String) Objects.requireNonNull(SessionData.get(uuid, Key.C_OLD_CONDITION))).isEmpty()) {
            data.set("conditions." + SessionData.get(uuid, Key.C_OLD_CONDITION), null);
            final Collection<Condition> temp = plugin.getLoadedConditions();
            temp.remove(plugin.getCondition((String) SessionData.get(uuid, Key.C_OLD_CONDITION)));
            plugin.setLoadedConditions(temp);
        }
        final ConfigurationSection section = data.createSection("conditions." + SessionData.get(uuid, Key.C_NAME));
        editingConditionNames.remove((String) SessionData.get(uuid, Key.C_NAME));
        if (SessionData.get(uuid, Key.C_FAIL_QUEST) != null) {
            final Boolean b = (Boolean) SessionData.get(uuid, Key.C_FAIL_QUEST);
            if (b != null) {
                section.set("fail-quest", b);
            }
        }
        if (SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY) != null) {
            section.set("ride-entity", SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY));
        }
        if (SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) != null) {
            section.set("ride-npc-uuid", SessionData.get(uuid, Key.C_WHILE_RIDING_NPC));
        }
        if (SessionData.get(uuid, Key.C_WHILE_PERMISSION) != null) {
            section.set("permission", SessionData.get(uuid, Key.C_WHILE_PERMISSION));
        }
        if (SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND) != null) {
            section.set("hold-main-hand", SessionData.get(uuid, Key.C_WHILE_HOLDING_MAIN_HAND));
        }
        if (SessionData.get(uuid, Key.C_WHILE_WEARING) != null) {
            section.set("wear", SessionData.get(uuid, Key.C_WHILE_WEARING));
        }
        if (SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD) != null) {
            section.set("stay-within-world", SessionData.get(uuid, Key.C_WHILE_WITHIN_WORLD));
        }
        if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START) != null) {
            section.set("stay-within-ticks.start", SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_START));
        }
        if (SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END) != null) {
            section.set("stay-within-ticks.end", SessionData.get(uuid, Key.C_WHILE_WITHIN_TICKS_END));
        }
        if (SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME) != null) {
            section.set("stay-within-biome", SessionData.get(uuid, Key.C_WHILE_WITHIN_BIOME));
        }
        if (SessionData.get(uuid, Key.C_WHILE_WITHIN_REGION) != null) {
            section.set("stay-within-region", SessionData.get(uuid, Key.C_WHILE_WITHIN_REGION));
        }
        if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID) != null) {
            section.set("check-placeholder-id", SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_ID));
        }
        if (SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL) != null) {
            section.set("check-placeholder-value", SessionData.get(uuid, Key.C_WHILE_PLACEHOLDER_VAL));
        }
        try {
            data.save(conditionsFile);
        } catch (final IOException e) {
            sender.sendMessage(ChatColor.RED + BukkitLang.get("questSaveError"));
            return;
        }
        final ReloadCallback<Boolean> callback = response -> {
            if (!response) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
            }
        };
        plugin.reload(callback);
        sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("conditionEditorSaved"));
        if (plugin.getConfigSettings().getConsoleLogging() > 0) {
            final String identifier = sender instanceof Player ? "Player " + uuid : "CONSOLE";
            plugin.getLogger().info(identifier + " saved condition " + SessionData.get(uuid, Key.C_NAME));
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            for (final Quest quest : q.getCurrentQuests().keySet()) {
                q.checkQuest(quest);
            }
        }
        clearData(uuid);
    }
}