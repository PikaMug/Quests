package me.blackvein.quests;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.blackvein.quests.prompts.ItemStackPrompt;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.citizensnpcs.api.CitizensAPI;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EventFactory implements ConversationAbandonedListener, ColorUtil{

    Quests quests;
    Map<Player, Quest> editSessions = new HashMap<Player, Quest>();
    Map<Player, Block> selectedExplosionLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedEffectLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedMobLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedLightningLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedTeleportLocations = new HashMap<Player, Block>();
    List<String> names = new LinkedList<String>();
    ConversationFactory convoCreator;
    File eventsFile;

    @SuppressWarnings("LeakingThisInConstructor")
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
        selectedExplosionLocations.remove(player);
        selectedEffectLocations.remove(player);
        selectedMobLocations.remove(player);
        selectedLightningLocations.remove(player);
        selectedTeleportLocations.remove(player);

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

            String text =
                    GOLD + "- " + Lang.get("eventEditorTitle") + " -\n"
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
                
                
                if(player.hasPermission("quests.editor.events.create")){
                    context.setSessionData("oldEvent", "");
                    return new EventNamePrompt();
                }else{
                    player.sendMessage(RED + "You do not have permission to create new Events.");
                    return new MenuPrompt();
                }
                
            }else if(input.equalsIgnoreCase("2")){
                
                if(player.hasPermission("quests.editor.events.edit")){
                
                    if(quests.events.isEmpty()){
                        ((Player)context.getForWhom()).sendMessage(YELLOW + Lang.get("eventEditorNoneToEdit"));
                        return new MenuPrompt();
                    }else{
                        return new SelectEditPrompt();
                    }
                
                }else{
                    
                    player.sendMessage(RED + "You do not have permission to edit Events.");
                    return new MenuPrompt();
                    
                }
            }else if(input.equalsIgnoreCase("3")){
                
                if(player.hasPermission("quests.editor.events.delete")){
                    
                    if(quests.events.isEmpty()){
                        ((Player)context.getForWhom()).sendMessage(YELLOW + Lang.get("eventEditorNoneToDelete"));
                        return new MenuPrompt();
                    }else{
                        return new SelectDeletePrompt();
                    }
                
                }else{
                    
                    player.sendMessage(RED + "You do not have permission to delete Events.");
                    return new MenuPrompt();
                    
                }
                
            }else if(input.equalsIgnoreCase("4")){
                ((Player)context.getForWhom()).sendMessage(YELLOW + Lang.get("exited"));
                return Prompt.END_OF_CONVERSATION;
            }

            return null;

        }
    }

    public Prompt returnToMenu() {

        return new CreateMenuPrompt();

    }

    public static void clearData(ConversationContext context) {

        context.setSessionData("oldEvent", null);
        context.setSessionData("evtName", null);
        context.setSessionData("evtMessage", null);
        context.setSessionData("evtClearInv", null);
        context.setSessionData("evtItemIds", null);
        context.setSessionData("evtItemAmounts", null);
        context.setSessionData("evtExplosions", null);
        context.setSessionData("evtEffects", null);
        context.setSessionData("evtEffectLocations", null);
        context.setSessionData("evtStormWorld", null);
        context.setSessionData("evtStormDuration", null);
        context.setSessionData("evtThunderWorld", null);
        context.setSessionData("evtThunderDuration", null);
        context.setSessionData("evtMobTypes", null);
        context.setSessionData("evtMobAmounts", null);
        context.setSessionData("evtMobLocations", null);
        context.setSessionData("evtLightningStrikes", null);
        context.setSessionData("evtPotionTypes", null);
        context.setSessionData("evtPotionDurations", null);
        context.setSessionData("evtPotionMagnitudes", null);
        context.setSessionData("evtHunger", null);
        context.setSessionData("evtSaturation", null);
        context.setSessionData("evtHealth", null);
        context.setSessionData("evtTeleportLocation", null);
        context.setSessionData("evtCommands", null);

    }

    public static void loadData(Event event, ConversationContext context){

        if(event.message != null)
            context.setSessionData("evtMessage", event.message);

        if(event.clearInv == true)
            context.setSessionData("evtClearInv", "Yes");
        else
            context.setSessionData("evtClearInv", "No");

        if(event.items != null && event.items.isEmpty() == false){

            LinkedList<ItemStack> items = new LinkedList<ItemStack>();
            items.addAll(event.items);

            context.setSessionData("evtItems", items);

        }

        if(event.explosions != null && event.explosions.isEmpty() == false){

            LinkedList<String> locs = new LinkedList<String>();

            for(Location loc : event.explosions)
                locs.add(Quests.getLocationInfo(loc));

            context.setSessionData("evtExplosions", locs);

        }

        if(event.effects != null && event.effects.isEmpty() == false){

            LinkedList<String> effs = new LinkedList<String>();
            LinkedList<String> locs = new LinkedList<String>();

            for(Entry e : event.effects.entrySet()){

                effs.add(((Effect) e.getKey()).toString());
                locs.add(Quests.getLocationInfo((Location) e.getValue()));

            }

            context.setSessionData("evtEffects", effs);
            context.setSessionData("evtEffectLocations", locs);

        }

        if(event.stormWorld != null){

            context.setSessionData("evtStormWorld", event.stormWorld.getName());
            context.setSessionData("evtStormDuration", (long)event.stormDuration);

        }

        if(event.thunderWorld != null){

            context.setSessionData("evtThunderWorld", event.thunderWorld.getName());
            context.setSessionData("evtThunderDuration", (long)event.thunderDuration);

        }

        if(event.mobSpawnTypes != null && event.mobSpawnTypes.isEmpty() == false){

            LinkedList<String> types = new LinkedList<String>();
            LinkedList<String> locs = new LinkedList<String>();
            LinkedList<Integer> amounts = new LinkedList<Integer>();

            for(int i = 0; i < event.mobSpawnTypes.size(); i++){

                types.add(Quester.prettyMobString(event.mobSpawnTypes.get(i)));
                locs.add(Quests.getLocationInfo(event.mobSpawnLocs.get(i)));
                amounts.add(event.mobSpawnAmounts.get(i));

            }

            context.setSessionData("evtMobTypes", types);
            context.setSessionData("evtMobAmounts", amounts);
            context.setSessionData("evtMobLocations", locs);

        }

        if(event.lightningStrikes != null && event.lightningStrikes.isEmpty() == false){

            LinkedList<String> locs = new LinkedList<String>();
            for(Location loc : event.lightningStrikes)
                locs.add(Quests.getLocationInfo(loc));
            context.setSessionData("evtLightningStrikes", locs);

        }

        if(event.potionEffects != null && event.potionEffects.isEmpty() == false){

            LinkedList<String> types = new LinkedList<String>();
            LinkedList<Long> durations = new LinkedList<Long>();
            LinkedList<Integer> mags = new LinkedList<Integer>();

            for(PotionEffect pe : event.potionEffects){

                types.add(pe.getType().getName());
                durations.add((long)pe.getDuration());
                mags.add(pe.getAmplifier());

            }

            context.setSessionData("evtPotionTypes", types);
            context.setSessionData("evtPotionDurations", durations);
            context.setSessionData("evtPotionMagnitudes", mags);

        }

        if(event.hunger > -1){

            context.setSessionData("evtHunger", (Integer) event.hunger);

        }

        if(event.saturation > -1){

            context.setSessionData("evtSaturation", (Integer) event.saturation);

        }

        if(event.health > -1){

            context.setSessionData("evtHealth", (Integer) event.health);

        }

        if(event.teleport != null){

            context.setSessionData("evtTeleportLocation", Quests.getLocationInfo(event.teleport));

        }

        if(event.commands != null){

            context.setSessionData("evtCommands", event.commands);

        }

    }

    private class SelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorEdit") + " -\n";

            for(Event evt : quests.events)
                text += AQUA + evt.name + YELLOW + ", ";

            text = text.substring(0, text.length() - 2) + "\n";
            text += YELLOW + Lang.get("eventEditorEnterEventName");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase(Lang.get("cmdCancel")) == false){

                for(Event evt : quests.events){

                    if(evt.name.equalsIgnoreCase(input)){
                        context.setSessionData("oldEvent", evt.name);
                        context.setSessionData("evtName", evt.name);
                        loadData(evt, context);
                        return new CreateMenuPrompt();
                    }

                }

                ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorNotFound"));
                return new SelectEditPrompt();

            }else{
                return new MenuPrompt();
            }

        }

    }

    private class SelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorDelete") + " -\n";

            for(Event evt : quests.events)
                text += AQUA + evt.name + YELLOW + ",";

            text = text.substring(0, text.length() - 1) + "\n";
            text += YELLOW + Lang.get("eventEditorEnterEventName");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase(Lang.get("cmdCancel")) == false){

                LinkedList<String> used = new LinkedList<String>();

                for(Event evt : quests.events){

                    if(evt.name.equalsIgnoreCase(input)){

                        for(Quest quest : quests.getQuests()){

                            for(Stage stage : quest.stages){

                                if(stage.event != null && stage.event.name.equalsIgnoreCase(evt.name)){
                                    used.add(quest.name);
                                    break;
                                }

                            }

                        }

                        if(used.isEmpty()){
                            context.setSessionData("delEvent", evt.name);
                            return new DeletePrompt();
                        }else{
                            ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorEventInUse") + " \"" + PURPLE + evt.name + RED + "\":");
                            for(String s : used){
                                ((Player)context.getForWhom()).sendMessage(RED + "- " + DARKRED + s);
                            }
                            ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorMustModifyQuests"));
                            return new SelectDeletePrompt();
                        }
                    }

                }

                ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorNotFound"));
                return new SelectDeletePrompt();

            }else{
                return new MenuPrompt();
            }

        }

    }

    private class DeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    RED + Lang.get("eventEditorDeletePrompt") + " \"" + GOLD + (String)context.getSessionData("delEvent") + RED + "\"?\n";
                    text += YELLOW + Lang.get("yes") + "/" + Lang.get("no");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase(Lang.get("yes"))){
                deleteEvent(context);
                return new MenuPrompt();
            }else if(input.equalsIgnoreCase(Lang.get("no"))){
                return new MenuPrompt();
            }else {
                return new DeletePrompt();
            }

        }

    }

    private class CreateMenuPrompt extends FixedSetPrompt {

        public CreateMenuPrompt() {

            super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    GOLD + "- " + Lang.get("event") + ": " + AQUA + context.getSessionData("evtName") + GOLD + " -\n";

            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetName") + "\n";

            if (context.getSessionData("evtMessage") == null) {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMessage") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMessage") + "(" + AQUA + "\"" + context.getSessionData("evtMessage") + "\"" + YELLOW + ")\n";
            }

            if (context.getSessionData("evtClearInv") == null) {
                context.setSessionData("evtClearInv", "No");
            }

            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorClearInv") + ": " + AQUA + context.getSessionData("evtClearInv") + "\n";

            if (context.getSessionData("evtItems") == null) {
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("eventEditorSetItems") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("eventEditorSetItems") + "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData("evtItems");

                for (ItemStack is : items) {

                    text += GRAY + "    - " + ItemUtil.getString(is) + "\n";

                }

            }

            if (context.getSessionData("evtExplosions") == null) {
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("eventEditorSetExplosions") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("eventEditorSetExplosions") + "\n";
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtExplosions");

                for (String loc : locations) {

                    text += GRAY + "    - " + AQUA + loc + "\n";

                }

            }

            if (context.getSessionData("evtEffects") == null) {
                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("eventEditorSetEffects") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("eventEditorSetEffects") + "\n";
                LinkedList<String> effects = (LinkedList<String>) context.getSessionData("evtEffects");
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtEffectLocations");

                for (String effect : effects) {

                    text += GRAY + "    - " + AQUA + effect + GRAY + " at " + DARKAQUA + locations.get(effects.indexOf(effect)) + "\n";

                }

            }

            if (context.getSessionData("evtStormWorld") == null) {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("eventEditorSetStorm") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("eventEditorSetStorm") + " (" + AQUA + (String) context.getSessionData("evtStormWorld") + YELLOW + " -> " + DARKAQUA + Quests.getTime((Long) context.getSessionData("evtStormDuration")) + YELLOW + ")\n";
            }

            if (context.getSessionData("evtThunderWorld") == null) {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("eventEditorSetThunder") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - " + Lang.get("eventEditorSetThunder") + " (" + AQUA + (String) context.getSessionData("evtThunderWorld") + YELLOW + " -> " + DARKAQUA + Quests.getTime((Long) context.getSessionData("evtThunderDuration")) + YELLOW + ")\n";
            }


            if (context.getSessionData("evtMobTypes") == null) {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobSpawns") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                LinkedList<String> types = (LinkedList<String>) context.getSessionData("evtMobTypes");
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData("evtMobAmounts");
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtMobLocations");

                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobSpawns") + "\n";

                for (String s : types) {
                    int amt = amounts.get(types.indexOf(s));
                    String loc = locations.get(types.indexOf(s));
                    text += GRAY + "    - " + AQUA + s + GRAY + " x " + DARKAQUA + amt + GRAY + " -> " + GREEN + loc + "\n";
                }
            }

            if (context.getSessionData("evtLightningStrikes") == null) {
                text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - " + Lang.get("eventEditorSetLightning") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - " + Lang.get("eventEditorSetLightning") + "\n";
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtLightningStrikes");

                for (String loc : locations) {

                    text += GRAY + "    - " + AQUA + loc + "\n";

                }

            }

            if (context.getSessionData("evtPotionTypes") == null) {
                text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + "\n";
                LinkedList<String> types = (LinkedList<String>) context.getSessionData("evtPotionTypes");
                LinkedList<Long> durations = (LinkedList<Long>) context.getSessionData("evtPotionDurations");
                LinkedList<Integer> mags = (LinkedList<Integer>) context.getSessionData("evtPotionMagnitudes");
                int index = -1;

                for (String type : types) {

                    index++;
                    text += GRAY + "    - " + AQUA + type + PURPLE + " " + Quests.getNumeral(mags.get(index)) + GRAY + " -> " + DARKAQUA + Quests.getTime(durations.get(index)*50L) + "\n";

                }

            }

            if(context.getSessionData("evtHunger") == null) {
                text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHunger") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHunger") + AQUA + " (" + (Integer)context.getSessionData("evtHunger") + ")\n";

            }

            if(context.getSessionData("evtSaturation") == null) {
                text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - " + Lang.get("eventEditorSetSaturation") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - " + Lang.get("eventEditorSetSaturation") + AQUA + " (" + (Integer)context.getSessionData("evtSaturation") + ")\n";

            }

            if(context.getSessionData("evtHealth") == null) {
                text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHealth") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - " + Lang.get("eventEditorSetHealth") + AQUA + " (" + (Integer)context.getSessionData("evtHealth") + ")\n";

            }

            if(context.getSessionData("evtTeleportLocation") == null) {
                text += BLUE + "" + BOLD + "15" + RESET + YELLOW + " - " + Lang.get("eventEditorSetTeleport") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "15" + RESET + YELLOW + " - " + Lang.get("eventEditorSetTeleport") + AQUA + " (" + (String)context.getSessionData("evtTeleportLocation") + ")\n";

            }

            if(context.getSessionData("evtCommands") == null) {
                text += BLUE + "" + BOLD + "16" + RESET + YELLOW + " - " + Lang.get("eventEditorSetCommands") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "16" + RESET + YELLOW + " - " + Lang.get("eventEditorSetCommands") + "\n";
                for(String s : (LinkedList<String>)context.getSessionData("evtCommands"))
                    text += GRAY + "    - " + AQUA + s + "\n";

            }

            text += GREEN + "" + BOLD + "17" + RESET + YELLOW + " - " + Lang.get("done") + "\n";
            text += RED + "" + BOLD + "18" + RESET + YELLOW + " - " + Lang.get("quit");

            return text;

        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {

                return new SetNamePrompt();

            } else if (input.equalsIgnoreCase("2")) {

                return new MessagePrompt();

            } else if (input.equalsIgnoreCase("3")) {

                String s = (String) context.getSessionData("evtClearInv");
                if (s.equalsIgnoreCase("Yes")) {
                    context.setSessionData("evtClearInv", "No");
                } else {
                    context.setSessionData("evtClearInv", "Yes");
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("4")) {

                return new ItemListPrompt();

            } else if (input.equalsIgnoreCase("5")) {

                selectedExplosionLocations.put((Player) context.getForWhom(), null);
                return new ExplosionPrompt();

            } else if (input.equalsIgnoreCase("6")) {

                return new EffectListPrompt();

            } else if (input.equalsIgnoreCase("7")) {

                return new StormPrompt();

            } else if (input.equalsIgnoreCase("8")) {

                return new ThunderPrompt();

            } else if (input.equalsIgnoreCase("9")) {

                return new MobPrompt();

            } else if (input.equalsIgnoreCase("10")) {

                selectedLightningLocations.put((Player) context.getForWhom(), null);
                return new LightningPrompt();

            } else if (input.equalsIgnoreCase("11")) {

                return new PotionEffectPrompt();

            } else if (input.equalsIgnoreCase("12")) {

                return new HungerPrompt();

            } else if (input.equalsIgnoreCase("13")) {

                return new SaturationPrompt();

            } else if (input.equalsIgnoreCase("14")) {

                return new HealthPrompt();

            } else if (input.equalsIgnoreCase("15")){

                selectedTeleportLocations.put((Player) context.getForWhom(), null);
                return new TeleportPrompt();

            } else if (input.equalsIgnoreCase("16")) {

                return new CommandsPrompt();

            } else if (input.equalsIgnoreCase("17")) {

                if(context.getSessionData("oldEvent") != null){
                    return new FinishPrompt((String)context.getSessionData("oldEvent"));
                }else{
                    return new FinishPrompt(null);
                }

            } else if (input.equalsIgnoreCase("18")) {

                return new QuitPrompt();

            }

            return null;

        }
    }

    private class QuitPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    GREEN + Lang.get("eventEditorQuitWithoutSaving") + "\n";
                    text += YELLOW + Lang.get("yes") + "/" + Lang.get("no");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase(Lang.get("yes"))){
                clearData(context);
                return new MenuPrompt();
            }else if(input.equalsIgnoreCase(Lang.get("no"))){
                return new CreateMenuPrompt();
            }else{
                ((Player) context.getForWhom()).sendMessage(RED + Lang.get("invalidOption"));
                return new QuitPrompt();
            }

        }

    }

    private class FinishPrompt extends StringPrompt {

        String modName = null;
        LinkedList<String> modified = new LinkedList<String>();

        public FinishPrompt(String modifiedName){

            if(modifiedName != null){

                modName = modifiedName;
                for(Quest q : quests.getQuests()){

                    for(Stage s : q.stages){

                        if(s.event != null && s.event.name != null){

                            if(s.event.name.equalsIgnoreCase(modifiedName)){
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

            String text =
                    RED + Lang.get("eventEditorFinishAndSave") + " \"" + GOLD + (String)context.getSessionData("evtName") + RED + "\"?\n";
                    if(modified.isEmpty() == false){
                        text += RED + Lang.get("eventEditorModifiedNote") + "\n";
                        for(String s : modified)
                            text += GRAY + "    - " + DARKRED + s + "\n";
                        text += RED + Lang.get("eventEditorForcedToQuit") + "\n";
                    }
                    text += YELLOW + Lang.get("yes") + "/" + Lang.get("no");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase(Lang.get("yes"))){
                saveEvent(context);
                return new MenuPrompt();
            }else if(input.equalsIgnoreCase(Lang.get("no"))){
                return new CreateMenuPrompt();
            }else{
                ((Player) context.getForWhom()).sendMessage(RED + Lang.get("invalidOption"));
                return new FinishPrompt(modName);
            }

        }

    }

    //Convenience methods to reduce typecasting
        private static String getCString(ConversationContext context, String path){
            return (String)context.getSessionData(path);
        }

        private static LinkedList<String> getCStringList(ConversationContext context, String path){
            return (LinkedList<String>)context.getSessionData(path);
        }

        private static Integer getCInt(ConversationContext context, String path){
            return (Integer)context.getSessionData(path);
        }

        private static LinkedList<Integer> getCIntList(ConversationContext context, String path){
            return (LinkedList<Integer>)context.getSessionData(path);
        }

        private static Boolean getCBoolean(ConversationContext context, String path){
            return (Boolean)context.getSessionData(path);
        }

        private static LinkedList<Boolean> getCBooleanList(ConversationContext context, String path){
            return (LinkedList<Boolean>)context.getSessionData(path);
        }

        private static Long getCLong(ConversationContext context, String path){
            return (Long)context.getSessionData(path);
        }

        private static LinkedList<Long> getCLongList(ConversationContext context, String path){
            return (LinkedList<Long>)context.getSessionData(path);
        }
    //

    private void deleteEvent(ConversationContext context) {

        YamlConfiguration data = new YamlConfiguration();

        try{
            eventsFile = new File(quests.getDataFolder(), "events.yml");
            data.load(eventsFile);
        }catch(Exception e){
            e.printStackTrace();
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
            return;
        }

        String event = (String)context.getSessionData("delEvent");
        ConfigurationSection sec = data.getConfigurationSection("events");
        sec.set(event, null);

        try{
            data.save(eventsFile);
        }catch (Exception e){
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
            return;
        }


        quests.reloadQuests();

        ((Player)context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorDeleted"));

        for(Quester q : quests.questers.values())
            q.checkQuest();

        clearData(context);

    }

    private void saveEvent(ConversationContext context) {

        YamlConfiguration data = new YamlConfiguration();

        try{
            eventsFile = new File(quests.getDataFolder(), "events.yml");
            data.load(eventsFile);
        }catch(Exception e){
            e.printStackTrace();
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorReadingFile"));
            return;
        }

        if(((String) context.getSessionData("oldEvent")).isEmpty() == false){
            data.set("events." + (String)context.getSessionData("oldEvent"), null);
            quests.events.remove(quests.getEvent((String)context.getSessionData("oldEvent")));
        }

        ConfigurationSection section = data.createSection("events." + (String)context.getSessionData("evtName"));
        names.remove((String)context.getSessionData("evtName"));

        if (context.getSessionData("evtMessage") != null) {
            section.set("message", getCString(context, "evtMessage"));
        }

        if (context.getSessionData("evtClearInv") != null) {
            String s = getCString(context, "evtClearInv");
            if(s.equalsIgnoreCase("Yes"))
                context.setSessionData("clear-inventory", "true");
        }

        if (context.getSessionData("evtItems") != null) {

            LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData("evtItems");
            LinkedList<String> lines = new LinkedList<String>();
            
            for(ItemStack is : items)
                lines.add(ItemUtil.serialize(is));
            
            section.set("items", lines);

        }

        if (context.getSessionData("evtExplosions") != null) {

            LinkedList<String> locations = getCStringList(context, "evtExplosions");
            section.set("explosions", locations);

        }

        if (context.getSessionData("evtEffects") != null) {

            LinkedList<String> effects = getCStringList(context, "evtEffects");
            LinkedList<String> locations = getCStringList(context, "evtEffectLocations");

            section.set("effects", effects);
            section.set("effect-locations", locations);

        }

        if (context.getSessionData("evtStormWorld") != null) {

            String world = getCString(context, "evtStormWorld");
            Long duration = getCLong(context, "evtStormDuration");

            section.set("storm-world", world);
            section.set("storm-duration", duration/50L);

        }

        if (context.getSessionData("evtThunderWorld") != null) {

            String world = getCString(context, "evtThunderWorld");
            Long duration = getCLong(context, "evtThunderDuration");

            section.set("thunder-world", world);
            section.set("thunder-duration", duration/50L);

        }

        if (context.getSessionData("evtMobTypes") != null) {

            LinkedList<String> types = getCStringList(context, "evtMobTypes");
            LinkedList<Integer> amounts = getCIntList(context, "evtMobAmounts");
            LinkedList<String> locations = getCStringList(context, "evtMobLocations");

            section.set("mob-spawn-types", types);
            section.set("mob-spawn-amounts", amounts);
            section.set("mob-spawn-locations", locations);

        }

        if (context.getSessionData("evtLightningStrikes") != null) {

            LinkedList<String> locations = getCStringList(context, "evtLightningStrikes");
            section.set("lightning-strikes", locations);

        }

        if (context.getSessionData("evtCommands") != null) {

            LinkedList<String> commands = getCStringList(context, "evtCommands");
            section.set("commands", commands);

        }

        if (context.getSessionData("evtPotionTypes") != null) {

            LinkedList<String> types = getCStringList(context, "evtPotionTypes");
            LinkedList<Long> durations = getCLongList(context, "evtPotionDurations");
            LinkedList<Integer> mags = getCIntList(context, "evtPotionMagnitudes");

            section.set("potion-effect-types", types);
            section.set("potion-effect-durations", durations);
            section.set("potion-effect-amplifiers", mags);

        }

        if (context.getSessionData("evtHunger") != null) {

            Integer i = getCInt(context, "evtHunger");
            section.set("hunger", i);

        }

        if (context.getSessionData("evtSaturation") != null) {

            Integer i = getCInt(context, "evtSaturation");
            section.set("saturation", i);

        }

        if (context.getSessionData("evtHealth") != null) {

            Integer i = getCInt(context, "evtHealth");
            section.set("health", i);

        }

        if (context.getSessionData("evtTeleportLocation") != null) {

            section.set("teleport-location", getCString(context, "evtTeleportLocation"));

        }

        try{
            data.save(eventsFile);
        }catch (Exception e){
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + Lang.get("eventEditorErrorSaving"));
            return;
        }


        quests.reloadQuests();

        ((Player)context.getForWhom()).sendMessage(ChatColor.YELLOW + Lang.get("eventEditorSaved"));

        for(Quester q : quests.questers.values())
            q.checkQuest();

        clearData(context);

    }

    private class EventNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    AQUA + Lang.get("eventEditorCreate") + GOLD + " - " + Lang.get("eventEditorEnterEventName");

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

                context.setSessionData("evtName", input);
                names.add(input);
                return new CreateMenuPrompt();

            } else {

                return new MenuPrompt();

            }

        }
    }

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

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedExplosionLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData("evtExplosions") != null) {
                        locs = (LinkedList<String>) context.getSessionData("evtExplosions");
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData("evtExplosions", locs);
                    selectedExplosionLocations.remove(player);

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new ExplosionPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData("evtExplosions", null);
                selectedExplosionLocations.remove(player);
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedExplosionLocations.remove(player);
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

                names.remove((String) context.getSessionData("evtName"));
                context.setSessionData("evtName", input);
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

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase("cmdNone") == false) {
                context.setSessionData("evtMessage", input);
            } else if (input.equalsIgnoreCase(Lang.get("cmdNone"))) {
                context.setSessionData("evtMessage", null);
            }

            return new CreateMenuPrompt();

        }
    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt(){

            super("1", "2", "3");

        }

        @Override
        public String getPromptText(ConversationContext context){

            // Check/add newly made item
            if(context.getSessionData("newItem") != null){
                if(context.getSessionData("evtItems") != null){
                    List<ItemStack> items = getItems(context);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData("evtItems", items);
                }else{
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData("evtItems", itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }
            
            String text = GOLD + "- Give Items -\n";
            if(context.getSessionData("evtItems") == null){
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Done";
            }else{

                for(ItemStack is : getItems(context)){

                    text += GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";

                }
                
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";

                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("1")){
                return new ItemStackPrompt(ItemListPrompt.this);
            }else if(input.equalsIgnoreCase("2")){
                context.getForWhom().sendRawMessage(YELLOW + "Event Items cleared.");
                context.setSessionData("evtItems", null);
                return new ItemListPrompt();
            }else if(input.equalsIgnoreCase("3")){
                return new CreateMenuPrompt();
            }
            return null;

        }

        private List<ItemStack> getItems(ConversationContext context){
            return (List<ItemStack>) context.getSessionData("evtItems");
        }

    }

    private class EffectListPrompt extends FixedSetPrompt {

        public EffectListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorEffects") + " -\n";
            if (context.getSessionData("evtEffects") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddEffect") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - Add effect location (" + Lang.get("eventEditorNoEffects") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorAddEffect") + "\n";
                for (String s : getEffects(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData("evtEffectLocations") == null) {
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
                if (context.getSessionData("evtEffects") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustAddEffects"));
                    return new EffectListPrompt();
                } else {
                    selectedEffectLocations.put((Player) context.getForWhom(), null);
                    return new EffectLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorEffectsCleared"));
                context.setSessionData("evtEffects", null);
                context.setSessionData("evtEffectLocations", null);
                return new EffectListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData("evtEffects") != null) {
                    one = getEffects(context).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData("evtEffectLocations") != null) {
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

        private List<String> getEffects(ConversationContext context) {
            return (List<String>) context.getSessionData("evtEffects");
        }

        private List<String> getEffectLocations(ConversationContext context) {
            return (List<String>) context.getSessionData("evtEffectLocations");
        }
    }

    private class EffectLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorEffectLocationPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedEffectLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData("evtEffectLocations") != null) {
                        locs = (LinkedList<String>) context.getSessionData("evtEffectLocations");
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData("evtEffectLocations", locs);
                    selectedEffectLocations.remove(player);

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new EffectLocationPrompt();
                }

                return new EffectListPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedEffectLocations.remove(player);
                return new EffectListPrompt();

            } else {
                return new EffectLocationPrompt();
            }

        }
    }

    private class EffectPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String effects = PINK + "- Effects - \n";
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

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (Quests.getEffect(input.toUpperCase()) != null) {

                    LinkedList<String> effects;
                    if (context.getSessionData("evtEffects") != null) {
                        effects = (LinkedList<String>) context.getSessionData("evtEffects");
                    } else {
                        effects = new LinkedList<String>();
                    }

                    effects.add(input.toUpperCase());
                    context.setSessionData("evtEffects", effects);
                    selectedEffectLocations.remove(player);
                    return new EffectListPrompt();

                } else {
                    player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorInvalidEffect"));
                    return new EffectPrompt();
                }

            } else {

                selectedEffectLocations.remove(player);
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

            String text = GOLD + "- " + Lang.get("eventEditorStorm") + " -\n";
            if (context.getSessionData("evtStormWorld") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + AQUA + ((String) context.getSessionData("evtStormWorld")) + YELLOW + ")\n";

                if (context.getSessionData("evtStormDuration") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    Long dur = (Long) context.getSessionData("evtStormDuration");

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
                if (context.getSessionData("evtStormWorld") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorSetWorldFirst"));
                    return new StormPrompt();
                } else {
                    return new StormDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorStormCleared"));
                context.setSessionData("evtStormWorld", null);
                context.setSessionData("evtStormDuration", null);
                return new StormPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                if (context.getSessionData("evtStormWorld") != null && context.getSessionData("evtStormDuration") == null) {
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

            String effects = PINK + "- " + Lang.get("worlds") + " - \n" + PURPLE;
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

                    context.setSessionData("evtStormWorld", quests.getServer().getWorld(input).getName());

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

            context.setSessionData("evtStormDuration", input.longValue());
            return new StormPrompt();

        }
    }

    private class ThunderPrompt extends FixedSetPrompt {

        public ThunderPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorThunder") + " -\n";

            if (context.getSessionData("evtThunderWorld") == null) {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetDuration") + " " + Lang.get("eventEditorNoWorld") + "\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetWorld") + " (" + AQUA + ((String) context.getSessionData("evtThunderWorld")) + YELLOW + ")\n";

                if (context.getSessionData("evtThunderDuration") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetDuration") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    Long dur = (Long) context.getSessionData("evtThunderDuration");
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
                if (context.getSessionData("evtThunderWorld") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorSetWorldFirst"));
                    return new ThunderPrompt();
                } else {
                    return new ThunderDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorThunderCleared"));
                context.setSessionData("evtThunderWorld", null);
                context.setSessionData("evtThunderDuration", null);
                return new ThunderPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                if (context.getSessionData("evtThunderWorld") != null && context.getSessionData("evtThunderDuration") == null) {
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

            String effects = PINK + "- Worlds - \n" + PURPLE;
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

                    context.setSessionData("evtThunderWorld", quests.getServer().getWorld(input).getName());

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
                context.setSessionData("evtThunderDuration", input.longValue());
            }

            return new ThunderPrompt();

        }
    }

    private class MobPrompt extends FixedSetPrompt {

        public MobPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorMobSpawns") + " -\n";
            if (context.getSessionData("evtMobTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetMobAmounts") + " " + Lang.get("eventEditorNoTypesSet") + "\n";
                text += GRAY + "3 - " + Lang.get("eventEditorAddSpawnLocation") + " " + Lang.get("eventEditorNoTypesSet") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobTypes") + "\n";
                for (String s : (LinkedList<String>) context.getSessionData("evtMobTypes")) {
                    text += GRAY + "    - " + AQUA + s + "\n";
                }

                if (context.getSessionData("evtMobAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobAmounts") + " (" + Lang.get("noneSet") + ")\n";
                    text += GRAY + "3 - " + Lang.get("eventEditorAddSpawnLocation") + Lang.get("eventEditorNoAmountsSet") + "\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetMobAmounts");
                    for (int i : (LinkedList<Integer>) context.getSessionData("evtMobAmounts")) {
                        text += GRAY + "    - " + DARKAQUA + i + "\n";
                    }


                    if (context.getSessionData("evtMobLocations") == null) {
                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorAddSpawnLocation") + " (" + Lang.get("noneSet") + ")\n";
                    } else {

                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorAddSpawnLocation") + "\n";
                        for (String s : (LinkedList<String>) context.getSessionData("evtMobLocations")) {
                            text += GRAY + "    - " + GREEN + s + "\n";
                        }

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new MobTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtMobTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetMobTypesFirst"));
                    return new MobPrompt();
                } else {
                    return new MobAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData("evtMobTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetMobTypesAndAmountsFirst"));
                    return new MobPrompt();
                } else if (context.getSessionData("evtMobAmounts") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetMobAmountsFirst"));
                    return new MobPrompt();
                } else {
                    selectedMobLocations.put((Player) context.getForWhom(), null);
                    return new MobLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorMobSpawnsCleared"));
                context.setSessionData("evtMobTypes", null);
                context.setSessionData("evtMobAmounts", null);
                context.setSessionData("evtMobLocations", null);
                return new MobPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData("evtMobTypes") != null) {
                    one = ((List<String>) context.getSessionData("evtMobTypes")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData("evtMobAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData("evtMobAmounts")).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData("evtMobLocations") != null) {
                    three = ((List<String>) context.getSessionData("evtMobLocations")).size();
                } else {
                    three = 0;
                }

                if (one == two && two == three) {
                    return new CreateMenuPrompt();
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorListSizeMismatch"));
                    return new MobPrompt();
                }

            }
            return null;

        }
    }

    private class MobTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String mobs = PINK + "- " + Lang.get("mobs") + " - \n";
            mobs += PURPLE + "Bat, ";
            mobs += PURPLE + "Blaze, ";
            mobs += PURPLE + "CaveSpider, ";
            mobs += PURPLE + "Chicken, ";
            mobs += PURPLE + "Cow, ";
            mobs += PURPLE + "Creeper, ";
            mobs += PURPLE + "Enderman, ";
            mobs += PURPLE + "EnderDragon, ";
            mobs += PURPLE + "Ghast, ";
            mobs += PURPLE + "Giant, ";
            mobs += PURPLE + "IronGolem, ";
            mobs += PURPLE + "MagmaCube, ";
            mobs += PURPLE + "MushroomCow, ";
            mobs += PURPLE + "Ocelot, ";
            mobs += PURPLE + "Pig, ";
            mobs += PURPLE + "PigZombie, ";
            mobs += PURPLE + "Sheep, ";
            mobs += PURPLE + "Silverfish, ";
            mobs += PURPLE + "Skeleton, ";
            mobs += PURPLE + "Slime, ";
            mobs += PURPLE + "Snowman, ";
            mobs += PURPLE + "Spider, ";
            mobs += PURPLE + "Squid, ";
            mobs += PURPLE + "Villager, ";
            mobs += PURPLE + "Witch, ";
            mobs += PURPLE + "Wither, ";
            mobs += PURPLE + "Wolf, ";
            mobs += PURPLE + "Zombie\n";

            return mobs + YELLOW + Lang.get("eventEditorSetMobTypesPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getMobType(s) != null) {

                        mobTypes.add(Quester.prettyMobString(Quests.getMobType(s)));
                        context.setSessionData("evtMobTypes", mobTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorInvalidMob"));
                        return new MobTypesPrompt();
                    }

                }

            }

            return new MobPrompt();

        }
    }

    private class MobAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorSetMobAmountsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorNotGreaterThanZero"));
                            return new MobAmountsPrompt();
                        }

                        mobAmounts.add(i);


                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("eventEditorNotANumber"));
                        return new MobAmountsPrompt();
                    }

                }

                context.setSessionData("evtMobAmounts", mobAmounts);

            }

            return new MobPrompt();

        }
    }

    private class MobLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorMobLocationPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedMobLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData("evtMobLocations") != null) {
                        locs = (LinkedList<String>) context.getSessionData("evtMobLocations");
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData("evtMobLocations", locs);
                    selectedMobLocations.remove(player);

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new MobLocationPrompt();
                }

                return new MobPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedMobLocations.remove(player);
                return new MobPrompt();

            } else {
                return new MobLocationPrompt();
            }

        }
    }

    private class LightningPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("eventEditorLightningPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = selectedLightningLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData("evtLightningStrikes") != null) {
                        locs = (LinkedList<String>) context.getSessionData("evtLightningStrikes");
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData("evtLightningStrikes", locs);
                    selectedLightningLocations.remove(player);

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new LightningPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData("evtLightningStrikes", null);
                selectedLightningLocations.remove(player);
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedLightningLocations.remove(player);
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

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("eventEditorPotionEffects") + " -\n";
            if (context.getSessionData("evtPotionTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("eventEditorSetPotionDurations") + " " + Lang.get("eventEditorNoTypesSet") + "\n";
                text += GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("eventEditorNoTypesSet") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionEffects") + "\n";
                for (String s : (LinkedList<String>) context.getSessionData("evtPotionTypes")) {
                    text += GRAY + "    - " + AQUA + s + "\n";
                }

                if (context.getSessionData("evtPotionDurations") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionDurations") + " (" + Lang.get("noneSet") + ")\n";
                    text += GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("eventEditorNoDurationsSet") + "\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("eventEditorNoDurationsSet") + "\n";
                    for (Long l : (LinkedList<Long>) context.getSessionData("evtPotionDurations")) {
                        text += GRAY + "    - " + DARKAQUA + Quests.getTime(l*50L) + "\n";
                    }

                    if (context.getSessionData("evtPotionMagnitudes") == null) {
                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionMagnitudes") + " (" + Lang.get("noneSet") + ")\n";
                    } else {

                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("eventEditorSetPotionMagnitudes") + "\n";
                        for (int i : (LinkedList<Integer>) context.getSessionData("evtPotionMagnitudes")) {
                            text += GRAY + "    - " + PURPLE + i + "\n";
                        }

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new PotionTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtPotionTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetPotionTypesFirst"));
                    return new PotionEffectPrompt();
                } else {
                    return new PotionDurationsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData("evtPotionTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetPotionTypesAndDurationsFirst"));
                    return new PotionEffectPrompt();
                } else if (context.getSessionData("evtPotionDurations") == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("eventEditorMustSetPotionDurationsFirst"));
                    return new PotionEffectPrompt();
                } else {
                    return new PotionMagnitudesPrompt();
                }

            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("eventEditorPotionsCleared"));
                context.setSessionData("evtPotionTypes", null);
                context.setSessionData("evtPotionDurations", null);
                context.setSessionData("evtPotionMagnitudes", null);
                return new PotionEffectPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData("evtPotionTypes") != null) {
                    one = ((List<String>) context.getSessionData("evtPotionTypes")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData("evtPotionDurations") != null) {
                    two = ((List<Long>) context.getSessionData("evtPotionDurations")).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData("evtPotionMagnitudes") != null) {
                    three = ((List<Integer>) context.getSessionData("evtPotionMagnitudes")).size();
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

            String effs = PINK + "- " + Lang.get("eventEditorPotionEffects") + " - \n";
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

                        context.setSessionData("evtPotionTypes", effTypes);

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

                        long l = Long.parseLong(s);

                        if (l < 1000) {
                            player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotGreaterThanOneSecond"));
                            return new PotionDurationsPrompt();
                        }

                        effDurations.add(l / 50L);


                    } catch (Exception e) {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotANumber"));
                        return new PotionDurationsPrompt();
                    }

                }

                context.setSessionData("evtPotionDurations", effDurations);

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


                    } catch (Exception e) {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("eventEditorNotANumber"));
                        return new PotionMagnitudesPrompt();
                    }

                }

                context.setSessionData("evtPotionMagnitudes", magAmounts);

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

                if(input.intValue() < 0){
                    ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorHungerLevelAtLeastZero"));
                    return new HungerPrompt();
                }else{
                    context.setSessionData("evtHunger", (Integer)input.intValue());
                }

            }else{
                context.setSessionData("evtHunger", null);
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

                if(input.intValue() < 0){
                    ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorSaturationLevelAtLeastZero"));
                    return new SaturationPrompt();
                }else{
                    context.setSessionData("evtSaturation", (Integer)input.intValue());
                }

            }else{
                context.setSessionData("evtSaturation", null);
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

                if(input.intValue() < 0){
                    ((Player)context.getForWhom()).sendMessage(RED + Lang.get("eventEditorHealthLevelAtLeastZero"));
                    return new HealthPrompt();
                }else{
                    context.setSessionData("evtHealth", (Integer)input.intValue());
                }

            }else{
                context.setSessionData("evtHealth", null);
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

                Block block = selectedTeleportLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    context.setSessionData("evtTeleportLocation", Quests.getLocationInfo(loc));
                    selectedTeleportLocations.remove(player);

                } else {
                    player.sendMessage(RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new TeleportPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData("evtTeleportLocation", null);
                selectedTeleportLocations.remove(player);
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                selectedTeleportLocations.remove(player);
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

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                String[] commands = input.split(",");
                LinkedList<String> cmdList = new LinkedList<String>();
                cmdList.addAll(Arrays.asList(commands));
                context.setSessionData("evtCommands", cmdList);

            }else if(input.equalsIgnoreCase(Lang.get("cmdClear")))
                context.setSessionData("evtCommands", null);

            return new CreateMenuPrompt();

        }
    }
}
