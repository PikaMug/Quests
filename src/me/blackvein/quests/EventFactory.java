package me.blackvein.quests;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EventFactory implements ConversationAbandonedListener {

    Quests quests;
    Map<Player, Quest> editSessions = new HashMap<Player, Quest>();
    Map<Player, Block> selectedExplosionLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedEffectLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedMobLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedLightningLocations = new HashMap<Player, Block>();
    Map<Player, Block> selectedTeleportLocations = new HashMap<Player, Block>();
    List<String> names = new LinkedList<String>();
    ConversationFactory convoCreator;
    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor DARKAQUA = ChatColor.DARK_AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor GRAY = ChatColor.GRAY;
    static final ChatColor PINK = ChatColor.LIGHT_PURPLE;
    static final ChatColor PURPLE = ChatColor.DARK_PURPLE;
    static final ChatColor GREEN = ChatColor.GREEN;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor RESET = ChatColor.RESET;
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
                    GOLD + "- Event Editor -\n"
                    + BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Create an Event\n"
                    + BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Edit an Event\n"
                    + BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Delete an Event\n"
                    + GREEN + "" + BOLD + "4" + RESET + YELLOW + " - Exit";

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                context.setSessionData("oldEvent", "");
                return new EventNamePrompt();
            }else if(input.equalsIgnoreCase("2")){
                if(quests.events.isEmpty()){
                    ((Player)context.getForWhom()).sendMessage(YELLOW + "No Events currently exist to be edited!");
                    return new MenuPrompt();
                }else{
                    return new SelectEditPrompt();
                }
            }else if(input.equalsIgnoreCase("3")){
                if(quests.events.isEmpty()){
                    ((Player)context.getForWhom()).sendMessage(YELLOW + "No Events currently exist to be deleted!");
                    return new MenuPrompt();
                }else{
                    return new SelectDeletePrompt();
                }
            }else if(input.equalsIgnoreCase("4")){
                ((Player)context.getForWhom()).sendMessage(YELLOW + "Exited.");
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

    }

    public static void loadData(Event event, ConversationContext context){

        if(event.message != null)
            context.setSessionData("evtMessage", event.message);

        if(event.clearInv == true)
            context.setSessionData("evtClearInv", "Yes");
        else
            context.setSessionData("evtClearInv", "No");

        if(event.items != null && event.items.isEmpty() == false){

            LinkedList<Integer> ids = new LinkedList<Integer>();
            LinkedList<Integer> amounts = new LinkedList<Integer>();
            for(Entry e : event.items.entrySet()){
                ids.add(((Material)e.getKey()).getId());
                amounts.add((Integer)e.getValue());
            }

            context.setSessionData("evtItemIds", ids);
            context.setSessionData("evtItemAmounts", amounts);

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

    }

    private class SelectEditPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Edit an Event -\n";

            for(Event evt : quests.events)
                text += AQUA + evt.name + YELLOW + ",";

            text = text.substring(0, text.length() - 1) + "\n";
            text += YELLOW + "Enter an Event name, or \"cancel\" to return.";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase("cancel") == false){

                for(Event evt : quests.events){

                    if(evt.name.equalsIgnoreCase(input)){
                        context.setSessionData("oldEvent", evt.name);
                        context.setSessionData("evtName", evt.name);
                        loadData(evt, context);
                        return new CreateMenuPrompt();
                    }

                }

                ((Player)context.getForWhom()).sendMessage(RED + "Event not found!");
                return new SelectEditPrompt();

            }else{
                return new MenuPrompt();
            }

        }

    }

    private class SelectDeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Delete an Event -\n";

            for(Event evt : quests.events)
                text += AQUA + evt.name + YELLOW + ",";

            text = text.substring(0, text.length() - 1) + "\n";
            text += YELLOW + "Enter an Event name, or \"cancel\" to return.";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase("cancel") == false){

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
                            ((Player)context.getForWhom()).sendMessage(RED + "The following Quests use the Event \"" + PURPLE + evt.name + RED + "\"");
                            for(String s : used){
                                ((Player)context.getForWhom()).sendMessage(RED + "- " + DARKRED + s);
                            }
                            ((Player)context.getForWhom()).sendMessage(RED + "You must modify these Quests so that they do not use the Event before deleting it.");
                            return new SelectDeletePrompt();
                        }
                    }

                }

                ((Player)context.getForWhom()).sendMessage(RED + "Event not found!");
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
                    RED + "Are you sure you want to delete the Event \"" + GOLD + (String)context.getSessionData("delEvent") + RED + "\"?\n";
                    text += YELLOW + "Yes/No";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase("Yes")){
                deleteEvent(context);
                return new MenuPrompt();
            }else if(input.equalsIgnoreCase("No")){
                return new MenuPrompt();
            }else {
                return new DeletePrompt();
            }

        }

    }

    private class CreateMenuPrompt extends FixedSetPrompt {

        public CreateMenuPrompt() {

            super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    GOLD + "- Event: " + AQUA + context.getSessionData("evtName") + GOLD + " -\n";

            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set name\n";

            if (context.getSessionData("evtMessage") == null) {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set message " + GRAY + "(None set)\n";
            } else {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set message (" + AQUA + "\"" + context.getSessionData("evtMessage") + "\"" + YELLOW + ")\n";
            }

            if (context.getSessionData("evtClearInv") == null) {
                context.setSessionData("evtClearInv", "No");
            }

            text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear player inventory: " + AQUA + context.getSessionData("evtClearInv") + "\n";

            if (context.getSessionData("evtItemIds") == null) {
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set items " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set items\n";
                LinkedList<Integer> ids = (LinkedList<Integer>) context.getSessionData("evtItemIds");
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData("evtItemAmounts");

                for (int i : ids) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + YELLOW + " x " + AQUA + amounts.get(ids.indexOf(i)) + "\n";

                }

            }

            if (context.getSessionData("evtExplosions") == null) {
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set explosion locations " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set explosion locations\n";
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtExplosions");

                for (String loc : locations) {

                    text += GRAY + "    - " + AQUA + loc + "\n";

                }

            }

            if (context.getSessionData("evtEffects") == null) {
                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set effects " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set effects\n";
                LinkedList<String> effects = (LinkedList<String>) context.getSessionData("evtEffects");
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtEffectLocations");

                for (String effect : effects) {

                    text += GRAY + "    - " + AQUA + effect + GRAY + " at " + DARKAQUA + locations.get(effects.indexOf(effect)) + "\n";

                }

            }

            if (context.getSessionData("evtStormWorld") == null) {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set storm " + GRAY + "(None set)\n";
            } else {
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Set storm (" + AQUA + (String) context.getSessionData("evtStormWorld") + YELLOW + " for " + DARKAQUA + Quests.getTime((Long) context.getSessionData("evtStormDuration")) + YELLOW + ")\n";
            }

            if (context.getSessionData("evtThunderWorld") == null) {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - Set thunder " + GRAY + "(None set)\n";
            } else {
                text += BLUE + "" + BOLD + "8" + RESET + YELLOW + " - Set thunder (" + AQUA + (String) context.getSessionData("evtThunderWorld") + YELLOW + " for " + DARKAQUA + Quests.getTime((Long) context.getSessionData("evtThunderDuration")) + YELLOW + ")\n";
            }


            if (context.getSessionData("evtMobTypes") == null) {
                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - Set mob spawns " + GRAY + "(None set)\n";
            } else {
                LinkedList<String> types = (LinkedList<String>) context.getSessionData("evtMobTypes");
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData("evtMobAmounts");
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtMobLocations");

                text += BLUE + "" + BOLD + "9" + RESET + YELLOW + " - Set mob spawns\n";

                for (String s : types) {
                    int amt = amounts.get(types.indexOf(s));
                    String loc = locations.get(types.indexOf(s));
                    text += GRAY + "    - " + AQUA + s + GRAY + " x " + DARKAQUA + amt + GRAY + " at " + GREEN + loc + "\n";
                }
            }

            if (context.getSessionData("evtLightningStrikes") == null) {
                text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - Set lightning strike locations " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "10" + RESET + YELLOW + " - Set lightning strike locations\n";
                LinkedList<String> locations = (LinkedList<String>) context.getSessionData("evtLightningStrikes");

                for (String loc : locations) {

                    text += GRAY + "    - " + AQUA + loc + "\n";

                }

            }

            if (context.getSessionData("evtPotionTypes") == null) {
                text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - Set potion effects " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "11" + RESET + YELLOW + " - Set potion effects\n";
                LinkedList<String> types = (LinkedList<String>) context.getSessionData("evtPotionTypes");
                LinkedList<Long> durations = (LinkedList<Long>) context.getSessionData("evtPotionDurations");
                LinkedList<Integer> mags = (LinkedList<Integer>) context.getSessionData("evtPotionMagnitudes");
                int index = -1;

                for (String type : types) {

                    index++;
                    text += GRAY + "    - " + AQUA + type + PURPLE + " " + Quests.getNumeral(mags.get(index)) + GRAY + " for " + DARKAQUA + Quests.getTime(durations.get(index)*50L) + "\n";

                }

            }

            if(context.getSessionData("evtHunger") == null) {
                text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - Set player hunger level " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "12" + RESET + YELLOW + " - Set player hunger level " + AQUA + "(" + (Integer)context.getSessionData("evtHunger") + ")\n";

            }

            if(context.getSessionData("evtSaturation") == null) {
                text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - Set player saturation level " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "13" + RESET + YELLOW + " - Set player saturation level " + AQUA + "(" + (Integer)context.getSessionData("evtSaturation") + ")\n";

            }

            if(context.getSessionData("evtHealth") == null) {
                text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - Set player health level " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "14" + RESET + YELLOW + " - Set player health level " + AQUA + "(" + (Integer)context.getSessionData("evtHealth") + ")\n";

            }

            if(context.getSessionData("evtTeleportLocation") == null) {
                text += BLUE + "" + BOLD + "15" + RESET + YELLOW + " - Set player teleport location " + GRAY + "(None set)\n";
            } else {

                text += BLUE + "" + BOLD + "15" + RESET + YELLOW + " - Set player teleport location " + AQUA + "(" + (String)context.getSessionData("evtTeleportLocation") + ")\n";

            }

            text += GREEN + "" + BOLD + "16" + RESET + YELLOW + " - Done\n";
            text += RED + "" + BOLD + "17" + RESET + YELLOW + " - Quit";

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

                if(context.getSessionData("oldEvent") != null){
                    return new FinishPrompt((String)context.getSessionData("oldEvent"));
                }else{
                    return new FinishPrompt(null);
                }

            } else if (input.equalsIgnoreCase("17")) {

                return new QuitPrompt();

            }

            return null;

        }
    }

    private class QuitPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    GREEN + "Are you sure you want to quit without saving?\n";
                    text += YELLOW + "Yes/No";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase("Yes")){
                clearData(context);
                return new MenuPrompt();
            }else if(input.equalsIgnoreCase("No")){
                return new CreateMenuPrompt();
            }else{
                ((Player) context.getForWhom()).sendMessage(RED + "Invalid option!");
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
                    RED + "Are you sure you want to finish and save the Event \"" + GOLD + (String)context.getSessionData("evtName") + GREEN + "\"?\n";
                    if(modified.isEmpty() == false){
                        text += RED + "Note: You have modified an Event that the following Quests use:\n";
                        for(String s : modified)
                            text += GRAY + "    - " + DARKRED + s + "\n";
                        text += RED + "If you save the Event, anyone who is actively doing any of these Quests\n";
                        text += RED + "will be forced to quit them.\n";
                    }
                    text += YELLOW + "Yes/No";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if(input.equalsIgnoreCase("Yes")){
                saveEvent(context);
                return new MenuPrompt();
            }else if(input.equalsIgnoreCase("No")){
                return new CreateMenuPrompt();
            }else{
                ((Player) context.getForWhom()).sendMessage(RED + "Invalid option!");
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
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + "Error reading Events file.");
            return;
        }

        String event = (String)context.getSessionData("delEvent");
        ConfigurationSection sec = data.getConfigurationSection("events");
        sec.set(event, null);

        try{
            data.save(eventsFile);
        }catch (Exception e){
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + "An error occurred while saving.");
            return;
        }


        quests.reloadQuests();

        ((Player)context.getForWhom()).sendMessage(ChatColor.YELLOW + "Event deleted, Quests and Events reloaded.");

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
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + "Error reading Events file.");
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

        if (context.getSessionData("evtItemIds") != null) {

            LinkedList<Integer> ids = getCIntList(context, "evtItemIds");
            LinkedList<Integer> amounts = getCIntList(context, "evtItemAmounts");

            section.set("item-ids", ids);
            section.set("item-amounts", amounts);

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
            ((Player)context.getForWhom()).sendMessage(ChatColor.RED + "An error occurred while saving.");
            return;
        }


        quests.reloadQuests();

        ((Player)context.getForWhom()).sendMessage(ChatColor.YELLOW + "Event saved, Quests and Events reloaded.");

        for(Quester q : quests.questers.values())
            q.checkQuest();

        clearData(context);

    }

    private class EventNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text =
                    AQUA + "Create new Event " + GOLD + "- Enter a name for the Event (Or enter \'cancel\' to return to the main menu)";

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                for (Event e : quests.events) {

                    if (e.name.equalsIgnoreCase(input)) {

                        context.getForWhom().sendRawMessage(RED + "Event already exists!");
                        return new EventNamePrompt();

                    }

                }

                if (names.contains(input)) {

                    context.getForWhom().sendRawMessage(RED + "Someone is creating/editing an Event with that name!");
                    return new EventNamePrompt();

                }

                if (StringUtils.isAlphanumeric(input) == false) {

                    context.getForWhom().sendRawMessage(RED + "Name must be alphanumeric!");
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

            return YELLOW + "Enter NPC ID (or -1 to return)";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if (quests.citizens.getNPCRegistry().getById(input.intValue()) == null) {
                    context.getForWhom().sendRawMessage(RED + "No NPC exists with that id!");
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

            return YELLOW + "Right-click on a block to spawn an explosion at, then enter \"add\" to add it to the list,\nor enter \"clear\" to clear the explosions list, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("add")) {

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
                    player.sendMessage(RED + "You must select a block first.");
                    return new ExplosionPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("clear")) {

                context.setSessionData("evtExplosions", null);
                selectedExplosionLocations.remove(player);
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

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

            return YELLOW + "Enter Quest name (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                for (Event e : quests.events) {

                    if (e.name.equalsIgnoreCase(input)) {
                        context.getForWhom().sendRawMessage(RED + "An Event with that name already exists!");
                        return new SetNamePrompt();
                    }

                }

                if (names.contains(input)) {
                    context.getForWhom().sendRawMessage(RED + "Someone is creating/editing an Event with that name!");
                    return new SetNamePrompt();
                }

                if (StringUtils.isAlphanumeric(input) == false) {

                    context.getForWhom().sendRawMessage(RED + "Name must be alphanumeric!");
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

            return YELLOW + "Enter message, or enter \'none\' to delete, or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("none") == false) {
                context.setSessionData("evtMessage", input);
            } else if (input.equalsIgnoreCase("none")) {
                context.setSessionData("evtMessage", null);
            }

            return new CreateMenuPrompt();

        }
    }

    private class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Event Items -\n";
            if (context.getSessionData("evtItemIds") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set item IDs (None set)\n";
                text += GRAY + "2 - Set item amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set item IDs\n";
                for (Integer i : getItemIds(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if (context.getSessionData("evtItemAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set item amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set item amounts\n";
                    for (Integer i : getItemAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ItemIdsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtItemIds") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set item IDs first!");
                    return new ItemListPrompt();
                } else {
                    return new ItemAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Event items cleared.");
                context.setSessionData("evtItemIds", null);
                context.setSessionData("evtItemAmounts", null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData("evtItemIds") != null) {
                    one = ((List<Integer>) context.getSessionData("evtItemIds")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData("evtItemAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData("evtItemAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateMenuPrompt();
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "item IDs list " + RED + "and " + GOLD + "item amounts list " + RED + "are not the same size!");
                    return new ItemListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getItemIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData("evtItemIds");
        }

        private List<Integer> getItemAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData("evtItemAmounts");
        }
    }

    private class ItemIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter item IDs separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + "List contains duplicates!");
                                return new ItemIdsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid item ID!");
                            return new ItemIdsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new ItemIdsPrompt();
                    }

                }

                context.setSessionData("evtItemIds", ids);

            }

            return new ItemListPrompt();

        }
    }

    private class ItemAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter item amounts (numbers) separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new ItemAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new ItemAmountsPrompt();
                    }

                }

                context.setSessionData("evtItemAmounts", amounts);

            }

            return new ItemListPrompt();

        }
    }

    private class EffectListPrompt extends FixedSetPrompt {

        public EffectListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Event Effects -\n";
            if (context.getSessionData("evtEffects") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add effect (None set)\n";
                text += GRAY + "2 - Add effect location (No effects set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add effect\n";
                for (String s : getEffects(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData("evtEffectLocations") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Add effect location (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Add effect location\n";
                    for (String s : getEffectLocations(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new EffectPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtEffects") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must add effects first!");
                    return new EffectListPrompt();
                } else {
                    selectedEffectLocations.put((Player) context.getForWhom(), null);
                    return new EffectLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Event effects cleared.");
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
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "effects list " + RED + "and " + GOLD + "effect locations list " + RED + "are not the same size!");
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

            return YELLOW + "Right-click on a block to play an effect at, then enter \"add\" to add it to the list,\nor enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("add")) {

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
                    player.sendMessage(RED + "You must select a block first.");
                    return new EffectLocationPrompt();
                }

                return new EffectListPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

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
            effects += PURPLE + "BLAZE_SHOOT " + GRAY + "- Sound of a Blaze firing\n";
            effects += PURPLE + "BOW_FIRE " + GRAY + "- Sound of a bow firing\n";
            effects += PURPLE + "CLICK1 " + GRAY + "- A click sound\n";
            effects += PURPLE + "CLICK2 " + GRAY + "- A different click sound\n";
            effects += PURPLE + "DOOR_TOGGLE " + GRAY + "- Sound of a door opening or closing\n";
            effects += PURPLE + "EXTINGUISH " + GRAY + "- Sound of fire being extinguished\n";
            effects += PURPLE + "GHAST_SHOOT " + GRAY + "- Sound of a Ghast firing\n";
            effects += PURPLE + "GHAST_SHRIEK " + GRAY + "- Sound of a Ghast shrieking\n";
            effects += PURPLE + "ZOMBIE_CHEW_IRON_DOOR " + GRAY + "- Sound of a Zombie chewing an iron door\n";
            effects += PURPLE + "ZOMBIE_CHEW_WOODEN_DOOR " + GRAY + "- Sound of a Zombie chewing a wooden door\n";

            return YELLOW + effects + "Enter an effect name to add it to the list, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

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
                    player.sendMessage(PINK + input + " " + RED + "is not a valid effect name!");
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

            String text = GOLD + "- Event Storm -\n";
            if (context.getSessionData("evtStormWorld") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set world (None set)\n";
                text += GRAY + "2 - Set duration (No world set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set world (" + AQUA + ((String) context.getSessionData("evtStormWorld")) + YELLOW + ")\n";

                if (context.getSessionData("evtStormDuration") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set duration (None set)\n";
                } else {

                    Long dur = (Long) context.getSessionData("evtStormDuration");

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set duration (" + AQUA + Quests.getTime(dur) + YELLOW + ")\n";

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new StormWorldPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtStormWorld") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set a world first!");
                    return new StormPrompt();
                } else {
                    return new StormDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Storm data cleared.");
                context.setSessionData("evtStormWorld", null);
                context.setSessionData("evtStormDuration", null);
                return new StormPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                if (context.getSessionData("evtStormWorld") != null && context.getSessionData("evtStormDuration") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set a storm duration!");
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

            String effects = PINK + "- Worlds - \n" + PURPLE;
            for (World w : quests.getServer().getWorlds()) {
                effects += w.getName() + ", ";
            }

            effects = effects.substring(0, effects.length());

            return YELLOW + effects + "Enter a world name for the storm to occur in, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                if (quests.getServer().getWorld(input) != null) {

                    context.setSessionData("evtStormWorld", quests.getServer().getWorld(input).getName());

                } else {
                    player.sendMessage(PINK + input + " " + RED + "is not a valid world name!");
                    return new StormWorldPrompt();
                }

            }
            return new StormPrompt();

        }
    }

    private class StormDurationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter duration (in milliseconds)";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.longValue() < 1000) {
                context.getForWhom().sendRawMessage(RED + "Amount must be at least 1 second! (1000 milliseconds)");
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

            String text = GOLD + "- Event Thunder -\n";

            if (context.getSessionData("evtThunderWorld") == null) {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set world (None set)\n";
                text += GRAY + "2 - Set duration (No world set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set world (" + AQUA + ((String) context.getSessionData("evtThunderWorld")) + YELLOW + ")\n";

                if (context.getSessionData("evtThunderDuration") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set duration (None set)\n";
                } else {

                    Long dur = (Long) context.getSessionData("evtThunderDuration");
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set duration (" + AQUA + Quests.getTime(dur) + YELLOW + ")\n";

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";


            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ThunderWorldPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtThunderWorld") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set a world first!");
                    return new ThunderPrompt();
                } else {
                    return new ThunderDurationPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Thunder data cleared.");
                context.setSessionData("evtThunderWorld", null);
                context.setSessionData("evtThunderDuration", null);
                return new ThunderPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                if (context.getSessionData("evtThunderWorld") != null && context.getSessionData("evtThunderDuration") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set a thunder duration!");
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

            return YELLOW + effects + "Enter a world name for the thunder to occur in, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                if (quests.getServer().getWorld(input) != null) {

                    context.setSessionData("evtThunderWorld", quests.getServer().getWorld(input).getName());

                } else {
                    player.sendMessage(PINK + input + " " + RED + "is not a valid world name!");
                    return new ThunderWorldPrompt();
                }

            }
            return new ThunderPrompt();

        }
    }

    private class ThunderDurationPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter duration (in milliseconds)";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.longValue() < 1000) {
                context.getForWhom().sendRawMessage(RED + "Amount must be at least 1 second! (1000 milliseconds)");
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

            String text = GOLD + "- Event Mob Spawns -\n";
            if (context.getSessionData("evtMobTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set mob types (None set)\n";
                text += GRAY + "2 - Set mob amounts (No types set)\n";
                text += GRAY + "3 - Add spawn location (No types set)\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set mob types\n";
                for (String s : (LinkedList<String>) context.getSessionData("evtMobTypes")) {
                    text += GRAY + "    - " + AQUA + s + "\n";
                }

                if (context.getSessionData("evtMobAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set mob amounts (None set)\n";
                    text += GRAY + "3 - Add spawn location (No amounts set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set mob amounts\n";
                    for (int i : (LinkedList<Integer>) context.getSessionData("evtMobAmounts")) {
                        text += GRAY + "    - " + DARKAQUA + i + "\n";
                    }


                    if (context.getSessionData("evtMobLocations") == null) {
                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Add mob location (None set)\n";
                    } else {

                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Add spawn location\n";
                        for (String s : (LinkedList<String>) context.getSessionData("evtMobLocations")) {
                            text += GRAY + "    - " + GREEN + s + "\n";
                        }

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new MobTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtMobTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set mob types first!");
                    return new MobPrompt();
                } else {
                    return new MobAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData("evtMobTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set mob types and amounts first!");
                    return new MobPrompt();
                } else if (context.getSessionData("evtMobAmounts") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set mob amounts first!");
                    return new MobPrompt();
                } else {
                    selectedMobLocations.put((Player) context.getForWhom(), null);
                    return new MobLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + "Mob spawns cleared.");
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
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "mob types list " + RED + ", " + GOLD + "mob amounts list" + RED + ", and " + GOLD + "item amounts list " + RED + "are not the same size!");
                    return new MobPrompt();
                }

            }
            return null;

        }
    }

    private class MobTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String mobs = PINK + "- Mobs - \n";
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

            return mobs + YELLOW + "Enter mob names separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getMobType(s) != null) {

                        mobTypes.add(Quester.prettyMobString(Quests.getMobType(s)));
                        context.setSessionData("evtMobTypes", mobTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + "is not a valid mob name!");
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

            return YELLOW + "Enter mob amounts separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + "is not greater than zero!");
                            return new MobAmountsPrompt();
                        }

                        mobAmounts.add(i);


                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + "is not a number!");
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

            return YELLOW + "Right-click on a block to select it, then enter \"add\" to add it to the mob spawn location list,\nor enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("add")) {

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
                    player.sendMessage(RED + "You must select a block first.");
                    return new MobLocationPrompt();
                }

                return new MobPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

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

            return YELLOW + "Right-click on a block to spawn a lightning strike at, then enter \"add\" to add it to the list,\nor enter \"clear\" to clear the locations list, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("add")) {

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
                    player.sendMessage(RED + "You must select a block first.");
                    return new LightningPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("clear")) {

                context.setSessionData("evtLightningStrikes", null);
                selectedLightningLocations.remove(player);
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

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

            String text = GOLD + "- Event Potion Effects -\n";
            if (context.getSessionData("evtPotionTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set potion effect types (None set)\n";
                text += GRAY + "2 - Set potion effect durations (No types set)\n";
                text += GRAY + "3 - Set potion effect magnitudes (No types set)\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set potion effect types\n";
                for (String s : (LinkedList<String>) context.getSessionData("evtPotionTypes")) {
                    text += GRAY + "    - " + AQUA + s + "\n";
                }

                if (context.getSessionData("evtPotionDurations") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set potion effect durations (None set)\n";
                    text += GRAY + "3 - Set potion effect magnitudes (No durations set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set potion effect durations\n";
                    for (Long l : (LinkedList<Long>) context.getSessionData("evtPotionDurations")) {
                        text += GRAY + "    - " + DARKAQUA + Quests.getTime(l*50L) + "\n";
                    }

                    if (context.getSessionData("evtPotionMagnitudes") == null) {
                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set potion effect magnitudes (None set)\n";
                    } else {

                        text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set potion effect magnitudes\n";
                        for (int i : (LinkedList<Integer>) context.getSessionData("evtPotionMagnitudes")) {
                            text += GRAY + "    - " + PURPLE + i + "\n";
                        }

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += GREEN + "" + BOLD + "5" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new PotionTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData("evtPotionTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set potion effect types first!");
                    return new PotionEffectPrompt();
                } else {
                    return new PotionDurationsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData("evtPotionTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must potion effect types and durations first!");
                    return new PotionEffectPrompt();
                } else if (context.getSessionData("evtPotionDurations") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set potion effect durations first!");
                    return new PotionEffectPrompt();
                } else {
                    return new PotionMagnitudesPrompt();
                }

            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + "Potion effects cleared.");
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
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "effect types list " + RED + ", " + GOLD + "effect durations list" + RED + ", and " + GOLD + "effect magnitudes list " + RED + "are not the same size!");
                    return new PotionEffectPrompt();
                }

            }
            return null;

        }
    }

    private class PotionTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String effs = PINK + "- Potion Effects - \n";
            for (PotionEffectType pet : PotionEffectType.values()) {
                effs += (pet != null && pet.getName() != null) ? (PURPLE + pet.getName() + "\n") : "";
            }

            return effs + YELLOW + "Enter potion effect types separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> effTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (PotionEffectType.getByName(s.toUpperCase()) != null) {

                        effTypes.add(PotionEffectType.getByName(s.toUpperCase()).getName());

                        context.setSessionData("evtPotionTypes", effTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + "is not a valid potion effect type!");
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

            return YELLOW + "Enter effect durations (in milliseconds) separating each one by a space,\n or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Long> effDurations = new LinkedList<Long>();
                for (String s : input.split(" ")) {

                    try {

                        long l = Long.parseLong(s);

                        if (l < 1000) {
                            player.sendMessage(PINK + s + " " + RED + "is not greater than 1 second! (1000 milliseconds)");
                            return new PotionDurationsPrompt();
                        }

                        effDurations.add(l / 50L);


                    } catch (Exception e) {
                        player.sendMessage(PINK + s + " " + RED + "is not a number!");
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

            return YELLOW + "Enter potion magnitudes separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> magAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + s + " " + RED + "is not greater than zero!");
                            return new PotionMagnitudesPrompt();
                        }

                        magAmounts.add(i);


                    } catch (Exception e) {
                        player.sendMessage(PINK + s + " " + RED + "is not a number!");
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

            return YELLOW + "Enter hunger level, or -1 to remove it";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if(input.intValue() < 0){
                    ((Player)context.getForWhom()).sendMessage(RED + "Hunger level must be at least 0!");
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

            return YELLOW + "Enter saturation level, or -1 to remove it";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if(input.intValue() < 0){
                    ((Player)context.getForWhom()).sendMessage(RED + "Saturation level must be at least 0!");
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

            return YELLOW + "Enter health level, or -1 to remove it";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {

            if (input.intValue() != -1) {

                if(input.intValue() < 0){
                    ((Player)context.getForWhom()).sendMessage(RED + "Health level must be at least 0!");
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

            return YELLOW + "Right-click on a block to teleport the player to, then enter \"done\" to finish,\nor enter \"clear\" to clear the teleport location, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("done")) {

                Block block = selectedTeleportLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    context.setSessionData("evtTeleportLocation", Quests.getLocationInfo(loc));
                    selectedTeleportLocations.remove(player);

                } else {
                    player.sendMessage(RED + "You must select a block first.");
                    return new TeleportPrompt();
                }

                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("clear")) {

                context.setSessionData("evtTeleportLocation", null);
                selectedTeleportLocations.remove(player);
                return new CreateMenuPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

                selectedTeleportLocations.remove(player);
                return new CreateMenuPrompt();

            } else {
                return new TeleportPrompt();
            }

        }
    }
}
