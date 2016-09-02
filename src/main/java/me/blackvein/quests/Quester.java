package me.blackvein.quests;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.item.Items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;

public class Quester {

    UUID id;
    boolean editorMode = false;
    boolean hasJournal = false;

    public String questToTake;
    public Map<Quest, Integer> currentQuests = new ConcurrentHashMap<Quest, Integer>() {

		private static final long serialVersionUID = 6361484975823846780L;

		@Override
        public Integer put(Quest key, Integer val) {
            Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(Map<? extends Quest, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }

    };

    int questPoints = 0;
    Quests plugin;
    public LinkedList<String> completedQuests = new LinkedList<String>() {

		private static final long serialVersionUID = -269110128568487000L;

		@Override
        public boolean add(String e) {
            boolean b = super.add(e);
            updateJournal();
            return b;
        }

        @Override
        public void add(int index, String element) {
            super.add(index, element);
            updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            boolean b = super.addAll(c);
            updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends String> c) {
            boolean b = super.addAll(index, c);
            updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            updateJournal();
            return b;
        }

        @Override
        public String remove(int index) {
            String s = super.remove(index);
            updateJournal();
            return s;
        }

        @Override
        public String set(int index, String element) {
            String s = super.set(index, element);
            updateJournal();
            return s;
        }

    };

    Map<String, Long> completedTimes = new HashMap<String, Long>();

    Map<String, Integer> amountsCompleted = new HashMap<String, Integer>() {

		private static final long serialVersionUID = 5475202358792520975L;

		/*@SuppressWarnings("unused")
		public void hardClear() {
            super.clear();
        }*/

        @Override
        public Integer put(String key, Integer val) {
            Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }

    };


    Map<Quest, QuestData> questData = new HashMap<Quest, QuestData>() {

		private static final long serialVersionUID = -4607112433003926066L;

		@Override
        public QuestData put(Quest key, QuestData val) {
            QuestData data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public QuestData remove(Object key) {
            QuestData data = super.remove(key);
            updateJournal();
            return data;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(Map<? extends Quest, ? extends QuestData> m) {
            super.putAll(m);
            updateJournal();
        }

    };

    final Random random = new Random();

    public Quester(Quests newPlugin) {

        plugin = newPlugin;

    }

    public Player getPlayer() {

        return Bukkit.getServer().getPlayer(id);

    }

    public OfflinePlayer getOfflinePlayer() {

        return Bukkit.getServer().getOfflinePlayer(id);

    }

    public void updateJournal() {

        if(!hasJournal)
            return;

        Inventory inv = getPlayer().getInventory();
        ItemStack[] arr = inv.getContents();
        int index = -1;

        for(int i = 0; i < arr.length; i++) {

            if(arr[i] != null) {

                if(ItemUtil.isJournal(arr[i])) {
                    index = i;
                    break;
                }

            }

        }

        if(index != -1) {

            ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + Lang.get("journalTitle"));

            BookMeta book = (BookMeta) meta;
            book.setTitle(ChatColor.LIGHT_PURPLE + Lang.get("journalTitle"));
            book.setAuthor(getPlayer().getName());

            if(currentQuests.isEmpty()) {

            	book.addPage(ChatColor.DARK_RED + Lang.get("journalNoQuests"));

            } else {

                int currentLength = 0;
                int currentLines = 0;
                String page = "";

                for(Quest quest : currentQuests.keySet()) {

                    if((currentLength + quest.name.length() > 240) || (currentLines + ((quest.name.length() % 19) == 0 ? (quest.name.length() / 19) : ((quest.name.length() / 19) + 1))) > 13) {

                        book.addPage(page);
                        page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.name + "\n";
                        currentLength = quest.name.length();
                        currentLines = (quest.name.length() % 19) == 0 ? (quest.name.length() / 19) : (quest.name.length() + 1);

                    } else {

                        page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.name + "\n";
                        currentLength += quest.name.length();
                        currentLines += (quest.name.length() / 19);

                    }

                    for(String obj : getObjectivesReal(quest)) {

                        //Length/Line check
                        if((currentLength + obj.length() > 240) || (currentLines + ((obj.length() % 19) == 0 ? (obj.length() / 19) : ((obj.length() / 19) + 1))) > 13) {
                            book.addPage(page);
                            page = obj + "\n";
                            currentLength = obj.length();
                            currentLines = (obj.length() % 19) == 0 ? (obj.length() / 19) : (obj.length() + 1);
                        } else {
                            page += obj + "\n";
                            currentLength += obj.length();
                            currentLines += (obj.length() / 19);
                        }

                    }

                    if(currentLines < 13)
                        page += "\n";

                    book.addPage(page);
                    page = "";
                    currentLines = 0;
                    currentLength = 0;

                }

            }

            stack.setItemMeta(book);
            inv.setItem(index, stack);

        }

    }

    public Stage getCurrentStage(Quest quest) {
    	if (currentQuests.containsKey(quest)) {
    		return quest.getStage(currentQuests.get(quest));
    	}
        return null;
    }

    public QuestData getQuestData(Quest quest) {
    	if (questData.containsKey(quest)) {
    		return questData.get(quest);
    	}
    	return null;
    }

    public void takeQuest(Quest q, boolean override) {

        Player player = getPlayer();

        if (q.testRequirements(player) == true || override) {

            addEmpties(q);
            currentQuests.put(q, 0);
            Stage stage = q.getStage(0);

            if (!override) {

                if (q.moneyReq > 0) {
                    Quests.economy.withdrawPlayer(getOfflinePlayer(), q.moneyReq);
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

            String msg = Lang.get("questObjectivesTitle");
            msg = msg.replaceAll("<quest>", q.name);
            getPlayer().sendMessage(ChatColor.GOLD + msg);

            for (String s : getObjectivesReal(q)) {
                player.sendMessage(s);
            }

            String stageStartMessage = stage.startMessage;
            if (stageStartMessage != null) {
                getPlayer().sendMessage(Quests.parseString(stageStartMessage, q));
            }

            if (stage.chatEvents.isEmpty() == false) {

                for (String chatTrigger : stage.chatEvents.keySet()) {

                    questData.get(q).eventFired.put(chatTrigger, false);

                }

            }

            if (q.initialEvent != null) {
                q.initialEvent.fire(this, q);
            }
            if (stage.startEvent != null) {
                stage.startEvent.fire(this, q);
            }
            q.updateCompass(this, stage);

            saveData();

        } else {

            player.sendMessage(q.failRequirements);

        }

    }

    public LinkedList<String> getObjectivesReal(Quest quest) {

        if (getCurrentStage(quest).objectiveOverride != null) {
            LinkedList<String> objectives = new LinkedList<String>();
            objectives.add(ChatColor.GREEN + getCurrentStage(quest).objectiveOverride);
            return objectives;
        } else {
            return getObjectives(quest);
        }

    }

    public LinkedList<String> getObjectives(Quest quest) {

        if(getQuestData(quest) == null)
            return new LinkedList<String>();

        LinkedList<String> unfinishedObjectives = new LinkedList<String>();
        LinkedList<String> finishedObjectives = new LinkedList<String>();
        LinkedList<String> objectives = new LinkedList<String>();

        for (ItemStack e : getCurrentStage(quest).blocksToDamage) {

        	for (ItemStack e2 : getQuestData(quest).blocksDamaged) {

                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {

                    if (e2.getAmount() < e.getAmount()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("damage") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("damage") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    }

                }

            }

        }

        for (ItemStack e : getCurrentStage(quest).blocksToBreak) {

        	for (ItemStack e2 : getQuestData(quest).blocksBroken) {
        		
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {

                    if (e2.getAmount() < e.getAmount()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("break") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get("break") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    }

                }

            }

        }

        for (ItemStack e : getCurrentStage(quest).blocksToPlace) {

        	for (ItemStack e2 : getQuestData(quest).blocksPlaced) {

                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {

                    if (e2.getAmount() < e.getAmount()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("place") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("place") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    }

                }

            }

        }

        for (ItemStack e : getCurrentStage(quest).blocksToUse) {

        	for (ItemStack e2 : getQuestData(quest).blocksUsed) {

                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {

                    if (e2.getAmount() < e.getAmount()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("use") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("use") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    }

                }

            }

        }

        for (ItemStack e : getCurrentStage(quest).blocksToCut) {

        	for (ItemStack e2 : getQuestData(quest).blocksCut) {

                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {

                    if (e2.getAmount() < e.getAmount()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("cut") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("cut") + " " + Items.itemByStack(e2).getName() + ": " + e2.getAmount() + "/" + e.getAmount());

                    }

                }

            }

        }

        if (getCurrentStage(quest).fishToCatch != null) {

            if (getQuestData(quest).getFishCaught() < getCurrentStage(quest).fishToCatch) {

                unfinishedObjectives.add(ChatColor.GREEN + Lang.get("catchFish") + ": " + getQuestData(quest).getFishCaught() + "/" + getCurrentStage(quest).fishToCatch);

            } else {

                finishedObjectives.add(ChatColor.GRAY + Lang.get("catchFish") + ": " + getQuestData(quest).getFishCaught() + "/" + getCurrentStage(quest).fishToCatch);

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

        for (Entry<Map<Enchantment, Material>, Integer> e : getCurrentStage(quest).itemsToEnchant.entrySet()) {

            for (Entry<Map<Enchantment, Material>, Integer> e2 : getQuestData(quest).itemsEnchanted.entrySet()) {

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
                        obj = obj.replaceAll("<item>", Quester.prettyItemString(mat.name()));
                        obj = obj.replaceAll("<enchantment>", Quester.prettyEnchantmentString(enchantment));
                        unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + num1 + "/" + num2);

                    } else {

                        String obj = Lang.get("enchantItem");
                        obj = obj.replaceAll("<item>", Quester.prettyItemString(mat.name()));
                        obj = obj.replaceAll("<enchantment>", Quester.prettyEnchantmentString(enchantment));
                        finishedObjectives.add(ChatColor.GRAY + obj + ": " + num1 + "/" + num2);

                    }

                }

            }

        }

        for (EntityType e : getCurrentStage(quest).mobsToKill) {

            for (EntityType e2 : getQuestData(quest).mobsKilled) {

                if (e == e2) {
                	if (getQuestData(quest).mobNumKilled.size() > getQuestData(quest).mobsKilled.indexOf(e2) && getCurrentStage(quest).mobNumToKill.size() > getCurrentStage(quest).mobsToKill.indexOf(e)) {

                		if (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2)) < getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))) {

                			if (getCurrentStage(quest).locationsToKillWithin.isEmpty()) {
                				unfinishedObjectives.add(ChatColor.GREEN + Lang.get("kill") + " " + Quester.prettyMobString(e) + ": " + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                			} else {
                				String obj = Lang.get("killAtLocation");
                				obj = obj.replaceAll("<mob>", Quester.prettyMobString(e));
                				obj = obj.replaceAll("<location>", getCurrentStage(quest).areaNames.get(getCurrentStage(quest).mobsToKill.indexOf(e)));
                				unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                			}
                		} else {

                			if (getCurrentStage(quest).locationsToKillWithin.isEmpty()) {
                				finishedObjectives.add(ChatColor.GRAY + Lang.get("kill") + " " + Quester.prettyMobString(e) + ": " + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                			} else {
                				String obj = Lang.get("killAtLocation");
                				obj = obj.replaceAll("<mob>", Quester.prettyMobString(e));
                				obj = obj.replaceAll("<location>", getCurrentStage(quest).areaNames.get(getCurrentStage(quest).mobsToKill.indexOf(e)));
                				finishedObjectives.add(ChatColor.GRAY + obj + ": " + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                			}

                		}
                	}

                }

            }

        }

        if (getCurrentStage(quest).playersToKill != null) {

            if (getQuestData(quest).getPlayersKilled() < getCurrentStage(quest).playersToKill) {

                unfinishedObjectives.add(ChatColor.GREEN + Lang.get("killPlayer") + ": " + getQuestData(quest).getPlayersKilled() + "/" + getCurrentStage(quest).playersToKill);

            } else {

                finishedObjectives.add(ChatColor.GRAY + Lang.get("killPlayer") + ": " + getQuestData(quest).getPlayersKilled() + "/" + getCurrentStage(quest).playersToKill);

            }

        }

        for (ItemStack is : getCurrentStage(quest).itemsToDeliver) {
        	
        	int delivered = 0;
        	int amt = is.getAmount();
        	Integer npc = null;
        	
            if (getQuestData(quest).itemsDelivered.get(is) != null) {
            	delivered = getQuestData(quest).itemsDelivered.get(is);
            }
            if (getCurrentStage(quest).itemDeliveryTargets.get(getCurrentStage(quest).itemsToDeliver.indexOf(is)) != null) {
            	npc = getCurrentStage(quest).itemDeliveryTargets.get(getCurrentStage(quest).itemsToDeliver.indexOf(is));
            }
        	
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

        for (Integer n : getCurrentStage(quest).citizensToInteract) {

            for (Entry<Integer, Boolean> e : getQuestData(quest).citizensInteracted.entrySet()) {

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

        for (Integer n : getCurrentStage(quest).citizensToKill) {

            for (Integer n2 : getQuestData(quest).citizensKilled) {

                if (n.equals(n2)) {
                    if (getQuestData(quest).citizenNumKilled.size() > getQuestData(quest).citizensKilled.indexOf(n2) && getCurrentStage(quest).citizenNumToKill.size() > getCurrentStage(quest).citizensToKill.indexOf(n)) {

                    	if (getQuestData(quest).citizenNumKilled.get(getQuestData(quest).citizensKilled.indexOf(n2)) < getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n))) {

                    		unfinishedObjectives.add(ChatColor.GREEN + Lang.get("kill") + " " + plugin.getNPCName(n) + ChatColor.GREEN + " " + getQuestData(quest).citizenNumKilled.get(getCurrentStage(quest).citizensToKill.indexOf(n)) + "/" + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n)));

                    	} else {

                    		finishedObjectives.add(ChatColor.GRAY + Lang.get("kill") + " " + plugin.getNPCName(n) + " " + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n)) + "/" + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n)));

                    	}
                    }
                }

            }

        }

        for (Entry<EntityType, Integer> e : getCurrentStage(quest).mobsToTame.entrySet()) {

            for (Entry<EntityType, Integer> e2 : getQuestData(quest).mobsTamed.entrySet()) {

                if (e.getKey().equals(e2.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get("tame") + " " + getCapitalized(e.getKey().name()) + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        finishedObjectives.add(ChatColor.GRAY + Lang.get("tame") + " " + getCapitalized(e.getKey().name()) + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Entry<DyeColor, Integer> e : getCurrentStage(quest).sheepToShear.entrySet()) {

            for (Entry<DyeColor, Integer> e2 : getQuestData(quest).sheepSheared.entrySet()) {

                if (e.getKey().equals(e2.getKey())) {

                    if (e2.getValue() < e.getValue()) {

                        String obj = Lang.get("shearSheep");
                        obj = obj.replaceAll("<color>", e.getKey().name().toLowerCase());
                        unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + e2.getValue() + "/" + e.getValue());

                    } else {

                        String obj = Lang.get("shearSheep");
                        obj = obj.replaceAll("<color>", e.getKey().name().toLowerCase());
                        finishedObjectives.add(ChatColor.GRAY + obj + ": " + e2.getValue() + "/" + e.getValue());

                    }

                }

            }

        }

        for (Location l : getCurrentStage(quest).locationsToReach) {

            for (Location l2 : getQuestData(quest).locationsReached) {

                if (l.equals(l2)) {
                	if (!getQuestData(quest).hasReached.isEmpty()) {

                		if (getQuestData(quest).hasReached.get(getQuestData(quest).locationsReached.indexOf(l2)) == false) {

                			String obj = Lang.get("goTo");
                			obj = obj.replaceAll("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest).locationsToReach.indexOf(l)));
                			unfinishedObjectives.add(ChatColor.GREEN + obj);

                		} else {

                			String obj = Lang.get("goTo");
                			obj = obj.replaceAll("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest).locationsToReach.indexOf(l)));
                        	finishedObjectives.add(ChatColor.GRAY + obj);

                		}

                	}

                }

            }

        }

        for (String s : getCurrentStage(quest).passwordDisplays) {

            if (getQuestData(quest).passwordsSaid.get(s) == false) {

                unfinishedObjectives.add(ChatColor.GREEN + s);

            } else {

                finishedObjectives.add(ChatColor.GRAY + s);

            }

        }

        int index = 0;
        for (CustomObjective co : getCurrentStage(quest).customObjectives) {

            for (Entry<String, Integer> entry : getQuestData(quest).customObjectiveCounts.entrySet()) {

                if (co.getName().equals(entry.getKey())) {

                    String display = co.getDisplay();

                    Map<String, Object> datamap = getCurrentStage(quest).customObjectiveData.get(index);
                    for (String key : co.datamap.keySet()) {
                        display = display.replaceAll("%" + ((String) key) + "%", ((String) datamap.get(key)));
                    }

                    if (entry.getValue() < getCurrentStage(quest).customObjectiveCounts.get(index)) {
                        if (co.isCountShown() && co.isEnableCount()) {
                            display = display.replaceAll("%count%", entry.getValue() + "/" + getCurrentStage(quest).customObjectiveCounts.get(index));
                        }
                        unfinishedObjectives.add(ChatColor.GREEN + display);
                    } else {
                        if (co.isCountShown() && co.isEnableCount()) {
                            display = display.replaceAll("%count%", getCurrentStage(quest).customObjectiveCounts.get(index) + "/" + getCurrentStage(quest).customObjectiveCounts.get(index));
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

    public boolean hasObjective(Quest quest, String s) {

        if (getCurrentStage(quest) == null) {
            return false;
        }

        if (s.equalsIgnoreCase("damageBlock")) {
            return !getCurrentStage(quest).blocksToDamage.isEmpty();

        } else if (s.equalsIgnoreCase("breakBlock")) {
            return !getCurrentStage(quest).blocksToBreak.isEmpty();

        } else if (s.equalsIgnoreCase("placeBlock")) {
            return !getCurrentStage(quest).blocksToPlace.isEmpty();

        } else if (s.equalsIgnoreCase("useBlock")) {
            return !getCurrentStage(quest).blocksToUse.isEmpty();

        } else if (s.equalsIgnoreCase("cutBlock")) {
            return !getCurrentStage(quest).blocksToCut.isEmpty();

        } else if (s.equalsIgnoreCase("catchFish")) {
            return getCurrentStage(quest).fishToCatch != null;

        } else if (s.equalsIgnoreCase("enchantItem")) {
            return !getCurrentStage(quest).itemsToEnchant.isEmpty();

        } else if (s.equalsIgnoreCase("killMob")) {
            return !getCurrentStage(quest).mobsToKill.isEmpty();

        } else if (s.equalsIgnoreCase("deliverItem")) {
            return !getCurrentStage(quest).itemsToDeliver.isEmpty();

        } else if (s.equalsIgnoreCase("killPlayer")) {
            return getCurrentStage(quest).playersToKill != null;

        } else if (s.equalsIgnoreCase("talkToNPC")) {
            return !getCurrentStage(quest).citizensToInteract.isEmpty();

        } else if (s.equalsIgnoreCase("killNPC")) {
            return !getCurrentStage(quest).citizensToKill.isEmpty();

        } else if (s.equalsIgnoreCase("tameMob")) {
            return !getCurrentStage(quest).mobsToTame.isEmpty();

        } else if (s.equalsIgnoreCase("shearSheep")) {
            return !getCurrentStage(quest).sheepToShear.isEmpty();

        } else if (s.equalsIgnoreCase("craftItem")) {
            return !getCurrentStage(quest).itemsToCraft.isEmpty();

        } else if (s.equalsIgnoreCase("password")) {
            return !getCurrentStage(quest).passwordPhrases.isEmpty();

        } else {
            return !getCurrentStage(quest).locationsToReach.isEmpty();

        }

    }

    public boolean hasCustomObjective(Quest quest, String s) {

        if (getQuestData(quest).customObjectiveCounts.containsKey(s)) {

            int count = getQuestData(quest).customObjectiveCounts.get(s);

            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
                if (getCurrentStage(quest).customObjectives.get(i).getName().equals(s)) {
                    index = i;
                    break;
                }
            }

            int count2 = getCurrentStage(quest).customObjectiveCounts.get(index);

            return count <= count2;

        }

        return false;

    }

    public void damageBlock(Quest quest, ItemStack m) {
    	ItemStack temp = m;
    	temp.setAmount(0);
    	ItemStack damaged = temp;
    	ItemStack toDamage = temp;
    	
    	for (ItemStack is : getQuestData(quest).blocksDamaged) {

    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			damaged = is;
    		}
    	}

    	for (ItemStack is : getCurrentStage(quest).blocksToDamage) {
    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			toDamage = is;
    		}
    	}

    	if (damaged != null && toDamage != null) {
    		if (damaged.getAmount() < toDamage.getAmount()) {
        		ItemStack newDamaged = damaged;
    			newDamaged.setAmount(damaged.getAmount() + 1);
        		
        		//TODO is this correct?
        		getQuestData(quest).blocksDamaged.set(getQuestData(quest).blocksDamaged.indexOf(damaged), newDamaged);

        		if (damaged.getAmount() == toDamage.getAmount()) {
                    finishObjective(quest, "damageBlock", m, null, null, null, null, null, null, null, null, null);
                }
        	}
    	} else {
    		//Do nothing
    		System.out.println(quest + " somehow threw a null value. Please report on Github!");
    	}

    }

    //Called when a block is broken in PlayerListener, hence ItemStack m will always have amount of 1
    public void breakBlock(Quest quest, ItemStack m) {
    	ItemStack temp = m;
    	temp.setAmount(0);
    	ItemStack broken = temp;
    	ItemStack toBreak = temp;
    	
    	for (ItemStack is : getQuestData(quest).blocksBroken) {

    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			broken = is;
    		}
    	}

    	for (ItemStack is : getCurrentStage(quest).blocksToBreak) {
    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			toBreak = is;
    		}
    	}

    	if (broken != null && toBreak != null) {
    		if (broken.getAmount() < toBreak.getAmount()) {
        		ItemStack newBroken = broken;
    			newBroken.setAmount(broken.getAmount() + 1);
        		
        		//TODO is this correct?
        		getQuestData(quest).blocksBroken.set(getQuestData(quest).blocksBroken.indexOf(broken), newBroken);

        		if (broken.getAmount() == toBreak.getAmount()) {
                    finishObjective(quest, "breakBlock", m, null, null, null, null, null, null, null, null, null);
                }
        	}
    	} else {
    		//Do nothing
    		System.out.println(quest + " somehow threw a null value. Please report on Github!");
    	}

    }

    public void placeBlock(Quest quest, ItemStack m) {
    	ItemStack temp = m;
    	temp.setAmount(0);
    	ItemStack placed = temp;
    	ItemStack toPlace = temp;
    	
    	for (ItemStack is : getQuestData(quest).blocksPlaced) {

    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			placed = is;
    		}
    	}

    	for (ItemStack is : getCurrentStage(quest).blocksToPlace) {
    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			toPlace = is;
    		}
    	}

    	if (placed != null && toPlace != null) {
    		if (placed.getAmount() < toPlace.getAmount()) {
        		ItemStack newplaced = placed;
    			newplaced.setAmount(placed.getAmount() + 1);
        		
        		//TODO is this correct?
        		getQuestData(quest).blocksPlaced.set(getQuestData(quest).blocksPlaced.indexOf(placed), newplaced);

        		if (placed.getAmount() == toPlace.getAmount()) {
                    finishObjective(quest, "placeBlock", m, null, null, null, null, null, null, null, null, null);
                }
        	}
    	} else {
    		//Do nothing
    		System.out.println(quest + " somehow threw a null value. Please report on Github!");
    	}

    }

    public void useBlock(Quest quest, ItemStack m) {
    	ItemStack temp = m;
    	temp.setAmount(0);
    	ItemStack used = temp;
    	ItemStack toUse = temp;
    	
    	for (ItemStack is : getQuestData(quest).blocksUsed) {

    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			used = is;
    		}
    	}

    	for (ItemStack is : getCurrentStage(quest).blocksToUse) {
    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			toUse = is;
    		}
    	}

    	if (used != null && toUse != null) {
    		if (used.getAmount() < toUse.getAmount()) {
        		ItemStack newUsed = used;
    			newUsed.setAmount(used.getAmount() + 1);
        		
        		//TODO is this correct?
        		getQuestData(quest).blocksUsed.set(getQuestData(quest).blocksUsed.indexOf(used), newUsed);

        		if (used.getAmount() == toUse.getAmount()) {
                    finishObjective(quest, "useBlock", m, null, null, null, null, null, null, null, null, null);
                }
        	}
    	} else {
    		//Do nothing
    		System.out.println(quest + " somehow threw a null value. Please report on Github!");
    	}

    }

    public void cutBlock(Quest quest, ItemStack m) {
    	ItemStack temp = m;
    	temp.setAmount(0);
    	ItemStack cut = temp;
    	ItemStack toCut = temp;
    	
    	for (ItemStack is : getQuestData(quest).blocksCut) {

    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			cut = is;
    		}
    	}

    	for (ItemStack is : getCurrentStage(quest).blocksToCut) {
    		if (m.getType() == is.getType() && m.getDurability() == is.getDurability()) {
    			toCut = is;
    		}
    	}

    	if (cut != null && toCut != null) {
    		if (cut.getAmount() < toCut.getAmount()) {
        		ItemStack newCut = cut;
    			newCut.setAmount(cut.getAmount() + 1);
        		
        		//TODO is this correct?
        		getQuestData(quest).blocksCut.set(getQuestData(quest).blocksCut.indexOf(cut), newCut);

        		if (cut.getAmount() == toCut.getAmount()) {
                    finishObjective(quest, "cutBlock", m, null, null, null, null, null, null, null, null, null);
                }
        	}
    	} else {
    		//Do nothing
    		System.out.println(quest + " somehow threw a null value. Please report on Github!");
    	}

    }

    public void catchFish(Quest quest) {

        if (getQuestData(quest).getFishCaught() < getCurrentStage(quest).fishToCatch) {
            getQuestData(quest).setFishCaught(getQuestData(quest).getFishCaught() + 1);

            if (((Integer) getQuestData(quest).getFishCaught()).equals(getCurrentStage(quest).fishToCatch)) {
                finishObjective(quest, "catchFish", null, null, null, null, null, null, null, null, null, null);
            }

        }

    }

    public void enchantItem(Quest quest, Enchantment e, Material m) {

        for (Entry<Map<Enchantment, Material>, Integer> entry : getQuestData(quest).itemsEnchanted.entrySet()) {

            if (entry.getKey().containsKey(e) && entry.getKey().containsValue(m)) {

                for (Entry<Map<Enchantment, Material>, Integer> entry2 : getCurrentStage(quest).itemsToEnchant.entrySet()) {

                    if (entry2.getKey().containsKey(e) && entry2.getKey().containsValue(m)) {

                        if (entry.getValue() < entry2.getValue()) {

                            Integer num = entry.getValue() + 1;
                            getQuestData(quest).itemsEnchanted.put(entry.getKey(), num);

                            if (num.equals(entry2.getValue())) {
                                finishObjective(quest, "enchantItem", new ItemStack(m, 1), null, e, null, null, null, null, null, null, null);
                            }

                        }
                        break;

                    }

                }

                break;

            }

        }

    }

    public void killMob(Quest quest, Location killedLocation, EntityType e) {
        QuestData questData = getQuestData(quest);

        if (questData.mobsKilled.contains(e) == false) {
            return;
        }

        Stage currentStage = getCurrentStage(quest);

        int indexOfMobKilled                                        = questData.mobsKilled.indexOf(e);
        Integer numberOfSpecificMobKilled                           = questData.mobNumKilled.get(indexOfMobKilled);
        Integer numberOfSpecificMobNeedsToBeKilledInCurrentStage    = currentStage.mobNumToKill.get(indexOfMobKilled);

        if (questData.locationsToKillWithin.isEmpty() == false) {
            Location locationToKillWithin = questData.locationsToKillWithin.get(indexOfMobKilled);
            double radius = questData.radiiToKillWithin.get(indexOfMobKilled);

            // Check world #name, not the object
            if ((killedLocation.getWorld().getName() == locationToKillWithin.getWorld().getName()) == false) {
                return;
            }
            // Radius check, it's a "circle", not cuboid
            if ((killedLocation.getX() < (locationToKillWithin.getX() + radius) && killedLocation.getX() > (locationToKillWithin.getX() - radius)) == false) {
                return;
            }
            if ((killedLocation.getZ() < (locationToKillWithin.getZ() + radius) && killedLocation.getZ() > (locationToKillWithin.getZ() - radius)) == false) {
                return;
            }
            if ((killedLocation.getY() < (locationToKillWithin.getY() + radius) && killedLocation.getY() > (locationToKillWithin.getY() - radius)) == false) {
                return;
            }
        }

        if (numberOfSpecificMobKilled < numberOfSpecificMobNeedsToBeKilledInCurrentStage) {
            Integer newNumberOfSpecificMobKilled = numberOfSpecificMobKilled + 1;

            questData.mobNumKilled.set(indexOfMobKilled, newNumberOfSpecificMobKilled);

            if ((newNumberOfSpecificMobKilled).equals(numberOfSpecificMobNeedsToBeKilledInCurrentStage)) {
                finishObjective(quest, "killMob", null, null, null, e, null, null, null, null, null, null);
            }
        }
    }

    public void killPlayer(Quest quest, Player player) {

        if (getQuestData(quest).playerKillTimes.containsKey(player.getUniqueId())) {

            long killTime = getQuestData(quest).playerKillTimes.get(player.getUniqueId());
            long comparator = plugin.killDelay * 1000;
            long currentTime = System.currentTimeMillis();

            if ((currentTime - killTime) < comparator) {

                String error = Lang.get("killNotValid");
                error = error.replaceAll("<time>", ChatColor.DARK_PURPLE + Quests.getTime(comparator - (currentTime - killTime)) + ChatColor.RED);
                error = error.replaceAll("<player>", ChatColor.DARK_PURPLE + player.getName() + ChatColor.RED);
                getPlayer().sendMessage(ChatColor.RED + error);
                return;

            }

        }

        getQuestData(quest).playerKillTimes.put(player.getUniqueId(), System.currentTimeMillis());

        if (getQuestData(quest).getPlayersKilled() < getCurrentStage(quest).playersToKill) {
            getQuestData(quest).setPlayersKilled(getQuestData(quest).getPlayersKilled() + 1);

            if (((Integer) getQuestData(quest).getPlayersKilled()).equals(getCurrentStage(quest).playersToKill)) {
                finishObjective(quest, "killPlayer", null, null, null, null, null, null, null, null, null, null);
            }

        }

    }

    public void interactWithNPC(Quest quest, NPC n) {

        if (getQuestData(quest).citizensInteracted.containsKey(n.getId())) {

            if (getQuestData(quest).citizensInteracted.get(n.getId()) == false) {
                getQuestData(quest).citizensInteracted.put(n.getId(), true);
                finishObjective(quest, "talkToNPC", null, null, null, null, null, n, null, null, null, null);
            }

        }

    }

    public void killNPC(Quest quest, NPC n) {

        if (getQuestData(quest).citizensKilled.contains(n.getId())) {

            int index = getQuestData(quest).citizensKilled.indexOf(n.getId());
            if (getQuestData(quest).citizenNumKilled.get(index) < getCurrentStage(quest).citizenNumToKill.get(index)) {
                getQuestData(quest).citizenNumKilled.set(index, getQuestData(quest).citizenNumKilled.get(index) + 1);
                if (getQuestData(quest).citizenNumKilled.get(index) == getCurrentStage(quest).citizenNumToKill.get(index)) {
                    finishObjective(quest, "killNPC", null, null, null, null, null, n, null, null, null, null);
                }
            }

        }

    }

    public void reachLocation(Quest quest, Location l) {

        for (Location location : getQuestData(quest).locationsReached) {
        	//TODO debug try/catch for #352  	
        	try {
            int index = getQuestData(quest).locationsReached.indexOf(location);
            Location locationToReach = getCurrentStage(quest).locationsToReach.get(index);
            double radius = getQuestData(quest).radiiToReachWithin.get(index);
            if (l.getX() < (locationToReach.getX() + radius) && l.getX() > (locationToReach.getX() - radius)) {

                if (l.getZ() < (locationToReach.getZ() + radius) && l.getZ() > (locationToReach.getZ() - radius)) {

                    if (l.getY() < (locationToReach.getY() + radius) && l.getY() > (locationToReach.getY() - radius)) {

                        if (getQuestData(quest).hasReached.get(index) == false) {

                            getQuestData(quest).hasReached.set(index, true);
                            finishObjective(quest, "reachLocation", null, null, null, null, null, null, location, null, null, null);

                        }

                    }

                }

            }
            } catch (IndexOutOfBoundsException e) {
            	System.out.println("An error has occurred with Quests. Please report on Github. Include the info below");
            	System.out.println("index = "  + getQuestData(quest).locationsReached.indexOf(location));
            	System.out.println("locationsReached = " + getQuestData(quest).locationsReached.toString());
            	e.printStackTrace();
            }

        }

    }

    public void tameMob(Quest quest, EntityType entity) {

        if (getQuestData(quest).mobsTamed.containsKey(entity)) {

            getQuestData(quest).mobsTamed.put(entity, (getQuestData(quest).mobsTamed.get(entity) + 1));

            if (getQuestData(quest).mobsTamed.get(entity).equals(getCurrentStage(quest).mobsToTame.get(entity))) {
                finishObjective(quest, "tameMob", null, null, null, entity, null, null, null, null, null, null);
            }

        }

    }

    public void shearSheep(Quest quest, DyeColor color) {

        if (getQuestData(quest).sheepSheared.containsKey(color)) {

            getQuestData(quest).sheepSheared.put(color, (getQuestData(quest).sheepSheared.get(color) + 1));

            if (getQuestData(quest).sheepSheared.get(color).equals(getCurrentStage(quest).sheepToShear.get(color))) {
                finishObjective(quest, "shearSheep", null, null, null, null, null, null, null, color, null, null);
            }

        }

    }

    @SuppressWarnings("deprecation")
	public void deliverItem(Quest quest, ItemStack i) {

        Player player = getPlayer();

        ItemStack found = null;

        for (ItemStack is : getQuestData(quest).itemsDelivered.keySet()) {

            if (ItemUtil.compareItems(i, is, true) == 0) {
                found = is;
                break;
            }

        }
        if (found != null) {

            int amount = getQuestData(quest).itemsDelivered.get(found);
            int req = getCurrentStage(quest).itemsToDeliver.get(getCurrentStage(quest).itemsToDeliver.indexOf(found)).getAmount();
            Material m = i.getType();

            if (amount < req) {

                if ((i.getAmount() + amount) > req) {

                    getQuestData(quest).itemsDelivered.put(found, req);
                    int index = player.getInventory().first(i);
                    i.setAmount(i.getAmount() - (req - amount)); //Take away the remaining amount needed to be delivered from the item stack
                    player.getInventory().setItem(index, i);
                    player.updateInventory();
                    finishObjective(quest, "deliverItem", new ItemStack(m, 1), found, null, null, null, null, null, null, null, null);

                } else if ((i.getAmount() + amount) == req) {

                    getQuestData(quest).itemsDelivered.put(found, req);
                    player.getInventory().setItem(player.getInventory().first(i), null);
                    player.updateInventory();
                    finishObjective(quest, "deliverItem", new ItemStack(m, 1), found, null, null, null, null, null, null, null, null);

                } else {

                    getQuestData(quest).itemsDelivered.put(found, (amount + i.getAmount()));
                    player.getInventory().setItem(player.getInventory().first(i), null);
                    player.updateInventory();
                    String message = Quests.parseString(getCurrentStage(quest).deliverMessages.get(random.nextInt(getCurrentStage(quest).deliverMessages.size())), plugin.citizens.getNPCRegistry().getById(getCurrentStage(quest).itemDeliveryTargets.get(getCurrentStage(quest).itemsToDeliver.indexOf(found))));
                    player.sendMessage(message);

                }

            }

        }

    }

    public void sayPass(Quest quest, AsyncPlayerChatEvent evt) {

        boolean done;
        for (LinkedList<String> passes : getCurrentStage(quest).passwordPhrases) {

            done = false;

            for (String pass : passes) {

                if (pass.equalsIgnoreCase(evt.getMessage())) {

                    evt.setCancelled(true);
                    String display = getCurrentStage(quest).passwordDisplays.get(getCurrentStage(quest).passwordPhrases.indexOf(passes));
                    getQuestData(quest).passwordsSaid.put(display, true);
                    done = true;
                    finishObjective(quest, "password", null, null, null, null, null, null, null, null, display, null);
                    break;

                }

            }

            if (done) {
                break;
            }

        }
    }
    
    /**
     * Complete quest objective
     * @param quest Quest containing the objective
     * @param objective Type of objective, e.g. "password" or "damageBlock"
     * @param material Block being damaged, broken, etc.
     * @param delivery Item being delivered
     * @param enchantment Enchantment being applied by user
     * @param mob Mob to be killed or tamed
     * @param player Currently unused
     * @param npc NPC to talk to or kill
     * @param location Location for user to reach
     * @param color shear color
     * @param pass Password
     * @param co See CustomObjective class
     */

    public void finishObjective(Quest quest, String objective, ItemStack material, ItemStack delivery, Enchantment enchantment, EntityType mob, String player, NPC npc, Location location, DyeColor color, String pass, CustomObjective co) {

        Player p = getPlayer();

        if (getCurrentStage(quest).objectiveOverride != null) {

            if (testComplete(quest)) {
                String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + getCurrentStage(quest).objectiveOverride;
                p.sendMessage(message);
                quest.nextStage(this);
            }
            return;

        }

        if (objective.equalsIgnoreCase("password")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + pass;
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("damageBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("damage") + " " + prettyItemString(material.getType().name());
            message = message + " " + material.getAmount() + "/" + material.getAmount();
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("breakBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("break") + " " + prettyItemString(material.getType().name());
            message = message + " " + material.getAmount() + "/" + material.getAmount();
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("placeBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("place") + " " + prettyItemString(material.getType().name());
            message = message + " " + material.getAmount() + "/" + material.getAmount();
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("useBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("use") + " " + prettyItemString(material.getType().name());
            message = message + " " + material.getAmount() + "/" + material.getAmount();
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("cutBlock")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("cut") + " " + prettyItemString(material.getType().name());
            message = message + " " + material.getAmount() + "/" + material.getAmount();
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("catchFish")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("catchFish") + " ";
            message = message + " " + getCurrentStage(quest).fishToCatch + "/" + getCurrentStage(quest).fishToCatch;
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("enchantItem")) {

            String obj = Lang.get("enchantItem");
            obj = obj.replaceAll("<item>", prettyItemString(material.getType().name()));
            obj = obj.replaceAll("<enchantment>", Quester.prettyEnchantmentString(enchantment));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            for (Map<Enchantment, Material> map : getCurrentStage(quest).itemsToEnchant.keySet()) {

                if (map.containsKey(enchantment)) {

                    message = message + " " + getCurrentStage(quest).itemsToEnchant.get(map) + "/" + getCurrentStage(quest).itemsToEnchant.get(map);
                    break;

                }

            }

            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("deliverItem")) {

            String obj = Lang.get("deliver");
            obj = obj.replaceAll("<item>", ItemUtil.getString(getCurrentStage(quest).itemsToDeliver.get(getCurrentStage(quest).itemsToDeliver.indexOf(delivery))));
            obj = obj.replaceAll("<npc>", plugin.getNPCName(getCurrentStage(quest).itemDeliveryTargets.get(getCurrentStage(quest).itemsToDeliver.indexOf(delivery))));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("killMob")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("kill") + " " + mob.name();
            message = message + " " + getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(mob)) + "/" + getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(mob));
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("killPlayer")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("killPlayer");
            message = message + " " + getCurrentStage(quest).playersToKill + "/" + getCurrentStage(quest).playersToKill;
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("talkToNPC")) {

            String obj = Lang.get("talkTo");
            obj = obj.replaceAll("<npc>", plugin.getNPCName(npc.getId()));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("killNPC")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("kill") + " " + npc.getName();
            message = message + " " + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(npc.getId())) + "/" + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(npc.getId()));
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("tameMob")) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + Lang.get("tame") + " " + getCapitalized(mob.name());
            message = message + " " + getCurrentStage(quest).mobsToTame.get(mob) + "/" + getCurrentStage(quest).mobsToTame.get(mob);
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("shearSheep")) {

            String obj = Lang.get("shearSheep");
            obj = obj.replaceAll("<color>", color.name().toLowerCase());
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            message = message + " " + getCurrentStage(quest).sheepToShear.get(color) + "/" + getCurrentStage(quest).sheepToShear.get(color);
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (objective.equalsIgnoreCase("reachLocation")) {

            String obj = Lang.get("goTo");
            obj = obj.replaceAll("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest).locationsToReach.indexOf(location)));
            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + obj;
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        } else if (co != null) {

            String message = ChatColor.GREEN + "(" + Lang.get("completed") + ") " + co.getDisplay();

            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
                if (getCurrentStage(quest).customObjectives.get(i).getName().equals(co.getName())) {
                    index = i;
                    break;
                }
            }

            Map<String, Object> datamap = getCurrentStage(quest).customObjectiveData.get(index);
            for (String key : co.datamap.keySet()) {
                message = message.replaceAll("%" + ((String) key) + "%", (String) datamap.get(key));
            }

            if (co.isCountShown() && co.isEnableCount()) {
                message = message.replaceAll("%count%", getCurrentStage(quest).customObjectiveCounts.get(index) + "/" + getCurrentStage(quest).customObjectiveCounts.get(index));
            }
            p.sendMessage(message);
            if (testComplete(quest)) {
                quest.nextStage(this);
            }

        }

    }

    public boolean testComplete(Quest quest) {

        for (String s : getObjectives(quest)) {

            if (s.contains(ChatColor.GREEN.toString())) {
                return false;
            }

        }
        return true;

    }

    public void addEmpties(Quest quest) {
        QuestData data = new QuestData(this);
        data.setDoJournalUpdate(false);

        if (quest.getStage(0).blocksToDamage.isEmpty() == false) {
            for (ItemStack i : quest.getStage(0).blocksToDamage) {
            	if (data.blocksDamaged.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksDamaged.set(data.blocksDamaged.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksDamaged.add(temp);
            	}
            }
        }

        if (quest.getStage(0).blocksToBreak.isEmpty() == false) {
            for (ItemStack i : quest.getStage(0).blocksToBreak) {
            	if (data.blocksBroken.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksBroken.set(data.blocksBroken.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksBroken.add(temp);
            	}
            }
        }

        if (quest.getStage(0).blocksToPlace.isEmpty() == false) {
            for (ItemStack i : quest.getStage(0).blocksToPlace) {
            	if (data.blocksPlaced.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksPlaced.set(data.blocksPlaced.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksPlaced.add(temp);
            	}
            }
        }

        if (quest.getStage(0).blocksToUse.isEmpty() == false) {
            for (ItemStack i : quest.getStage(0).blocksToUse) {
            	if (data.blocksUsed.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksUsed.set(data.blocksUsed.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksUsed.add(temp);
            	}
            }
        }

        if (quest.getStage(0).blocksToCut.isEmpty() == false) {
            for (ItemStack i : quest.getStage(0).blocksToCut) {
            	if (data.blocksCut.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksCut.set(data.blocksCut.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksCut.add(temp);
            	}
            }
        }

        data.setFishCaught(0);

        if (quest.getStage(0).itemsToEnchant.isEmpty() == false) {
            for (Entry<Map<Enchantment, Material>, Integer> e : quest.getStage(0).itemsToEnchant.entrySet()) {

                Map<Enchantment, Material> map = (Map<Enchantment, Material>) e.getKey();
                data.itemsEnchanted.put(map, 0);

            }
        }

        if (quest.getStage(0).mobsToKill.isEmpty() == false) {
            for (EntityType e : quest.getStage(0).mobsToKill) {

                data.mobsKilled.add(e);
                data.mobNumKilled.add(0);
                if (quest.getStage(0).locationsToKillWithin.isEmpty() == false) {
                    data.locationsToKillWithin.add(quest.getStage(0).locationsToKillWithin.get(data.mobsKilled.indexOf(e)));
                }
                if (quest.getStage(0).radiiToKillWithin.isEmpty() == false) {
                    data.radiiToKillWithin.add(quest.getStage(0).radiiToKillWithin.get(data.mobsKilled.indexOf(e)));
                }

            }
        }

        data.setPlayersKilled(0);

        if (quest.getStage(0).itemsToDeliver.isEmpty() == false) {
            for (ItemStack is : quest.getStage(0).itemsToDeliver) {

                data.itemsDelivered.put(is, 0);

            }
        }

        if (quest.getStage(0).citizensToInteract.isEmpty() == false) {
            for (Integer n : quest.getStage(0).citizensToInteract) {

                data.citizensInteracted.put(n, false);

            }
        }

        if (quest.getStage(0).citizensToKill.isEmpty() == false) {
            for (Integer n : quest.getStage(0).citizensToKill) {

                data.citizensKilled.add(n);
                data.citizenNumKilled.add(0);

            }
        }

        if (quest.getStage(0).locationsToReach.isEmpty() == false) {
            for (Location l : quest.getStage(0).locationsToReach) {

                data.locationsReached.add(l);
                data.hasReached.add(false);
                data.radiiToReachWithin.add(quest.getStage(0).radiiToReachWithin.get(data.locationsReached.indexOf(l)));

            }
        }

        if (quest.getStage(0).mobsToTame.isEmpty() == false) {
            for (EntityType e : quest.getStage(0).mobsToTame.keySet()) {

                data.mobsTamed.put(e, 0);

            }
        }

        if (quest.getStage(0).sheepToShear.isEmpty() == false) {
            for (DyeColor d : quest.getStage(0).sheepToShear.keySet()) {

                data.sheepSheared.put(d, 0);

            }
        }

        if (quest.getStage(0).passwordDisplays.isEmpty() == false) {
            for (String display : quest.getStage(0).passwordDisplays) {
                data.passwordsSaid.put(display, false);
            }
        }

        if (quest.getStage(0).customObjectives.isEmpty() == false) {
            for (CustomObjective co : quest.getStage(0).customObjectives) {
                data.customObjectiveCounts.put(co.getName(), 0);
            }
        }

        data.setDoJournalUpdate(true);
        hardDataPut(quest, data);

    }

    public void addEmptiesFor(Quest quest, int stage) {
        QuestData data = new QuestData(this);
        data.setDoJournalUpdate(false);

        if (quest.getStage(stage).blocksToDamage.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToDamage) {
            	if (data.blocksDamaged.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksDamaged.set(data.blocksDamaged.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksDamaged.add(temp);
            	}
            }
        }

        if (quest.getStage(stage).blocksToBreak.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToBreak) {
            	if (data.blocksBroken.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksBroken.set(data.blocksBroken.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksBroken.add(temp);
            	}
            }
        }

        if (quest.getStage(stage).blocksToPlace.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToPlace) {
            	if (data.blocksPlaced.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksPlaced.set(data.blocksPlaced.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksPlaced.add(temp);
            	}
            }
        }

        if (quest.getStage(stage).blocksToUse.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToUse) {
            	if (data.blocksUsed.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksUsed.set(data.blocksUsed.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksUsed.add(temp);
            	}
            }
        }

        if (quest.getStage(stage).blocksToCut.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToCut) {
            	if (data.blocksCut.indexOf(i) != -1) {
            		//TODO Will this ever happen?
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksCut.set(data.blocksCut.indexOf(temp), temp);
            	} else {
            		ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
            		data.blocksCut.add(temp);
            	}
            }
        }

        data.setFishCaught(0);

        if (quest.getStage(stage).itemsToEnchant.isEmpty() == false) {
            for (Entry<Map<Enchantment, Material>, Integer> e : quest.getStage(stage).itemsToEnchant.entrySet()) {

                Map<Enchantment, Material> map = (Map<Enchantment, Material>) e.getKey();
                data.itemsEnchanted.put(map, 0);

            }
        }

        if (quest.getStage(stage).mobsToKill.isEmpty() == false) {
            for (EntityType e : quest.getStage(stage).mobsToKill) {

                data.mobsKilled.add(e);
                data.mobNumKilled.add(0);
                if (quest.getStage(stage).locationsToKillWithin.isEmpty() == false) {
                    data.locationsToKillWithin.add(quest.getStage(stage).locationsToKillWithin.get(data.mobsKilled.indexOf(e)));
                }
                if (quest.getStage(stage).radiiToKillWithin.isEmpty() == false) {
                    data.radiiToKillWithin.add(quest.getStage(stage).radiiToKillWithin.get(data.mobsKilled.indexOf(e)));
                }

            }
        }

        data.setPlayersKilled(0);

        if (quest.getStage(stage).itemsToDeliver.isEmpty() == false) {
            for (ItemStack is : quest.getStage(stage).itemsToDeliver) {

                data.itemsDelivered.put(is, 0);

            }
        }

        if (quest.getStage(stage).citizensToInteract.isEmpty() == false) {
            for (Integer n : quest.getStage(stage).citizensToInteract) {

                data.citizensInteracted.put(n, false);

            }
        }

        if (quest.getStage(stage).citizensToKill.isEmpty() == false) {
            for (Integer n : quest.getStage(stage).citizensToKill) {

                data.citizensKilled.add(n);
                data.citizenNumKilled.add(0);

            }
        }

        if (quest.getStage(stage).blocksToCut.isEmpty() == false) {
        	for (ItemStack is : quest.getStage(stage).blocksToCut) {
        		
        		//TODO should be .set() ?
        		is.setAmount(0);
        		data.blocksCut.add(is);
        	}
        }

        if (quest.getStage(stage).locationsToReach.isEmpty() == false) {
            for (Location l : quest.getStage(stage).locationsToReach) {

                data.locationsReached.add(l);
                data.hasReached.add(false);
                data.radiiToReachWithin.add(quest.getStage(stage).radiiToReachWithin.get(data.locationsReached.indexOf(l)));

            }
        }

        if (quest.getStage(stage).mobsToTame.isEmpty() == false) {
            for (EntityType e : quest.getStage(stage).mobsToTame.keySet()) {

                data.mobsTamed.put(e, 0);

            }
        }

        if (quest.getStage(stage).sheepToShear.isEmpty() == false) {
            for (DyeColor d : quest.getStage(stage).sheepToShear.keySet()) {

                data.sheepSheared.put(d, 0);

            }
        }

        if (quest.getStage(stage).passwordDisplays.isEmpty() == false) {
            for (String display : quest.getStage(stage).passwordDisplays) {
                data.passwordsSaid.put(display, false);
            }
        }

        if (quest.getStage(stage).customObjectives.isEmpty() == false) {
            for (CustomObjective co : quest.getStage(stage).customObjectives) {
                data.customObjectiveCounts.put(co.getName(), 0);
            }
        }

        data.setDoJournalUpdate(true);
        hardDataPut(quest, data);

    }

    public static String getCapitalized(String target) {
        String firstLetter = target.substring(0, 1);
        String remainder = target.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();

        return capitalized;
    }

    
    //TODO eliminate and replace all with Items.itemByStack(is).getName()
    public static String prettyItemString(String itemName) {
        String baseString = Material.matchMaterial(itemName).toString();
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

    public static String fullPotionString(ItemStack is) {

        Potion potion = Potion.fromItemStack(is);
        String potionName = "";
        boolean isPrimary = false;

        try {

            potionName = "Potion of " + potion.getType().getEffectType().getName();

        } catch (NullPointerException e) { // Potion is primary

            isPrimary = true;

            if (is.getDurability() == 0) {
                potionName = "Water Bottle";
            } else if (is.getDurability() == 16) {
                potionName = "Awkward Potion";
            } else if (is.getDurability() == 32) {
                potionName = "Thick Potion";
            } else if (is.getDurability() == 64) {
                potionName = "Mundane Potion (Extended)";
            } else if (is.getDurability() == 8192) {
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

        int max = s.length();

        for (int i = 1; i < max; i++) {

            if (Character.isUpperCase(s.charAt(i))) {

                s = s.substring(0, i) + " " + s.substring(i);
                i++;
                max++;

            }

        }

        return s;

    }

    public static String spaceToCapital(String s) {

        int index = s.indexOf(' ');
        if (index == -1) {
            return null;
        }

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
    	
    	if (Lang.get("ENCHANTMENT_" + e.getName()).equals("NULL")) {
    		Bukkit.getLogger().warning(e.getName() + " was not found in Lang.yml, please ask the developer to "
    				+ "update the file or simply add an entry for the enchantment");
    		return "ENCHANTMENT_" + e.getName();
    	}
    	
        return (Lang.get("ENCHANTMENT_" + e.getName()));

    }

    public static String prettyColorString(DyeColor color) {

        return Lang.get("COLOR_" + color.name());

    }

    public void saveData() {

        FileConfiguration data = getBaseData();
        try {
            data.save(new File(plugin.getDataFolder(), "data/" + id + ".yml"));
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

        if (currentQuests.isEmpty() == false) {

            ArrayList<String> questNames = new ArrayList<String>();
            ArrayList<Integer> questStages = new ArrayList<Integer>();

            for (Quest quest : currentQuests.keySet()) {

                questNames.add(quest.name);
                questStages.add(currentQuests.get(quest));

            }

            data.set("currentQuests", questNames);
            data.set("currentStages", questStages);
            data.set("quest-points", questPoints);

            ConfigurationSection dataSec = data.createSection("questData");

            for (Quest quest : currentQuests.keySet()) {
            	
                ConfigurationSection questSec = dataSec.createSection(quest.name);
                QuestData questData = getQuestData(quest);
                if (questData == null) continue;
                
                if (questData.blocksDamaged.isEmpty() == false) {

                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();

                    for (ItemStack m : questData.blocksDamaged) {
                    	blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }

                    questSec.set("blocks-damaged-names", blockNames);
                    questSec.set("blocks-damaged-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);

                }

                if (questData.blocksBroken.isEmpty() == false) {
                	
                	LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();
                	
                	for (ItemStack m : questData.blocksBroken) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }

                    questSec.set("blocks-broken-names", blockNames);
                    questSec.set("blocks-broken-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);

                }

                if (questData.blocksPlaced.isEmpty() == false) {

                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();

                    for (ItemStack m : questData.blocksPlaced) {
                    	blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }

                    questSec.set("blocks-placed-names", blockNames);
                    questSec.set("blocks-placed-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);

                }

                if (questData.blocksUsed.isEmpty() == false) {

                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();

                    for (ItemStack m : questData.blocksUsed) {
                    	blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }

                    questSec.set("blocks-used-names", blockNames);
                    questSec.set("blocks-used-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);

                }

                if (questData.blocksCut.isEmpty() == false) {

                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();

                    for (ItemStack m : questData.blocksCut) {
                    	blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }

                    questSec.set("blocks-cut-names", blockNames);
                    questSec.set("blocks-cut-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);

                }

                if (getCurrentStage(quest).fishToCatch != null) {
                    questSec.set("fish-caught", questData.getFishCaught());
                }

                if (getCurrentStage(quest).playersToKill != null) {
                    questSec.set("players-killed", questData.getPlayersKilled());
                }

                if (questData.itemsEnchanted.isEmpty() == false) {

                    LinkedList<String> enchantments = new LinkedList<String>();
                    LinkedList<String> itemNames = new LinkedList<String>();
                    LinkedList<Integer> enchAmounts = new LinkedList<Integer>();

                    for (Entry<Map<Enchantment, Material>, Integer> e : questData.itemsEnchanted.entrySet()) {

                        Map<Enchantment, Material> enchMap = (Map<Enchantment, Material>) e.getKey();
                        enchAmounts.add(questData.itemsEnchanted.get(enchMap));
                        for (Entry<Enchantment, Material> e2 : enchMap.entrySet()) {

                            enchantments.add(Quester.prettyEnchantmentString((Enchantment) e2.getKey()));
                            itemNames.add(((Material) e2.getValue()).name());

                        }

                    }

                    questSec.set("enchantments", enchantments);
                    questSec.set("enchantment-item-names", itemNames);
                    questSec.set("times-enchanted", enchAmounts);

                }

                if (questData.mobsKilled.isEmpty() == false) {

                    LinkedList<String> mobNames = new LinkedList<String>();
                    LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                    LinkedList<String> locations = new LinkedList<String>();
                    LinkedList<Integer> radii = new LinkedList<Integer>();

                    for (EntityType e : questData.mobsKilled) {

                        mobNames.add(Quester.prettyMobString(e));

                    }

                    for (int i : questData.mobNumKilled) {

                        mobAmounts.add(i);

                    }

                    questSec.set("mobs-killed", mobNames);
                    questSec.set("mobs-killed-amounts", mobAmounts);

                    if (questData.locationsToKillWithin.isEmpty() == false) {

                        for (Location l : questData.locationsToKillWithin) {

                            locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());

                        }

                        for (int i : questData.radiiToKillWithin) {

                            radii.add(i);

                        }

                        questSec.set("mob-kill-locations", locations);
                        questSec.set("mob-kill-location-radii", radii);

                    }

                }

                if (questData.itemsDelivered.isEmpty() == false) {

                    LinkedList<Integer> deliveryAmounts = new LinkedList<Integer>();

                    for (Entry<ItemStack, Integer> e : questData.itemsDelivered.entrySet()) {

                        deliveryAmounts.add(e.getValue());

                    }

                    questSec.set("item-delivery-amounts", deliveryAmounts);

                }

                if (questData.citizensInteracted.isEmpty() == false) {

                    LinkedList<Integer> npcIds = new LinkedList<Integer>();
                    LinkedList<Boolean> hasTalked = new LinkedList<Boolean>();

                    for (Integer n : questData.citizensInteracted.keySet()) {

                        npcIds.add(n);
                        hasTalked.add(questData.citizensInteracted.get(n));

                    }

                    questSec.set("citizen-ids-to-talk-to", npcIds);
                    questSec.set("has-talked-to", hasTalked);

                }

                if (questData.citizensKilled.isEmpty() == false) {

                    LinkedList<Integer> npcIds = new LinkedList<Integer>();

                    for (Integer n : questData.citizensKilled) {

                        npcIds.add(n);

                    }

                    questSec.set("citizen-ids-killed", npcIds);
                    questSec.set("citizen-amounts-killed", questData.citizenNumKilled);

                }

                if (questData.locationsReached.isEmpty() == false) {

                    LinkedList<String> locations = new LinkedList<String>();
                    LinkedList<Boolean> has = new LinkedList<Boolean>();
                    LinkedList<Integer> radii = new LinkedList<Integer>();

                    for (Location l : questData.locationsReached) {

                        locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());

                    }

                    for (boolean b : questData.hasReached) {
                        has.add(b);
                    }

                    for (int i : questData.radiiToReachWithin) {
                        radii.add(i);
                    }

                    questSec.set("locations-to-reach", locations);
                    questSec.set("has-reached-location", has);
                    questSec.set("radii-to-reach-within", radii);

                }

                if (questData.potionsBrewed.isEmpty() == false) {

                    LinkedList<String> potionNames = new LinkedList<String>();
                    LinkedList<Integer> potionAmounts = new LinkedList<Integer>();

                    for (Entry<String, Integer> entry : questData.potionsBrewed.entrySet()) {

                        potionNames.add(entry.getKey());
                        potionAmounts.add(entry.getValue());

                    }

                    questSec.set("potions-brewed-names", potionNames);
                    questSec.set("potions-brewed-amounts", potionAmounts);

                }

                if (questData.mobsTamed.isEmpty() == false) {

                    LinkedList<String> mobNames = new LinkedList<String>();
                    LinkedList<Integer> mobAmounts = new LinkedList<Integer>();

                    for (EntityType e : questData.mobsTamed.keySet()) {

                        mobNames.add(Quester.prettyMobString(e));
                        mobAmounts.add(questData.mobsTamed.get(e));

                    }

                    questSec.set("mobs-to-tame", mobNames);
                    questSec.set("mob-tame-amounts", mobAmounts);

                }

                if (questData.sheepSheared.isEmpty() == false) {

                    LinkedList<String> colors = new LinkedList<String>();
                    LinkedList<Integer> shearAmounts = new LinkedList<Integer>();

                    for (DyeColor d : questData.sheepSheared.keySet()) {

                        colors.add(Quester.prettyColorString(d));
                        shearAmounts.add(questData.sheepSheared.get(d));

                    }

                    questSec.set("sheep-to-shear", colors);
                    questSec.set("sheep-sheared", shearAmounts);

                }

                if (questData.passwordsSaid.isEmpty() == false) {

                    LinkedList<String> passwords = new LinkedList<String>();
                    LinkedList<Boolean> said = new LinkedList<Boolean>();

                    for (Entry<String, Boolean> entry : questData.passwordsSaid.entrySet()) {

                        passwords.add(entry.getKey());
                        said.add(entry.getValue());

                    }

                    questSec.set("passwords", passwords);
                    questSec.set("passwords-said", said);

                }

                if (questData.customObjectiveCounts.isEmpty() == false) {

                    LinkedList<String> customObj = new LinkedList<String>();
                    LinkedList<Integer> customObjCounts = new LinkedList<Integer>();

                    for (Entry<String, Integer> entry : questData.customObjectiveCounts.entrySet()) {

                        customObj.add(entry.getKey());
                        customObjCounts.add(entry.getValue());

                    }

                    questSec.set("custom-objectives", customObj);
                    questSec.set("custom-objective-counts", customObjCounts);

                }

                if (questData.delayTimeLeft > 0) {
                    questSec.set("stage-delay", questData.delayTimeLeft);
                }

                if (questData.eventFired.isEmpty() == false) {

                    LinkedList<String> triggers = new LinkedList<String>();
                    for (String trigger : questData.eventFired.keySet()) {

                        if (questData.eventFired.get(trigger) == true) {
                            triggers.add(trigger);
                        }

                    }

                    if (triggers.isEmpty() == false) {
                        questSec.set("chat-triggers", triggers);
                    }

                }

            }

        } else {

            data.set("currentQuests", "none");
            data.set("currentStages", "none");
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

        // #getPlayer is faster
        OfflinePlayer represented_player = getPlayer();
        if (represented_player == null) {
            represented_player = getOfflinePlayer();
        }

        data.set("hasJournal", hasJournal);
        data.set("lastKnownName", represented_player.getName());

        return data;

    }

    public boolean loadData() {

        FileConfiguration data = new YamlConfiguration();
        try {

            File dataFile = new File(plugin.getDataFolder(), "data/" + id.toString() + ".yml");
            if (dataFile.exists() == false) {
                OfflinePlayer p = getOfflinePlayer();
                dataFile = new File(plugin.getDataFolder(), "data/" + p.getName() + ".yml");
                if (dataFile.exists() == false) {
                    return false;
                }
            }

            data.load(dataFile);

        } catch (IOException e) {
            return false;
        } catch (InvalidConfigurationException e) {
            return false;
        }

        hardClear();

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

        if (data.contains("amountsCompletedQuests")) {

            List<String> list1 = data.getStringList("amountsCompletedQuests");
            List<Integer> list2 = data.getIntegerList("amountsCompleted");

            for (int i = 0; i < list1.size(); i++) {

                amountsCompleted.put(list1.get(i), list2.get(i));

            }

        }

        questPoints = data.getInt("quest-points");

        hasJournal = data.getBoolean("hasJournal");

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

        if (data.isString("currentQuests") == false) {

            List<String> questNames = data.getStringList("currentQuests");
            List<Integer> questStages = data.getIntegerList("currentStages");

            // These appear to differ sometimes? That seems bad.
            int maxSize = Math.min(questNames.size(), questStages.size());

            for (int i = 0; i < maxSize; i++) {
            	if (plugin.getQuest(questNames.get(i)) != null) {
            		currentQuests.put(plugin.getQuest(questNames.get(i)), questStages.get(i));
            	}

            }

            ConfigurationSection dataSec = data.getConfigurationSection("questData");

            if (dataSec == null || dataSec.getKeys(false).isEmpty()) {
            	return false;
            }

            for (String key : dataSec.getKeys(false)) {

                ConfigurationSection questSec = dataSec.getConfigurationSection(key);

                Quest quest = plugin.getQuest(key);
                Stage stage;

                if (quest == null || currentQuests.containsKey(quest) == false) {
                    continue;
                }

                stage = getCurrentStage(quest);

                if (stage == null) {
                    quest.completeQuest(this);
                    plugin.getLogger().log(Level.SEVERE, "[Quests] Invalid stage number for player: \"" + id + "\" on Quest \"" + quest.name + "\". Quest ended.");
                    continue;
                }

                addEmpties(quest);

                if (questSec == null) continue;

                if (questSec.contains("blocks-damaged-names")) {

                    List<String> names = questSec.getStringList("blocks-damaged-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-damaged-amounts");
                    List<Short> durability = questSec.getShortList("blocks-damaged-durability");

                    for (String s : names) {
                    	ItemStack is;
                    	//if (durability.get(names.indexOf(s)) != -1) {
                    	if (durability.indexOf(names.indexOf(s)) != -1) {
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
                    	} else {
                    		//Legacy
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
                    	}
                    	
                    	getQuestData(quest).blocksDamaged.add(is); //TODO should be .set() ?
                    }

                }

                if (questSec.contains("blocks-broken-names")) {

					List<String> names = questSec.getStringList("blocks-broken-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-broken-amounts");
                    List<Short> durability = questSec.getShortList("blocks-broken-durability");

                    for (String s : names) {
                    	ItemStack is;
                    	//if (durability.get(names.indexOf(s)) != -1) {
                    	if (durability.indexOf(names.indexOf(s)) != -1) {
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
                    	} else {
                    		//Legacy
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
                    	}
                    	
                    	getQuestData(quest).blocksBroken.add(is); //TODO should be .set() ?
                    }

                }

                if (questSec.contains("blocks-placed-names")) {

                    List<String> names = questSec.getStringList("blocks-placed-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-placed-amounts");
                    List<Short> durability = questSec.getShortList("blocks-placed-durability");

                    for (String s : names) {
                    	ItemStack is;
                    	//if (durability.get(names.indexOf(s)) != -1) {
                    	if (durability.indexOf(names.indexOf(s)) != -1) {
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
                    	} else {
                    		//Legacy
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
                    	}
                    	
                    	getQuestData(quest).blocksPlaced.add(is); //TODO should be .set() ?
                    }

                }

                if (questSec.contains("blocks-used-names")) {

                    List<String> names = questSec.getStringList("blocks-used-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-used-amounts");
                    List<Short> durability = questSec.getShortList("blocks-used-durability");

                    for (String s : names) {
                    	ItemStack is;
                    	//if (durability.get(names.indexOf(s)) != -1) {
                    	if (durability.indexOf(names.indexOf(s)) != -1) {
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
                    	} else {
                    		//Legacy
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
                    	}
                    	
                    	getQuestData(quest).blocksUsed.add(is); //TODO should be .set() ?
                    }

                }

                if (questSec.contains("blocks-cut-names")) {

                    List<String> names = questSec.getStringList("blocks-cut-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-cut-amounts");
                    List<Short> durability = questSec.getShortList("blocks-cut-durability");

                    for (String s : names) {
                    	ItemStack is;
                    	//if (durability.get(names.indexOf(s)) != -1) {
                    	if (durability.indexOf(names.indexOf(s)) != -1) {
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
                    	} else {
                    		//Legacy
                    		is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
                    	}
                    	
                    	getQuestData(quest).blocksCut.add(is); //TODO should be .set() ?
                    }

                }

                if (questSec.contains("fish-caught")) {
                    getQuestData(quest).setFishCaught(questSec.getInt("fish-caught"));
                }

                if (questSec.contains("players-killed")) {

                    getQuestData(quest).setPlayersKilled(questSec.getInt("players-killed"));

                    List<String> playerNames = questSec.getStringList("player-killed-names");
                    List<Long> killTimes = questSec.getLongList("kill-times");

                    for (String s : playerNames) {

                        getQuestData(quest).playerKillTimes.put(UUID.fromString(s), killTimes.get(playerNames.indexOf(s)));

                    }

                }

                if (questSec.contains("enchantments")) {

                    LinkedList<Enchantment> enchantments = new LinkedList<Enchantment>();
                    LinkedList<Material> materials = new LinkedList<Material>();
                    LinkedList<Integer> amounts = new LinkedList<Integer>();

                    List<String> enchantNames = questSec.getStringList("enchantments");
                    List<String> names = questSec.getStringList("enchantment-item-names");
                    List<Integer> times = questSec.getIntegerList("times-enchanted");

                    for (String s : enchantNames) {

                        enchantments.add(Quests.getEnchantment(s));

                        materials.add(Material.matchMaterial(names.get(enchantNames.indexOf(s))));
                        amounts.add(times.get(enchantNames.indexOf(s)));

                    }

                    for (Enchantment e : enchantments) {

                        Map<Enchantment, Material> map = new HashMap<Enchantment, Material>();
                        map.put(e, materials.get(enchantments.indexOf(e)));

                        getQuestData(quest).itemsEnchanted.put(map, amounts.get(enchantments.indexOf(e)));

                    }

                }

                if (questSec.contains("mobs-killed")) {

                    LinkedList<EntityType> mobs = new LinkedList<EntityType>();
                    List<Integer> amounts = questSec.getIntegerList("mobs-killed-amounts");

                    for (String s : questSec.getStringList("mobs-killed")) {

                        EntityType mob = Quests.getMobType(s);
                        if (mob != null) {
                            mobs.add(mob);
                        }

                        getQuestData(quest).mobsKilled.clear();
                        getQuestData(quest).mobNumKilled.clear();

                        for (EntityType e : mobs) {

                            getQuestData(quest).mobsKilled.add(e);
                            getQuestData(quest).mobNumKilled.add(amounts.get(mobs.indexOf(e)));

                        }

                        if (questSec.contains("mob-kill-locations")) {

                            LinkedList<Location> locations = new LinkedList<Location>();
                            List<Integer> radii = questSec.getIntegerList("mob-kill-location-radii");

                            for (String loc : questSec.getStringList("mob-kill-locations")) {

                                String[] info = loc.split(" ");
                                double x = Double.parseDouble(info[1]);
                                double y = Double.parseDouble(info[2]);
                                double z = Double.parseDouble(info[3]);
                                Location finalLocation = new Location(plugin.getServer().getWorld(info[0]), x, y, z);
                                locations.add(finalLocation);

                            }

                            getQuestData(quest).locationsToKillWithin = locations;
                            getQuestData(quest).radiiToKillWithin.clear();
                            for (int i : radii) {
                                getQuestData(quest).radiiToKillWithin.add(i);
                            }

                        }

                    }

                }

                if (questSec.contains("item-delivery-amounts")) {

                    List<Integer> deliveryAmounts = questSec.getIntegerList("item-delivery-amounts");

                    for (int i = 0; i < deliveryAmounts.size(); i++) {

                    	if (i < getCurrentStage(quest).itemsToDeliver.size()) {

                            getQuestData(quest).itemsDelivered.put(getCurrentStage(quest).itemsToDeliver.get(i), deliveryAmounts.get(i));


                    	}

                    }

                }

                if (questSec.contains("citizen-ids-to-talk-to")) {

                    List<Integer> ids = questSec.getIntegerList("citizen-ids-to-talk-to");
                    List<Boolean> has = questSec.getBooleanList("has-talked-to");

                    for (int i : ids) {

                        getQuestData(quest).citizensInteracted.put(i, has.get(ids.indexOf(i)));

                    }

                }

                if (questSec.contains("citizen-ids-killed")) {

                    List<Integer> ids = questSec.getIntegerList("citizen-ids-killed");
                    List<Integer> num = questSec.getIntegerList("citizen-amounts-killed");

                    getQuestData(quest).citizensKilled.clear();
                    getQuestData(quest).citizenNumKilled.clear();

                    for (int i : ids) {

                        getQuestData(quest).citizensKilled.add(i);
                        getQuestData(quest).citizenNumKilled.add(num.get(ids.indexOf(i)));

                    }

                }

                if (questSec.contains("locations-to-reach")) {

                    LinkedList<Location> locations = new LinkedList<Location>();
                    List<Boolean> has = questSec.getBooleanList("has-reached-location");
                    List<Integer> radii = questSec.getIntegerList("radii-to-reach-within");

                    for (String loc : questSec.getStringList("locations-to-reach")) {

                        String[] info = loc.split(" ");
                        double x = Double.parseDouble(info[1]);
                        double y = Double.parseDouble(info[2]);
                        double z = Double.parseDouble(info[3]);
                        Location finalLocation = new Location(plugin.getServer().getWorld(info[0]), x, y, z);
                        locations.add(finalLocation);

                    }

                    getQuestData(quest).locationsReached = locations;
                    getQuestData(quest).hasReached.clear();
                    getQuestData(quest).radiiToReachWithin.clear();

                    for (boolean b : has) {
                        getQuestData(quest).hasReached.add(b);
                    }

                    for (int i : radii) {
                        getQuestData(quest).radiiToReachWithin.add(i);
                    }

                }

                if (questSec.contains("potions-brewed-names")) {

                    List<String> names = questSec.getStringList("potions-brewed-names");
                    List<Integer> amounts = questSec.getIntegerList("potions-brewed-amounts");

                    for (String s : names) {

                        getQuestData(quest).potionsBrewed.put(s, amounts.get(names.indexOf(s)));

                    }

                }

                if (questSec.contains("mobs-to-tame")) {

                    List<String> mobs = questSec.getStringList("mobs-to-tame");
                    List<Integer> amounts = questSec.getIntegerList("mob-tame-amounts");

                    for (String mob : mobs) {

                        if (mob.equalsIgnoreCase("Wolf")) {

                            getQuestData(quest).mobsTamed.put(EntityType.WOLF, amounts.get(mobs.indexOf(mob)));

                        } else {

                            getQuestData(quest).mobsTamed.put(EntityType.OCELOT, amounts.get(mobs.indexOf(mob)));

                        }

                    }

                }

                if (questSec.contains("sheep-to-shear")) {

                    List<String> colors = questSec.getStringList("sheep-to-shear");
                    List<Integer> amounts = questSec.getIntegerList("sheep-sheared");

                    for (String color : colors) {

                        getQuestData(quest).sheepSheared.put(Quests.getDyeColor(color), amounts.get(colors.indexOf(color)));

                    }

                }

                if (questSec.contains("passwords")) {

                    List<String> passwords = questSec.getStringList("passwords");
                    List<Boolean> said = questSec.getBooleanList("passwords-said");
                    for (int i = 0; i < passwords.size(); i++) {
                        getQuestData(quest).passwordsSaid.put(passwords.get(i), said.get(i));
                    }

                }

                if (questSec.contains("custom-objectives")) {

                    List<String> customObj = questSec.getStringList("custom-objectives");
                    List<Integer> customObjCount = questSec.getIntegerList("custom-objective-counts");

                    for (int i = 0; i < customObj.size(); i++) {
                        getQuestData(quest).customObjectiveCounts.put(customObj.get(i), customObjCount.get(i));
                    }

                }

                if (questSec.contains("stage-delay")) {

                    getQuestData(quest).delayTimeLeft = questSec.getLong("stage-delay");

                }

                if (getCurrentStage(quest).chatEvents.isEmpty() == false) {

                    for (String trig : getCurrentStage(quest).chatEvents.keySet()) {

                        getQuestData(quest).eventFired.put(trig, false);

                    }

                }

                if (questSec.contains("chat-triggers")) {

                    List<String> triggers = questSec.getStringList("chat-triggers");
                    for (String s : triggers) {

                        getQuestData(quest).eventFired.put(s, true);

                    }

                }

            }

        }

        return true;

    }

public static ConfigurationSection getLegacyQuestData(FileConfiguration questSec, String questName) {

    ConfigurationSection newData = questSec.createSection("questData");

    if (questSec.contains("blocks-damaged-names")) {

        List<String> names = questSec.getStringList("blocks-damaged-names");
        List<Integer> amounts = questSec.getIntegerList("blocks-damaged-amounts");

        newData.set(questName + ".blocks-damaged-names", names);
        newData.set(questName + ".blocks-damaged-amounts", amounts);
    }

    if (questSec.contains("blocks-broken-names")) {

        List<String> names = questSec.getStringList("blocks-broken-names");
        List<Integer> amounts = questSec.getIntegerList("blocks-broken-amounts");

        newData.set(questName + ".blocks-broken-names", names);
        newData.set(questName + ".blocks-broken-amounts", amounts);

    }

    if (questSec.contains("blocks-placed-names")) {

        List<String> names = questSec.getStringList("blocks-placed-names");
        List<Integer> amounts = questSec.getIntegerList("blocks-placed-amounts");

        newData.set(questName + ".blocks-placed-names", names);
        newData.set(questName + ".blocks-placed-amounts", amounts);

    }

    if (questSec.contains("blocks-used-names")) {

        List<String> names = questSec.getStringList("blocks-used-names");
        List<Integer> amounts = questSec.getIntegerList("blocks-used-amounts");

        newData.set(questName + ".blocks-used-names", names);
        newData.set(questName + ".blocks-used-amounts", amounts);

    }

    if (questSec.contains("blocks-cut-names")) {

        List<String> names = questSec.getStringList("blocks-cut-names");
        List<Integer> amounts = questSec.getIntegerList("blocks-cut-amounts");

        newData.set(questName + ".blocks-cut-names", names);
        newData.set(questName + ".blocks-cut-amounts", amounts);

    }

    if (questSec.contains("fish-caught")) {
        newData.set(questName + ".fish-caught", questSec.getInt("fish-caught"));
    }

    if (questSec.contains("players-killed")) {

        List<String> playerNames = questSec.getStringList("player-killed-names");
        List<Long> killTimes = questSec.getLongList("kill-times");

        newData.set(questName + ".players-killed", questSec.getInt("players-killed"));
        newData.set(questName + ".player-killed-names", playerNames);
        newData.set(questName + ".kill-times", killTimes);

    }

    if (questSec.contains("enchantments")) {

        List<String> enchantNames = questSec.getStringList("enchantments");
        List<String> names = questSec.getStringList("enchantment-item-names");
        List<Integer> times = questSec.getIntegerList("times-enchanted");

        newData.set(questName + ".enchantments", enchantNames);
        newData.set(questName + ".enchantment-item-names", names);
        newData.set(questName + ".times-enchanted", times);

    }

    if (questSec.contains("mobs-killed")) {

        List<String> mobs = questSec.getStringList("mobs-killed");
        List<Integer> amounts = questSec.getIntegerList("mobs-killed-amounts");

        newData.set(questName + ".mobs-killed", mobs);
        newData.set(questName + ".mobs-killed-amounts", amounts);

        if (questSec.contains("mob-kill-locations")) {

            List<String> locations = questSec.getStringList("mob-kill-locations");
            List<Integer> radii = questSec.getIntegerList("mob-kill-location-radii");

            newData.set(questName + ".mob-kill-locations", locations);
            newData.set(questName + ".mob-kill-location-radii", radii);

        }

    }

    if (questSec.contains("item-delivery-amounts")) {

        List<Integer> deliveryAmounts = questSec.getIntegerList("item-delivery-amounts");

        newData.set(questName + ".item-delivery-amounts", deliveryAmounts);

    }

    if (questSec.contains("citizen-ids-to-talk-to")) {

        List<Integer> ids = questSec.getIntegerList("citizen-ids-to-talk-to");
        List<Boolean> has = questSec.getBooleanList("has-talked-to");

        newData.set(questName + ".citizen-ids-to-talk-to", ids);
        newData.set(questName + ".has-talked-to", has);

    }

    if (questSec.contains("citizen-ids-killed")) {

        List<Integer> ids = questSec.getIntegerList("citizen-ids-killed");
        List<Integer> num = questSec.getIntegerList("citizen-amounts-killed");

        newData.set(questName + ".citizen-ids-killed", ids);
        newData.set(questName + ".citizen-amounts-killed", num);

    }

    if (questSec.contains("locations-to-reach")) {

        List<String> locations = questSec.getStringList("locations-to-reach");
        List<Boolean> has = questSec.getBooleanList("has-reached-location");
        List<Integer> radii = questSec.getIntegerList("radii-to-reach-within");

        newData.set(questName + ".locations-to-reach", locations);
        newData.set(questName + ".has-reached-location", has);
        newData.set(questName + ".radii-to-reach-within", radii);

    }

    if (questSec.contains("potions-brewed-names")) {

        List<String> names = questSec.getStringList("potions-brewed-names");
        List<Integer> amounts = questSec.getIntegerList("potions-brewed-amounts");

        newData.set(questName + ".potions-brewed-names", names);
        newData.set(questName + ".potions-brewed-amounts", amounts);

    }

    if (questSec.contains("mobs-to-tame")) {

        List<String> mobs = questSec.getStringList("mobs-to-tame");
        List<Integer> amounts = questSec.getIntegerList("mob-tame-amounts");

        newData.set(questName + ".mobs-to-tame", mobs);
        newData.set(questName + ".mob-tame-amounts", amounts);

    }

    if (questSec.contains("sheep-to-shear")) {

        List<String> colors = questSec.getStringList("sheep-to-shear");
        List<Integer> amounts = questSec.getIntegerList("sheep-sheared");

        newData.set(questName + ".sheep-to-shear", colors);
        newData.set(questName + ".sheep-sheared", amounts);

    }

    if (questSec.contains("passwords")) {

        List<String> passwords = questSec.getStringList("passwords");
        List<Boolean> said = questSec.getBooleanList("passwords-said");

        newData.set(questName + ".passwords", passwords);
        newData.set(questName + ".passwords-said", said);

    }

    if (questSec.contains("custom-objectives")) {

        List<String> customObj = questSec.getStringList("custom-objectives");
        List<Integer> customObjCount = questSec.getIntegerList("custom-objective-counts");

        newData.set(questName + ".custom-objectives", customObj);
        newData.set(questName + ".custom-objective-counts", customObjCount);

    }

    if (questSec.contains("stage-delay")) {

        newData.set(questName + ".stage-delay", questSec.getLong("stage-delay"));

    }

    if (questSec.contains("chat-triggers")) {

        List<String> triggers = questSec.getStringList("chat-triggers");
        newData.set(questName + ".chat-triggers", triggers);

    }

    return newData;

}

public void startStageTimer(Quest quest) {

if (getQuestData(quest).delayTimeLeft > -1) {
    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), (long) (getQuestData(quest).delayTimeLeft * 0.02));
} else {
    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), (long) (getCurrentStage(quest).delay * 0.02));
    if (getCurrentStage(quest).delayMessage != null) {
        plugin.getServer().getPlayer(id).sendMessage(Quests.parseString((getCurrentStage(quest).delayMessage), quest));
    }
}

getQuestData(quest).delayStartTime = System.currentTimeMillis();

}

public void stopStageTimer(Quest quest) {

if (getQuestData(quest).delayTimeLeft > -1) {
    getQuestData(quest).delayTimeLeft = getQuestData(quest).delayTimeLeft - (System.currentTimeMillis() - getQuestData(quest).delayStartTime);
} else {
    getQuestData(quest).delayTimeLeft = getCurrentStage(quest).delay - (System.currentTimeMillis() - getQuestData(quest).delayStartTime);
}

getQuestData(quest).delayOver = false;

}

public long getStageTime(Quest quest) {

if (getQuestData(quest).delayTimeLeft > -1) {
    return getQuestData(quest).delayTimeLeft - (System.currentTimeMillis() - getQuestData(quest).delayStartTime);
} else {
    return getCurrentStage(quest).delay - (System.currentTimeMillis() - getQuestData(quest).delayStartTime);
}

}

public boolean hasData() {

if (currentQuests.isEmpty() == false || questData.isEmpty() == false) {
    return true;
}

if (questPoints > 1) {
    return true;
}

return completedQuests.isEmpty() == false;

}

public void checkQuest(Quest quest) {

if (quest != null) {

    boolean exists = false;

    for (Quest q : plugin.quests) {
        if (q.name.equalsIgnoreCase(quest.name)) {
            Stage stage = getCurrentStage(quest);
            quest.updateCompass(this, stage);
            exists = true;

            // Meh, let's not.
            /*
            if (q.equals(quest) == false) {

                hardQuit(quest);

                if (plugin.getServer().getPlayer(id) != null) {
                    String error = Lang.get("questModified");
                    error = error.replaceAll("<quest>", ChatColor.DARK_PURPLE + quest.name + ChatColor.RED);
                    plugin.getServer().getPlayer(id).sendMessage(ChatColor.GOLD + "[Quests] " + ChatColor.RED + error);
                    updateJournal();
                }

            }
            */

            break;

        }

    }

    if (exists == false) {

        if (plugin.getServer().getPlayer(id) != null) {
            String error = Lang.get("questNotExist");
            error = error.replaceAll("<quest>", ChatColor.DARK_PURPLE + quest.name + ChatColor.RED);
            plugin.getServer().getPlayer(id).sendMessage(ChatColor.GOLD + "[Quests] " + ChatColor.RED + error);
        }

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

    public void showGUIDisplay(NPC npc, LinkedList<Quest> quests) {

        Player player = getPlayer();
        int size = ((quests.size() / 9) + 1) * 9;

        Inventory inv = Bukkit.getServer().createInventory(player, size, Lang.get("quests") + " | " + npc.getName());

        int inc = 0;
        for (int i = 0; i < quests.size(); i++) {

            if (quests.get(i).guiDisplay != null) {

                ItemStack display = quests.get(i).guiDisplay;
                ItemMeta meta = display.getItemMeta();
                meta.setDisplayName(ChatColor.DARK_PURPLE + Quests.parseString(quests.get(i).getName(), npc));

                if (!meta.hasLore()) {
                	LinkedList<String> lines = new LinkedList<String>();

                	lines = MiscUtil.makeLines(quests.get(i).description, " ", 40, ChatColor.DARK_GREEN);

                	meta.setLore(lines);
                }

                display.setItemMeta(meta);

                inv.setItem(inc, display);
                inc++;
            }

        }

        player.openInventory(inv);

    }

    public void hardQuit(Quest quest) {

        try {
            currentQuests.remove(quest);
            if (questData.containsKey(quest)) {
            	questData.remove(quest);
            }
        } catch (Exception ex) {
            Logger.getLogger(Quests.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
	public void hardRemove(Quest quest) {
		
		try {
			completedQuests.remove(quest.name);
		} catch (Exception ex) {
			Logger.getLogger(Quests.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

    public void hardClear() {

        try {
            currentQuests.clear();
            questData.clear();
            amountsCompleted.clear();
        } catch (Exception ex) {
            Logger.getLogger(Quests.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void hardStagePut(Quest key, Integer val) {

        try {
            currentQuests.put(key, val);
        } catch (Exception ex) {
            Logger.getLogger(Quests.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void hardDataPut(Quest key, QuestData val) {

        try {
            questData.put(key, val);
        } catch (Exception ex) {
            Logger.getLogger(Quests.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void resetCompass() {
        if (!Quests.useCompass) return;
        Player player = getPlayer();
        if (player == null) return;

        Location defaultLocation = player.getBedSpawnLocation();
        if (defaultLocation == null) {
            defaultLocation = player.getWorld().getSpawnLocation();
        }
        player.setCompassTarget(defaultLocation);
    }
    
    public void findCompassTarget() {
        if (!Quests.useCompass) return;
        Player player = getPlayer();
        if (player == null) return;
        
        for (Quest quest : currentQuests.keySet()) {
            Stage stage = getCurrentStage(quest);
            if (stage != null && quest.updateCompass(this, stage)) break;
        }
    }
}
