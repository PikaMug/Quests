/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions.tasks;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.conditions.ConditionsEditorIntegerPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.BukkitConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConditionEntityPrompt extends ConditionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ConditionEntityPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("conditionEditorEntity");
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
        switch (number) {
            case 1:
            case 2:
                return ChatColor.BLUE;
            case 3:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorRideEntity");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("conditionEditorRideNPC");
        case 3:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch(number) {
        case 1:
            if (SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileRidingEntity = (List<String>) SessionData.get(uuid, Key.C_WHILE_RIDING_ENTITY);
                if (whileRidingEntity != null) {
                    for (final String s: whileRidingEntity) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitMiscUtil.getProperMobType(s));
                    }
                }
                return text.toString();
            }
        case 2:
            if (plugin.getDependencies().hasAnyNpcDependencies()) {
                if (SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<UUID> whileRidingNpc = (List<UUID>) SessionData.get(uuid, Key.C_WHILE_RIDING_NPC);
                    if (whileRidingNpc != null) {
                        for (final UUID u : whileRidingNpc) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                    .append(plugin.getDependencies().getNpcName(u));
                        }
                    }
                    return text.toString();
                }
            } else {
                return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
            }
        case 3:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitConditionsEditorPostOpenNumericPromptEvent event
                = new BukkitConditionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
        switch(input.intValue()) {
        case 1:
            new ConditionEntitiesPrompt(uuid).start();
        case 2:
            new ConditionNpcsPrompt(uuid).start();
        case 3:
            try {
                new ConditionMainPrompt(uuid).start();
            } catch (final Exception e) {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return;
            }
        default:
            new ConditionEntityPrompt(uuid).start();
        }
    }
    
    public class ConditionEntitiesPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionEntitiesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorEntitiesTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("conditionEditorEntitiesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<EntityType> mobArr = new LinkedList<>(Arrays.asList(EntityType.values()));
            final List<EntityType> toRemove = new LinkedList<>();
            for (final EntityType type : mobArr) {
                if (type.getEntityClass() == null || !Vehicle.class.isAssignableFrom(type.getEntityClass())) {
                    toRemove.add(type);
                }
            }
            mobArr.removeAll(toRemove);
            mobArr.sort(Comparator.comparing(EntityType::name));
            for (int i = 0; i < mobArr.size(); i++) {
                mobs.append(ChatColor.AQUA).append(BukkitMiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()));
                if (i < (mobArr.size() - 1)) {
                    mobs.append(ChatColor.GRAY).append(", ");
                }
            }
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return mobs.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperMobType(s) != null) {
                        final EntityType type = BukkitMiscUtil.getProperMobType(s);
                        if (type != null && type.getEntityClass() != null
                                && Vehicle.class.isAssignableFrom(type.getEntityClass())) {
                            mobTypes.add(s);
                            SessionData.set(uuid, Key.C_WHILE_RIDING_ENTITY, mobTypes);
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                    .replace("<input>", s));
                            new ConditionEntitiesPrompt(uuid).start();
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                .replace("<input>", s));
                        new ConditionEntitiesPrompt(uuid).start();
                    }
                }
            }
            new ConditionEntityPrompt(uuid).start();
        }
    }
    
    public class ConditionNpcsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionNpcsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("conditionEditorNpcsTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("enterNpcUniqueIds");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitConditionsEditorPostOpenStringPromptEvent event
                    = new BukkitConditionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            if (BukkitMiscUtil.getEntity(uuid) instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(uuid);
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> npcs = SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) != null
                        ? (LinkedList<String>) SessionData.get(uuid, Key.C_WHILE_RIDING_NPC) : new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (npcs != null && plugin.getDependencies().isNpc(uuid)) {
                            npcs.add(uuid.toString());
                        } else {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            new ConditionNpcsPrompt(uuid).start();
                        }
                    } catch (final IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", input));
                        new ConditionNpcsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.C_WHILE_RIDING_NPC, npcs);
            }
            if (sender instanceof Player) {
                final ConcurrentSkipListSet<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) sender).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            new ConditionEntityPrompt(uuid).start();
        }
    }
}
