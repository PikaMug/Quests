package me.blackvein.quests;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

public class Quester {

    String name;
    boolean editorMode = false;
    boolean holdingQuestItemFromStorage = false;
    boolean delayOver = true;
    public Quest currentQuest;
    public String questToTake;
    public Stage currentStage;
    public int currentStageIndex = 0;
    int questPoints = 0;
    Quests plugin;
    public LinkedList<String> completedQuests = new LinkedList<String>();
    Map<String, Long> completedTimes = new HashMap<String, Long>();
    Map<String, Integer> amountsCompleted = new HashMap<String, Integer>();
    Map<Material, Integer> blocksDamaged = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksBroken = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksPlaced = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksUsed = new EnumMap<Material, Integer>(Material.class);
    Map<Material, Integer> blocksCut = new EnumMap<Material, Integer>(Material.class);
    Map<Integer, Integer> potionsBrewed = new HashMap<Integer, Integer>();
    Map<ItemStack, Integer> itemsDelivered = new HashMap<ItemStack, Integer>();
    int fishCaught = 0;
    int playersKilled = 0;
    long delayStartTime = 0;
    long delayTimeLeft = -1;
    Map<String, Long> playerKillTimes = new HashMap<String, Long>();
    Map<Map<Enchantment, Material>, Integer> itemsEnchanted = new HashMap<Map<Enchantment, Material>, Integer>();
    LinkedList<EntityType> mobsKilled = new LinkedList<EntityType>();
    LinkedList<Integer> mobNumKilled = new LinkedList<Integer>();
    LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
    LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
    Map<Integer, Boolean> citizensInteracted = new HashMap<Integer, Boolean>();
    LinkedList<Integer> citizensKilled = new LinkedList<Integer>();
    LinkedList<Integer> citizenNumKilled = new LinkedList<Integer>();
    LinkedList<Location> locationsReached = new LinkedList<Location>();
    LinkedList<Boolean> hasReached = new LinkedList<Boolean>();
    LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>();
    Map<EntityType, Integer> mobsTamed = new EnumMap<EntityType, Integer>(EntityType.class);
    Map<DyeColor, Integer> sheepSheared = new EnumMap<DyeColor, Integer>(DyeColor.class);
    Map<String, Boolean> passwordsSaid = new HashMap<String, Boolean>();
    public Map<String, Integer> customObjectiveCounts = new HashMap<String, Integer>();
    public Map<String, Boolean> eventFired = new HashMap<String, Boolean>();
    final Random random = new Random();

    public Quester(Quests newPlugin) {

        plugin = newPlugin;

    }

    public Player getPlayer() {

        return plugin.getServer().getPlayerExact(name);

    }

    public void takeQuest(Quest q, boolean override) {

        Player player = plugin.getServer().getPlayer(name);

        if (q.testRequirements(player) == true || override) {

            currentQuest = q;
            currentStage = q.orderedStages.getFirst();
            addEmpties();

            if (!override) {

                if (q.moneyReq > 0) {
                    Quests.economy.withdrawPlayer(name, q.moneyReq);
                }

                for (ItemStack is : q.items) {
                    if (q.removeItems.get(q.items.indexOf(is)) == true) {
                        Quests.removeItem(player.getInventory(), is);
                    }
                }

                String accepted = Lang.get("questAccepted");
                accepted = accepted.replaceAll("<quest>", q.name);
                
                player.sendMessage(ChatColor.GREEN + accepted);
                player.sendMessage("");

            }

            player.sendMessage(ChatColor.GOLD + Lang.get("questObjectivesTitle"));

            for (String s : getObjectivesReal()) {
                player.sendMessage(s);
            }

            String stageStartMessage = currentStage.startMessage;
            if (stageStartMessage != null) {
                getPlayer().sendMessage(Quests.parseString(stageStartMessage, currentQuest));
            }

            if (currentStage.chatEvents.isEmpty() == false) {

                for (String chatTrigger : currentStage.chatEvents.keySet()) {

                    eventFired.put(chatTrigger, false);

                }

            }

            if (q.initialEvent != null) {
                q.initialEvent.fire(this);
            }
            if (currentStage.startEvent != null) {
                currentStage.startEvent.fire(this);
            }

            saveData();

        } else {

            player.sendMessage(q.failRequirements);

        }

    }

    public LinkedList<String> getObjectivesReal() {
        
        if (currentStage.objectiveOverride != null) {
            LinkedList<String> objectives = new LinkedList<String>();
            objectives.add(ChatColor.GREEN + currentStage.objectiveOverride);
            return objectives;
        } else {
            return getObjectives();
        }
        
    }
    
