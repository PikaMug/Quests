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

package me.blackvein.quests.convo.actions.main;

import me.blackvein.quests.Quests;
import me.blackvein.quests.actions.IAction;
import me.blackvein.quests.convo.QuestsNumericPrompt;
import me.blackvein.quests.convo.actions.ActionsEditorNumericPrompt;
import me.blackvein.quests.convo.actions.ActionsEditorStringPrompt;
import me.blackvein.quests.convo.actions.tasks.EffectPrompt;
import me.blackvein.quests.convo.actions.tasks.PlayerPrompt;
import me.blackvein.quests.convo.actions.tasks.TimerPrompt;
import me.blackvein.quests.convo.actions.tasks.WeatherPrompt;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.entity.BukkitQuestMob;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.quests.IStage;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActionMainPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public ActionMainPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }

    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("event") + ": " + context.getSessionData(CK.E_NAME);
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 8:
            return ChatColor.BLUE;
        case 7:
            if (plugin.getDependencies().getDenizenApi() == null) {
                return ChatColor.GRAY;
            } else {
                return ChatColor.BLUE;
            }
        case 9:
            return ChatColor.GREEN;
        case 10:
            return ChatColor.RED;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("eventEditorSetName");
        case 2:
            return ChatColor.GOLD + Lang.get("eventEditorPlayer");
        case 3:
            return ChatColor.GOLD + Lang.get("eventEditorTimer");
        case 4:
            return ChatColor.GOLD + Lang.get("eventEditorEffect");
        case 5:
            return ChatColor.GOLD + Lang.get("eventEditorWeather");
        case 6:
            return ChatColor.YELLOW + Lang.get("eventEditorSetMobSpawns");
        case 7:
            if (plugin.getDependencies().getDenizenApi() == null) {
                return ChatColor.GRAY + Lang.get("stageEditorDenizenScript");
            } else {
                return ChatColor.YELLOW + Lang.get("stageEditorDenizenScript");
            }
        case 8:
            return ChatColor.YELLOW + Lang.get("eventEditorFailQuest");
        case 9:
            return ChatColor.GREEN + Lang.get("save");
        case 10:
            return ChatColor.RED + Lang.get("exit");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 9:
        case 10:
            return "";
        case 6:
            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                final StringBuilder text = new StringBuilder();
                if (types != null) {
                    for (final String s : types) {
                        final BukkitQuestMob qm = BukkitQuestMob.fromString(s);
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                .append(qm.getType().name())
                                .append((qm.getName() != null) ? " (" + qm.getName() + ")" : "").append(ChatColor.GRAY)
                                .append(" x ").append(ChatColor.DARK_AQUA).append(qm.getSpawnAmounts())
                                .append("\n").append(ChatColor.GRAY).append("        \u2515 ").append(ChatColor.GREEN)
                                .append(ConfigUtil.getLocationInfo(qm.getSpawnLocation()));
                    }
                }
                return text.toString();
            }
        case 7:
            if (plugin.getDependencies().getDenizenApi() == null) {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            } else {
                if (context.getSessionData(CK.E_DENIZEN) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_DENIZEN) 
                            + ChatColor.GRAY + ")";
                }
            }
        case 8:
            if (context.getSessionData(CK.E_FAIL_QUEST) == null) {
                return ChatColor.GRAY + "(" + ChatColor.RED + Lang.get("false") + ChatColor.GRAY + ")";
            } else {
                final Boolean failOpt = (Boolean) context.getSessionData(CK.E_FAIL_QUEST);
                return ChatColor.GRAY + "(" + (Boolean.TRUE.equals(failOpt) ? ChatColor.GREEN + Lang.get("true")
                        : ChatColor.RED + Lang.get("false")) + ChatColor.GRAY + ")";
            }
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final ActionsEditorPostOpenNumericPromptEvent event
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context).replaceFirst(": ", ": "
                + ChatColor.AQUA) + ChatColor.GOLD + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new ActionNamePrompt(context);
        case 2:
            return new PlayerPrompt(context);
        case 3:
            return new TimerPrompt(context);
        case 4:
            return new EffectPrompt(context);
        case 5:
            return new WeatherPrompt(context);
        case 6:
            return new ActionMobListPrompt(context);
        case 7:
            if (!plugin.hasLimitedAccess(context.getForWhom())) {
                return new ActionDenizenPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("modeDeny")
                        .replace("<mode>", Lang.get("trialMode")));
                return new ActionMainPrompt(context);
            }
        case 8:
            final Boolean b = (Boolean) context.getSessionData(CK.E_FAIL_QUEST);
            if (Boolean.TRUE.equals(b)) {
                context.setSessionData(CK.E_FAIL_QUEST, false);
            } else {
                context.setSessionData(CK.E_FAIL_QUEST, true);
            }
            return new ActionMainPrompt(context);
        case 9:
            if (context.getSessionData(CK.E_OLD_EVENT) != null) {
                return new ActionSavePrompt(context, (String) context.getSessionData(CK.E_OLD_EVENT));
            } else {
                return new ActionSavePrompt(context, null);
            }
        case 10:
            return new ActionExitPrompt(context);
        default:
            return new ActionMainPrompt(context);
        }
    }
    
    public class ActionNamePrompt extends ActionsEditorStringPrompt {

        public ActionNamePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorEnterEventName");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event 
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            if (context.getPlugin() != null) {
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                for (final IAction a : plugin.getLoadedActions()) {
                    if (a.getName().equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorExists"));
                        return new ActionNamePrompt(context);
                    }
                }
                final List<String> actionNames = plugin.getActionFactory().getNamesOfActionsBeingEdited();
                if (actionNames.contains(input)) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorBeingEdited"));
                    return new ActionNamePrompt(context);
                }
                if (input.contains(",")) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("questEditorInvalidQuestName"));
                    return new ActionNamePrompt(context);
                }
                actionNames.remove((String) context.getSessionData(CK.E_NAME));
                context.setSessionData(CK.E_NAME, input);
                actionNames.add(input);
                plugin.getActionFactory().setNamesOfActionsBeingEdited(actionNames);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class ActionMobListPrompt extends ActionsEditorNumericPrompt {
        
        public ActionMobListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorMobSpawnsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("eventEditorAddMobTypes");
            case 2:
                return ChatColor.RED + Lang.get("clear");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.E_MOB_TYPES) != null) {
                    @SuppressWarnings("unchecked")
                    final
                    LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                    final StringBuilder text = new StringBuilder();
                    if (types != null) {
                        for (final String type : types) {
                            final BukkitQuestMob qm = BukkitQuestMob.fromString(type);
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA)
                                    .append(qm.getType().name())
                                    .append((qm.getName() != null) ? " (" + qm.getName() + ")" : "")
                                    .append(" x ").append(qm.getSpawnAmounts()).append("\n").append(ChatColor.GRAY)
                                    .append("        \u2515 ").append(ChatColor.GREEN)
                                    .append(ConfigUtil.getLocationInfo(qm.getSpawnLocation()));
                        }
                    }
                    return text.toString();
                }
            case 2:
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ActionMobPrompt(context, null);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
                context.setSessionData(CK.E_MOB_TYPES, null);
                return new ActionMobListPrompt(context);
            case 3:
                return new ActionMainPrompt(context);
            default:
                return new ActionMobListPrompt(context);
            }
        }
    }

    public class ActionMobPrompt extends ActionsEditorNumericPrompt {

        private BukkitQuestMob questMob;

        public ActionMobPrompt(final ConversationContext context, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
        }
        
        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorAddMobTypesTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return ChatColor.BLUE;
            case 6:
                return ChatColor.RED;
            case 7:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("eventEditorSetMobName");
            case 2:
                return ChatColor.YELLOW + Lang.get("eventEditorSetMobType");
            case 3:
                return ChatColor.YELLOW + Lang.get("eventEditorSetMobSpawnAmount");
            case 4:
                return ChatColor.YELLOW + Lang.get("eventEditorAddSpawnLocation");
            case 5:
                return ChatColor.YELLOW + Lang.get("eventEditorSetEquipment");
            case 6:
                return ChatColor.RED + Lang.get("cancel");
            case 7:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GRAY + "(" + (questMob.getName() == null ? Lang.get("noneSet") : ChatColor.AQUA
                        + questMob.getName()) + ChatColor.GRAY + ")";
            case 2:
                return ChatColor.GRAY + "(" + (questMob.getType() == null ? Lang.get("noneSet") : ChatColor.AQUA
                        + questMob.getType().name()) + ChatColor.GRAY + ")";
            case 3:
                return ChatColor.GRAY + "(" + (questMob.getSpawnAmounts() == null ? ChatColor.GRAY
                        + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getSpawnAmounts()) + ChatColor.GRAY
                        + ")";
            case 4:
                return ChatColor.GRAY + "(" + (questMob.getSpawnLocation() == null ? ChatColor.GRAY
                        + Lang.get("noneSet") : ChatColor.AQUA + ConfigUtil.getLocationInfo(questMob
                        .getSpawnLocation())) + ChatColor.GRAY + ")";
            case 5:
                return ChatColor.GRAY + "(" + (questMob.getInventory()[0] == null ? ChatColor.GRAY
                        + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob.getInventory()[0]))
                        + ChatColor.GRAY + ")";
            case 6:
            case 7:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            if (questMob == null) {
                questMob = new BukkitQuestMob();
            }
            
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i))
                        .append(ChatColor.GRAY).append(" ").append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ActionMobNamePrompt(context, questMob);
            case 2:
                return new ActionMobTypePrompt(context, questMob);
            case 3:
                return new ActionMobAmountPrompt(context, questMob);
            case 4:
                if (context.getForWhom() instanceof Player) {
                    final Map<UUID, Block> selectedMobLocations = plugin.getActionFactory().getSelectedMobLocations();
                    selectedMobLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                    plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                    return new ActionMobLocationPrompt(context, questMob);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                    return new ActionMainPrompt(context);
                }
            case 5:
                return new ActionMobEquipmentPrompt(context, questMob);
            case 6:
                return new ActionMobListPrompt(context);
            case 7:
                if (questMob.getType() == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobTypesFirst"));
                    return new ActionMobPrompt(context, questMob);
                } else if (questMob.getSpawnLocation() == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobLocationFirst"));
                    return new ActionMobPrompt(context, questMob);
                } else if (questMob.getSpawnAmounts() == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetMobAmountsFirst"));
                    return new ActionMobPrompt(context, questMob);
                }
                final LinkedList<String> list = context.getSessionData(CK.E_MOB_TYPES) == null ? new LinkedList<>()
                        : (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                if (list != null) {
                    list.add(questMob.serialize());
                    context.setSessionData(CK.E_MOB_TYPES, list);
                }
                return new ActionMobListPrompt(context);
            default:
                return new ActionMobPrompt(context, questMob);
            }
        }
    }

    public class ActionMobEquipmentPrompt extends ActionsEditorNumericPrompt {

        private BukkitQuestMob questMob;
        private Integer itemIndex = -1;

        public ActionMobEquipmentPrompt(final ConversationContext context, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
        }

        private final int size = 12;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorAddEquipmentTitle");
        }

        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    return ChatColor.BLUE;
                case 11:
                    return ChatColor.RED;
                case 12:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }

        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobItemInHand");
                case 2:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobItemInHandDrop");
                case 3:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobBoots");
                case 4:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobBootsDrop");
                case 5:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobLeggings");
                case 6:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobLeggingsDrop");
                case 7:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobChestPlate");
                case 8:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobChestPlateDrop");
                case 9:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobHelmet");
                case 10:
                    return ChatColor.YELLOW + Lang.get("eventEditorSetMobHelmetDrop");
                case 11:
                    return ChatColor.RED + Lang.get("cancel");
                case 12:
                    return ChatColor.GREEN + Lang.get("done");
                default:
                    return null;
            }
        }

        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[0] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob
                            .getInventory()[0])) + ChatColor.GRAY + ")";
                case 2:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[0] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[0])
                            + ChatColor.GRAY + ")";
                case 3:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[1] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob
                            .getInventory()[1])) + ChatColor.GRAY + ")";
                case 4:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[1] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[1])
                            + ChatColor.GRAY + ")";
                case 5:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[2] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob
                            .getInventory()[2])) + ChatColor.GRAY + ")";
                case 6:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[2] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[2])
                            + ChatColor.GRAY + ")";
                case 7:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[3] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob
                            .getInventory()[3])) + ChatColor.GRAY + ")";
                case 8:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[3] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[3])
                            + ChatColor.GRAY + ")";
                case 9:
                    return ChatColor.GRAY + "(" + (questMob.getInventory()[4] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + ItemUtil.getDisplayString(questMob
                            .getInventory()[4])) + ChatColor.GRAY + ")";
                case 10:
                    return ChatColor.GRAY + "(" + (questMob.getDropChances()[4] == null ? ChatColor.GRAY
                            + Lang.get("noneSet") : ChatColor.AQUA + "" + questMob.getDropChances()[4])
                            + ChatColor.GRAY + ")";
                case 11:
                case 12:
                    return "";
                default:
                    return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            if (questMob == null) {
                questMob = new BukkitQuestMob();
            }
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (itemIndex >= 0) {
                    questMob.getInventory()[itemIndex] = ((ItemStack) context.getSessionData("tempStack"));
                    try {
                        if (questMob.getDropChances()[itemIndex] == null) {
                            final Float[] chances = questMob.getDropChances();
                            chances[itemIndex] = 1.0f;
                            questMob.setDropChances(chances);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    itemIndex = -1;
                }
                ItemStackPrompt.clearSessionData(context);
            }

            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i))
                        .append(ChatColor.GRAY).append(" ").append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
                case 1:
                    itemIndex = 0;
                    return new ItemStackPrompt(context, ActionMobEquipmentPrompt.this);
                case 2:
                    return new ActionMobDropPrompt(context, 0, questMob);
                case 3:
                    itemIndex = 1;
                    return new ItemStackPrompt(context, ActionMobEquipmentPrompt.this);
                case 4:
                    return new ActionMobDropPrompt(context, 1, questMob);
                case 5:
                    itemIndex = 2;
                    return new ItemStackPrompt(context, ActionMobEquipmentPrompt.this);
                case 6:
                    return new ActionMobDropPrompt(context, 2, questMob);
                case 7:
                    itemIndex = 3;
                    return new ItemStackPrompt(context, ActionMobEquipmentPrompt.this);
                case 8:
                    return new ActionMobDropPrompt(context, 3, questMob);
                case 9:
                    itemIndex = 4;
                    return new ItemStackPrompt(context, ActionMobEquipmentPrompt.this);
                case 10:
                    return new ActionMobDropPrompt(context, 4, questMob);
                case 11:
                    return new ActionMobListPrompt(context);
                default:
                    return new ActionMobPrompt(context, questMob);
            }
        }
    }

    public class ActionMobNamePrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobNamePrompt(final ConversationContext context, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetMobNamePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            if (context.getPlugin() != null) {
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionMobPrompt(context, questMob);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                questMob.setName(null);
                return new ActionMobPrompt(context, questMob);
            } else {
                input = ChatColor.translateAlternateColorCodes('&', input);
                questMob.setName(input);
                return new ActionMobPrompt(context, questMob);
            }
        }
    }

    public class ActionMobTypePrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobTypePrompt(final ConversationContext context, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetMobTypesPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            if (context.getPlugin() != null) {
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }

            final StringBuilder mobs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {
                final EntityType type = mobArr[i];
                if (!type.isAlive()) {
                    continue;
                }
                if (i < (mobArr.length - 1)) {
                    mobs.append(MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name())).append(", ");
                } else {
                    mobs.append(MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name())).append("\n");
                }
            }
            return mobs.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                if (MiscUtil.getProperMobType(input) != null) {
                    questMob.setType(MiscUtil.getProperMobType(input));
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                            + Lang.get("stageEditorInvalidMob"));
                    return new ActionMobTypePrompt(context, questMob);
                }
            }
            return new ActionMobPrompt(context, questMob);
        }
    }

    public class ActionMobAmountPrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobAmountPrompt(final ConversationContext context, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetMobAmountsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            if (context.getPlugin() != null) {
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 1) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                .replace("<number>", "1"));
                        return new ActionMobAmountPrompt(context, questMob);
                    }
                    questMob.setSpawnAmounts(i);
                    return new ActionMobPrompt(context, questMob);
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new ActionMobAmountPrompt(context, questMob);
                }
            }
            return new ActionMobPrompt(context, questMob);
        }
    }

    public class ActionMobLocationPrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;

        public ActionMobLocationPrompt(final ConversationContext context, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetMobLocationPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final ActionsEditorPostOpenStringPromptEvent event
                        = new ActionsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                final Map<UUID, Block> selectedMobLocations = plugin.getActionFactory().getSelectedMobLocations();
                final Block block = selectedMobLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    questMob.setSpawnLocation(loc);
                    selectedMobLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new ActionMobLocationPrompt(context, questMob);
                }
                return new ActionMobPrompt(context, questMob);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedMobLocations = plugin.getActionFactory().getSelectedMobLocations();
                selectedMobLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedMobLocations(selectedMobLocations);
                return new ActionMobPrompt(context, questMob);
            } else {
                return new ActionMobLocationPrompt(context, questMob);
            }
        }
    }

    public class ActionMobDropPrompt extends ActionsEditorStringPrompt {

        private final BukkitQuestMob questMob;
        private final Integer invIndex;

        public ActionMobDropPrompt(final ConversationContext context, final int invIndex, final BukkitQuestMob questMob) {
            super(context);
            this.questMob = questMob;
            this.invIndex = invIndex;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetDropChance").replace("<least>", "0.0").replaceFirst("<greatest>", "1.0");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final ActionsEditorPostOpenStringPromptEvent event
                        = new ActionsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new ActionMobPrompt(context, questMob);
            }
            final float chance;
            try {
                chance = Float.parseFloat(input);
            } catch (final NumberFormatException e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                        .replace("<least>", "0.0").replace("<greatest>", "1.0"));
                return new ActionMobDropPrompt(context, invIndex, questMob);
            }
            if (chance > 1 || chance < 0) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidRange")
                        .replace("<least>", "0.0").replace("<greatest>", "1.0"));
                return new ActionMobDropPrompt(context, invIndex, questMob);
            }
            final Float[] temp = questMob.getDropChances();
            temp[invIndex] = chance;
            questMob.setDropChances(temp);
            return new ActionMobEquipmentPrompt(context, questMob);
        }
    }
    
    public class ActionDenizenPrompt extends ActionsEditorStringPrompt {
        
        public ActionDenizenPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("stageEditorDenizenScript");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("stageEditorScriptPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final ActionsEditorPostOpenStringPromptEvent event
                        = new ActionsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_AQUA + "- " + getTitle(context) + " -");
            if (plugin.getDependencies().getDenizenApi() != null
                    && plugin.getDependencies().getDenizenApi().getScriptNames() != null) {
                for (final String s : plugin.getDependencies().getDenizenApi().getScriptNames()) {
                    text.append("\n").append(ChatColor.AQUA).append("- ").append(s);
                }
            }
            return text + "\n" + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))
                    && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                if (plugin.getDependencies().getDenizenApi().containsScript(input)) {
                    context.setSessionData(CK.E_DENIZEN, input.toUpperCase());
                    return new ActionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorInvalidScript"));
                    return new ActionDenizenPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_DENIZEN, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorDenizenCleared"));
                return new ActionMainPrompt(context);
            } else {
                return new ActionMainPrompt(context);
            }
        }
    }

    public class ActionSavePrompt extends ActionsEditorStringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<>();

        public ActionSavePrompt(final ConversationContext context, final String modifiedName) {
            super(context);
            if (modifiedName != null) {
                modName = modifiedName;
                for (final IQuest q : plugin.getLoadedQuests()) {
                    for (final IStage s : q.getStages()) {
                        if (s.getFinishAction() != null && s.getFinishAction().getName() != null) {
                            if (s.getFinishAction().getName().equalsIgnoreCase(modifiedName)) {
                                modified.add(q.getName());
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("questEditorSave");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final ActionsEditorPostOpenStringPromptEvent event
                        = new ActionsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context));
            if (!modified.isEmpty()) {
                text.append("\n").append(ChatColor.RED).append(Lang.get("eventEditorModifiedNote"));
                for (final String s : modified) {
                    text.append("\n").append(ChatColor.GRAY).append("    - ").append(ChatColor.DARK_RED).append(s);
                }
                text.append("\n").append(ChatColor.RED).append(Lang.get("eventEditorForcedToQuit"));
            }
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                if (plugin.hasLimitedAccess(context.getForWhom()) && !plugin.getSettings().canTrialSave()) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("modeDeny")
                            .replace("<mode>", Lang.get("trialMode")));
                    return new ActionMainPrompt(context);
                }
                plugin.getActionFactory().saveAction(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ActionMainPrompt(context);
            } else {
                return new ActionSavePrompt(context, modName);
            }
        }
    }
    
    public class ActionExitPrompt extends ActionsEditorStringPrompt {
        
        public ActionExitPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 2;
        
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
            }
        }
        
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
            }
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("confirmDelete");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            if (context.getPlugin() != null) {
                final ActionsEditorPostOpenStringPromptEvent event
                        = new ActionsEditorPostOpenStringPromptEvent(context, this);
                context.getPlugin().getServer().getPluginManager().callEvent(event);
            }
            
            final StringBuilder text = new StringBuilder(ChatColor.YELLOW + getQueryText(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
            }
            return QuestsNumericPrompt.sendClickableSelection(text.toString(), context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.getForWhom().sendRawMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + Lang.get("exited"));
                plugin.getActionFactory().clearData(context);
                return Prompt.END_OF_CONVERSATION;
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new ActionMainPrompt(context);
            } else {
                return new ActionExitPrompt(context);
            }
        }
    }
}
