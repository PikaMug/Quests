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
import me.pikamug.quests.convo.conditions.ConditionsEditorNumericPrompt;
import me.pikamug.quests.convo.conditions.ConditionsEditorStringPrompt;
import me.pikamug.quests.convo.conditions.main.ConditionMainPrompt;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.conditions.ConditionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConditionEntityPrompt extends ConditionsEditorNumericPrompt {
    
    private final BukkitQuestsPlugin plugin;
    
    public ConditionEntityPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("conditionEditorEntity");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
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
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(Key.C_WHILE_RIDING_ENTITY) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final List<String> whileRidingEntity = (List<String>) context.getSessionData(Key.C_WHILE_RIDING_ENTITY);
                if (whileRidingEntity != null) {
                    for (final String s: whileRidingEntity) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.BLUE)
                                .append(BukkitMiscUtil.getProperMobType(s));
                    }
                }
                return text.toString();
            }
        case 2:
            if (plugin.getDependencies().getCitizens() != null || plugin.getDependencies().getZnpcsPlus() != null || plugin.getDependencies().getZnpcsPlusApi() != null) {
                if (context.getSessionData(Key.C_WHILE_RIDING_NPC) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    final List<UUID> whileRidingNpc = (List<UUID>) context.getSessionData(Key.C_WHILE_RIDING_NPC);
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
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final ConditionsEditorPostOpenNumericPromptEvent event
                = new ConditionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.AQUA + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }
    
    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            return new ConditionEntitiesPrompt(context);
        case 2:
            return new ConditionNpcsPrompt(context);
        case 3:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new ConditionEntityPrompt(context);
        }
    }
    
    public class ConditionEntitiesPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionEntitiesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEntitiesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("conditionEditorEntitiesPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
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
            mobs.append("\n").append(ChatColor.YELLOW).append(getQueryText(context));
            return mobs.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> mobTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (BukkitMiscUtil.getProperMobType(s) != null) {
                        final EntityType type = BukkitMiscUtil.getProperMobType(s);
                        if (type != null && type.getEntityClass() != null
                                && Vehicle.class.isAssignableFrom(type.getEntityClass())) {
                            mobTypes.add(s);
                            context.setSessionData(Key.C_WHILE_RIDING_ENTITY, mobTypes);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                    .replace("<input>", s));
                            return new ConditionEntitiesPrompt(context);
                        }
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidMob")
                                .replace("<input>", s));
                        return new ConditionEntitiesPrompt(context);
                    }
                }
            }
            return new ConditionEntityPrompt(context);
        }
    }
    
    public class ConditionNpcsPrompt extends ConditionsEditorStringPrompt {
        
        public ConditionNpcsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("conditionEditorNpcsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("enterNpcUniqueIds");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ConditionsEditorPostOpenStringPromptEvent event
                    = new ConditionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.add(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
                return ChatColor.YELLOW + BukkitLang.get("questEditorClickNPCStart");
            } else {
                return ChatColor.YELLOW + getQueryText(context);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> npcs = context.getSessionData(Key.C_WHILE_RIDING_NPC) != null
                        ? (LinkedList<String>) context.getSessionData(Key.C_WHILE_RIDING_NPC) : new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (plugin.getDependencies().getNpcEntity(uuid) != null && npcs != null) {
                            npcs.add(uuid.toString());
                        } else if (plugin.getDependencies().getZnpcsPlusApi() != null && npcs!= null
                                && plugin.getDependencies().getZnpcsPlusApi().getNpcRegistry().getByUuid(uuid) != null) {
                            npcs.add(uuid.toString());
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorInvalidNPC")
                                    .replace("<input>", s));
                            return new ConditionNpcsPrompt(context);
                        }
                    } catch (final IllegalArgumentException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("stageEditorNotListOfUniqueIds")
                                .replace("<data>", input));
                        return new ConditionNpcsPrompt(context);
                    }
                }
                context.setSessionData(Key.C_WHILE_RIDING_NPC, npcs);
            }
            if (context.getForWhom() instanceof Player) {
                final Set<UUID> selectingNpcs = plugin.getQuestFactory().getSelectingNpcs();
                selectingNpcs.remove(((Player) context.getForWhom()).getUniqueId());
                plugin.getQuestFactory().setSelectingNpcs(selectingNpcs);
            }
            return new ConditionEntityPrompt(context);
        }
    }
}
