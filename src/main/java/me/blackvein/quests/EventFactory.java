package me.blackvein.quests;

import me.blackvein.quests.util.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.blackvein.quests.prompts.ItemStackPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.QuestMob;
import net.citizensnpcs.api.CitizensAPI;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EventFactory implements ConversationAbandonedListener, ColorUtil {

    Quests quests;
    Map<UUID, Quest> editSessions = new HashMap<UUID, Quest>();
    Map<UUID, Block> selectedExplosionLocations = new HashMap<UUID, Block>();
    Map<UUID, Block> selectedEffectLocations = new HashMap<UUID, Block>();
    Map<UUID, Block> selectedMobLocations = new HashMap<UUID, Block>();
    Map<UUID, Block> selectedLightningLocations = new HashMap<UUID, Block>();
    Map<UUID, Block> selectedTeleportLocations = new HashMap<UUID, Block>();
    List<String> names = new LinkedList<String>();
    ConversationFactory convoCreator;
    File eventsFile;

    public EventFactory(Quests plugin) {

        quests = plugin;

        //Ensure to initialize convoCreator last, to ensure that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin)
                .withModality(false)
                .withLocalEcho(false)
                .withPrefix(new QuestCreatorPrefix())
                .withFirstPrompt(new MenuPrompt())
                .withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);

    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {

        Player player = (Player) abandonedEvent.getContext().getForWhom();
        selectedExplosionLocations.remove(player.getUniqueId());
        selectedEffectLocations.remove(player.getUniqueId());
        selectedMobLocations.remove(player.getUniqueId());
        selectedLightningLocations.remove(player.getUniqueId());
        selectedTeleportLocations.remove(player.getUniqueId());

    }

    private class QuestCreatorPrefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext context) {

            return "";

        }
    }

    private class MenuPrompt extends FixedSetPrompt {

        public MenuPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text
                    = GOLD + Lang.get("eventEditorTitle") + "\n"
                    + BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorCreate") + "\n"
                    + BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorEdit") + "\n"
                    + BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorDelete") + "\n"
                    + GREEN + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("exit");

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            final Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("1")) {

                if (player.hasPermission("quests.editor.events.create")) {
                    context.setSessionData(CK.E_OLD_EVENT, "");
                    return new EventNamePrompt();
                } else {
                    player.sendMessage(RED + Lang.get("eventEditorCreatePermisssions"));
                    return new MenuPrompt();
                }

            } else if (input.equalsIgnoreCase("2")) {

                if (player.hasPermission("quests.editor.events.edit")) {

                    if (quests.events.isEmpty()) {
                        ((Player) context.getForWhom()).sendMessage(YELLOW + Lang.get("eventEditorNoneToEdit"));
                        return new MenuPrompt();
                    } else {
                        return new SelectEditPrompt();
                    }

                } else {

                    player.sendMessage(RED + Lang.get("eventEditorEditPermisssions"));
                    return new MenuPrompt();

                }
            } else if (input.equalsIgnoreCase("3")) {

                if (player.hasPermission("quests.editor.events.delete")) {

                    if (quests.events.isEmpty()) {
                        ((Player) context.getForWhom()).sendMessage(YELLOW + Lang.get("eventEditorNoneToDelete"));
                        return new MenuPrompt();
                    } else {
                        return new SelectDeletePrompt();
                    }

                } else {

                    player.sendMessage(RED + Lang.get("eventEditorDeletePermisssions"));
                    return new MenuPrompt();

                }

            } else if (input.equalsIgnoreCase("4")) {
                ((Player) context.getForWhom()).sendMessage(YELLOW + Lang.get("exited"));
                return Prompt.END_OF_CONVERSATION;
            }

            return null;

        }
    }

    public Prompt returnToMenu() {

        return new CreateMenuPrompt();

    }

    public static void clearData(ConversationContext context) {

        context.setSessionData(CK.E_OLD_EVENT, null);
        context.setSessionData(CK.E_NAME, null);
        context.setSessionData(CK.E_MESSAGE, null);
        context.setSessionData(CK.E_CLEAR_INVENTORY, null);
        context.setSessionData(CK.E_FAIL_QUEST, null);
        context.setSessionData(CK.E_ITEMS, null);
        context.setSessionData(CK.E_ITEMS_AMOUNTS, null);
        context.setSessionData(CK.E_EXPLOSIONS, null);
        context.setSessionData(CK.E_EFFECTS, null);
        context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
        context.setSessionData(CK.E_WORLD_STORM, null);
        context.setSessionData(CK.E_WORLD_STORM_DURATION, null);
        context.setSessionData(CK.E_WORLD_THUNDER, null);
        context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
        context.setSessionData(CK.E_MOB_TYPES, null);
        context.setSessionData(CK.E_LIGHTNING, null);
        context.setSessionData(CK.E_POTION_TYPES, null);
        context.setSessionData(CK.E_POTION_DURATIONS, null);
        context.setSessionData(CK.E_POTION_STRENGHT, null);
        context.setSessionData(CK.E_HUNGER, null);
        context.setSessionData(CK.E_SATURATION, null);
        context.setSessionData(CK.E_HEALTH, null);
        context.setSessionData(CK.E_TELEPORT, null);
        context.setSessionData(CK.E_COMMANDS, null);

    }

    public static void loadData(Event event, ConversationContext context) {

        if (event.message != null) {
            context.setSessionData(CK.E_MESSAGE, event.message);
        }

        if (event.clearInv == true) {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
        }

        if (event.failQuest == true) {
            context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
        } else {
            context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
        }

        if (event.items != null && event.items.isEmpty() == false) {

            LinkedList<ItemStack> items = new LinkedList<ItemStack>();
            items.addAll(event.items);

            context.setSessionData(CK.E_ITEMS, items);

        }

        if (event.explosions != null && event.explosions.isEmpty() == false) {

            LinkedList<String> locs = new LinkedList<String>();

            for (Location loc : event.explosions) {
                locs.add(Quests.getLocationInfo(loc));
            }

            context.setSessionData(CK.E_EXPLOSIONS, locs);

        }

        if (event.effects != null && event.effects.isEmpty() == false) {

            LinkedList<String> locs = new LinkedList<String>();
            LinkedList<String> effs = new LinkedList<String>();

            for (Entry<Location, Effect> e : event.effects.entrySet()) {

                locs.add(Quests.getLocationInfo((Location) e.getKey()));
                effs.add(((Effect) e.getValue()).toString());

            }

            context.setSessionData(CK.E_EFFECTS, effs);
            context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);

        }

        if (event.stormWorld != null) {

            context.setSessionData(CK.E_WORLD_STORM, event.stormWorld.getName());
            context.setSessionData(CK.E_WORLD_STORM_DURATION, (long) event.stormDuration);

        }

        if (event.thunderWorld != null) {

            context.setSessionData(CK.E_WORLD_THUNDER, event.thunderWorld.getName());
            context.setSessionData(CK.E_WORLD_THUNDER_DURATION, (long) event.thunderDuration);

        }

        if (event.mobSpawns != null && event.mobSpawns.isEmpty() == false) {

            LinkedList<String> questMobs = new LinkedList<String>();

            for (QuestMob questMob : event.mobSpawns) {
                questMobs.add(questMob.serialize());
            }

            context.setSessionData(CK.E_MOB_TYPES, questMobs);
        }

        if (event.lightningStrikes != null && event.lightningStrikes.isEmpty() == false) {

            LinkedList<String> locs = new LinkedList<String>();
            for (Location loc : event.lightningStrikes) {
                locs.add(Quests.getLocationInfo(loc));
            }
            context.setSessionData(CK.E_LIGHTNING, locs);

        }

        if (event.potionEffects != null && event.potionEffects.isEmpty() == false) {

            LinkedList<String> types = new LinkedList<String>();
            LinkedList<Long> durations = new LinkedList<Long>();
            LinkedList<Integer> mags = new LinkedList<Integer>();

            for (PotionEffect pe : event.potionEffects) {

                types.add(pe.getType().getName());
                durations.add((long) pe.getDuration());
                mags.add(pe.getAmplifier());

            }

            context.setSessionData(CK.E_POTION_TYPES, types);
            context.setSessionData(CK.E_POTION_DURATIONS, durations);
            context.setSessionData(CK.E_POTION_STRENGHT, mags);

        }

        if (event.hunger > -1) {

            context.setSessionData(CK.E_HUNGER, (Integer) event.hunger);

        }

        if (event.saturation > -1) {

            context.setSessionData(CK.E_SATURATION, (Integer) event.saturation);

        }

        if (event.health > -1) {

            context.setSessionData(CK.E_HEALTH, (Float) event.health);

        }

        if (event.teleport != null) {

            context.setSessionData(CK.E_TELEPORT, Quests.getLocationInfo(event.teleport));

        }

        if (event.commands != null) {

            context.setSessionData(CK.E_COMMANDS, event.commands);

        }

    }

    private class SelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorEdit") + " -\n";

            for (Event evt : quests.events) {
                text += AQUA + evt.name + YELLOW + ", ";
            }

            text = text.substring(0, text.length() - 2) + "\n";
            text += YELLOW + Lang.get("eventEditorEnterEventName");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                for (Event evt : quests.events) {

                    if (evt.name.equalsIgnoreCase(input)) {
                        context.setSessionData(CK.E_OLD_EVENT, evt.name);
                        context.setSessionData(CK.E_NAME, evt.name);
                        loadData(evt, context);
                        return new CreateMenuPrompt();
                    }

                }

                ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorNotFound"));
                return new SelectEditPrompt();

            } else {
                return new MenuPrompt();
            }

        }

    }

    private class SelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorDelete") + " -\n";

            for (Event evt : quests.events) {
                text += AQUA + evt.name + YELLOW + ",";
            }

            text = text.substring(0, text.length() - 1) + "\n";
            text += YELLOW + Lang.get("eventEditorEnterEventName");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> used = new LinkedList<String>();

                for (Event evt : quests.events) {

                    if (evt.name.equalsIgnoreCase(input)) {

                        for (Quest quest : quests.getQuests()) {

                            for (Stage stage : quest.orderedStages) {

                                if (stage.finishEvent != null && stage.finishEvent.name.equalsIgnoreCase(evt.name)) {
                                    used.add(quest.name);
                                    break;
                                }

                            }

                        }

                        if (used.isEmpty()) {
                            context.setSessionData(CK.ED_EVENT_DELETE, evt.name);
                            return new DeletePrompt();
                        } else {
                            ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorEventInUse") + " \"" + PURPLE + evt.name + RED + "\":");
                            for (String s : used) {
                                ((Player) context.getForWhom()).sendMessage(RED + "- " + DARKRED + s);
                            }
                            ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorMustModifyQuests"));
                            return new SelectDeletePrompt();
                        }
                    }

                }

                ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorNotFound"));
                return new SelectDeletePrompt();

            } else {
                return new MenuPrompt();
            }

        }

    }

    private class DeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text
                    = RED + Lang.get("eventEditorDeletePrompt") + " \"" + GOLD + (String) context.getSessionData(CK.ED_EVENT_DELETE) + RED + "\"?\n";
            text += YELLOW + Lang.get("yesWord") + "/" + Lang.get("noWord");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("yesWord"))) {
                deleteEvent(context);
                return new MenuPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new MenuPrompt();
            } else {
                return new DeletePrompt();
            }

        }

    }

    private class CreateMenuPrompt extends FixedSetPrompt {

        public CreateMenuPrompt() {

            super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19");

        }

        @SuppressWarnings("unchecked")
		@Override
        public String getPromptText(ConversationContext context) {

            String text
                    = GOLD + "- " + Lang.get("event") + ": " + AQUA + context.getSessionData(CK.E_NAME) + GOLD + " -\n";

            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetName") + "\n";

            if (context.getSessionData(CK.E_MESSAGE) == null) {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMessage") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMessage") + "(" + AQUA + "\"" + context.getSessionData(CK.E_MESSAGE) + "\"" + YELLOW + ")\n";
            }

            if (context.getSessionData(CK.E_CLEAR_INVENTORY) == null) {
                context.setSessionData(CK.E_CLEAR_INVENTORY, "No");
            }

            if (context.getSessionData(CK.E_FAIL_QUEST) == null) {
                context.setSessionData(CK.E_FAIL_QUEST, "No");
            }

            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorClearInv") + ": " + AQUA + context.getSessionData(CK.E_CLEAR_INVENTORY) + "\n";
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("eventEditorFailQuest") + ": " + AQUA + context.getSessionData(CK.E_FAIL_QUEST) + "\n";

            if (context.getSessionData(CK.E_ITEMS) == null) {
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("eventEditorSetItems") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("eventEditorSetItems") + "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS);

                for (ItemStack is : items) {

                    text += GRAY + "    - " + ItemUtil.getString(is) + "\n";

                }

            }

            if (context.getSessionData(CK.E_EXPLOSIONS) == null) {
                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("eventEditorSetExplosions") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("eventEditorSetExplosions") + "\n";
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);

                for (String loc : locations) {

                    text += GRAY + "    - " + AQUA + loc + "\n";

                }

            }

            if (context.getSessionData(CK.E_EFFECTS) == null) {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("eventEditorSetEffects") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("eventEditorSetEffects") + "\n";
                LinkedList<String> effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);

                for (String effect : effects) {

                    text += GRAY + "    - " + AQUA + effect + GRAY + " at " + DARKAQUA + locations.get(effects.indexOf(effect)) + "\n";

                }

            }

            if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("eventEditorSetStorm") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("eventEditorSetStorm") + " (" + AQUA + (String) context.getSessionData(CK.E_WORLD_STORM) + YELLOW + " -> " + DARKAQUA + Quests.getTime((Long) context.getSessionData(CK.E_WORLD_STORM_DURATION)) + YELLOW + ")\n";
            }

            if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("eventEditorSetThunder") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("eventEditorSetThunder") + " (" + AQUA + (String) context.getSessionData(CK.E_WORLD_THUNDER) + YELLOW + " -> " + DARKAQUA + Quests.getTime((Long) context.getSessionData(CK.E_WORLD_THUNDER_DURATION)) + YELLOW + ")\n";
            }

            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobSpawns") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);

                text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobSpawns") + "\n";

                for (String s : types) {
                    QuestMob qm = QuestMob.fromString(s);
                    text += GRAY + "    - " + AQUA + qm.getType().name() + ((qm.getName() != null) ? ": " + qm.getName() : "") + GRAY + " x " + DARKAQUA + qm.getSpawnAmounts() + GRAY + " -> " + GREEN + Quests.getLocationInfo(qm.getSpawnLocation()) + "\n";
                }
            }

            if (context.getSessionData(CK.E_LIGHTNING) == null) {
                text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - " + Lang.get("eventEditorSetLightning") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - " + Lang.get("eventEditorSetLightning") + "\n";
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);

                for (String loc : locations) {

                    text += GRAY + "    - " + AQUA + loc + "\n";

                }

            }

            if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + "\n";
                LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES);
                LinkedList<Long> durations = (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS);
                LinkedList<Integer> mags = (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT);
                int index = -1;

                for (String type : types) {

                    index++;
                    text += GRAY + "    - " + AQUA + type + PURPLE + " " + Quests.getNumeral(mags.get(index)) + GRAY + " -> " + DARKAQUA + Quests.getTime(durations.get(index) * 50L) + "\n";

                }

            }

            if (context.getSessionData(CK.E_HUNGER) == null) {
                text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHunger") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHunger") + AQUA + " (" + (Integer) context.getSessionData(CK.E_HUNGER) + ")\n";

            }

            if (context.getSessionData(CK.E_SATURATION) == null) {
                text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - " + Lang.get("eventEditorSetSaturation") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - " + Lang.get("eventEditorSetSaturation") + AQUA + " (" + (Integer) context.getSessionData(CK.E_SATURATION) + ")\n";

            }

            if (context.getSessionData(CK.E_HEALTH) == null) {
                text += BLUE + "" + BOLD + "15" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHealth") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "15" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHealth") + AQUA + " (" + (Integer) context.getSessionData(CK.E_HEALTH) + ")\n";

            }

            if (context.getSessionData(CK.E_TELEPORT) == null) {
                text += BLUE + "" + BOLD + "16" + RESET + YELLOW + " - " + Lang.get("eventEditorSetTeleport") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "16" + RESET + YELLOW + " - " + Lang.get("eventEditorSetTeleport") + AQUA + " (" + (String) context.getSessionData(CK.E_TELEPORT) + ")\n";

            }

            if (context.getSessionData(CK.E_COMMANDS) == null) {
                text += BLUE + "" + BOLD + "17" + RESET + YELLOW + " - " + Lang.get("eventEditorSetCommands") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "17" + RESET + YELLOW + " - " + Lang.get("eventEditorSetCommands") + "\n";
                for (String s : (LinkedList<String>) context.getSessionData(CK.E_COMMANDS)) {
                    text += GRAY + "    - " + AQUA + s + "\n";
                }

            }

            text += GREEN + "" + BOLD + "18" + RESET + YELLOW + " - " + Lang.get("done") + "\n";
            text += RED + "" + BOLD + "19" + RESET + YELLOW + " - " + Lang.get("quit");

            return text;

        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {

                return new SetNamePrompt();

            } else if (input.equalsIgnoreCase("2")) {

                return new MessagePrompt();

            } else if (input.equalsIgnoreCase("3")) {

                String s = (String) context.getSessionData(CK.E_CLEAR_INVENTORY);
                if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                    context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
                } else {
                    context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("4")) {

                String s = (String) context.getSessionData(CK.E_FAIL_QUEST);
                if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                    context.setSessionData(CK.E_FAIL_QUEST, Lang.get("noWord"));
                } else {
                    context.setSessionData(CK.E_FAIL_QUEST, Lang.get("yesWord"));
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("5")) {

                return new ItemListPrompt();

            } else if (input.equalsIgnoreCase("6")) {

                selectedExplosionLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                return new ExplosionPrompt();

            } else if (input.equalsIgnoreCase("7")) {

                return new EffectListPrompt();

            } else if (input.equalsIgnoreCase("8")) {

                return new StormPrompt();

            } else if (input.equalsIgnoreCase("9")) {

                return new ThunderPrompt();

            } else if (input.equalsIgnoreCase("10")) {

                return new MobPrompt();

            } else if (input.equalsIgnoreCase("11")) {

                selectedLightningLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                return new LightningPrompt();

            } else if (input.equalsIgnoreCase("12")) {

                return new PotionEffectPrompt();

            } else if (input.equalsIgnoreCase("13")) {

                return new HungerPrompt();

            } else if (input.equalsIgnoreCase("14")) {

                return new SaturationPrompt();

            } else if (input.equalsIgnoreCase("15")) {

                return new HealthPrompt();

            } else if (input.equalsIgnoreCase("16")) {

                selectedTeleportLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                return new TeleportPrompt();

            } else if (input.equalsIgnoreCase("17")) {

                return new CommandsPrompt();

            } else if (input.equalsIgnoreCase("18")) {

                if (context.getSessionData(CK.E_OLD_EVENT) != null) {
                    return new FinishPrompt((String) context.getSessionData(CK.E_OLD_EVENT));
                } else {
                    return new FinishPrompt(null);
                }

            } else if (input.equalsIgnoreCase("19")) {

                return new QuitPrompt();

            }

            return null;

        }
    }

    private class QuitPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text
                    = GREEN + Lang.get("eventEditorQuitWithoutSaving") + "\n";
            text += YELLOW + Lang.get("yesWord") + "/" + Lang.get("noWord");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("yesWord"))) {
                clearData(context);
                return new MenuPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new CreateMenuPrompt();
            } else {
                ((Player) context.getForWhom()).sendMessage(RED + Lang.get("invalidOption"));
                return new QuitPrompt();
            }

        }

    }

    private class FinishPrompt extends StringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<String>();

        public FinishPrompt(String modifiedName) {

            if (modifiedName != null) {

                modName = modifiedName;
                for (Quest q : quests.getQuests()) {

                    for (Stage s : q.orderedStages) {

                        if (s.finishEvent != null && s.finishEvent.name != null) {

                            if (s.finishEvent.name.equalsIgnoreCase(modifiedName)) {
                                modified.add(q.getName());
                                break;
                            }

                        }

                    }

                }

            }

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text
                    = RED + Lang.get("eventEditorFinishAndSave") + " \"" + GOLD + (String) context.getSessionData(CK.E_NAME) + RED + "\"?\n";
            if (modified.isEmpty() == false) {
                text += RED + Lang.get("eventEditorModifiedNote") + "\n";
                for (String s : modified) {
                    text += GRAY + "    - " + DARKRED + s + "\n";
                }
                text += RED + Lang.get("eventEditorForcedToQuit") + "\n";
            }
            text += YELLOW + Lang.get("yesWord") + "/" + Lang.get("noWord");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("yesWord"))) {
                saveEvent(context);
                return new MenuPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("noWord"))) {
                return new CreateMenuPrompt();
            } else {
                ((Player) context.getForWhom()).sendMessage(RED + Lang.get("invalidOption"));
                return new FinishPrompt(modName);
            }

        }

    }

    //Convenience methods to reduce typecasting
    private static String getCString(ConversationContext context, String path) {
        return (String) context.getSessionData(path);
    }

    @SuppressWarnings("unchecked")
    private static LinkedList<String> getCStringList(ConversationContext context, String path) {
        return (LinkedList<String>) context.getSessionData(path);
    }

    private static Integer getCInt(ConversationContext context, String path) {
        return (Integer) context.getSessionData(path);
    }

    @SuppressWarnings("unchecked")
    private static LinkedList<Integer> getCIntList(ConversationContext context, String path) {
        return (LinkedList<Integer>) context.getSessionData(path);
    }

    @SuppressWarnings("unused")
	private static Boolean getCBoolean(ConversationContext context, String path) {
        return (Boolean) context.getSessionData(path);
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private static LinkedList<Boolean> getCBooleanList(ConversationContext context, String path) {
        return (LinkedList<Boolean>) context.getSessionData(path);
    }

    private static Long getCLong(ConversationContext context, String path) {
        return (Long) context.getSessionData(path);
    }

    @SuppressWarnings("unchecked")
    private static LinkedList<Long> getCLongList(ConversationContext context, String path) {
        return (LinkedList<Long>) context.getSessionData(path);
    }
    //

    private void deleteEvent(ConversationContext context) {

        YamlConfiguration data = new YamlConfiguration();

        try {
            eventsFile = new File(quests.getDataFolder(), "events.yml");
            data.load(eventsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
            return;
        }

        String event = (String) context.getSessionData(CK.ED_EVENT_DELETE);
        ConfigurationSection sec = data.getConfigurationSection("events");
        sec.set(event, null);

        try {
            data.save(eventsFile);
        } catch (IOException e) {
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
            return;
        }

        quests.reloadQuests();

        ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorDeleted"));

        for (Quester q : quests.questers.values()) {

            for (Quest quest : q.currentQuests.keySet()) {
                q.checkQuest(quest);
            }

        }

        clearData(context);

    }

    private void saveEvent(ConversationContext context) {

        YamlConfiguration data = new YamlConfiguration();

        try {
            eventsFile = new File(quests.getDataFolder(), "events.yml");
            data.load(eventsFile);
        } catch (IOException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
            return;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
            return;
        }

        if (((String) context.getSessionData(CK.E_OLD_EVENT)).isEmpty() == false) {
            data.set("events." + (String) context.getSessionData(CK.E_OLD_EVENT), null);
            quests.events.remove(quests.getEvent((String) context.getSessionData(CK.E_OLD_EVENT)));
        }

        ConfigurationSection section = data.createSection("events." + (String) context.getSessionData(CK.E_NAME));
        names.remove((String) context.getSessionData(CK.E_NAME));

        if (context.getSessionData(CK.E_MESSAGE) != null) {
            section.set("message", getCString(context, CK.E_MESSAGE));
        }

        if (context.getSessionData(CK.E_CLEAR_INVENTORY) != null) {
            String s = getCString(context, CK.E_CLEAR_INVENTORY);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("clear-inventory", true);
            }
        }

        if (context.getSessionData(CK.E_FAIL_QUEST) != null) {
            String s = getCString(context, CK.E_FAIL_QUEST);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                section.set("fail-quest", true);
            }
        }

        if (context.getSessionData(CK.E_ITEMS) != null) {

            @SuppressWarnings("unchecked")
			LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS);
            LinkedList<String> lines = new LinkedList<String>();

            for (ItemStack is : items) {
                lines.add(ItemUtil.serialize(is));
            }

            section.set("items", lines);

        }

        if (context.getSessionData(CK.E_EXPLOSIONS) != null) {

            LinkedList<String> locations = getCStringList(context, CK.E_EXPLOSIONS);
            section.set("explosions", locations);

        }

        if (context.getSessionData(CK.E_EFFECTS) != null) {

            LinkedList<String> effects = getCStringList(context, CK.E_EFFECTS);
            LinkedList<String> locations = getCStringList(context, CK.E_EFFECTS_LOCATIONS);

            section.set("effects", effects);
            section.set("effect-locations", locations);

        }

        if (context.getSessionData(CK.E_WORLD_STORM) != null) {

            String world = getCString(context, CK.E_WORLD_STORM);
            Long duration = getCLong(context, CK.E_WORLD_STORM_DURATION);

            section.set("storm-world", world);
            section.set("storm-duration", duration / 50L);

        }

        if (context.getSessionData(CK.E_WORLD_THUNDER) != null) {

            String world = getCString(context, CK.E_WORLD_THUNDER);
            Long duration = getCLong(context, CK.E_WORLD_THUNDER_DURATION);

            section.set("thunder-world", world);
            section.set("thunder-duration", duration / 50L);

        }

        try {
            if (context.getSessionData(CK.E_MOB_TYPES) != null) {
                int count = 0;

                for (String s : getCStringList(context, CK.E_MOB_TYPES)) {
                    ConfigurationSection ss = section.getConfigurationSection("mob-spawns." + count);
                    if (ss == null) {
                        ss = section.createSection("mob-spawns." + count);
                    }
                    QuestMob questMob = QuestMob.fromString(s);

                    if (questMob == null) {
                        continue;
                    }

                    ss.set("name", questMob.getName());
                    ss.set("spawn-location", Quests.getLocationInfo(questMob.getSpawnLocation()));
                    ss.set("mob-type", questMob.getType().name());
                    ss.set("spawn-amounts", questMob.getSpawnAmounts());
                    ss.set("held-item", ItemUtil.serialize(questMob.inventory[0]));
                    ss.set("held-item-drop-chance", questMob.dropChances[0]);
                    ss.set("boots", ItemUtil.serialize(questMob.inventory[1]));
                    ss.set("boots-drop-chance", questMob.dropChances[1]);
                    ss.set("leggings", ItemUtil.serialize(questMob.inventory[2]));
                    ss.set("leggings-drop-chance", questMob.dropChances[2]);
                    ss.set("chest-plate", ItemUtil.serialize(questMob.inventory[3]));
                    ss.set("chest-plate-drop-chance", questMob.dropChances[3]);
                    ss.set("helmet", ItemUtil.serialize(questMob.inventory[4]));
                    ss.set("helmet-drop-chance", questMob.dropChances[4]);
                    count++;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (context.getSessionData(CK.E_LIGHTNING) != null) {

            LinkedList<String> locations = getCStringList(context, CK.E_LIGHTNING);
            section.set("lightning-strikes", locations);

        }

        if (context.getSessionData(CK.E_COMMANDS) != null) {

            LinkedList<String> commands = getCStringList(context, CK.E_COMMANDS);
            if (commands.isEmpty() == false) {
                section.set("commands", commands);
            }

        }

        if (context.getSessionData(CK.E_POTION_TYPES) != null) {

            LinkedList<String> types = getCStringList(context, CK.E_POTION_TYPES);
            LinkedList<Long> durations = getCLongList(context, CK.E_POTION_DURATIONS);
            LinkedList<Integer> mags = getCIntList(context, CK.E_POTION_STRENGHT);

            section.set("potion-effect-types", types);
            section.set("potion-effect-durations", durations);
            section.set("potion-effect-amplifiers", mags);

        }

        if (context.getSessionData(CK.E_HUNGER) != null) {

            Integer i = getCInt(context, CK.E_HUNGER);
            section.set("hunger", i);

        }

        if (context.getSessionData(CK.E_SATURATION) != null) {

            Integer i = getCInt(context, CK.E_SATURATION);
            section.set("saturation", i);

        }

        if (context.getSessionData(CK.E_HEALTH) != null) {

            Integer i = getCInt(context, CK.E_HEALTH);
            section.set("health", i);

        }

        if (context.getSessionData(CK.E_TELEPORT) != null) {

            section.set("teleport-location", getCString(context, CK.E_TELEPORT));

        }

        try {
            data.save(eventsFile);
        } catch (IOException e) {
            ((Player) context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
            return;
        }

        quests.reloadQuests();

        ((Player) context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorSaved"));

        for (Quester q : quests.questers.values()) {

            for (Quest quest : q.currentQuests.keySet()) {
                q.checkQuest(quest);
            }

        }

        clearData(context);

    }

    private class EventNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text
                    = AQUA + Lang.get("eventEditorCreate") + GOLD + " - " + Lang.get("eventEditorEnterEventName");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                for (Event e : quests.events) {

                    if (e.name.equalsIgnoreCase(input)) {

                        context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorExists"));
                        return new EventNamePrompt();

                    }

                }

                if (names.contains(input)) {

                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorSomeone"));
                    return new EventNamePrompt();

                }

                if (StringUtils.isAlphanumeric(input) == false) {

                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorAlpha"));
                    return new EventNamePrompt();

                }

                context.setSessionData(CK.E_NAME, input);
                names.add(input);
                return new CreateMenuPrompt();

            } else {

                return new MenuPrompt();

            }

        }
    }

    @SuppressWarnings("unused")
	private class SetNpcStartPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorEnterNPCId");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if (CitizensAPI.getNPCRegistry().getById(input.intValue()) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorNoNPCExists"));
                    return new SetNpcStartPrompt();
                }

                context.setSessionData("npcStart", input.intValue());

            }

            return new CreateMenuPrompt();

        }
    }

    private class ExplosionPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorExplosionPrompt");

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedExplosionLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(CK.E_EXPLOSIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(CK.E_EXPLOSIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(CK.E_EXPLOSIONS, locs);
                    selectedExplosionLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new ExplosionPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData(CK.E_EXPLOSIONS, null);
                selectedExplosionLocations.remove(player.getUniqueId());
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedExplosionLocations.remove(player.getUniqueId());
                return new CreateMenuPrompt();

            } else {
                return new ExplosionPrompt();
            }

        }
    }

    private class SetNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorEnterEventName");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                for (Event e : quests.events) {

                    if (e.name.equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorExists"));
                        return new SetNamePrompt();
                    }

                }

                if (names.contains(input)) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorSomeone"));
                    return new SetNamePrompt();
                }

                if (StringUtils.isAlphanumeric(input) == false) {

                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorAlpha"));
                    return new SetNamePrompt();

                }

                names.remove((String) context.getSessionData(CK.E_NAME));
                context.setSessionData(CK.E_NAME, input);
                names.add(input);

            }

            return new CreateMenuPrompt();

        }
    }

    private class MessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetMessagePrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdNone")) == false) {
                context.setSessionData(CK.E_MESSAGE, input);
            } else if (input.equalsIgnoreCase(Lang.get("cmdNone"))) {
                context.setSessionData(CK.E_MESSAGE, null);
            }

            return new CreateMenuPrompt();

        }
    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt() {

            super("1", "2", "3");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(CK.E_ITEMS) != null) {
                    List<ItemStack> items = getItems(context);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.E_ITEMS, items);
                } else {
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.E_ITEMS, itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }

            String text = GOLD + Lang.get("eventEditorGiveItemsTitle") + "\n";
            if (context.getSessionData(CK.E_ITEMS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddItem") + "\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                for (ItemStack is : getItems(context)) {

                    text += GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";

                }

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddItem") + "\n";

                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(ItemListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorItemsCleared"));
                context.setSessionData(CK.E_ITEMS, null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new CreateMenuPrompt();
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.E_ITEMS);
        }

    }

    private class EffectListPrompt extends FixedSetPrompt {

        public EffectListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorEffects") + " -\n";
            if (context.getSessionData(CK.E_EFFECTS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddEffect") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorAddEffectLocation") + " (" + Lang.get("eventEditorNoEffects") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddEffect") + "\n";
                for (String s : getEffects(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorAddEffectLocation") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorAddEffectLocation") + "\n";
                    for (String s : getEffectLocations(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new EffectPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_EFFECTS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustAddEffects"));
                    return new EffectListPrompt();
                } else {
                    selectedEffectLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                    return new EffectLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorEffectsCleared"));
                context.setSessionData(CK.E_EFFECTS, null);
                context.setSessionData(CK.E_EFFECTS_LOCATIONS, null);
                return new EffectListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(CK.E_EFFECTS) != null) {
                    one = getEffects(context).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
                    two = getEffectLocations(context).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateMenuPrompt();
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorListSizeMismatch"));
                    return new EffectListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getEffects(ConversationContext context) {
            return (List<String>) context.getSessionData(CK.E_EFFECTS);
        }

        @SuppressWarnings("unchecked")
		private List<String> getEffectLocations(ConversationContext context) {
            return (List<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
        }
    }

    private class EffectLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorEffectLocationPrompt");

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedEffectLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(CK.E_EFFECTS_LOCATIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS_LOCATIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(CK.E_EFFECTS_LOCATIONS, locs);
                    selectedEffectLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new EffectLocationPrompt();
                }

                return new EffectListPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedEffectLocations.remove(player.getUniqueId());
                return new EffectListPrompt();

            } else {
                return new EffectLocationPrompt();
            }

        }
    }

    private class EffectPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String effects = PINK + Lang.get("eventEditorEffectsTitle") + "\n";
            effects += PURPLE + "BLAZE_SHOOT " + GRAY + "- " + Lang.get("effBlazeShoot") + "\n";
            effects += PURPLE + "BOW_FIRE " + GRAY + "- " + Lang.get("effBowFire") + "\n";
            effects += PURPLE + "CLICK1 " + GRAY + "- " + Lang.get("effClick1") + "\n";
            effects += PURPLE + "CLICK2 " + GRAY + "- " + Lang.get("effClick2") + "\n";
            effects += PURPLE + "DOOR_TOGGLE " + GRAY + "- " + Lang.get("effDoorToggle") + "\n";
            effects += PURPLE + "EXTINGUISH " + GRAY + "- " + Lang.get("effExtinguish") + "\n";
            effects += PURPLE + "GHAST_SHOOT " + GRAY + "- " + Lang.get("effGhastShoot") + "\n";
            effects += PURPLE + "GHAST_SHRIEK " + GRAY + "- " + Lang.get("effGhastShriek") + "\n";
            effects += PURPLE + "ZOMBIE_CHEW_IRON_DOOR " + GRAY + "- " + Lang.get("effZombieWood") + "\n";
            effects += PURPLE + "ZOMBIE_CHEW_WOODEN_DOOR " + GRAY + "- " + Lang.get("effZombieIron") + "\n";

            return YELLOW + effects + Lang.get("effEnterName");

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (Quests.getEffect(input.toUpperCase()) != null) {

                    LinkedList<String> effects;
                    if (context.getSessionData(CK.E_EFFECTS) != null) {
                        effects = (LinkedList<String>) context.getSessionData(CK.E_EFFECTS);
                    } else {
                        effects = new LinkedList<String>();
                    }

                    effects.add(input.toUpperCase());
                    context.setSessionData(CK.E_EFFECTS, effects);
                    selectedEffectLocations.remove(player.getUniqueId());
                    return new EffectListPrompt();

                } else {
                    player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorInvalidEffect"));
                    return new EffectPrompt();
                }

            } else {

                selectedEffectLocations.remove(player.getUniqueId());
                return new EffectListPrompt();

            }

        }
    }

    private class StormPrompt extends FixedSetPrompt {

        public StormPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + Lang.get("eventEditorStormTitle") + "\n";
            if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + AQUA + ((String) context.getSessionData(CK.E_WORLD_STORM)) + YELLOW + ")\n";

                if (context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    Long dur = (Long) context.getSessionData(CK.E_WORLD_STORM_DURATION);

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + AQUA + Quests.getTime(dur) + YELLOW + ")\n";

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new StormWorldPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_WORLD_STORM) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorSetWorldFirst"));
                    return new StormPrompt();
                } else {
                    return new StormDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorStormCleared"));
                context.setSessionData(CK.E_WORLD_STORM, null);
                context.setSessionData(CK.E_WORLD_STORM_DURATION, null);
                return new StormPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                if (context.getSessionData(CK.E_WORLD_STORM) != null && context.getSessionData(CK.E_WORLD_STORM_DURATION) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetStormDuration"));
                    return new StormPrompt();
                } else {
                    return new CreateMenuPrompt();
                }

            }
            return null;

        }
    }

    private class StormWorldPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String effects = PINK + Lang.get("eventEditorWorldsTitle") + "\n" + PURPLE;
            for (World w : quests.getServer().getWorlds()) {
                effects += w.getName() + ", ";
            }

            effects = effects.substring(0, effects.length());

            return YELLOW + effects + Lang.get("eventEditorEnterStormWorld");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (quests.getServer().getWorld(input) != null) {

                    context.setSessionData(CK.E_WORLD_STORM, quests.getServer().getWorld(input).getName());

                } else {
                    player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorInvalidWorld"));
                    return new StormWorldPrompt();
                }

            }
            return new StormPrompt();

        }
    }

    private class StormDurationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorEnterStormDuration");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.longValue() < 1000) {
                context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorAtLeastOneSecond"));
                return new StormDurationPrompt();
            }

            context.setSessionData(CK.E_WORLD_STORM_DURATION, input.longValue());
            return new StormPrompt();

        }
    }

    private class ThunderPrompt extends FixedSetPrompt {

        public ThunderPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + Lang.get("eventEditorThunderTitle") + "\n";

            if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + AQUA + ((String) context.getSessionData(CK.E_WORLD_THUNDER)) + YELLOW + ")\n";

                if (context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    Long dur = (Long) context.getSessionData(CK.E_WORLD_THUNDER_DURATION);
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + AQUA + Quests.getTime(dur) + YELLOW + ")\n";

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ThunderWorldPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_WORLD_THUNDER) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorSetWorldFirst"));
                    return new ThunderPrompt();
                } else {
                    return new ThunderDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorThunderCleared"));
                context.setSessionData(CK.E_WORLD_THUNDER, null);
                context.setSessionData(CK.E_WORLD_THUNDER_DURATION, null);
                return new ThunderPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                if (context.getSessionData(CK.E_WORLD_THUNDER) != null && context.getSessionData(CK.E_WORLD_THUNDER_DURATION) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetThunderDuration"));
                    return new ThunderPrompt();
                } else {
                    return new CreateMenuPrompt();
                }

            }
            return null;

        }
    }

    private class ThunderWorldPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String effects = PINK + Lang.get("eventEditorWorldsTitle") + "\n" + PURPLE;
            for (World w : quests.getServer().getWorlds()) {
                effects += w.getName() + ", ";
            }

            effects = effects.substring(0, effects.length());

            return YELLOW + effects + Lang.get("eventEditorEnterThunderWorld");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (quests.getServer().getWorld(input) != null) {

                    context.setSessionData(CK.E_WORLD_THUNDER, quests.getServer().getWorld(input).getName());

                } else {
                    player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorInvalidWorld"));
                    return new ThunderWorldPrompt();
                }

            }
            return new ThunderPrompt();

        }
    }

    private class ThunderDurationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorEnterDuration");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.longValue() < 1000) {
                context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorAtLeastOneSecond"));
                return new ThunderDurationPrompt();
            } else {
                context.setSessionData(CK.E_WORLD_THUNDER_DURATION, input.longValue());
            }

            return new ThunderPrompt();

        }
    }

    private class MobPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + Lang.get("eventEditorMobSpawnsTitle") + "\n";
            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("done");
            } else {
                @SuppressWarnings("unchecked")
				LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);

                for (int i = 0; i < types.size(); i++) {
                    QuestMob qm = QuestMob.fromString(types.get(i));
                    text += GOLD + "  " + (i + 1) + " - " + Lang.get("edit") + ": " + AQUA + qm.getType().name() + ((qm.getName() != null) ? ": " + qm.getName() : "") + GRAY + " x " + DARKAQUA + qm.getSpawnAmounts() + GRAY + " -> " + GREEN + Quests.getLocationInfo(qm.getSpawnLocation()) + "\n";
                }

                text += BLUE + "" + BOLD + (types.size() + 1) + RESET + YELLOW + " - " + Lang.get("eventEditorAddMobTypes") + "\n";
                text += BLUE + "" + BOLD + (types.size() + 2) + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += GREEN + "" + BOLD + (types.size() + 3) + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                if (input.equalsIgnoreCase("1")) {
                    return new QuestMobPrompt(0, null);
                } else if (input.equalsIgnoreCase("2")) {
                    context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
                    context.setSessionData(CK.E_MOB_TYPES, null);
                    return new MobPrompt();
                } else if (input.equalsIgnoreCase("3")) {
                    return new CreateMenuPrompt();
                }
            } else {
                @SuppressWarnings("unchecked")
				LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                int inp;
                try {
                    inp = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorNotANumber"));
                    return new MobPrompt();
                }

                if (inp == types.size() + 1) {
                    return new QuestMobPrompt(inp - 1, null);
                } else if (inp == types.size() + 2) {
                    context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
                    context.setSessionData(CK.E_MOB_TYPES, null);
                    return new MobPrompt();
                } else if (inp == types.size() + 3) {
                    return new CreateMenuPrompt();
                } else if (inp > types.size()) {
                    return new MobPrompt();
                } else {
                    return new QuestMobPrompt(inp - 1, QuestMob.fromString(types.get(inp - 1)));
                }
            }

            return new MobPrompt();
        }
    }

    private class QuestMobPrompt extends StringPrompt {

        private QuestMob questMob;
        private Integer itemIndex = -1;
        private final Integer mobIndex;

        public QuestMobPrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + Lang.get("eventEditorAddMobTypesTitle") + "\n";

            if (questMob == null) {
                questMob = new QuestMob();
            }

            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (itemIndex >= 0) {
                    questMob.inventory[itemIndex] = ((ItemStack) context.getSessionData("tempStack"));
                    itemIndex = -1;
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }

            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobName") + GRAY + " (" + ((questMob.getName() == null) ? Lang.get("noneSet") : AQUA + questMob.getName()) + ")\n";
            text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobType") + GRAY + " (" + ((questMob.getType() == null) ? Lang.get("eventEditorNoTypesSet") : AQUA + questMob.getType().name()) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorAddSpawnLocation") + GRAY + " (" + ((questMob.getSpawnLocation() == null) ? GRAY + Lang.get("noneSet") : AQUA + Quests.getLocationInfo(questMob.getSpawnLocation())) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobSpawnAmount") + GRAY + " (" + ((questMob.getSpawnAmounts() == null) ? GRAY + Lang.get("eventEditorNoAmountsSet") : AQUA + "" + questMob.getSpawnAmounts()) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobItemInHand") + GRAY + " (" + ((questMob.inventory[0] == null) ? GRAY + Lang.get("noneSet") : AQUA + ItemUtil.getDisplayString(questMob.inventory[0])) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobItemInHandDrop") + GRAY + " (" + ((questMob.dropChances[0] == null) ? GRAY + Lang.get("noneSet") : AQUA + "" + questMob.dropChances[0]) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobBoots") + GRAY + " (" + ((questMob.inventory[1] == null) ? GRAY + Lang.get("noneSet") : AQUA + ItemUtil.getDisplayString(questMob.inventory[1])) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobBootsDrop") + GRAY + " (" + ((questMob.dropChances[1] == null) ? GRAY + Lang.get("noneSet") : AQUA + "" + questMob.dropChances[1]) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobLeggings") + GRAY + " (" + ((questMob.inventory[2] == null) ? GRAY + Lang.get("noneSet") : AQUA + ItemUtil.getDisplayString(questMob.inventory[2])) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobLeggingsDrop") + GRAY + " (" + ((questMob.dropChances[2] == null) ? GRAY + Lang.get("noneSet") : AQUA + "" + questMob.dropChances[2]) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobChestPlate") + GRAY + " (" + ((questMob.inventory[3] == null) ? GRAY + Lang.get("noneSet") : AQUA + ItemUtil.getDisplayString(questMob.inventory[3])) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobChestPlateDrop") + GRAY + " (" + ((questMob.dropChances[3] == null) ? GRAY + Lang.get("noneSet") : AQUA + "" + questMob.dropChances[3]) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobHelmet") + GRAY + " (" + ((questMob.inventory[4] == null) ? GRAY + Lang.get("noneSet") : AQUA + ItemUtil.getDisplayString(questMob.inventory[4])) + GRAY + ")\n";
            text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobHelmetDrop") + GRAY + " (" + ((questMob.dropChances[4] == null) ? GRAY + Lang.get("noneSet") : AQUA + "" + questMob.dropChances[4]) + GRAY + ")\n";

            text += GREEN + "" + BOLD + "15" + RESET + YELLOW + " - " + Lang.get("done") + "\n";
            text += RED + "" + BOLD + "16" + RESET + YELLOW + " - " + Lang.get("cancel");

            return text;

        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new MobNamePrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("2")) {
                return new MobTypePrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("3")) {
                selectedMobLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                return new MobLocationPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("4")) {
                return new MobAmountPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase("5")) {
                itemIndex = 0;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("6")) {
                return new MobDropPrompt(0, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("7")) {
                itemIndex = 1;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("8")) {
                return new MobDropPrompt(1, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("9")) {
                itemIndex = 2;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("10")) {
                return new MobDropPrompt(2, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("11")) {
                itemIndex = 3;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("12")) {
                return new MobDropPrompt(3, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("13")) {
                itemIndex = 4;
                return new ItemStackPrompt(QuestMobPrompt.this);
            } else if (input.equalsIgnoreCase("14")) {
                return new MobDropPrompt(4, mobIndex, questMob);
            } else if (input.equalsIgnoreCase("15")) {
                if (questMob.getType() == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetMobTypesFirst"));
                    return new QuestMobPrompt(mobIndex, questMob);
                } else if (questMob.getSpawnLocation() == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetMobLocationFirst"));
                    return new QuestMobPrompt(mobIndex, questMob);
                } else if (questMob.getSpawnAmounts() == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetMobAmountsFirst"));
                    return new QuestMobPrompt(mobIndex, questMob);
                }
                if (context.getSessionData(CK.E_MOB_TYPES) == null) {
                    LinkedList<String> list = new LinkedList<String>();
                    list.add(questMob.serialize());
                    context.setSessionData(CK.E_MOB_TYPES, list);
                } else {
                    if (((LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES)).isEmpty()) {
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(questMob.serialize());
                        context.setSessionData(CK.E_MOB_TYPES, list);
                    } else {
                        LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.E_MOB_TYPES);
                        list.set(mobIndex, questMob.serialize());
                        context.setSessionData(CK.E_MOB_TYPES, list);
                    }
                }
                return new MobPrompt();
            } else if (input.equalsIgnoreCase("16")) {
                return new MobPrompt();
            } else {
                return new QuestMobPrompt(mobIndex, questMob);
            }

        }
    }

    private class MobNamePrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobNamePrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = YELLOW + Lang.get("eventEditorSetMobNamePrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new QuestMobPrompt(mobIndex, questMob);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                questMob.setName(null);
                return new QuestMobPrompt(mobIndex, questMob);
            } else {
                input = ChatColor.translateAlternateColorCodes('&', input);
                questMob.setName(input);
                return new QuestMobPrompt(mobIndex, questMob);
            }
        }
    }

    private class MobTypePrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobTypePrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext arg0) {

            String mobs = PINK + Lang.get("eventEditorMobsTitle") + "\n";
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {

                final EntityType type = mobArr[i];
                if (type.isAlive() == false) {
                    continue;
                }

                if (i < (mobArr.length - 1)) {
                    mobs += MiscUtil.getProperMobName(mobArr[i]) + ", ";
                } else {
                    mobs += MiscUtil.getProperMobName(mobArr[i]) + "\n";
                }
            }

            return mobs + YELLOW + Lang.get("eventEditorSetMobTypesPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (MiscUtil.getProperMobType(input) != null) {

                    questMob.setType(MiscUtil.getProperMobType(input));

                } else {
                    player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorInvalidMob"));
                    return new MobTypePrompt(mobIndex, questMob);
                }
            }

            return new QuestMobPrompt(mobIndex, questMob);
        }
    }

    private class MobAmountPrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobAmountPrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetMobAmountsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                try {

                    int i = Integer.parseInt(input);

                    if (i < 1) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorNotGreaterThanZero"));
                        return new MobAmountPrompt(mobIndex, questMob);
                    }

                    questMob.setSpawnAmounts(i);
                    return new QuestMobPrompt(mobIndex, questMob);
                } catch (NumberFormatException e) {
                    player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorNotANumber"));
                    return new MobAmountPrompt(mobIndex, questMob);
                }

            }

            return new QuestMobPrompt(mobIndex, questMob);

        }
    }

    private class MobLocationPrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;

        public MobLocationPrompt(int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetMobLocationPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedMobLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    questMob.setSpawnLocation(loc);
                    selectedMobLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new MobLocationPrompt(mobIndex, questMob);
                }

                return new QuestMobPrompt(mobIndex, questMob);

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedMobLocations.remove(player.getUniqueId());
                return new QuestMobPrompt(mobIndex, questMob);

            } else {
                return new MobLocationPrompt(mobIndex, questMob);
            }

        }
    }

    private class MobDropPrompt extends StringPrompt {

        private final QuestMob questMob;
        private final Integer mobIndex;
        private final Integer invIndex;

        public MobDropPrompt(int invIndex, int mobIndex, QuestMob questMob) {
            this.questMob = questMob;
            this.mobIndex = mobIndex;
            this.invIndex = invIndex;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = YELLOW + Lang.get("eventEditorSetDropChance");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            float chance;

            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new QuestMobPrompt(mobIndex, questMob);
            }

            try {
                chance = Float.parseFloat(input);
            } catch (NumberFormatException e) {
                context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorInvalidDropChance"));
                return new MobDropPrompt(invIndex, mobIndex, questMob);
            }
            if (chance > 1 || chance < 0) {
                context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorInvalidDropChance"));
                return new MobDropPrompt(invIndex, mobIndex, questMob);
            }

            questMob.dropChances[invIndex] = chance;

            return new QuestMobPrompt(mobIndex, questMob);
        }
    }

    private class LightningPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorLightningPrompt");

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedLightningLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(CK.E_LIGHTNING) != null) {
                        locs = (LinkedList<String>) context.getSessionData(CK.E_LIGHTNING);
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(CK.E_LIGHTNING, locs);
                    selectedLightningLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new LightningPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData(CK.E_LIGHTNING, null);
                selectedLightningLocations.remove(player.getUniqueId());
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedLightningLocations.remove(player.getUniqueId());
                return new CreateMenuPrompt();

            } else {
                return new LightningPrompt();
            }

        }
    }

    private class PotionEffectPrompt extends FixedSetPrompt {

        public PotionEffectPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @SuppressWarnings("unchecked")
		@Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + Lang.get("eventEditorPotionEffectsTitle") + "\n";
            if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffectTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetPotionDurations") + " " + Lang.get("eventEditorNoTypesSet") + "\n";
                text += GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("eventEditorNoTypesSet") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffectTypes") + "\n";
                for (String s : (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES)) {
                    text += GRAY + "    - " + AQUA + s + "\n";
                }

                if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionDurations") + " (" + Lang.get("noneSet") + ")\n";
                    text += GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("eventEditorNoDurationsSet") + "\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorNoDurationsSet") + "\n";
                    for (Long l : (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS)) {
                        text += GRAY + "    - " + DARKAQUA + Quests.getTime(l * 50L) + "\n";
                    }

                    if (context.getSessionData(CK.E_POTION_STRENGHT) == null) {
                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionMagnitudes") + " (" + Lang.get("noneSet") + ")\n";
                    } else {

                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionMagnitudes") + "\n";
                        for (int i : (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT)) {
                            text += GRAY + "    - " + PURPLE + i + "\n";
                        }

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new PotionTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetPotionTypesFirst"));
                    return new PotionEffectPrompt();
                } else {
                    return new PotionDurationsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetPotionTypesAndDurationsFirst"));
                    return new PotionEffectPrompt();
                } else if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetPotionDurationsFirst"));
                    return new PotionEffectPrompt();
                } else {
                    return new PotionMagnitudesPrompt();
                }

            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorPotionsCleared"));
                context.setSessionData(CK.E_POTION_TYPES, null);
                context.setSessionData(CK.E_POTION_DURATIONS, null);
                context.setSessionData(CK.E_POTION_STRENGHT, null);
                return new PotionEffectPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData(CK.E_POTION_TYPES) != null) {
                    one = ((List<String>) context.getSessionData(CK.E_POTION_TYPES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(CK.E_POTION_DURATIONS) != null) {
                    two = ((List<Long>) context.getSessionData(CK.E_POTION_DURATIONS)).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData(CK.E_POTION_STRENGHT) != null) {
                    three = ((List<Integer>) context.getSessionData(CK.E_POTION_STRENGHT)).size();
                } else {
                    three = 0;
                }

                if (one == two && two == three) {
                    return new CreateMenuPrompt();
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorListSizeMismatch"));
                    return new PotionEffectPrompt();
                }

            }
            return null;

        }
    }

    private class PotionTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String effs = PINK + Lang.get("eventEditorPotionTypesTitle") + "\n";
            for (PotionEffectType pet : PotionEffectType.values()) {
                effs += (pet != null && pet.getName() != null) ? (PURPLE + pet.getName() + "\n") : "";
            }

            return effs + YELLOW + Lang.get("eventEditorSetPotionEffectsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> effTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (PotionEffectType.getByName(s.toUpperCase()) != null) {

                        effTypes.add(PotionEffectType.getByName(s.toUpperCase()).getName());

                        context.setSessionData(CK.E_POTION_TYPES, effTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorInvalidPotionType"));
                        return new PotionTypesPrompt();
                    }

                }

            }

            return new PotionEffectPrompt();

        }
    }

    private class PotionDurationsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetPotionDurationsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<Long> effDurations = new LinkedList<Long>();
                for (String s : input.split(" ")) {

                    try {
                    	
                    	int i = Integer.parseInt(s);
                        long l = i * 1000;

                        if (l < 1000) {
                            player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotGreaterThanOneSecond"));
                            return new PotionDurationsPrompt();
                        }

                        effDurations.add(l / 50L);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotANumber"));
                        return new PotionDurationsPrompt();
                    }

                }

                context.setSessionData(CK.E_POTION_DURATIONS, effDurations);

            }

            return new PotionEffectPrompt();

        }
    }

    private class PotionMagnitudesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetPotionMagnitudesPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<Integer> magAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotGreaterThanZero"));
                            return new PotionMagnitudesPrompt();
                        }

                        magAmounts.add(i);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotANumber"));
                        return new PotionMagnitudesPrompt();
                    }

                }

                context.setSessionData(CK.E_POTION_STRENGHT, magAmounts);

            }

            return new PotionEffectPrompt();

        }
    }

    private class HungerPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetHungerPrompt");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if (input.intValue() < 0) {
                    ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorHungerLevelAtLeastZero"));
                    return new HungerPrompt();
                } else {
                    context.setSessionData(CK.E_HUNGER, (Integer) input.intValue());
                }

            } else {
                context.setSessionData(CK.E_HUNGER, null);
            }

            return new CreateMenuPrompt();

        }
    }

    private class SaturationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetSaturationPrompt");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if (input.intValue() < 0) {
                    ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorSaturationLevelAtLeastZero"));
                    return new SaturationPrompt();
                } else {
                    context.setSessionData(CK.E_SATURATION, (Integer) input.intValue());
                }

            } else {
                context.setSessionData(CK.E_SATURATION, null);
            }

            return new CreateMenuPrompt();

        }
    }

    private class HealthPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetHealthPrompt");

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if (input.intValue() < 0) {
                    ((Player) context.getForWhom()).sendMessage(RED + Lang.get("eventEditorHealthLevelAtLeastZero"));
                    return new HealthPrompt();
                } else {
                    context.setSessionData(CK.E_HEALTH, (Integer) input.intValue());
                }

            } else {
                context.setSessionData(CK.E_HEALTH, null);
            }

            return new CreateMenuPrompt();

        }
    }

    private class TeleportPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetTeleportPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {

                Block block = selectedTeleportLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    context.setSessionData(CK.E_TELEPORT, Quests.getLocationInfo(loc));
                    selectedTeleportLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new TeleportPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData(CK.E_TELEPORT, null);
                selectedTeleportLocations.remove(player.getUniqueId());
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedTeleportLocations.remove(player.getUniqueId());
                return new CreateMenuPrompt();

            } else {
                return new TeleportPrompt();
            }

        }
    }

    private class CommandsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "" + ITALIC + Lang.get("eventEditorCommandsNote");
            return YELLOW + Lang.get("eventEditorSetCommandsPrompt") + "\n" + text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                String[] commands = input.split(",");
                LinkedList<String> cmdList = new LinkedList<String>();
                cmdList.addAll(Arrays.asList(commands));
                context.setSessionData(CK.E_COMMANDS, cmdList);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_COMMANDS, null);
            }

            return new CreateMenuPrompt();

        }
    }
}