    public LinkedList<String> getObjectives() {

        LinkedList<String> unfinishedObjectives = new LinkedList<String>();
        LinkedList<String> finishedObjectives = new LinkedList<String>();
        LinkedList<String> objectives = new LinkedList<String>();

        for (Entry<Material, Integer> e : currentStage.blocksToDamage.entrySet()) {

            for (Entry<Material, Integer> e2 : blocksDamaged.entrySet()) {

                if (e2.getKey().equals(e.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("damage") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("damage") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Entry<Material, Integer> e : currentStage.blocksToBreak.entrySet()) {

            for (Entry<Material, Integer> e2 : blocksBroken.entrySet()) {

                if (e2.getKey().equals(e.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("break") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("break") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Entry<Material, Integer> e : currentStage.blocksToPlace.entrySet()) {

            for (Entry<Material, Integer> e2 : blocksPlaced.entrySet()) {

                if (e2.getKey().equals(e.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("place") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("place") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Entry<Material, Integer> e : currentStage.blocksToUse.entrySet()) {

            for (Entry<Material, Integer> e2 : blocksUsed.entrySet()) {

                if (e2.getKey().equals(e.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("use") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("use") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Entry<Material, Integer> e : currentStage.blocksToCut.entrySet()) {

            for (Entry<Material, Integer> e2 : blocksCut.entrySet()) {

                if (e2.getKey().equals(e.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("cut") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("cut") + " " + Quester.prettyItemString(e2.getKey().getId()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        if (currentStage.fishToCatch != null) {

            if (fishCaught < currentStage.fishToCatch) {

                unfinishedObjectives.add(ChatColor.GREEN + Lang.get("catchFish") + ": " + fishCaught + "/" + currentStage.fishToCatch);

            } else {

                finishedObjectives.add(ChatColor.GRAY + Lang.get("catchFish") + ": " + fishCaught + "/" + currentStage.fishToCatch);

            }

        }

        Map<Enchantment, Material> set;
        Map<Enchantment, Material> set2;
        Set<Enchantment> enchantSet;
        Set<Enchantment> enchantSet2;
        Collection<Material> matSet;
        Enchantment enchantment = null;
        Enchantment enchantment2 = null;
        Material mat = null;
        int num1;
        int num2;

        for (Entry<Map<Enchantment, Material>, Integer> e : currentStage.itemsToEnchant.entrySet()) {

            for (Entry<Map<Enchantment, Material>, Integer> e2 : itemsEnchanted.entrySet()) {

                set = e2.getKey();
                set2 = e.getKey();
                enchantSet = set.keySet();
                enchantSet2 = set2.keySet();
                for (Object o : enchantSet.toArray()) {

                    enchantment = (Enchantment) o;

                }
                for (Object o : enchantSet2.toArray()) {

                    enchantment2 = (Enchantment) o;

                }
                num1 = e2.getValue();
                num2 = e.getValue();

                matSet = set.values();

                for (Object o : matSet.toArray()) {

                    mat = (Material) o;

                }

                if (enchantment2 == enchantment) {

                    if (num1 < num2) {

                        String obj = Lang.get("enchantItem");
                        obj = obj.replaceAll("<item>", Quester.prettyItemString(mat.getId()));
                        obj = obj.replaceAll("<enchantment>", Quester.prettyEnchantmentString(enchantment));
                        unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + num1 + "/" + num2);

                    } else {

                        String obj = Lang.get("enchantItem");
                        obj = obj.replaceAll("<item>", Quester.prettyItemString(mat.getId()));
                        obj = obj.replaceAll("<enchantment>", Quester.prettyEnchantmentString(enchantment));
                        finishedObjectives.add(ChatColor.GRAY + obj + ": " + num1 + "/" + num2);

                    }

                }

            }

        }

        for (EntityType e : currentStage.mobsToKill) {

            for (EntityType e2 : mobsKilled) {

                if (e == e2) {

                    if (mobNumKilled.get(mobsKilled.indexOf(e2)) < currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(e))) {

                        if (currentStage.locationsToKillWithin.isEmpty()) {
                            unfinishedObjectives.add(ChatColor.GREEN + Lang.get("kill") + " " + Quester.prettyMobString(e) + ": " + (mobNumKilled.get(mobsKilled.indexOf(e2))) + "/" + (currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(e))));
                        } else {
                            String obj = Lang.get("killAtLocation");
                            obj = obj.replaceAll("<mob>", Quester.prettyMobString(e));
                            obj = obj.replaceAll("<location>", currentStage.areaNames.get(currentStage.mobsToKill.indexOf(e)));
                            unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + (mobNumKilled.get(mobsKilled.indexOf(e2))) + "/" + (currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(e))));
                        }
                    } else {

                        if (currentStage.locationsToKillWithin.isEmpty()) {
                            finishedObjectives.add(ChatColor.GRAY + Lang.get("kill") + " " + Quester.prettyMobString(e) + ": " + (mobNumKilled.get(mobsKilled.indexOf(e2))) + "/" + (currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(e))));
                        } else {
                            String obj = Lang.get("killAtLocation");
                            obj = obj.replaceAll("<mob>", Quester.prettyMobString(e));
                            obj = obj.replaceAll("<location>", currentStage.areaNames.get(currentStage.mobsToKill.indexOf(e)));
                            finishedObjectives.add(ChatColor.GRAY + obj + ": " + (mobNumKilled.get(mobsKilled.indexOf(e2))) + "/" + (currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(e))));
                        }

                    }

                }

            }

        }

        if (currentStage.playersToKill != null) {

            if (playersKilled < currentStage.playersToKill) {

                unfinishedObjectives.add(ChatColor.GREEN + Lang.get("killPlayer") + ": " + playersKilled + "/" + currentStage.playersToKill);

            } else {

                finishedObjectives.add(ChatColor.GRAY + Lang.get("killPlayer") + ": " + playersKilled + "/" + currentStage.playersToKill);

            }

        }

        for (ItemStack is : currentStage.itemsToDeliver) {

            int delivered = itemsDelivered.get(is);
            int amt = is.getAmount();
            Integer npc = currentStage.itemDeliveryTargets.get(currentStage.itemsToDeliver.indexOf(is));

            if (delivered < amt) {

                String obj = Lang.get("deliver");
                obj = obj.replaceAll("<item>", ItemUtil.getName(is));
                obj = obj.replaceAll("<npc>", plugin.getNPCName(npc));
                unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + delivered + "/" + amt);

            } else {

                String obj = Lang.get("deliver");
                obj = obj.replaceAll("<item>", ItemUtil.getName(is));
                obj = obj.replaceAll("<npc>", plugin.getNPCName(npc));
                finishedObjectives.add(ChatColor.GRAY + obj + ": " + delivered + "/" + amt);

            }

        }

        for (Integer n : currentStage.citizensToInteract) {

            for (Entry<Integer, Boolean> e : citizensInteracted.entrySet()) {

                if (e.getKey().equals(n)) {

                    if (e.getValue() == false) {

                        String obj = Lang.get("talkTo");
                        obj = obj.replaceAll("<npc>", plugin.getNPCName(n));
                        unfinishedObjectives.add(ChatColor.GREEN + obj);

                    } else {

                        String obj = Lang.get("talkTo");
                        obj = obj.replaceAll("<npc>", plugin.getNPCName(n));
                        finishedObjectives.add(ChatColor.GRAY + obj);

                    }

                }

            }

        }

        for (Integer n : currentStage.citizensToKill) {

            for (Integer n2 : citizensKilled) {

                if (n.equals(n2)) {

                    if (citizenNumKilled.get(citizensKilled.indexOf(n2)) < currentStage.citizenNumToKill.get(currentStage.citizensToKill.indexOf(n))) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("kill") + " " + plugin.getNPCName(n) + ChatColor.GREEN + " " + citizenNumKilled.get(currentStage.citizensToKill.indexOf(n)) + "/" + currentStage.citizenNumToKill.get(currentStage.citizensToKill.indexOf(n)));

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("kill") + " " + plugin.getNPCName(n) + " " + currentStage.citizenNumToKill.get(currentStage.citizensToKill.indexOf(n)) + "/" + currentStage.citizenNumToKill.get(currentStage.citizensToKill.indexOf(n)));

                    }

                }

            }

        }

        for (Entry<EntityType, Integer> e : currentStage.mobsToTame.entrySet()) {

            for (Entry<EntityType, Integer> e2 : mobsTamed.entrySet()) {

                if (e.getKey().equals(e2.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("tame") + " " + getCapitalized(e.getKey().getName()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("tame") + " " + getCapitalized(e.getKey().getName()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Entry<DyeColor, Integer> e : currentStage.sheepToShear.entrySet()) {

            for (Entry<DyeColor, Integer> e2 : sheepSheared.entrySet()) {

                if (e.getKey().equals(e2.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        String obj = Lang.get("shearSheep");
                        obj = obj.replaceAll("<color>", e.getKey().name().toString().toLowerCase());
                        unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        String obj = Lang.get("shearSheep");
                        obj = obj.replaceAll("<color>", e.getKey().name().toString().toLowerCase());
                        finishedObjectives.add(ChatColor.GRAY + obj + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Location l : currentStage.locationsToReach) {

            for (Location l2 : locationsReached) {

                if (l.equals(l2)) {

                    if (hasReached.get(locationsReached.indexOf(l2)) == false) {

                        String obj = Lang.get("goTo");
                        obj = obj.replaceAll("<location>", currentStage.locationNames.get(currentStage.locationsToReach.indexOf(l)));
                        unfinishedObjectives.add(ChatColor.GREEN + obj);

                    } else {

                        String obj = Lang.get("goTo");
                        obj = obj.replaceAll("<location>", currentStage.locationNames.get(currentStage.locationsToReach.indexOf(l)));
                        finishedObjectives.add(ChatColor.GRAY + obj);

                    }

                }

            }

        }

        for (String s : currentStage.passwordDisplays) {

            if (passwordsSaid.get(s) == false) {

                unfinishedObjectives.add(ChatColor.GREEN + s);

            } else {

                finishedObjectives.add(ChatColor.GRAY + s);

            }

        }

        int index = 0;
        for (CustomObjective co : currentStage.customObjectives) {

            for (Entry<String, Integer> entry : customObjectiveCounts.entrySet()) {

                if (co.getName().equals(entry.getKey())) {

                    String display = co.getDisplay();

                    Map<String, Object> datamap = currentStage.customObjectiveData.get(index);
                    for (String key : co.datamap.keySet()) {
                        display = display.replaceAll("%" + ((String) key) + "%", ((String) datamap.get(key)));
                    }

                    if (entry.getValue() < currentStage.customObjectiveCounts.get(index)) {
                        if (co.isCountShown() && co.isEnableCount()) {
                            display = display.replaceAll("%count%", entry.getValue() + "/" + currentStage.customObjectiveCounts.get(index));
                        }
                        unfinishedObjectives.add(ChatColor.GREEN + display);
                    } else {
                        if (co.isCountShown() && co.isEnableCount()) {
                            display = display.replaceAll("%count%", currentStage.customObjectiveCounts.get(index) + "/" + currentStage.customObjectiveCounts.get(index));
                        }
                        finishedObjectives.add(ChatColor.GRAY + display);
                    }

                }

            }

            index++;

        }

        objectives.addAll(unfinishedObjectives);
        objectives.addAll(finishedObjectives);

        return objectives;

    }

    public boolean hasObjective(String s) {

        if (currentStage == null) {
            return false;
        }

        if (s.equalsIgnoreCase("damageBlock")) {
            return !currentStage.blocksToDamage.isEmpty();

        } else if (s.equalsIgnoreCase("breakBlock")) {
            return !currentStage.blocksToBreak.isEmpty();

        } else if (s.equalsIgnoreCase("placeBlock")) {
            return !currentStage.blocksToPlace.isEmpty();

        } else if (s.equalsIgnoreCase("useBlock")) {
            return !currentStage.blocksToUse.isEmpty();

        } else if (s.equalsIgnoreCase("cutBlock")) {
            return !currentStage.blocksToCut.isEmpty();

        } else if (s.equalsIgnoreCase("catchFish")) {
            return currentStage.fishToCatch != null;

        } else if (s.equalsIgnoreCase("enchantItem")) {
            return !currentStage.itemsToEnchant.isEmpty();

        } else if (s.equalsIgnoreCase("killMob")) {
            return !currentStage.mobsToKill.isEmpty();

        } else if (s.equalsIgnoreCase("deliverItem")) {
            return !currentStage.itemsToDeliver.isEmpty();

        } else if (s.equalsIgnoreCase("killPlayer")) {
            return currentStage.playersToKill != null;

        } else if (s.equalsIgnoreCase("talkToNPC")) {
            return !currentStage.citizensToInteract.isEmpty();

        } else if (s.equalsIgnoreCase("killNPC")) {
            return !currentStage.citizensToKill.isEmpty();

        } else if (s.equalsIgnoreCase("tameMob")) {
            return !currentStage.mobsToTame.isEmpty();

        } else if (s.equalsIgnoreCase("shearSheep")) {
            return !currentStage.sheepToShear.isEmpty();

        } else if (s.equalsIgnoreCase("craftItem")) {
            return !currentStage.itemsToCraft.isEmpty();

        } else if (s.equalsIgnoreCase("password")) {
            return !currentStage.passwordPhrases.isEmpty();

        } else {
            return !currentStage.locationsToReach.isEmpty();

        }

    }

    public boolean hasCustomObjective(String s) {

        if (customObjectiveCounts.containsKey(s)) {

            int count = customObjectiveCounts.get(s);

            int index = -1;
            for (int i = 0; i < currentStage.customObjectives.size(); i++) {
                if (currentStage.customObjectives.get(i).getName().equals(s)) {
                    index = i;
                    break;
                }
            }

            int count2 = currentStage.customObjectiveCounts.get(index);

            return count <= count2;

        }

        return false;

    }

    public void damageBlock(Material m) {

        if (blocksDamaged.containsKey(m)) {

            if (blocksDamaged.get(m) < currentStage.blocksToDamage.get(m)) {
                int i = blocksDamaged.get(m);
                blocksDamaged.put(m, (i + 1));

                if (blocksDamaged.get(m).equals(currentStage.blocksToDamage.get(m))) {
                    finishObjective("damageBlock", m, null, null, null, null, null, null, null, null, null);
                }

            }

        }

    }

    public void breakBlock(Material m) {

        if (blocksBroken.containsKey(m)) {

            if (blocksBroken.get(m) < currentStage.blocksToBreak.get(m)) {
                int i = blocksBroken.get(m);
                blocksBroken.put(m, (i + 1));

                if (blocksBroken.get(m).equals(currentStage.blocksToBreak.get(m))) {
                    finishObjective("breakBlock", m, null, null, null, null, null, null, null, null, null);
                }
            }

        }

    }

    public void placeBlock(Material m) {

        if (blocksPlaced.containsKey(m)) {

            if (blocksPlaced.get(m) < currentStage.blocksToPlace.get(m)) {
                int i = blocksPlaced.get(m);
                blocksPlaced.put(m, (i + 1));

                if (blocksPlaced.get(m).equals(currentStage.blocksToPlace.get(m))) {
                    finishObjective("placeBlock", m, null, null, null, null, null, null, null, null, null);
                }
            }

        }

    }

    public void useBlock(Material m) {

        if (blocksUsed.containsKey(m)) {

            if (blocksUsed.get(m) < currentStage.blocksToUse.get(m)) {
                int i = blocksUsed.get(m);
                blocksUsed.put(m, (i + 1));

                if (blocksUsed.get(m).equals(currentStage.blocksToUse.get(m))) {
                    finishObjective("useBlock", m, null, null, null, null, null, null, null, null, null);
                }

            }

        }

    }

    public void cutBlock(Material m) {

        if (blocksCut.containsKey(m)) {

            if (blocksCut.get(m) < currentStage.blocksToCut.get(m)) {
                int i = blocksCut.get(m);
                blocksCut.put(m, (i + 1));

                if (blocksCut.get(m).equals(currentStage.blocksToCut.get(m))) {
                    finishObjective("cutBlock", m, null, null, null, null, null, null, null, null, null);
                }

            }

        }

    }

    public void catchFish() {

        if (fishCaught < currentStage.fishToCatch) {
            fishCaught++;

            if (((Integer) fishCaught).equals(currentStage.fishToCatch)) {
                finishObjective("catchFish", null, null, null, null, null, null, null, null, null, null);
            }

        }

    }

    public void enchantItem(Enchantment e, Material m) {

        for (Entry<Map<Enchantment, Material>, Integer> entry : itemsEnchanted.entrySet()) {

            if (entry.getKey().containsKey(e) && entry.getKey().containsValue(m)) {

                for (Entry<Map<Enchantment, Material>, Integer> entry2 : currentStage.itemsToEnchant.entrySet()) {

                    if (entry2.getKey().containsKey(e) && entry2.getKey().containsValue(m)) {

                        if (entry.getValue() < entry2.getValue()) {

                            Integer num = entry.getValue() + 1;
                            itemsEnchanted.put(entry.getKey(), num);

                            if (num.equals(entry2.getValue())) {
                                finishObjective("enchantItem", m, null, e, null, null, null, null, null, null, null);
                            }

                        }
                        break;

                    }

                }

                break;

            }

        }

    }

    public void killMob(Location l, EntityType e) {

        if (mobsKilled.contains(e)) {

            if (locationsToKillWithin.isEmpty() == false) {

                int index = mobsKilled.indexOf(e);
                Location locationToKillWithin = locationsToKillWithin.get(index);
                double radius = radiiToKillWithin.get(index);
                int numKilled = mobNumKilled.get(index);
                if (l.getX() < (locationToKillWithin.getX() + radius) && l.getX() > (locationToKillWithin.getX() - radius)) {

                    if (l.getZ() < (locationToKillWithin.getZ() + radius) && l.getZ() > (locationToKillWithin.getZ() - radius)) {

                        if (l.getY() < (locationToKillWithin.getY() + radius) && l.getY() > (locationToKillWithin.getY() - radius)) {

                            if (numKilled < currentStage.mobNumToKill.get(index)) {

                                Integer numKilledInteger = numKilled + 1;

                                mobNumKilled.set(index, numKilledInteger);

                                if ((numKilledInteger).equals(currentStage.mobNumToKill.get(index))) {
                                    finishObjective("killMob", null, null, null, e, null, null, null, null, null, null);
                                }

                            }

                        }

                    }

                }

            } else {

                if (mobNumKilled.get(mobsKilled.indexOf(e)) < currentStage.mobNumToKill.get(mobsKilled.indexOf(e))) {

                    mobNumKilled.set(mobsKilled.indexOf(e), mobNumKilled.get(mobsKilled.indexOf(e)) + 1);

                    if ((mobNumKilled.get(mobsKilled.indexOf(e))).equals(currentStage.mobNumToKill.get(mobsKilled.indexOf(e)))) {
                        finishObjective("killMob", null, null, null, e, null, null, null, null, null, null);
                    }

                }

            }

        }

    }

    public void killPlayer(String player) {

        if (playerKillTimes.containsKey(player)) {

            long killTime = playerKillTimes.get(player);
            long comparator = plugin.killDelay * 1000;
            long currentTime = System.currentTimeMillis();

            if ((currentTime - killTime) < comparator) {

                String error = Lang.get("killNotValid");
                error = error.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(comparator - (currentTime - killTime)) + ChatColor.RED);
                error = error.replaceAll("<player>", ChatColor.DARK_PURPLE + player + ChatColor.RED);
                plugin.getServer().getPlayer(name).sendMessage(ChatColor.RED + error);
                return;

            }

        }

        playerKillTimes.put(player, System.currentTimeMillis());

        if (playersKilled < currentStage.playersToKill) {
            playersKilled++;

            if (((Integer) playersKilled).equals(currentStage.playersToKill)) {
                finishObjective("killPlayer", null, null, null, null, null, null, null, null, null, null);
            }

        }

    }

    public void interactWithNPC(NPC n) {

        if (citizensInteracted.containsKey(n.getId())) {

            if (citizensInteracted.get(n.getId()) == false) {
                citizensInteracted.put(n.getId(), true);
                finishObjective("talkToNPC", null, null, null, null, null, n, null, null, null, null);
            }

        }

    }

    public void killNPC(NPC n) {

        if (citizensKilled.contains(n.getId())) {

            int index = citizensKilled.indexOf(n.getId());
            if (citizenNumKilled.get(index) < currentStage.citizenNumToKill.get(index)) {
                citizenNumKilled.set(index, citizenNumKilled.get(index) + 1);
                if (citizenNumKilled.get(index) == currentStage.citizenNumToKill.get(index)) {
                    finishObjective("killNPC", null, null, null, null, null, n, null, null, null, null);
                }
            }

        }

    }

    public void reachLocation(Location l) {

        for (Location location : locationsReached) {

            int index = locationsReached.indexOf(location);
            Location locationToReach = currentStage.locationsToReach.get(index);
            double radius = radiiToReachWithin.get(index);
            if (l.getX() < (locationToReach.getX() + radius) && l.getX() > (locationToReach.getX() - radius)) {

                if (l.getZ() < (locationToReach.getZ() + radius) && l.getZ() > (locationToReach.getZ() - radius)) {

                    if (l.getY() < (locationToReach.getY() + radius) && l.getY() > (locationToReach.getY() - radius)) {

                        if (hasReached.get(index) == false) {

                            hasReached.set(index, true);
                            finishObjective("reachLocation", null, null, null, null, null, null, location, null, null, null);

                        }

                    }

                }

            }

        }

    }

    public void tameMob(EntityType entity) {

        if (mobsTamed.containsKey(entity)) {

            mobsTamed.put(entity, (mobsTamed.get(entity) + 1));

            if (mobsTamed.get(entity).equals(currentStage.mobsToTame.get(entity))) {
                finishObjective("tameMob", null, null, null, entity, null, null, null, null, null, null);
            }

        }

    }

    public void shearSheep(DyeColor color) {

        if (sheepSheared.containsKey(color)) {

            sheepSheared.put(color, (sheepSheared.get(color) + 1));

            if (sheepSheared.get(color).equals(currentStage.sheepToShear.get(color))) {
                finishObjective("shearSheep", null, null, null, null, null, null, null, color, null, null);
            }

        }

    }

    public void deliverItem(ItemStack i) {

        Player player = plugin.getServer().getPlayer(name);

        ItemStack found = null;

        for (ItemStack is : itemsDelivered.keySet()) {

            if (ItemUtil.compareItems(i, is, true) == 0) {
                found = is;
                break;
            }

        }
        if (found != null) {

            int amount = itemsDelivered.get(found);
            int req = currentStage.itemsToDeliver.get(currentStage.itemsToDeliver.indexOf(found)).getAmount();

            if (amount < req) {

                if ((i.getAmount() + amount) > req) {

                    itemsDelivered.put(found, req);
                    int index = player.getInventory().first(i);
                    i.setAmount(i.getAmount() - (req - amount)); //Take away the remaining amount needed to be delivered from the item stack
                    player.getInventory().setItem(index, i);
                    player.updateInventory();
                    finishObjective("deliverItem", null, found, null, null, null, null, null, null, null, null);

                } else if ((i.getAmount() + amount) == req) {

                    itemsDelivered.put(found, req);
                    player.getInventory().setItem(player.getInventory().first(i), null);
                    player.updateInventory();
                    finishObjective("deliverItem", null, found, null, null, null, null, null, null, null, null);

                } else {

                    itemsDelivered.put(found, (amount + i.getAmount()));
                    player.getInventory().setItem(player.getInventory().first(i), null);
                    player.updateInventory();
                    String message = Quests.parseString(currentStage.deliverMessages.get(random.nextInt(currentStage.deliverMessages.size())), plugin.citizens.getNPCRegistry().getById(currentStage.itemDeliveryTargets.get(currentStage.itemsToDeliver.indexOf(found))));
                    player.sendMessage(message);

                }

            }

        }

    }

    public void sayPass(AsyncPlayerChatEvent evt) {

        boolean done;
        for (LinkedList<String> passes : currentStage.passwordPhrases) {

            done = false;
            
            for(String pass : passes){
                
                if (pass.equalsIgnoreCase(evt.getMessage())) {

                    evt.setCancelled(true);
                    String display = currentStage.passwordDisplays.get(currentStage.passwordPhrases.indexOf(passes));
                    passwordsSaid.put(display, true);
                    done = true;
                    finishObjective("password", null, null, null, null, null, null, null, null, display, null);
                    break;

                }
            
            }
            
            if(done)
                break;

        }

    }

    public void finishObjective(String objective, Material material, ItemStack itemstack, Enchantment enchantment, EntityType mob, String player, NPC npc, Location location, DyeColor color, String pass, CustomObjective co) {

        Player p = plugin.getServer().getPlayerExact(name);

        if (currentStage.objectiveOverride != null) {

            if (testComplete()) {
                String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + currentStage.objectiveOverride;
                p.sendMessage(message);
                currentQuest.nextStage(this);
            }
            return;

        }

        if (objective.equalsIgnoreCase("password")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + pass;
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("damageBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("damage") + " " + prettyItemString(material.getId());
            message = message + " " + currentStage.blocksToDamage.get(material) + "/" + currentStage.blocksToDamage.get(material);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("breakBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("break") + " " + prettyItemString(material.getId());
            message = message + " " + currentStage.blocksToBreak.get(material) + "/" + currentStage.blocksToBreak.get(material);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("placeBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("place") + " " + prettyItemString(material.getId());
            message = message + " " + currentStage.blocksToPlace.get(material) + "/" + currentStage.blocksToPlace.get(material);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("useBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("use") + " " + prettyItemString(material.getId());
            message = message + " " + currentStage.blocksToUse.get(material) + "/" + currentStage.blocksToUse.get(material);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("cutBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("cut") + " " + prettyItemString(material.getId());
            message = message + " " + currentStage.blocksToCut.get(material) + "/" + currentStage.blocksToCut.get(material);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("catchFish")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("catchFish") + " ";
            message = message + " " + currentStage.fishToCatch + "/" + currentStage.fishToCatch;
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("enchantItem")) {

            String obj = Lang.get("enchantItem");
            obj = obj.replaceAll("<item>", prettyItemString(material.getId()));
            obj = obj.replaceAll("<enchantment>", Quester.prettyEnchantmentString(enchantment));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            for (Map<Enchantment, Material> map : currentStage.itemsToEnchant.keySet()) {

                if (map.containsKey(enchantment)) {

                    message = message + " " + currentStage.itemsToEnchant.get(map) + "/" + currentStage.itemsToEnchant.get(map);
                    break;

                }

            }

            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("deliverItem")) {

            String obj = Lang.get("deliver");
            obj = obj.replaceAll("<item>", ItemUtil.getString(currentStage.itemsToDeliver.get(currentStage.itemsToDeliver.indexOf(itemstack))));
            obj = obj.replaceAll("<npc>", plugin.getNPCName(currentStage.itemDeliveryTargets.get(currentStage.itemsToDeliver.indexOf(itemstack))));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("killMob")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("kill") + mob.getName();
            message = message + " " + currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(mob)) + "/" + currentStage.mobNumToKill.get(currentStage.mobsToKill.indexOf(mob));
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("killPlayer")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("killPlayer");
            message = message + " " + currentStage.playersToKill + "/" + currentStage.playersToKill;
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("talkToNPC")) {

            String obj = Lang.get("talkTo");
            obj = obj.replaceAll("<npc>", plugin.getNPCName(npc.getId()));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("killNPC")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("kill") + npc.getName();
            message = message + " " + currentStage.citizenNumToKill.get(currentStage.citizensToKill.indexOf(npc.getId())) + "/" + currentStage.citizenNumToKill.get(currentStage.citizensToKill.indexOf(npc.getId()));
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("tameMob")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("tame") + getCapitalized(mob.getName());
            message = message + " " + currentStage.mobsToTame.get(mob) + "/" + currentStage.mobsToTame.get(mob);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("shearSheep")) {

            String obj = Lang.get("shearSheep");
            obj = obj.replaceAll("<color>", color.name().toString().toLowerCase());
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            message = message + " " + currentStage.sheepToShear.get(color) + "/" + currentStage.sheepToShear.get(color);
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("reachLocation")) {

            String obj = Lang.get("goTo");
            obj = obj.replaceAll("<location>", currentStage.locationNames.get(currentStage.locationsToReach.indexOf(location)));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        } else if (co != null) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + co.getDisplay();

            int index = -1;
            for (int i = 0; i < currentStage.customObjectives.size(); i++) {
                if (currentStage.customObjectives.get(i).getName().equals(co.getName())) {
                    index = i;
                    break;
                }
            }

            Map<String, Object> datamap = currentStage.customObjectiveData.get(index);
            for (String key : co.datamap.keySet()) {
                message = message.replaceAll("%" + ((String) key) + "%", (String) datamap.get(key));
            }

            if (co.isCountShown() && co.isEnableCount()) {
                message = message.replaceAll("%count%", currentStage.customObjectiveCounts.get(index) + "/" + currentStage.customObjectiveCounts.get(index));
            }
            p.sendMessage(message);
            if (testComplete()) {
                currentQuest.nextStage(this);
            }

        }

    }

    public boolean testComplete() {

        for (String s : getObjectives()) {

            if (s.contains(ChatColor.GREEN.toString())) {
                return false;
            }

        }
        return true;

    }

    public void addEmpties() {

        if (currentStage.blocksToDamage.isEmpty() == false) {
            for (Material m : currentStage.blocksToDamage.keySet()) {

                blocksDamaged.put(m, 0);

            }
        }

        if (currentStage.blocksToBreak.isEmpty() == false) {
            for (Material m : currentStage.blocksToBreak.keySet()) {

                blocksBroken.put(m, 0);

            }
        }

        if (currentStage.blocksToPlace.isEmpty() == false) {
            for (Material m : currentStage.blocksToPlace.keySet()) {

                blocksPlaced.put(m, 0);

            }
        }

        if (currentStage.blocksToUse.isEmpty() == false) {
            for (Material m : currentStage.blocksToUse.keySet()) {

                blocksUsed.put(m, 0);

            }
        }

        if (currentStage.blocksToCut.isEmpty() == false) {
            for (Material m : currentStage.blocksToCut.keySet()) {

                blocksCut.put(m, 0);

            }
        }

        fishCaught = 0;

        if (currentStage.itemsToEnchant.isEmpty() == false) {
            for (Entry<Map<Enchantment, Material>, Integer> e : currentStage.itemsToEnchant.entrySet()) {

                Map<Enchantment, Material> map = (Map<Enchantment, Material>) e.getKey();
                itemsEnchanted.put(map, 0);

            }
        }

        if (currentStage.mobsToKill.isEmpty() == false) {
            for (EntityType e : currentStage.mobsToKill) {

                mobsKilled.add(e);
                mobNumKilled.add(0);
                if (currentStage.locationsToKillWithin.isEmpty() == false) {
                    locationsToKillWithin.add(currentStage.locationsToKillWithin.get(mobsKilled.indexOf(e)));
                }
                if (currentStage.radiiToKillWithin.isEmpty() == false) {
                    radiiToKillWithin.add(currentStage.radiiToKillWithin.get(mobsKilled.indexOf(e)));
                }

            }
        }

        playersKilled = 0;

        if (currentStage.itemsToDeliver.isEmpty() == false) {
            for (ItemStack is : currentStage.itemsToDeliver) {

                itemsDelivered.put(is, 0);

            }
        }

        if (currentStage.citizensToInteract.isEmpty() == false) {
            for (Integer n : currentStage.citizensToInteract) {

                citizensInteracted.put(n, false);

            }
        }

        if (currentStage.citizensToKill.isEmpty() == false) {
            for (Integer n : currentStage.citizensToKill) {

                citizensKilled.add(n);
                citizenNumKilled.add(0);

            }
        }

        if (currentStage.blocksToCut.isEmpty() == false) {
            for (Material m : currentStage.blocksToCut.keySet()) {

                blocksCut.put(m, 0);

            }
        }

        if (currentStage.locationsToReach.isEmpty() == false) {
            for (Location l : currentStage.locationsToReach) {

                locationsReached.add(l);
                hasReached.add(false);
                radiiToReachWithin.add(currentStage.radiiToReachWithin.get(locationsReached.indexOf(l)));

            }
        }

        if (currentStage.mobsToTame.isEmpty() == false) {
            for (EntityType e : currentStage.mobsToTame.keySet()) {

                mobsTamed.put(e, 0);

            }
        }

        if (currentStage.sheepToShear.isEmpty() == false) {
            for (DyeColor d : currentStage.sheepToShear.keySet()) {

                sheepSheared.put(d, 0);

            }
        }

        if (currentStage.passwordDisplays.isEmpty() == false) {
            for (String display : currentStage.passwordDisplays) {
                passwordsSaid.put(display, false);
            }
        }

        if (currentStage.customObjectives.isEmpty() == false) {
            for (CustomObjective co : currentStage.customObjectives) {
                customObjectiveCounts.put(co.getName(), 0);
            }
        }

    }

    public void resetObjectives() {

        blocksDamaged.clear();
        blocksBroken.clear();
        blocksPlaced.clear();
        blocksUsed.clear();
        blocksCut.clear();
        fishCaught = 0;
        itemsEnchanted.clear();
        mobsKilled.clear();
        mobNumKilled.clear();
        locationsToKillWithin.clear();
        radiiToKillWithin.clear();
        playersKilled = 0;
        itemsDelivered.clear();
        citizensInteracted.clear();
        citizensKilled.clear();
        citizenNumKilled.clear();
        locationsReached.clear();
        hasReached.clear();
        radiiToReachWithin.clear();
        mobsTamed.clear();
        sheepSheared.clear();
        customObjectiveCounts.clear();
        passwordsSaid.clear();

    }

    public static String getCapitalized(String target) {
        String firstLetter = target.substring(0, 1);
        String remainder = target.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();

        return capitalized;
    }

    public static String prettyItemString(int itemID) {
        String baseString = Material.getMaterial(itemID).toString();
        String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;

        for (String s : substrings) {
            prettyString = prettyString.concat(Quester.getCapitalized(s));

            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }

            size++;
        }

        return prettyString;
    }

    public static String fullPotionString(short dv) {

        Potion potion = Potion.fromDamage(dv);
        String potionName = "";
        boolean isPrimary = false;

        try {

            potionName = "Potion of " + potion.getType().getEffectType().getName();

        } catch (NullPointerException e) { // Potion is primary

            isPrimary = true;

            if (dv == 0) {
                potionName = "Water Bottle";
            } else if (dv == 16) {
                potionName = "Awkward Potion";
            } else if (dv == 32) {
                potionName = "Thick Potion";
            } else if (dv == 64) {
                potionName = "Mundane Potion (Extended)";
            } else if (dv == 8192) {
                potionName = "Mundane Potion";
            }

        }

        if (isPrimary == false) {

            if (potion.hasExtendedDuration()) {
                potionName = potionName + " (Extended)";
            } else if (potion.getLevel() == 2) {
                potionName = potionName + " II";
            }

            if (potion.isSplash()) {
                potionName = "Splash " + potionName;
            }

        }

        return potionName;

    }

    public static String prettyMobString(EntityType type) {

        String baseString = type.toString();
        String[] substrings = baseString.split("_");
        String prettyString = "";
        int size = 1;

        for (String s : substrings) {
            prettyString = prettyString.concat(Quester.getCapitalized(s));

            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }

            size++;
        }

        if (type.equals((EntityType.OCELOT))) {
            prettyString = "Ocelot";
        }

        return prettyString;
    }

    public static String prettyString(String s) {

        String[] substrings = s.split("_");
        String prettyString = "";
        int size = 1;

        for (String sb : substrings) {
            prettyString = prettyString.concat(Quester.getCapitalized(sb));

            if (size < substrings.length) {
                prettyString = prettyString.concat(" ");
            }

            size++;
        }

        return prettyString;

    }

    public static String capitalsToSpaces(String s) {
        
        for(int i = 1; i < s.length(); i++) {
            
            if(Character.isUpperCase(s.charAt(i))) {
                
                s = s.substring(0, i) + " " + s.substring(i);
                
            }
            
        }
        
        return s;
        
    }
    
    public static String spaceToCapital(String s) {
        
        int index = s.indexOf(' ');
        if(index == -1)
            return null;
        
        s = s.substring(0, (index + 1)) + Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
        s = s.replaceFirst(" ", "");
        
        return s;
        
    }
    
    public static String prettyEnchantmentString(Enchantment e) {

        String prettyString = enchantmentString(e);
        prettyString = capitalsToSpaces(prettyString);

        return prettyString;
        
    }

    public static String enchantmentString(Enchantment e) {

        return(Lang.get("ENCHANTMENT_" + e.getName()));

    }

    public static String prettyColorString(DyeColor color) {

        return Lang.get("COLOR_" + color.name());

    }

    public void saveData() {

        FileConfiguration data = getBaseData();
        try {
            data.save(new File(plugin.getDataFolder(), "data/" + name + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public long getDifference(Quest q) {

        long currentTime = System.currentTimeMillis();
        long lastTime;
        if (completedTimes.containsKey(q.name) == false) {
            lastTime = System.currentTimeMillis();
            completedTimes.put(q.name, System.currentTimeMillis());
        } else {
            lastTime = completedTimes.get(q.name);
        }
        long comparator = q.redoDelay;
        long difference = (comparator - (currentTime - lastTime));

        return difference;

    }

    public FileConfiguration getBaseData() {

        FileConfiguration data = new YamlConfiguration();

        if (currentQuest != null) {

            data.set("currentQuest", currentQuest.name);
            data.set("currentStage", currentStageIndex);
            data.set("quest-points", questPoints);

            if (blocksDamaged.isEmpty() == false) {

                LinkedList<Integer> blockIds = new LinkedList<Integer>();
                LinkedList<Integer> blockAmounts = new LinkedList<Integer>();

                for (Material m : blocksDamaged.keySet()) {
                    blockIds.add(m.getId());
                    blockAmounts.add(blocksDamaged.get(m));
                }

                data.set("blocks-damaged-ids", blockIds);
                data.set("blocks-damaged-amounts", blockAmounts);

            }

            if (blocksBroken.isEmpty() == false) {

                LinkedList<Integer> blockIds = new LinkedList<Integer>();
                LinkedList<Integer> blockAmounts = new LinkedList<Integer>();

                for (Material m : blocksBroken.keySet()) {
                    blockIds.add(m.getId());
                    blockAmounts.add(blocksBroken.get(m));
                }

                data.set("blocks-broken-ids", blockIds);
                data.set("blocks-broken-amounts", blockAmounts);

            }

            if (blocksPlaced.isEmpty() == false) {

                LinkedList<Integer> blockIds = new LinkedList<Integer>();
                LinkedList<Integer> blockAmounts = new LinkedList<Integer>();

                for (Material m : blocksPlaced.keySet()) {
                    blockIds.add(m.getId());
                    blockAmounts.add(blocksPlaced.get(m));
                }

                data.set("blocks-placed-ids", blockIds);
                data.set("blocks-placed-amounts", blockAmounts);

            }

            if (blocksUsed.isEmpty() == false) {

                LinkedList<Integer> blockIds = new LinkedList<Integer>();
                LinkedList<Integer> blockAmounts = new LinkedList<Integer>();

                for (Material m : blocksUsed.keySet()) {
                    blockIds.add(m.getId());
                    blockAmounts.add(blocksUsed.get(m));
                }

                data.set("blocks-used-ids", blockIds);
                data.set("blocks-used-amounts", blockAmounts);

            }

            if (blocksCut.isEmpty() == false) {

                LinkedList<Integer> blockIds = new LinkedList<Integer>();
                LinkedList<Integer> blockAmounts = new LinkedList<Integer>();

                for (Material m : blocksCut.keySet()) {
                    blockIds.add(m.getId());
                    blockAmounts.add(blocksCut.get(m));
                }

                data.set("blocks-cut-ids", blockIds);
                data.set("blocks-cut-amounts", blockAmounts);

            }

            if (currentStage.fishToCatch != null) {
                data.set("fish-caught", fishCaught);
            }

            if (currentStage.playersToKill != null) {
                data.set("players-killed", playersKilled);
            }

            if (itemsEnchanted.isEmpty() == false) {

                LinkedList<String> enchantments = new LinkedList<String>();
                LinkedList<Integer> itemIds = new LinkedList<Integer>();
                LinkedList<Integer> enchAmounts = new LinkedList<Integer>();

                for (Entry<Map<Enchantment, Material>, Integer> e : itemsEnchanted.entrySet()) {

                    Map<Enchantment, Material> enchMap = (Map<Enchantment, Material>) e.getKey();
                    enchAmounts.add(itemsEnchanted.get(enchMap));
                    for (Entry<Enchantment, Material> e2 : enchMap.entrySet()) {

                        enchantments.add(Quester.prettyEnchantmentString((Enchantment) e2.getKey()));
                        itemIds.add(((Material) e2.getValue()).getId());

                    }

                }

                data.set("enchantments", enchantments);
                data.set("enchantment-item-ids", itemIds);
                data.set("times-enchanted", enchAmounts);

            }

            if (mobsKilled.isEmpty() == false) {

                LinkedList<String> mobNames = new LinkedList<String>();
                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                LinkedList<String> locations = new LinkedList<String>();
                LinkedList<Integer> radii = new LinkedList<Integer>();

                for (EntityType e : mobsKilled) {

                    mobNames.add(Quester.prettyMobString(e));

                }

                for (int i : mobNumKilled) {

                    mobAmounts.add(i);

                }

                data.set("mobs-killed", mobNames);
                data.set("mobs-killed-amounts", mobAmounts);

                if (locationsToKillWithin.isEmpty() == false) {

                    for (Location l : locationsToKillWithin) {

                        locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());

                    }

                    for (int i : radiiToKillWithin) {

                        radii.add(i);

                    }

                    data.set("mob-kill-locations", locations);
                    data.set("mob-kill-location-radii", radii);

                }

            }

            if (itemsDelivered.isEmpty() == false) {

                LinkedList<Integer> deliveryAmounts = new LinkedList<Integer>();

                for (Entry<ItemStack, Integer> e : itemsDelivered.entrySet()) {

                    deliveryAmounts.add(e.getValue());

                }

                data.set("item-delivery-amounts", deliveryAmounts);

            }

            if (citizensInteracted.isEmpty() == false) {

                LinkedList<Integer> npcIds = new LinkedList<Integer>();
                LinkedList<Boolean> hasTalked = new LinkedList<Boolean>();

                for (Integer n : citizensInteracted.keySet()) {

                    npcIds.add(n);
                    hasTalked.add(citizensInteracted.get(n));

                }

                data.set("citizen-ids-to-talk-to", npcIds);
                data.set("has-talked-to", hasTalked);

            }

            if (citizensKilled.isEmpty() == false) {

                LinkedList<Integer> npcIds = new LinkedList<Integer>();

                for (Integer n : citizensKilled) {

                    npcIds.add(n);

                }

                data.set("citizen-ids-killed", npcIds);
                data.set("citizen-amounts-killed", citizenNumKilled);

            }

            if (locationsReached.isEmpty() == false) {

                LinkedList<String> locations = new LinkedList<String>();
                LinkedList<Boolean> has = new LinkedList<Boolean>();
                LinkedList<Integer> radii = new LinkedList<Integer>();

                for (Location l : locationsReached) {

                    locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());

                }

                for (boolean b : hasReached) {
                    has.add(b);
                }

                for (int i : radiiToReachWithin) {
                    radii.add(i);
                }

                data.set("locations-to-reach", locations);
                data.set("has-reached-location", has);
                data.set("radii-to-reach-within", radii);

            }

            if (potionsBrewed.isEmpty() == false) {

                LinkedList<Integer> potionIds = new LinkedList<Integer>();
                LinkedList<Integer> potionAmounts = new LinkedList<Integer>();

                for (Entry<Integer, Integer> entry : potionsBrewed.entrySet()) {

                    potionIds.add((Integer) entry.getKey());
                    potionAmounts.add((Integer) entry.getValue());

                }

                data.set("potions-brewed-ids", potionIds);
                data.set("potions-brewed-amounts", potionAmounts);

            }

            if (mobsTamed.isEmpty() == false) {

                LinkedList<String> mobNames = new LinkedList<String>();
                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();

                for (EntityType e : mobsTamed.keySet()) {

                    mobNames.add(Quester.prettyMobString(e));
                    mobAmounts.add(mobsTamed.get(e));

                }

                data.set("mobs-to-tame", mobNames);
                data.set("mob-tame-amounts", mobAmounts);

            }

            if (sheepSheared.isEmpty() == false) {

                LinkedList<String> colors = new LinkedList<String>();
                LinkedList<Integer> shearAmounts = new LinkedList<Integer>();

                for (DyeColor d : sheepSheared.keySet()) {

                    colors.add(Quester.prettyColorString(d));
                    shearAmounts.add(sheepSheared.get(d));

                }

                data.set("sheep-to-shear", colors);
                data.set("sheep-sheared", shearAmounts);

            }

            if (passwordsSaid.isEmpty() == false) {

                LinkedList<String> passwords = new LinkedList<String>();
                LinkedList<Boolean> said = new LinkedList<Boolean>();

                for (Entry<String, Boolean> entry : passwordsSaid.entrySet()) {

                    passwords.add(entry.getKey());
                    said.add(entry.getValue());

                }

                data.set("passwords", passwords);
                data.set("passwords-said", said);

            }

            if (customObjectiveCounts.isEmpty() == false) {

                LinkedList<String> customObj = new LinkedList<String>();
                LinkedList<Integer> customObjCounts = new LinkedList<Integer>();

                for (Entry<String, Integer> entry : customObjectiveCounts.entrySet()) {

                    customObj.add(entry.getKey());
                    customObjCounts.add(entry.getValue());

                }

                data.set("custom-objectives", customObj);
                data.set("custom-objective-counts", customObjCounts);

            }

            if (delayTimeLeft > 0) {
                data.set("stage-delay", delayTimeLeft);
            }

            if (eventFired.isEmpty() == false) {

                LinkedList<String> triggers = new LinkedList<String>();
                for (String trigger : eventFired.keySet()) {

                    if (eventFired.get(trigger) == true) {
                        triggers.add(trigger);
                    }

                }

                if (triggers.isEmpty() == false) {
                    data.set("chat-triggers", triggers);
                }

            }

        } else {

            data.set("currentQuest", "none");
            data.set("currentStage", "none");
            data.set("quest-points", questPoints);

        }

        if (completedQuests.isEmpty()) {

            data.set("completed-Quests", "none");

        } else {

            String[] completed = new String[completedQuests.size()];
            for (String s : completedQuests) {

                completed[completedQuests.indexOf(s)] = s;

            }
            data.set("completed-Quests", completed);

        }

        if (completedTimes.isEmpty() == false) {

            List<String> questTimeNames = new LinkedList<String>();
            List<Long> questTimes = new LinkedList<Long>();

            for (String s : completedTimes.keySet()) {

                questTimeNames.add(s);
                questTimes.add(completedTimes.get(s));

            }

            data.set("completedRedoableQuests", questTimeNames);
            data.set("completedQuestTimes", questTimes);

        }

        if (amountsCompleted.isEmpty() == false) {

            List<String> list1 = new LinkedList<String>();
            List<Integer> list2 = new LinkedList<Integer>();

            for (Entry<String, Integer> entry : amountsCompleted.entrySet()) {

                list1.add(entry.getKey());
                list2.add(entry.getValue());

            }

            data.set("amountsCompletedQuests", list1);
            data.set("amountsCompleted", list2);

        }

        return data;

    }

    public boolean loadData() {

        FileConfiguration data = new YamlConfiguration();
        try {
            data.load(new File(plugin.getDataFolder(), "data/" + name + ".yml"));
        } catch (IOException e) {
            return false;
        } catch (InvalidConfigurationException e) {
            return false;
        }

        if (data.contains("completedRedoableQuests")) {

            for (String s : data.getStringList("completedRedoableQuests")) {

                for (Object o : data.getList("completedQuestTimes")) {

                    for (Quest q : plugin.quests) {

                        if (q.name.equalsIgnoreCase(s)) {
                            completedTimes.put(q.name, (Long) o);
                            break;
                        }

                    }

                }

            }

        }

        amountsCompleted.clear();

        if (data.contains("amountsCompletedQuests")) {

            List<String> list1 = data.getStringList("amountsCompletedQuests");
            List<Integer> list2 = data.getIntegerList("amountsCompleted");

            for (int i = 0; i < list1.size(); i++) {

                amountsCompleted.put(list1.get(i), list2.get(i));

            }

        }

        questPoints = data.getInt("quest-points");

        if (data.isList("completed-Quests")) {

            for (String s : data.getStringList("completed-Quests")) {

                for (Quest q : plugin.quests) {

                    if (q.name.equalsIgnoreCase(s)) {
                        completedQuests.add(q.name);
                        break;
                    }

                }

            }

        } else {
            completedQuests.clear();
        }

        if (data.getString("currentQuest").equalsIgnoreCase("none") == false) {

            Quest quest = null;
            Stage stage = null;

            for (Quest q : plugin.quests) {

                if (q.name.equalsIgnoreCase(data.getString("currentQuest"))) {
                    quest = q;
                    break;
                }

            }

            if (quest == null) {
                return true;
            }

            currentStageIndex = data.getInt("currentStage");

            for (Stage s : quest.orderedStages) {

                if (quest.orderedStages.indexOf(s) == (currentStageIndex)) {
                    stage = s;
                    break;
                }

            }

            if (stage == null) {
                currentQuest = quest;
                currentQuest.completeQuest(this);
                Quests.log.log(Level.SEVERE, "[Quests] Invalid stage for player: \"" + name + "\". Quest ended.");
                return true;
            }

            currentQuest = quest;
            currentStage = stage;

            addEmpties();

            if (data.contains("blocks-damaged-ids")) {

                List<Integer> ids = data.getIntegerList("blocks-damaged-ids");
                List<Integer> amounts = data.getIntegerList("blocks-damaged-amounts");

                for (int i : ids) {

                    blocksDamaged.put(Material.getMaterial(i), amounts.get(ids.indexOf(i)));

                }

            }

            if (data.contains("blocks-broken-ids")) {

                List<Integer> ids = data.getIntegerList("blocks-broken-ids");
                List<Integer> amounts = data.getIntegerList("blocks-broken-amounts");

                for (int i : ids) {

                    blocksBroken.put(Material.getMaterial(i), amounts.get(ids.indexOf(i)));

                }

            }

            if (data.contains("blocks-placed-ids")) {

                List<Integer> ids = data.getIntegerList("blocks-placed-ids");
                List<Integer> amounts = data.getIntegerList("blocks-placed-amounts");

                for (int i : ids) {

                    blocksPlaced.put(Material.getMaterial(i), amounts.get(ids.indexOf(i)));

                }

            }

            if (data.contains("blocks-used-ids")) {

                List<Integer> ids = data.getIntegerList("blocks-used-ids");
                List<Integer> amounts = data.getIntegerList("blocks-used-amounts");

                for (int i : ids) {

                    blocksUsed.put(Material.getMaterial(i), amounts.get(ids.indexOf(i)));

                }

            }

            if (data.contains("blocks-cut-ids")) {

                List<Integer> ids = data.getIntegerList("blocks-cut-ids");
                List<Integer> amounts = data.getIntegerList("blocks-cut-amounts");

                for (int i : ids) {

                    blocksCut.put(Material.getMaterial(i), amounts.get(ids.indexOf(i)));

                }

            }

            if (data.contains("fish-caught")) {
                fishCaught = data.getInt("fish-caught");
            }

            if (data.contains("players-killed")) {

                playersKilled = data.getInt("players-killed");

                List<String> playerNames = data.getStringList("player-killed-names");
                List<Long> killTimes = data.getLongList("kill-times");

                for (String s : playerNames) {

                    playerKillTimes.put(s, killTimes.get(playerNames.indexOf(s)));

                }

            }

            if (data.contains("enchantments")) {

                LinkedList<Enchantment> enchantments = new LinkedList<Enchantment>();
                LinkedList<Material> materials = new LinkedList<Material>();
                LinkedList<Integer> amounts = new LinkedList<Integer>();

                List<String> enchantNames = data.getStringList("enchantments");
                List<Integer> ids = data.getIntegerList("enchantment-item-ids");

                for (String s : enchantNames) {

                    enchantments.add(Quests.getEnchantment(s));

                    materials.add(Material.getMaterial(ids.get(enchantNames.indexOf(s))));
                    amounts.add(enchantNames.indexOf(s));

                }

                for (Enchantment e : enchantments) {

                    Map<Enchantment, Material> map = new HashMap<Enchantment, Material>();
                    map.put(e, materials.get(enchantments.indexOf(e)));

                    itemsEnchanted.put(map, amounts.get(enchantments.indexOf(e)));

                }

            }

            if (data.contains("mobs-killed")) {

                LinkedList<EntityType> mobs = new LinkedList<EntityType>();
                List<Integer> amounts = data.getIntegerList("mobs-killed-amounts");

                for (String s : data.getStringList("mobs-killed")) {

                    EntityType mob = Quests.getMobType(s);
                    if (mob != null) {
                        mobs.add(mob);
                    }

                    mobsKilled.clear();
                    mobNumKilled.clear();

                    for (EntityType e : mobs) {

                        mobsKilled.add(e);
                        mobNumKilled.add(amounts.get(mobs.indexOf(e)));

                    }

                    if (data.contains("mob-kill-locations")) {

                        LinkedList<Location> locations = new LinkedList<Location>();
                        List<Integer> radii = data.getIntegerList("mob-kill-location-radii");

                        for (String loc : data.getStringList("mob-kill-locations")) {

                            String[] info = loc.split(" ");
                            double x = Double.parseDouble(info[1]);
                            double y = Double.parseDouble(info[2]);
                            double z = Double.parseDouble(info[3]);
                            Location finalLocation = new Location(plugin.getServer().getWorld(info[0]), x, y, z);
                            locations.add(finalLocation);

                        }

                        locationsToKillWithin = locations;
                        radiiToKillWithin.clear();
                        for (int i : radii) {
                            radiiToKillWithin.add(i);
                        }

                    }

                }

            }

            if (data.contains("item-delivery-amounts")) {

                List<Integer> deliveryAmounts = data.getIntegerList("item-delivery-amounts");

                for (int i = 0; i < deliveryAmounts.size(); i++) {

                    itemsDelivered.put(currentStage.itemsToDeliver.get(i), deliveryAmounts.get(i));

                }

            }

            if (data.contains("citizen-ids-to-talk-to")) {

                List<Integer> ids = data.getIntegerList("citizen-ids-to-talk-to");
                List<Boolean> has = data.getBooleanList("has-talked-to");

                for (int i : ids) {

                    citizensInteracted.put(i, has.get(ids.indexOf(i)));

                }

            }

            if (data.contains("citizen-ids-killed")) {

                List<Integer> ids = data.getIntegerList("citizen-ids-killed");
                List<Integer> num = data.getIntegerList("citizen-amounts-killed");

                citizensKilled.clear();
                citizenNumKilled.clear();

                for (int i : ids) {

                    citizensKilled.add(i);
                    citizenNumKilled.add(num.get(ids.indexOf(i)));

                }

            }

            if (data.contains("locations-to-reach")) {

                LinkedList<Location> locations = new LinkedList<Location>();
                List<Boolean> has = data.getBooleanList("has-reached-location");
                List<Integer> radii = data.getIntegerList("radii-to-reach-within");

                for (String loc : data.getStringList("locations-to-reach")) {

                    String[] info = loc.split(" ");
                    double x = Double.parseDouble(info[1]);
                    double y = Double.parseDouble(info[2]);
                    double z = Double.parseDouble(info[3]);
                    Location finalLocation = new Location(plugin.getServer().getWorld(info[0]), x, y, z);
                    locations.add(finalLocation);

                }

                locationsReached = locations;
                hasReached.clear();
                radiiToReachWithin.clear();

                for (boolean b : has) {
                    hasReached.add(b);
                }

                for (int i : radii) {
                    radiiToReachWithin.add(i);
                }

            }

            if (data.contains("potions-brewed-ids")) {

                List<Integer> ids = data.getIntegerList("potions-brewed-ids");
                List<Integer> amounts = data.getIntegerList("potions-brewed-amounts");

                for (int i : ids) {

                    potionsBrewed.put(i, amounts.get(ids.indexOf(i)));

                }

            }

            if (data.contains("mobs-to-tame")) {

                List<String> mobs = data.getStringList("mobs-to-tame");
                List<Integer> amounts = data.getIntegerList("mob-tame-amounts");

                for (String mob : mobs) {

                    if (mob.equalsIgnoreCase("Wolf")) {

                        mobsTamed.put(EntityType.WOLF, amounts.get(mobs.indexOf(mob)));

                    } else {

                        mobsTamed.put(EntityType.OCELOT, amounts.get(mobs.indexOf(mob)));

                    }

                }

            }

            if (data.contains("sheep-to-shear")) {

                List<String> colors = data.getStringList("sheep-to-shear");
                List<Integer> amounts = data.getIntegerList("sheep-sheared");

                for (String color : colors) {

                    sheepSheared.put(Quests.getDyeColor(color), amounts.get(colors.indexOf(color)));

                }

            }

            if (data.contains("passwords")) {

                List<String> passwords = data.getStringList("passwords");
                List<Boolean> said = data.getBooleanList("passwords-said");
                for(int i = 0; i < passwords.size(); i++){
                    passwordsSaid.put(passwords.get(i), said.get(i));
                }
                
            }

            if (data.contains("custom-objectives")) {

                List<String> customObj = data.getStringList("custom-objectives");
                List<Integer> customObjCount = data.getIntegerList("custom-objective-counts");

                for (int i = 0; i < customObj.size(); i++) {
                    customObjectiveCounts.put(customObj.get(i), customObjCount.get(i));
                }

            }

            if (data.contains("stage-delay")) {

                delayTimeLeft = data.getLong("stage-delay");

            }

            if (currentStage.chatEvents.isEmpty() == false) {

                for (String trig : currentStage.chatEvents.keySet()) {

                    eventFired.put(trig, false);

                }

            }

            if (data.contains("chat-triggers")) {

                List<String> triggers = data.getStringList("chat-triggers");
                for (String s : triggers) {

                    eventFired.put(s, true);

                }

            }

        }

        return true;

    }

    public void startStageTimer() {

        if (delayTimeLeft > -1) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this), (long) (delayTimeLeft * 0.02));
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this), (long) (currentStage.delay * 0.02));
            if (currentStage.delayMessage != null) {
                plugin.getServer().getPlayer(name).sendMessage(Quests.parseString((currentStage.delayMessage), currentQuest));
            }
        }

        delayStartTime = System.currentTimeMillis();

    }

    public void stopStageTimer() {

        if (delayTimeLeft > -1) {
            delayTimeLeft = delayTimeLeft - (System.currentTimeMillis() - delayStartTime);
        } else {
            delayTimeLeft = currentStage.delay - (System.currentTimeMillis() - delayStartTime);
        }

        delayOver = false;

    }

    public long getStageTime() {

        if (delayTimeLeft > -1) {
            return delayTimeLeft - (System.currentTimeMillis() - delayStartTime);
        } else {
            return currentStage.delay - (System.currentTimeMillis() - delayStartTime);
        }

    }

    public boolean hasData() {

        if (currentQuest != null || currentStage != null) {
            return true;
        }

        if (questPoints > 1) {
            return true;
        }

        return completedQuests.isEmpty() == false;

    }

    public void checkQuest() {

        if (currentQuest != null) {

            boolean exists = false;

            for (Quest q : plugin.quests) {

                if (q.name.equalsIgnoreCase(currentQuest.name)) {

                    exists = true;
                    if (q.equals(currentQuest) == false) {

                        currentStage = null;
                        currentStageIndex = 0;
                        resetObjectives();
                        if (plugin.getServer().getPlayer(name) != null) {
                            String error = Lang.get("questModified");
                            error = error.replaceAll("<quest>", ChatColor.DARK_PURPLE + currentQuest.name + ChatColor.RED);
                            plugin.getServer().getPlayer(name).sendMessage(ChatColor.GOLD + "[Quests] " + ChatColor.RED + error);
                        }
                        currentQuest = null;

                    }

                    break;

                }

            }

            if (exists == false) {

                currentStage = null;
                currentStageIndex = 0;
                resetObjectives();
                if (plugin.getServer().getPlayer(name) != null) {
                    String error = Lang.get("questNotExist");
                    error = error.replaceAll("<quest>", ChatColor.DARK_PURPLE + currentQuest.name + ChatColor.RED);
                    plugin.getServer().getPlayer(name).sendMessage(ChatColor.GOLD + "[Quests] " + ChatColor.RED + error);
                }
                currentQuest = null;

            }

        }

    }

    public static String checkPlacement(Inventory inv, int rawSlot) {

        if (rawSlot < 0) {
            return Lang.get("questNoDrop");
        }

        InventoryType type = inv.getType();

        if (type.equals(InventoryType.BREWING)) {

            if (rawSlot < 4) {
                return Lang.get("questNoBrew");
            }

        } else if (type.equals(InventoryType.CHEST)) {

            if (inv.getContents().length == 27) {
                if (rawSlot < 27) {
                    return Lang.get("questNoStore");
                }

            } else {
                if (rawSlot < 54) {
                    return Lang.get("questNoStore");
                }

            }

        } else if (type.equals(InventoryType.CRAFTING)) {

            if (rawSlot < 5) {
                return Lang.get("questNoCraft");
            } else if (rawSlot < 9) {
                return Lang.get("questNoEquip");
            }

        } else if (type.equals(InventoryType.DISPENSER)) {

            if (rawSlot < 9) {
                return Lang.get("questNoDispense");
            }

        } else if (type.equals(InventoryType.ENCHANTING)) {

            if (rawSlot == 0) {
                return Lang.get("questNoEnchant");
            }

        } else if (type.equals(InventoryType.ENDER_CHEST)) {

            if (rawSlot < 27) {
                return Lang.get("questNoStore");
            }

        } else if (type.equals(InventoryType.FURNACE)) {

            if (rawSlot < 3) {
                return Lang.get("questNoSmelt");
            }

        } else if (type.equals(InventoryType.WORKBENCH)) {

            if (rawSlot < 10) {
                return Lang.get("questNoCraft");
            }

        }
        return null;

    }

    public static List<Integer> getChangedSlots(Inventory inInv, ItemStack inNew) {
        List<Integer> changed = new ArrayList<Integer>();
        if (inInv.contains(inNew.getType())) {
            int amount = inNew.getAmount();
            HashMap<Integer, ? extends ItemStack> items = inInv.all(inNew.getType());
            for (int i = 0; i < inInv.getSize(); i++) {
                if (!items.containsKey((Integer) i)) {
                    continue;
                }

                ItemStack item = items.get((Integer) i);
                int slotamount = item.getMaxStackSize() - item.getAmount();
                if (slotamount > 1) {
                    if (amount > slotamount) {
                        int toAdd = slotamount - amount;
                        amount = amount - toAdd;
                        changed.add(i);
                    } else {
                        changed.add(i);
                        amount = 0;
                        break;
                    }
                }
            }

            if (amount > 0) {
                if (inInv.firstEmpty() != -1) {
                    changed.add(inInv.firstEmpty());
                }
            }
        } else {
            if (inInv.firstEmpty() != -1) {
                changed.add(inInv.firstEmpty());
            }
        }
        return changed;
    }
    
    public void showGUIDisplay(LinkedList<Quest> quests) {
        
        Player player = getPlayer();
        int size = ((quests.size() / 9) + 1) * 9;
        
        Inventory inv = Bukkit.getServer().createInventory(player, size, "Quests");
        
        
        
        int inc = 0;
        for(int i = 0; i < quests.size(); i++) {
            
            if(quests.get(i).guiDisplay != null) {
                inv.setItem(inc, quests.get(i).guiDisplay);
                inc++;
            }
            
        }
        
        player.openInventory(inv);
        
    }
    
}
