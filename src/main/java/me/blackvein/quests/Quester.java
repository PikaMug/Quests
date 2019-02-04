/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

import me.blackvein.quests.timers.StageTimer;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.LocaleQuery;
import me.blackvein.quests.util.MiscUtil;
import net.citizensnpcs.api.npc.NPC;

public class Quester {

	private Quests plugin;
	public boolean hasJournal = false;
	private UUID id;
	protected String questToTake;
	protected int questPoints = 0;
	protected ConcurrentHashMap<Integer, Quest> timers = new ConcurrentHashMap<Integer, Quest>();
	protected ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<Quest, Integer>() {

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
	protected LinkedList<String> completedQuests = new LinkedList<String>() {

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
	protected Map<String, Long> completedTimes = new HashMap<String, Long>();
	protected Map<String, Integer> amountsCompleted = new HashMap<String, Integer>() {

		private static final long serialVersionUID = 5475202358792520975L;

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
	protected Map<Quest, QuestData> questData = new HashMap<Quest, QuestData>() {

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
	
	public Quester(Quests newPlugin) {
		plugin = newPlugin;
	}

	public UUID getUUID() {
		return id;
	}

	public void setUUID(UUID id) {
		this.id = id;
	}

	public String getQuestToTake() {
		return questToTake;
	}

	public void setQuestToTake(String questToTake) {
		this.questToTake = questToTake;
	}

	public int getQuestPoints() {
		return questPoints;
	}

	public void setQuestPoints(int questPoints) {
		this.questPoints = questPoints;
	}

	public ConcurrentHashMap<Integer, Quest> getTimers() {
		return timers;
	}

	public void setTimers(ConcurrentHashMap<Integer, Quest> timers) {
		this.timers = timers;
	}
	
	public void removeTimer(Integer timerId) {
		this.timers.remove(timerId);
	}

	public ConcurrentHashMap<Quest, Integer> getCurrentQuests() {
		return currentQuests;
	}

	public void setCurrentQuests(ConcurrentHashMap<Quest, Integer> currentQuests) {
		this.currentQuests = currentQuests;
	}

	public LinkedList<String> getCompletedQuests() {
		return completedQuests;
	}

	public void setCompletedQuests(LinkedList<String> completedQuests) {
		this.completedQuests = completedQuests;
	}

	public Map<String, Long> getCompletedTimes() {
		return completedTimes;
	}

	public void setCompletedTimes(Map<String, Long> completedTimes) {
		this.completedTimes = completedTimes;
	}

	public Map<String, Integer> getAmountsCompleted() {
		return amountsCompleted;
	}

	public void setAmountsCompleted(Map<String, Integer> amountsCompleted) {
		this.amountsCompleted = amountsCompleted;
	}

	public Map<Quest, QuestData> getQuestData() {
		return questData;
	}

	public void setQuestData(Map<Quest, QuestData> questData) {
		this.questData = questData;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(id);
	}

	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getServer().getOfflinePlayer(id);
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

	public void updateJournal() {
		if (!hasJournal)
			return;
		Inventory inv = getPlayer().getInventory();
		ItemStack[] arr = inv.getContents();
		int index = -1;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null) {
				if (ItemUtil.isJournal(arr[i])) {
					index = i;
					break;
				}
			}
		}
		if (index != -1) {
			ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(ChatColor.LIGHT_PURPLE + Lang.get(getPlayer(), "journalTitle"));
			BookMeta book = (BookMeta) meta;
			book.setTitle(ChatColor.LIGHT_PURPLE + Lang.get(getPlayer(), "journalTitle"));
			book.setAuthor(getPlayer().getName());
			if (currentQuests.isEmpty()) {
				book.addPage(ChatColor.DARK_RED + Lang.get(getPlayer(), "journalNoQuests"));
			} else {
				int currentLength = 0;
				int currentLines = 0;
				String page = "";
				for (Quest quest : currentQuests.keySet()) {
					if ((currentLength + quest.getName().length() > 240) || (currentLines + ((quest.getName().length() % 19) 
							== 0 ? (quest.getName().length() / 19) : ((quest.getName().length() / 19) + 1))) > 13) {
						book.addPage(page);
						page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.getName() + "\n";
						currentLength = quest.getName().length();
						currentLines = (quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19) : (quest.getName().length() + 1);
					} else {
						page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.getName() + "\n";
						currentLength += quest.getName().length();
						currentLines += (quest.getName().length() / 19);
					}
					if (getObjectives(quest, false) != null) {
						for (String obj : getObjectives(quest, false)) {
							// Length/Line check
							if ((currentLength + obj.length() > 240) || (currentLines + ((obj.length() % 19) 
									== 0 ? (obj.length() / 19) : ((obj.length() / 19) + 1))) > 13) {
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
					} else {
						plugin.getLogger().severe("Quest Journal: objectives were null for " + quest.getName());
					}
					if (currentLines < 13)
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

	public void takeQuest(Quest q, boolean override) {
		if (q == null) {
			return;
		}
		Player player = getPlayer();
		Planner pln = q.getPlanner();
		long start = -1;
		long end = -1;
		if (pln.getStart() != null) {
			Calendar cal = Calendar.getInstance();
			String[] s = pln.getStart().split(":");
			cal.set(Integer.valueOf(s[2]), Integer.valueOf(s[1]), Integer.valueOf(s[0]),
					Integer.valueOf(s[3]), Integer.valueOf(s[4]), Integer.valueOf(s[5]));
			start = cal.getTimeInMillis();
		}
		if (pln.getEnd() != null) {
			Calendar cal = Calendar.getInstance();
			String[] s = pln.getEnd().split(":");
			cal.set(Integer.valueOf(s[2]), Integer.valueOf(s[1]), Integer.valueOf(s[0]),
					Integer.valueOf(s[3]), Integer.valueOf(s[4]), Integer.valueOf(s[5]));
			end = cal.getTimeInMillis();
		}
		if (start > -1 && end > -1) {
			if (pln.getRepeat() > -1) {
				long questLength = end - start;
				long nextStart = start;
				while (System.currentTimeMillis() >= nextStart) {
					nextStart += questLength + pln.getRepeat();
				}
				long nextEnd = nextStart + questLength;
				if (System.currentTimeMillis() < nextStart) {
					String early = Lang.get("plnTooEarly");
					early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
					early = early.replace("<time>", ChatColor.DARK_PURPLE
							+ Quests.getTime(nextStart - System.currentTimeMillis()) + ChatColor.YELLOW);
					player.sendMessage(ChatColor.YELLOW + early);
					return;
				}
				if (System.currentTimeMillis() > nextEnd) {
					if (System.currentTimeMillis() > end) {
						String late = Lang.get("plnTooLate");
						late = late.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.RED);
						late = late.replace("<time>", ChatColor.DARK_PURPLE
								+ Quests.getTime(System.currentTimeMillis() - end) + ChatColor.RED);
						player.sendMessage(ChatColor.RED + late);
						return;
					}
					return;
				}
			}
		}
		if (pln.getStart() != null) {
			if (System.currentTimeMillis() < start) {
				String early = Lang.get("plnTooEarly");
				early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
				early = early.replace("<time>", ChatColor.DARK_PURPLE
						+ Quests.getTime(start - System.currentTimeMillis()) + ChatColor.YELLOW);
				player.sendMessage(ChatColor.YELLOW + early);
				return;
			}
		}
		if (pln.getEnd() != null) {
			if (System.currentTimeMillis() > end) {
				String late = Lang.get("plnTooLate");
				late = late.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.RED);
				late = late.replace("<time>", ChatColor.DARK_PURPLE
						+ Quests.getTime(System.currentTimeMillis() - end) + ChatColor.RED);
				player.sendMessage(ChatColor.RED + late);
				return;
			}
		}
		if (q.testRequirements(player) == true || override) {
			addEmptiesFor(q, 0);
			try {
				currentQuests.put(q, 0);
			} catch (NullPointerException npe) {
				plugin.getLogger().severe("Unable to add quest" + q.getName() + " for player " + player.getName()
						+ ". Consider resetting player data or report on Github");
			}
			Stage stage = q.getStage(0);
			Requirements reqs = q.getRequirements();
			if (!override) {
				if (reqs.getMoney() > 0) {
					if (plugin.getDependencies().getVaultEconomy() != null) {
						plugin.getDependencies().getVaultEconomy().withdrawPlayer(getOfflinePlayer(), reqs.getMoney());
					}
				}
				for (ItemStack is : reqs.getItems()) {
					if (reqs.getRemoveItems().get(reqs.getItems().indexOf(is)) == true) {
						Quests.removeItem(player.getInventory(), is);
					}
				}
				String accepted = Lang.get(getPlayer(), "questAccepted");
				accepted = accepted.replace("<quest>", q.getName());
				player.sendMessage(ChatColor.GREEN + accepted);
				player.sendMessage("");
				if (plugin.getSettings().canShowQuestTitles()) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "title " + player.getName()
							+ " title " + "{\"text\":\"" + Lang.get(getPlayer(), "quest") + " " 
							+ Lang.get(getPlayer(), "accepted") +  "\",\"color\":\"gold\"}");
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "title " + player.getName()
							+ " subtitle " + "{\"text\":\"" + q.getName() + "\",\"color\":\"yellow\"}");
				}
			}
			String msg = Lang.get(getPlayer(), "questObjectivesTitle");
			msg = msg.replace("<quest>", q.getName());
			getPlayer().sendMessage(ChatColor.GOLD + msg);
			plugin.showObjectives(q, this, false);
			String stageStartMessage = stage.startMessage;
			if (stageStartMessage != null) {
				getPlayer().sendMessage(plugin.parseString(stageStartMessage, q, getPlayer()));
			}
			if (stage.chatEvents.isEmpty() == false) {
				for (String chatTrigger : stage.chatEvents.keySet()) {
					questData.get(q).eventFired.put(chatTrigger, false);
				}
			}
			if (stage.commandEvents.isEmpty() == false) {
				for (String commandTrigger : stage.commandEvents.keySet()) {
					questData.get(q).eventFired.put(commandTrigger, false);
				}
			}
			if (q.initialEvent != null) {
				q.initialEvent.fire(this, q);
			}
			if (stage.startEvent != null) {
				stage.startEvent.fire(this, q);
			}
			q.updateCompass(this, stage);
			q.updateGPS(this);
			saveData();
		} else {
			player.sendMessage(q.getRequirements().getFailRequirements());
		}
	}
	
	/**
	 * Get all objectives for a quest.
	 * 
	 * @param quest The quest to get objectives of
	 * @param ignoreOverrides Whether to ignore OP-specified objectives-overrides
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public LinkedList<String> getObjectives(Quest quest, boolean ignoreOverrides) {
		if (!ignoreOverrides) {
			if (getCurrentStage(quest) != null) {
				if (getCurrentStage(quest).objectiveOverride != null) {
					LinkedList<String> objectives = new LinkedList<String>();
					objectives.add(ChatColor.GREEN + getCurrentStage(quest).objectiveOverride);
					return objectives;
				}
			}
		}
		if (getQuestData(quest) == null)
			return new LinkedList<String>();
		LinkedList<String> unfinishedObjectives = new LinkedList<String>();
		LinkedList<String> finishedObjectives = new LinkedList<String>();
		LinkedList<String> objectives = new LinkedList<String>();
		for (ItemStack e : getCurrentStage(quest).blocksToBreak) {
			for (ItemStack e2 : getQuestData(quest).blocksBroken) {
				if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
					if (e2.getAmount() < e.getAmount()) {
						unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "break") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
					} else {
						finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "break") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
					}
				}
			}
		}
		for (ItemStack e : getCurrentStage(quest).blocksToDamage) {
			for (ItemStack e2 : getQuestData(quest).blocksDamaged) {
				if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
					if (e2.getAmount() < e.getAmount()) {
						unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "damage") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
					} else {
						finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "damage") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
					}
				}
			}
		}
		for (ItemStack e : getCurrentStage(quest).blocksToPlace) {
			for (ItemStack e2 : getQuestData(quest).blocksPlaced) {
				if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
					if (e2.getAmount() < e.getAmount()) {
						unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "place") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
					} else {
						finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "place") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
					}
				}
			}
		}
		for (ItemStack e : getCurrentStage(quest).blocksToUse) {
			for (ItemStack e2 : getQuestData(quest).blocksUsed) {
				if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
					if (e2.getAmount() < e.getAmount()) {
						unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "use") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
					} else {
						finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "use") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
					}
				}
			}
		}
		for (ItemStack e : getCurrentStage(quest).blocksToCut) {
			for (ItemStack e2 : getQuestData(quest).blocksCut) {
				if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
					if (e2.getAmount() < e.getAmount()) {
						unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "cut") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
					} else {
						finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "cut") + " " + ItemUtil.getName(e2) 
								+ ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
					}
				}
			}
		}
		if (getCurrentStage(quest).fishToCatch != null) {
			if (getQuestData(quest).getFishCaught() < getCurrentStage(quest).fishToCatch) {
				unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "catchFish") + ChatColor.GREEN + ": " 
			+ getQuestData(quest).getFishCaught() + "/" + getCurrentStage(quest).fishToCatch);
			} else {
				finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "catchFish") + ChatColor.GRAY + ": " 
			+ getQuestData(quest).getFishCaught() + "/" + getCurrentStage(quest).fishToCatch);
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
						String obj = Lang.get(getPlayer(), "enchantItem");
						obj = obj.replace("<item>", ItemUtil.getName(new ItemStack(mat)) + ChatColor.GREEN);
						obj = obj.replace("<enchantment>", ChatColor.LIGHT_PURPLE + Quester.prettyEnchantmentString(enchantment) + ChatColor.GREEN);
						unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + num1 + "/" + num2);
					} else {
						String obj = Lang.get(getPlayer(), "enchantItem");
						obj = obj.replace("<item>", ItemUtil.getName(new ItemStack(mat)) + ChatColor.GRAY);
						obj = obj.replace("<enchantment>", ChatColor.LIGHT_PURPLE + Quester.prettyEnchantmentString(enchantment) + ChatColor.GRAY);
						finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + num1 + "/" + num2);
					}
				}
			}
		}
		for (EntityType e : getCurrentStage(quest).mobsToKill) {
			for (EntityType e2 : getQuestData(quest).mobsKilled) {
				if (e == e2) {
					if (getQuestData(quest).mobNumKilled.size() > getQuestData(quest).mobsKilled.indexOf(e2) 
							&& getCurrentStage(quest).mobNumToKill.size() > getCurrentStage(quest).mobsToKill.indexOf(e)) {
						if (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2)) 
								< getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))) {
							if (getCurrentStage(quest).locationsToKillWithin.isEmpty()) {
								unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "kill") + " " 
										+ ChatColor.AQUA + Quester.prettyMobString(e) + ChatColor.GREEN + ": " 
										+ (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) 
										+ "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
							} else {
								String obj = Lang.get(getPlayer(), "killAtLocation");
								obj = obj.replace("<mob>", ChatColor.LIGHT_PURPLE + Quester.prettyMobString(e));
								obj = obj.replace("<location>", getCurrentStage(quest).killNames.get(getCurrentStage(quest).mobsToKill.indexOf(e)));
								unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " 
										+ (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) 
										+ "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
							}
						} else {
							if (getCurrentStage(quest).locationsToKillWithin.isEmpty()) {
								finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "kill") + " " 
										+ ChatColor.AQUA + Quester.prettyMobString(e) + ChatColor.GRAY + ": " 
										+ (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) 
										+ "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
							} else {
								String obj = Lang.get(getPlayer(), "killAtLocation");
								obj = obj.replace("<mob>", ChatColor.LIGHT_PURPLE + Quester.prettyMobString(e));
								obj = obj.replace("<location>", getCurrentStage(quest).killNames.get(getCurrentStage(quest).mobsToKill.indexOf(e)));
								finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " 
										+ (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2))) 
										+ "/" + (getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(e))));
							}
						}
					}
				}
			}
		}
		if (getCurrentStage(quest).playersToKill != null) {
			if (getQuestData(quest).getPlayersKilled() < getCurrentStage(quest).playersToKill) {
				unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "killPlayer") + ChatColor.GREEN + ": " 
			+ getQuestData(quest).getPlayersKilled() + "/" + getCurrentStage(quest).playersToKill);
			} else {
				finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "killPlayer") + ChatColor.GRAY + ": " 
			+ getQuestData(quest).getPlayersKilled() + "/" + getCurrentStage(quest).playersToKill);
			}
		}
		int index2 = 0;
		for (ItemStack is : getCurrentStage(quest).itemsToDeliver) {
			int delivered = 0;
			if (getQuestData(quest).itemsDelivered.containsKey(is)) {
				delivered = getQuestData(quest).itemsDelivered.get(is);
			}
			int amt = is.getAmount();
			Integer npc = getCurrentStage(quest).itemDeliveryTargets.get(index2);
			index2++;
			if (delivered < amt) {
				String obj = Lang.get(getPlayer(), "deliver");
				obj = obj.replace("<item>", ItemUtil.getName(is) + ChatColor.GREEN);
				obj = obj.replace("<npc>", plugin.getNPCName(npc));
				unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + delivered + "/" + amt);
			} else {
				String obj = Lang.get(getPlayer(), "deliver");
				obj = obj.replace("<item>", ItemUtil.getName(is) + ChatColor.GRAY);
				obj = obj.replace("<npc>", plugin.getNPCName(npc));
				finishedObjectives.add(ChatColor.GRAY + obj + ": " + delivered + "/" + amt);
			}
		}
		for (Integer n : getCurrentStage(quest).citizensToInteract) {
			for (Entry<Integer, Boolean> e : getQuestData(quest).citizensInteracted.entrySet()) {
				if (e.getKey().equals(n)) {
					if (e.getValue() == false) {
						String obj = Lang.get(getPlayer(), "talkTo");
						obj = obj.replace("<npc>", plugin.getNPCName(n));
						unfinishedObjectives.add(ChatColor.GREEN + obj);
					} else {
						String obj = Lang.get(getPlayer(), "talkTo");
						obj = obj.replace("<npc>", plugin.getNPCName(n));
						finishedObjectives.add(ChatColor.GRAY + obj);
					}
				}
			}
		}
		for (Integer n : getCurrentStage(quest).citizensToKill) {
			for (Integer n2 : getQuestData(quest).citizensKilled) {
				if (n.equals(n2)) {
					if (getQuestData(quest).citizenNumKilled.size() > getQuestData(quest).citizensKilled.indexOf(n2) 
							&& getCurrentStage(quest).citizenNumToKill.size() > getCurrentStage(quest).citizensToKill.indexOf(n)) {
						if (getQuestData(quest).citizenNumKilled.get(getQuestData(quest).citizensKilled.indexOf(n2)) 
								< getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n))) {
							unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "kill") + " " + plugin.getNPCName(n) 
									+ ChatColor.GREEN + " " + getQuestData(quest).citizenNumKilled.get(getCurrentStage(quest).citizensToKill.indexOf(n)) 
									+ "/" + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n)));
						} else {
							finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "kill") + " " + plugin.getNPCName(n) 
									+ ChatColor.GRAY + " " + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n)) 
									+ "/" + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(n)));
						}
					}
				}
			}
		}
		for (Entry<EntityType, Integer> e : getCurrentStage(quest).mobsToTame.entrySet()) {
			for (Entry<EntityType, Integer> e2 : getQuestData(quest).mobsTamed.entrySet()) {
				if (e.getKey().equals(e2.getKey())) {
					if (e2.getValue() < e.getValue()) {
						unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "tame") + " " + getCapitalized(e.getKey().name()) 
								+ ChatColor.GREEN + ": " + e2.getValue() + "/" + e.getValue());
					} else {
						finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "tame") + " " + getCapitalized(e.getKey().name()) 
								+ ChatColor.GRAY + ": " + e2.getValue() + "/" + e.getValue());
					}
				}
			}
		}
		for (Entry<DyeColor, Integer> e : getCurrentStage(quest).sheepToShear.entrySet()) {
			for (Entry<DyeColor, Integer> e2 : getQuestData(quest).sheepSheared.entrySet()) {
				if (e.getKey().equals(e2.getKey())) {
					if (e2.getValue() < e.getValue()) {
						String obj = Lang.get(getPlayer(), "shearSheep");
						obj = obj.replace("<color>", e.getKey().name().toLowerCase());
						unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + e2.getValue() + "/" + e.getValue());
					} else {
						String obj = Lang.get(getPlayer(), "shearSheep");
						obj = obj.replace("<color>", e.getKey().name().toLowerCase());
						finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + e2.getValue() + "/" + e.getValue());
					}
				}
			}
		}
		for (Location l : getCurrentStage(quest).locationsToReach) {
			for (Location l2 : getQuestData(quest).locationsReached) {
				if (l.equals(l2)) {
					if (!getQuestData(quest).hasReached.isEmpty()) {
						if (getQuestData(quest).hasReached.get(getQuestData(quest).locationsReached.indexOf(l2)) == false) {
							String obj = Lang.get(getPlayer(), "goTo");
							obj = obj.replace("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest).locationsToReach.indexOf(l)));
							unfinishedObjectives.add(ChatColor.GREEN + obj);
						} else {
							String obj = Lang.get(getPlayer(), "goTo");
							obj = obj.replace("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest).locationsToReach.indexOf(l)));
							finishedObjectives.add(ChatColor.GRAY + obj);
						}
					}
				}
			}
		}
		for (String s : getCurrentStage(quest).passwordDisplays) {
			if (getQuestData(quest).passwordsSaid.containsKey(s)) {
				Boolean b = getQuestData(quest).passwordsSaid.get(s);
				if (b != null && !b) {
					unfinishedObjectives.add(ChatColor.GREEN + s);
				} else {
					finishedObjectives.add(ChatColor.GRAY + s);
				}
			}
		}
		for (CustomObjective co : getCurrentStage(quest).customObjectives) {
			int countsIndex = 0;
			String display = co.getDisplay();
			boolean addUnfinished = false;
			boolean addFinished = false;
			for (Entry<String, Integer> entry : getQuestData(quest).customObjectiveCounts.entrySet()) {
				if (co.getName().equals(entry.getKey())) {
					int dataIndex = 0;
					for (Entry<String,Object> prompt : co.getData()) {
						String replacement = "%" + prompt.getKey() + "%";
						try {
							if (display.contains(replacement))
								display = display.replace(replacement, ((String) getCurrentStage(quest).customObjectiveData.get(dataIndex).getValue()));
						} catch (NullPointerException ne) {
							plugin.getLogger().severe("Unable to fetch display for " + co.getName() + " on " + quest.getName());
							ne.printStackTrace();
						}
						dataIndex++;
					}
					if (entry.getValue() < getCurrentStage(quest).customObjectiveCounts.get(countsIndex)) {
						if (co.canShowCount()) {
							display = display.replace("%count%", entry.getValue() + "/" + getCurrentStage(quest).customObjectiveCounts.get(countsIndex));
						}
						addUnfinished = true;
					} else {
						if (co.canShowCount()) {
							display = display.replace("%count%", getCurrentStage(quest).customObjectiveCounts.get(countsIndex) 
									+ "/" + getCurrentStage(quest).customObjectiveCounts.get(countsIndex));
						}
						addFinished = true;
					}
				}
				countsIndex++;
			}
			if (addUnfinished) {
				unfinishedObjectives.add(ChatColor.GREEN + display);
			}
			if (addFinished) {
				finishedObjectives.add(ChatColor.GRAY + display);
			}
		}
		objectives.addAll(unfinishedObjectives);
		objectives.addAll(finishedObjectives);
		return objectives;
	}

	/**
	 * Check if player's current stage has the specified objective<p>
	 * 
	 * Accepted strings are: breakBlock, damageBlock, placeBlock, useBlock,
	 * cutBlock, catchFish, enchantItem, killMob, deliverItem, killPlayer,
	 * talkToNPC, killNPC, tameMob, shearSheep, password, reachLocation
	 * 
	 * @deprecated Use containsObjective() instead
	 * @param quest The quest to check objectives of
	 * @param s The type of objective to check for
	 * @return true if quest contains specified objective
	 */
	public boolean hasObjective(Quest quest, String s) {
		return containsObjective(quest, s);
	}
	
	/**
	 * Check if player's current stage has the specified objective<p>
	 * 
	 * Accepted strings are: breakBlock, damageBlock, placeBlock, useBlock,
	 * cutBlock, catchFish, enchantItem, killMob, deliverItem, killPlayer,
	 * talkToNPC, killNPC, tameMob, shearSheep, password, reachLocation
	 * 
	 * @param quest The quest to check objectives of
	 * @param s The type of objective to check for
	 * @return true if quest contains specified objective
	 */
	public boolean containsObjective(Quest quest, String s) {
		if (getCurrentStage(quest) == null) {
			return false;
		}
		if (s.equalsIgnoreCase("breakBlock")) {
			return !getCurrentStage(quest).blocksToBreak.isEmpty();
		} else if (s.equalsIgnoreCase("damageBlock")) {
			return !getCurrentStage(quest).blocksToDamage.isEmpty();
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
		} else if (s.equalsIgnoreCase("password")) {
			return !getCurrentStage(quest).passwordPhrases.isEmpty();
		} else if (s.equalsIgnoreCase("reachLocation")) {
			return !getCurrentStage(quest).locationsToReach.isEmpty();
		} else {
			return false;
		}
	}

	public boolean hasCustomObjective(Quest quest, String s) {
		if (getQuestData(quest) == null) {
			return false;
		}
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
	
	@SuppressWarnings("deprecation")
	public void breakBlock(Quest quest, ItemStack m) {
		ItemStack temp = m;
		temp.setAmount(0);
		ItemStack broken = temp;
		ItemStack toBreak = temp;
		for (ItemStack is : getQuestData(quest).blocksBroken) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						broken = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						broken = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					broken = is;
				}
			}
		}
		for (ItemStack is : getCurrentStage(quest).blocksToBreak) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						toBreak = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						toBreak = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					toBreak = is;
				}
			}
		}
		if (broken.getAmount() < toBreak.getAmount()) {
			ItemStack newBroken = broken;
			newBroken.setAmount(broken.getAmount() + 1);
			if (getQuestData(quest).blocksBroken.contains(broken)) {
				getQuestData(quest).blocksBroken.set(getQuestData(quest).blocksBroken.indexOf(broken), newBroken);
				if (broken.getAmount() == toBreak.getAmount()) {
					finishObjective(quest, "breakBlock", m, null, null, null, null, null, null, null, null, null);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void damageBlock(Quest quest, ItemStack m) {
		ItemStack temp = m;
		temp.setAmount(0);
		ItemStack damaged = temp;
		ItemStack toDamage = temp;
		for (ItemStack is : getQuestData(quest).blocksDamaged) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						damaged = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						damaged = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					damaged = is;
				}
			}
		}
		for (ItemStack is : getCurrentStage(quest).blocksToDamage) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						toDamage = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						toDamage = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					toDamage = is;
				}
			}
		}
		if (damaged.getAmount() < toDamage.getAmount()) {
			ItemStack newDamaged = damaged;
			newDamaged.setAmount(damaged.getAmount() + 1);
			if (getQuestData(quest).blocksDamaged.contains(damaged)) {
				getQuestData(quest).blocksDamaged.set(getQuestData(quest).blocksDamaged.indexOf(damaged), newDamaged);
				if (damaged.getAmount() == toDamage.getAmount()) {
					finishObjective(quest, "damageBlock", m, null, null, null, null, null, null, null, null, null);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void placeBlock(Quest quest, ItemStack m) {
		ItemStack temp = m;
		temp.setAmount(0);
		ItemStack placed = temp;
		ItemStack toPlace = temp;
		for (ItemStack is : getQuestData(quest).blocksPlaced) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						placed = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						placed = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					placed = is;
				}
			}
		}
		for (ItemStack is : getCurrentStage(quest).blocksToPlace) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						toPlace = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						toPlace = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					toPlace = is;
				}
			}
		}
		if (placed.getAmount() < toPlace.getAmount()) {
			ItemStack newplaced = placed;
			newplaced.setAmount(placed.getAmount() + 1);
			if (getQuestData(quest).blocksPlaced.contains(placed)) {
				getQuestData(quest).blocksPlaced.set(getQuestData(quest).blocksPlaced.indexOf(placed), newplaced);
				if (placed.getAmount() == toPlace.getAmount()) {
					finishObjective(quest, "placeBlock", m, null, null, null, null, null, null, null, null, null);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void useBlock(Quest quest, ItemStack m) {
		ItemStack temp = m;
		temp.setAmount(0);
		ItemStack used = temp;
		ItemStack toUse = temp;
		for (ItemStack is : getQuestData(quest).blocksUsed) {
			if (m.getType() == is.getType() ) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						used = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						used = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					used = is;
				}
			}
		}
		for (ItemStack is : getCurrentStage(quest).blocksToUse) {
			if (m.getType() == is.getType() ) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid, so check durability
					if (m.getDurability() == is.getDurability()) {
						toUse = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						toUse = is;
					}
				} else {
					//Blocks are not solid, so ignore durability
					toUse = is;
				}
			}
		}
		if (used.getAmount() < toUse.getAmount()) {
			ItemStack newUsed = used;
			newUsed.setAmount(used.getAmount() + 1);
			if (getQuestData(quest).blocksUsed.contains(used)) {
				getQuestData(quest).blocksUsed.set(getQuestData(quest).blocksUsed.indexOf(used), newUsed);
				if (used.getAmount() == toUse.getAmount()) {
					finishObjective(quest, "useBlock", m, null, null, null, null, null, null, null, null, null);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void cutBlock(Quest quest, ItemStack m) {
		ItemStack temp = m;
		temp.setAmount(0);
		ItemStack cut = temp;
		ItemStack toCut = temp;
		for (ItemStack is : getQuestData(quest).blocksCut) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						cut = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						cut = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					cut = is;
				}
			}
		}
		for (ItemStack is : getCurrentStage(quest).blocksToCut) {
			if (m.getType() == is.getType()) {
				if (m.getType().isSolid() && is.getType().isSolid()) {
					//Blocks are solid so check for durability
					if (m.getDurability() == is.getDurability()) {
						toCut = is;
					} else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
						//Ignore durability for 1.13+
						toCut = is;
					}
				} else {
					//Blocks are not solid so ignore durability
					toCut = is;
				}
			}
		}
		if (cut.getAmount() < toCut.getAmount()) {
			ItemStack newCut = cut;
			newCut.setAmount(cut.getAmount() + 1);
			if (getQuestData(quest).blocksCut.contains(cut)) {
				getQuestData(quest).blocksCut.set(getQuestData(quest).blocksCut.indexOf(cut), newCut);
				if (cut.getAmount() == toCut.getAmount()) {
					finishObjective(quest, "cutBlock", m, null, null, null, null, null, null, null, null, null);
				}
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
	
	public void catchFish(Quest quest) {
		if (getQuestData(quest).getFishCaught() < getCurrentStage(quest).fishToCatch) {
			getQuestData(quest).setFishCaught(getQuestData(quest).getFishCaught() + 1);
			if (((Integer) getQuestData(quest).getFishCaught()).equals(getCurrentStage(quest).fishToCatch)) {
				finishObjective(quest, "catchFish", null, null, null, null, null, null, null, null, null, null);
			}
		}
	}

	public void killMob(Quest quest, Location killedLocation, EntityType e) {
		QuestData questData = getQuestData(quest);
		if (e == null) {
			return;
		}
		if (questData.mobsKilled.contains(e) == false) {
			return;
		}
		Stage currentStage = getCurrentStage(quest);
		int indexOfMobKilled = questData.mobsKilled.indexOf(e);
		int numberOfSpecificMobKilled = questData.mobNumKilled.get(indexOfMobKilled);
		int numberOfSpecificMobNeedsToBeKilledInCurrentStage = currentStage.mobNumToKill.get(indexOfMobKilled);
		if (questData.locationsToKillWithin.isEmpty() == false) {
			Location locationToKillWithin = questData.locationsToKillWithin.get(indexOfMobKilled);
			double radius = questData.radiiToKillWithin.get(indexOfMobKilled);
			// Check world #name, not the object
			if ((killedLocation.getWorld().getName().equals(locationToKillWithin.getWorld().getName())) == false) {
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
		if (getQuestData(quest).getPlayersKilled() < getCurrentStage(quest).playersToKill) {
			getQuestData(quest).setPlayersKilled(getQuestData(quest).getPlayersKilled() + 1);
			if (((Integer) getQuestData(quest).getPlayersKilled()).equals(getCurrentStage(quest).playersToKill)) {
				finishObjective(quest, "killPlayer", null, null, null, null, null, null, null, null, null, null);
			}
		}
	}

	public void interactWithNPC(Quest quest, NPC n) {
			if (getQuestData(quest).citizensInteracted.containsKey(n.getId())) {
				Boolean b = getQuestData(quest).citizensInteracted.get(n.getId());
				if (b != null && !b) {
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
		if (getQuestData(quest).locationsReached == null) {
			return;
		}
		if (getQuestData(quest).locationsReached.isEmpty()) {
			return;
		}
		for (Location location : getQuestData(quest).locationsReached) {
			try {
				int index = getQuestData(quest).locationsReached.indexOf(location);
				Location locationToReach = getCurrentStage(quest).locationsToReach.get(index);
				double radius = getQuestData(quest).radiiToReachWithin.get(index);
				if (l.getX() < (locationToReach.getX() + radius) && l.getX() > (locationToReach.getX() - radius)) {
					if (l.getZ() < (locationToReach.getZ() + radius) && l.getZ() > (locationToReach.getZ() - radius)) {
						if (l.getY() < (locationToReach.getY() + radius) && l.getY() > (locationToReach.getY() - radius)) {
							if (l.getWorld().getName().equals(locationToReach.getWorld().getName())) {
								if (getQuestData(quest).hasReached.get(index) == false) {
									getQuestData(quest).hasReached.set(index, true);
									finishObjective(quest, "reachLocation", null, null, null, null, null, null, location, null, null, null);
								}
							}
						}
					}
				}
			} catch (IndexOutOfBoundsException e) {
				plugin.getLogger().severe("An error has occurred with Quests. Please report on Github. Include the info below");
				plugin.getLogger().severe("index = " + getQuestData(quest).locationsReached.indexOf(location));
				plugin.getLogger().severe("locationsReached = " + getQuestData(quest).locationsReached.toString());
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
			if (getCurrentStage(quest).itemsToDeliver.indexOf(found) < 0) {
				plugin.getLogger().severe("Index out of bounds while delivering " + found.getType() + " x " + found.getAmount() + " for quest " 
						+ quest.getName() + " with " + i.getType() + " x " + i.getAmount() + " already delivered. Int -amount- reports value of " + 
						+ amount + ". Please report this error on Github!");
				player.sendMessage("Quests had a problem delivering your item, please contact an administrator!");
				return;
			}
			int req = getCurrentStage(quest).itemsToDeliver.get(getCurrentStage(quest).itemsToDeliver.indexOf(found)).getAmount();
			Material m = i.getType();
			if (amount < req) {
				if ((i.getAmount() + amount) > req) {
					getQuestData(quest).itemsDelivered.put(found, req);
					int index = player.getInventory().first(i);
					i.setAmount(i.getAmount() - (req - amount)); // Take away the remaining amount needed to be delivered from the item stack
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
					String message = Quests.parseString(getCurrentStage(quest).deliverMessages.get(new Random().nextInt(
							getCurrentStage(quest).deliverMessages.size())), plugin.getDependencies().getCitizens().getNPCRegistry().getById(
									getCurrentStage(quest).itemDeliveryTargets.get(getCurrentStage(quest).itemsToDeliver.indexOf(found))));
					player.sendMessage(message);
				}
			}
		}
	}

	public void sayPassword(Quest quest, AsyncPlayerChatEvent evt) {
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
	 * 
	 * @param quest
	 *            Quest containing the objective
	 * @param objective
	 *            Type of objective, e.g. "password" or "damageBlock"
	 * @param material
	 *            Block being damaged, broken, etc.
	 * @param delivery
	 *            Item being delivered
	 * @param enchantment
	 *            Enchantment being applied by user
	 * @param mob
	 *            Mob to be killed or tamed
	 * @param player
	 *            Currently unused
	 * @param npc
	 *            NPC to talk to or kill
	 * @param location
	 *            Location for user to reach
	 * @param color
	 *            shear color
	 * @param pass
	 *            Password
	 * @param co
	 *            See CustomObjective class
	 */
	@SuppressWarnings("deprecation")
	public void finishObjective(Quest quest, String objective, ItemStack itemStack, ItemStack delivery, Enchantment enchantment, 
			EntityType mob, String player, NPC npc, Location location, DyeColor color, String pass, CustomObjective co) {
		Player p = getPlayer();
		if (getCurrentStage(quest).objectiveOverride != null) {
			if (testComplete(quest)) {
				String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + getCurrentStage(quest).objectiveOverride;
				p.sendMessage(message);
				quest.nextStage(this);
			}
			return;
		}
		if (objective.equalsIgnoreCase("password")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + pass;
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("breakBlock")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "break") + " <item>";
			String stack = getQuestData(quest).blocksBroken.toString();
			String amount = stack.substring(stack.lastIndexOf(" x ") + 3).replace("}]", "");
			message = message + " " + amount + "/" + amount;
			plugin.getLocaleQuery().sendMessage(p, message, itemStack.getType(), itemStack.getDurability());
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("damageBlock")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "damage") + " <item>";
			String stack = getQuestData(quest).blocksDamaged.toString();
			String amount = stack.substring(stack.lastIndexOf(" x ") + 3).replace("}]", "");
			message = message + " " + amount + "/" + amount;
			plugin.getLocaleQuery().sendMessage(p, message, itemStack.getType(), itemStack.getDurability());
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("placeBlock")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "place") + " <item>";
			String stack = getQuestData(quest).blocksPlaced.toString();
			String amount = stack.substring(stack.lastIndexOf(" x ") + 3).replace("}]", "");
			message = message + " " + amount + "/" + amount;
			plugin.getLocaleQuery().sendMessage(p, message, itemStack.getType(), itemStack.getDurability());
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("useBlock")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "use") + " <item>";
			String stack = getQuestData(quest).blocksUsed.toString();
			String amount = stack.substring(stack.lastIndexOf(" x ") + 3).replace("}]", "");
			message = message + " " + amount + "/" + amount;
			plugin.getLocaleQuery().sendMessage(p, message, itemStack.getType(), itemStack.getDurability());
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("cutBlock")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "cut") + " <item>";
			String stack = getQuestData(quest).blocksCut.toString();
			String amount = stack.substring(stack.lastIndexOf(" x ") + 3).replace("}]", "");
			message = message + " " + amount + "/" + amount;
			plugin.getLocaleQuery().sendMessage(p, message, itemStack.getType(), itemStack.getDurability());
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("catchFish")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "catchFish") + " ";
			message = message + " " + getCurrentStage(quest).fishToCatch + "/" + getCurrentStage(quest).fishToCatch;
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("enchantItem")) {
			String obj = Lang.get(p, "enchantItem");
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
			for (Map<Enchantment, Material> map : getCurrentStage(quest).itemsToEnchant.keySet()) {
				if (map.containsKey(enchantment)) {
					message = message + " " + getCurrentStage(quest).itemsToEnchant.get(map) + "/" + getCurrentStage(quest).itemsToEnchant.get(map);
					break;
				}
			}
			plugin.getLocaleQuery().sendMessage(p, message, itemStack.getType(), itemStack.getDurability(), enchantment);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("deliverItem")) {
			String obj = Lang.get(p, "deliver");
			obj = obj.replace("<npc>", plugin.getNPCName(getCurrentStage(quest).itemDeliveryTargets.get(getCurrentStage(quest).itemsToDeliver.indexOf(delivery))));
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
			ItemStack is = getCurrentStage(quest).itemsToDeliver.get(getCurrentStage(quest).itemsToDeliver.indexOf(delivery));
			plugin.getLocaleQuery().sendMessage(p, message, is.getType(), is.getDurability());
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("killMob")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "kill") + " <mob>";
			message = message + " " + getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(mob)) + "/" + getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill.indexOf(mob));
			plugin.getLocaleQuery().sendMessage(p, message, mob);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("killPlayer")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "killPlayer");
			message = message + " " + getCurrentStage(quest).playersToKill + "/" + getCurrentStage(quest).playersToKill;
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("talkToNPC")) {
			String obj = Lang.get(p, "talkTo");
			obj = obj.replace("<npc>", plugin.getNPCName(npc.getId()));
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("killNPC")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "kill") + " " + npc.getName();
			message = message + " " + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(npc.getId())) + "/" + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill.indexOf(npc.getId()));
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("tameMob")) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "tame") + " <mob>";
			message = message + " " + getCurrentStage(quest).mobsToTame.get(mob) + "/" + getCurrentStage(quest).mobsToTame.get(mob);
			plugin.getLocaleQuery().sendMessage(p, message, mob);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("shearSheep")) {
			String obj = Lang.get(p, "shearSheep");
			obj = obj.replace("<color>", color.name().toLowerCase());
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
			message = message + " " + getCurrentStage(quest).sheepToShear.get(color) + "/" + getCurrentStage(quest).sheepToShear.get(color);
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (objective.equalsIgnoreCase("reachLocation")) {
			String obj = Lang.get(p, "goTo");
			obj = obj.replace("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest).locationsToReach.indexOf(location)));
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		} else if (co != null) {
			String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + co.getDisplay();
			int index = -1;
			for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
				if (getCurrentStage(quest).customObjectives.get(i).getName().equals(co.getName())) {
					index = i;
					break;
				}
			}
			Entry<String, Object> datamap = getCurrentStage(quest).customObjectiveData.get(index);
			message = message.replace("%" + ((String) datamap.getKey()) + "%", (String) datamap.getValue());
			
			if (co.canShowCount()) {
				message = message.replace("%count%", getCurrentStage(quest).customObjectiveCounts.get(index) + "/" + getCurrentStage(quest).customObjectiveCounts.get(index));
			}
			p.sendMessage(message);
			if (testComplete(quest)) {
				quest.nextStage(this);
			}
		}
	}

	public boolean testComplete(Quest quest) {
		for (String s : getObjectives(quest, true)) {
			if (s.startsWith(ChatColor.GREEN.toString())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Add empty map values per Quest stage
	 * 
	 * @param quest Quest with at least one stage
	 * @param stage Where first stage is '0'
	 */
	@SuppressWarnings("deprecation")
	public void addEmptiesFor(Quest quest, int stage) {
		QuestData data = new QuestData(this);
		data.setDoJournalUpdate(false);
		if (quest.getStage(stage).blocksToBreak.isEmpty() == false) {
			for (ItemStack i : quest.getStage(stage).blocksToBreak) {
				if (data.blocksBroken.indexOf(i) != -1) {
					ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
					data.blocksBroken.set(data.blocksBroken.indexOf(temp), temp);
				} else {
					ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
					data.blocksBroken.add(temp);
				}
			}
		}
		if (quest.getStage(stage).blocksToDamage.isEmpty() == false) {
			for (ItemStack i : quest.getStage(stage).blocksToDamage) {
				if (data.blocksDamaged.indexOf(i) != -1) {
					ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
					data.blocksDamaged.set(data.blocksDamaged.indexOf(temp), temp);
				} else {
					ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
					data.blocksDamaged.add(temp);
				}
			}
		}
		if (quest.getStage(stage).blocksToPlace.isEmpty() == false) {
			for (ItemStack i : quest.getStage(stage).blocksToPlace) {
				if (data.blocksPlaced.indexOf(i) != -1) {
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

	/**
	 * Cleans up item names. 'WOODEN_BUTTON' becomes 'Wooden Button'
	 * 
	 * @param itemName any item name, ideally
	 * @return cleaned-up string
	 */
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
	
	/**
	 * Gets player-friendly name from type. 'ENDER_DRAGON' becomes 'Ender Dragon'
	 * 
	 * @param type any mob type, ideally
	 * @return cleaned-up string
	 */
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
	
	public static String prettyColorString(DyeColor color) {
		return Lang.get("COLOR_" + color.name());
	}

	/**
	 * Gets player-friendly name from enchantment. 'FIRE_ASPECT' becomes 'Fire Aspect'
	 * 
	 * @param e Enchantment to get pretty localized name of
	 * @return pretty localized name
	 */
	public static String prettyEnchantmentString(Enchantment e) {
		String prettyString = enchantmentString(e);
		prettyString = capitalsToSpaces(prettyString);
		return prettyString;
	}
	
	/**
	 * Gets name of enchantment exactly as it appears in lang file
	 * 
	 * @param e Enchantment to get localized name of
	 * @return localized name
	 */
	@SuppressWarnings("deprecation") // since 1.13
	public static String enchantmentString(Enchantment e) {
		try {
			return (Lang.get("ENCHANTMENT_" + e.getName()));
		} catch (NullPointerException ne) {
			Bukkit.getLogger().warning(e.getName() + " was not found in Lang.yml, please ask the developer to " 
					+ "update the file or simply add an entry for the enchantment");
			return e.getName().toLowerCase().replace("_", " ");
		}
	}
	
	/**
	 * Adds a single space in front of all capital letters
	 * 
	 * @param s string to process
	 * @return processed string
	 */
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

	/**
	 * Capitalize character after space
	 * 
	 * @param s string to process
	 * @return processed string
	 */
	public static String spaceToCapital(String s) {
		int index = s.indexOf(' ');
		if (index == -1) {
			return null;
		}
		s = s.substring(0, (index + 1)) + Character.toUpperCase(s.charAt(index + 1)) + s.substring(index + 2);
		s = s.replaceFirst(" ", "");
		return s;
	}

	public void saveData() {
		FileConfiguration data = getBaseData();
		try {
			data.save(new File(plugin.getDataFolder(), "data" + File.separator + id + ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long getCooldownDifference(Quest q) {
		long currentTime = System.currentTimeMillis();
		long lastTime;
		if (completedTimes.containsKey(q.getName()) == false) {
			lastTime = System.currentTimeMillis();
			completedTimes.put(q.getName(), System.currentTimeMillis());
		} else {
			lastTime = completedTimes.get(q.getName());
		}
		long comparator = q.getPlanner().getCooldown();
		long difference = (comparator - (currentTime - lastTime));
		return difference;
	}

	@SuppressWarnings("deprecation")
	public FileConfiguration getBaseData() {
		FileConfiguration data = new YamlConfiguration();
		if (currentQuests.isEmpty() == false) {
			ArrayList<String> questNames = new ArrayList<String>();
			ArrayList<Integer> questStages = new ArrayList<Integer>();
			for (Quest quest : currentQuests.keySet()) {
				questNames.add(quest.getName());
				questStages.add(currentQuests.get(quest));
			}
			data.set("currentQuests", questNames);
			data.set("currentStages", questStages);
			data.set("quest-points", questPoints);
			ConfigurationSection dataSec = data.createSection("questData");
			for (Quest quest : currentQuests.keySet()) {
				if (quest.getName() == null || quest.getName().isEmpty()) {
					plugin.getLogger().severe("Quest name was null or empty while loading data");
					return null;
				}
				ConfigurationSection questSec = dataSec.createSection(quest.getName());
				QuestData questData = getQuestData(quest);
				if (questData == null)
					continue;
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
					questSec.set("blocks-damaged-durability", blockDurability);
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
					questSec.set("blocks-placed-durability", blockDurability);
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
					questSec.set("blocks-used-durability", blockDurability);
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
					questSec.set("blocks-cut-durability", blockDurability);
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
					LinkedList<String> chatTriggers = new LinkedList<String>();
					for (String trigger : questData.eventFired.keySet()) {
						if (questData.eventFired.get(trigger) == true) {
							chatTriggers.add(trigger);
						}
					}
					if (chatTriggers.isEmpty() == false) {
						questSec.set("chat-triggers", chatTriggers);
					}
				}
				if (questData.eventFired.isEmpty() == false) {
					LinkedList<String> commandTriggers = new LinkedList<String>();
					for (String commandTrigger : questData.eventFired.keySet()) {
						if (questData.eventFired.get(commandTrigger) == true) {
							commandTriggers.add(commandTrigger);
						}
					}
					if (commandTriggers.isEmpty() == false) {
						questSec.set("command-triggers", commandTriggers);
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
			List<String> noDupe = new ArrayList<String>();
			for (String s : completedQuests)
				if (!noDupe.contains(s))
					noDupe.add(s);
			String[] completed = new String[noDupe.size()];
			int index = 0;
			for (String s : noDupe) {
				completed[index] = s;
				index++;
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

	@SuppressWarnings("deprecation")
	public boolean loadData() {
		FileConfiguration data = new YamlConfiguration();
		try {
			File dataFile = new File(plugin.getDataFolder(), "data" + File.separator + id.toString() + ".yml");
			if (dataFile.exists() == false) {
				OfflinePlayer p = getOfflinePlayer();
				dataFile = new File(plugin.getDataFolder(), "data" + File.separator + p.getName() + ".yml");
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
			List<String> redoNames = data.getStringList("completedRedoableQuests");
			List<Long> redoTimes = data.getLongList("completedQuestTimes");
			for (String s : redoNames) {
				for (Quest q : plugin.getQuests()) {
					if (q.getName().equalsIgnoreCase(s)) {
						completedTimes.put(q.getName(), redoTimes.get(redoNames.indexOf(s)));
						break;
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
				for (Quest q : plugin.getQuests()) {
					if (q.getName().equalsIgnoreCase(s)) {
						if (!completedQuests.contains(q.getName())) {
							completedQuests.add(q.getName());
						}
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
					plugin.getLogger().severe("[Quests] Invalid stage number for player: \"" + id + "\" on Quest \"" + quest.getName() + "\". Quest ended.");
					continue;
				}
				addEmptiesFor(quest, 0);
				if (questSec == null)
					continue;
				if (questSec.contains("blocks-broken-names")) {
					List<String> names = questSec.getStringList("blocks-broken-names");
					List<Integer> amounts = questSec.getIntegerList("blocks-broken-amounts");
					List<Short> durability = questSec.getShortList("blocks-broken-durability");
					for (String s : names) {
						ItemStack is;
						try {
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
						} catch (IndexOutOfBoundsException e) {
							// Legacy
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
						}
						getQuestData(quest).blocksBroken.set(names.indexOf(s), is);
					}
				}
				if (questSec.contains("blocks-damaged-names")) {
					List<String> names = questSec.getStringList("blocks-damaged-names");
					List<Integer> amounts = questSec.getIntegerList("blocks-damaged-amounts");
					List<Short> durability = questSec.getShortList("blocks-damaged-durability");
					for (String s : names) {
						ItemStack is;
						try {
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
						} catch (IndexOutOfBoundsException e) {
							// Legacy
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
						}
						getQuestData(quest).blocksDamaged.set(names.indexOf(s), is);
					}
				}
				if (questSec.contains("blocks-placed-names")) {
					List<String> names = questSec.getStringList("blocks-placed-names");
					List<Integer> amounts = questSec.getIntegerList("blocks-placed-amounts");
					List<Short> durability = questSec.getShortList("blocks-placed-durability");
					for (String s : names) {
						ItemStack is;
						try {
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
						} catch (IndexOutOfBoundsException e) {
							// Legacy
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
						}
						getQuestData(quest).blocksPlaced.set(names.indexOf(s), is);
					}
				}
				if (questSec.contains("blocks-used-names")) {
					List<String> names = questSec.getStringList("blocks-used-names");
					List<Integer> amounts = questSec.getIntegerList("blocks-used-amounts");
					List<Short> durability = questSec.getShortList("blocks-used-durability");
					for (String s : names) {
						ItemStack is;
						try {
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
						} catch (IndexOutOfBoundsException e) {
							// Legacy
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
						}
						getQuestData(quest).blocksUsed.set(names.indexOf(s), is);
					}
				}
				if (questSec.contains("blocks-cut-names")) {
					List<String> names = questSec.getStringList("blocks-cut-names");
					List<Integer> amounts = questSec.getIntegerList("blocks-cut-amounts");
					List<Short> durability = questSec.getShortList("blocks-cut-durability");
					for (String s : names) {
						ItemStack is;
						try {
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), durability.get(names.indexOf(s)));
						} catch (IndexOutOfBoundsException e) {
							// Legacy
							is = new ItemStack(Material.matchMaterial(s), amounts.get(names.indexOf(s)), (short) 0);
						}
						getQuestData(quest).blocksCut.set(names.indexOf(s), is);
					}
				}
				if (questSec.contains("fish-caught")) {
					getQuestData(quest).setFishCaught(questSec.getInt("fish-caught"));
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
								if (Quests.getLocation(loc) != null) {
									locations.add(Quests.getLocation(loc));
								}
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
					while (has.size() < locations.size()) {
						// TODO - find proper cause of Github issue #646
						plugin.getLogger().info("Added missing has-reached-location data for Quester " + id);
						has.add(false);
					}
					List<Integer> radii = questSec.getIntegerList("radii-to-reach-within");
					for (String loc : questSec.getStringList("locations-to-reach")) {
						if (Quests.getLocation(loc) != null) {
							locations.add(Quests.getLocation(loc));
						}
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
						getQuestData(quest).mobsTamed.put(EntityType.valueOf(mob.toUpperCase()), amounts.get(mobs.indexOf(mob)));
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
					for (String chatTrig : getCurrentStage(quest).chatEvents.keySet()) {
						getQuestData(quest).eventFired.put(chatTrig, false);
					}
				}
				if (questSec.contains("chat-triggers")) {
					List<String> chatTriggers = questSec.getStringList("chat-triggers");
					for (String s : chatTriggers) {
						getQuestData(quest).eventFired.put(s, true);
					}
				}
				if (getCurrentStage(quest).commandEvents.isEmpty() == false) {
					for (String commandTrig : getCurrentStage(quest).commandEvents.keySet()) {
						getQuestData(quest).eventFired.put(commandTrig, false);
					}
				}
				if (questSec.contains("command-triggers")) {
					List<String> commandTriggers = questSec.getStringList("command-triggers");
					for (String s : commandTriggers) {
						getQuestData(quest).eventFired.put(s, true);
					}
				}
			}
		}
		return true;
	}

	public static ConfigurationSection getLegacyQuestData(FileConfiguration questSec, String questName) {
		ConfigurationSection newData = questSec.createSection("questData");
		if (questSec.contains("blocks-broken-names")) {
			List<String> names = questSec.getStringList("blocks-broken-names");
			List<Integer> amounts = questSec.getIntegerList("blocks-broken-amounts");
			newData.set(questName + ".blocks-broken-names", names);
			newData.set(questName + ".blocks-broken-amounts", amounts);
		}
		if (questSec.contains("blocks-damaged-names")) {
			List<String> names = questSec.getStringList("blocks-damaged-names");
			List<Integer> amounts = questSec.getIntegerList("blocks-damaged-amounts");
			newData.set(questName + ".blocks-damaged-names", names);
			newData.set(questName + ".blocks-damaged-amounts", amounts);
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
			List<String> chatTriggers = questSec.getStringList("chat-triggers");
			newData.set(questName + ".chat-triggers", chatTriggers);
		}
		if (questSec.contains("command-triggers")) {
			List<String> commandTriggers = questSec.getStringList("command-triggers");
			newData.set(questName + ".command-triggers", commandTriggers);
		}
		return newData;
	}

	/**
	 * Initiate the stage timer
	 * @param quest The quest of which the timer is for
	 */
	public void startStageTimer(Quest quest) {
		if (getQuestData(quest).delayTimeLeft > -1) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), 
					(long) (getQuestData(quest).delayTimeLeft * 0.02));
		} else {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), 
					(long) (getCurrentStage(quest).delay * 0.02));
			if (getCurrentStage(quest).delayMessage != null) {
				Player p = plugin.getServer().getPlayer(id);
				p.sendMessage(plugin.parseString((getCurrentStage(quest).delayMessage), quest, p));
			}
		}
		getQuestData(quest).delayStartTime = System.currentTimeMillis();
	}
	
	/**
	 * Pause the stage timer. Useful when a player quits
	 * @param quest The quest of which the timer is for
	 */
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
			for (Quest q : plugin.getQuests()) {
				if (q.getName().equalsIgnoreCase(quest.getName())) {
					Stage stage = getCurrentStage(quest);
					quest.updateCompass(this, stage);
					exists = true;
					// Meh, let's not.
					/*
					 * if (q.equals(quest) == false) {
					 * 
					 * hardQuit(quest);
					 * 
					 * if (plugin.getServer().getPlayer(id) != null) { String error = Lang.get("questModified"); error = error.replace("<quest>",
					 * ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED); plugin.getServer().getPlayer(id).sendMessage(ChatColor.GOLD + "[Quests] "
					 * + ChatColor.RED + error); updateJournal(); }
					 * 
					 * }
					 */
					break;
				}
			}
			if (exists == false) {
				if (plugin.getServer().getPlayer(id) != null) {
					String error = Lang.get("questNotExist");
					error = error.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED);
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
		Inventory inv = Bukkit.getServer().createInventory(player, size, Lang.get(player, "quests") + " | " + npc.getName());
		int inc = 0;
		for (int i = 0; i < quests.size(); i++) {
			if (quests.get(i).guiDisplay != null) {
				ItemStack display = quests.get(i).guiDisplay;
				ItemMeta meta = display.getItemMeta();
				if (completedQuests.contains(quests.get(i).getName())) {
					meta.setDisplayName(ChatColor.DARK_PURPLE + Quests.parseString(quests.get(i).getName()
							+ " " + ChatColor.GREEN + Lang.get(player, "redoCompleted"), npc));
				} else {
					meta.setDisplayName(ChatColor.DARK_PURPLE + Quests.parseString(quests.get(i).getName(), npc));
				}
				if (!meta.hasLore()) {
					LinkedList<String> lines = new LinkedList<String>();
					String desc = quests.get(i).description;
					if (desc.equals(ChatColor.stripColor(desc))) {
						lines = MiscUtil.makeLines(desc, " ", 40, ChatColor.DARK_GREEN);
					} else {
						lines = MiscUtil.makeLines(desc, " ", 40, null);
					}
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
			if (!timers.isEmpty()) {
				for (Map.Entry<Integer, Quest> entry : timers.entrySet()) {
					if (entry.getValue().getName().equals(quest.getName())) {
						plugin.getServer().getScheduler().cancelTask(entry.getKey());
						timers.remove(entry.getKey());
					}
				}
			}
			if (plugin.getDependencies().getGpsApi() != null) {
				if (plugin.getDependencies().getGpsApi().gpsIsActive(this.getPlayer())) {
					plugin.getDependencies().getGpsApi().stopGPS(this.getPlayer());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void hardRemove(Quest quest) {
		try {
			completedQuests.remove(quest.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void hardClear() {
		try {
			currentQuests.clear();
			questData.clear();
			amountsCompleted.clear();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void hardStagePut(Quest key, Integer val) {
		try {
			currentQuests.put(key, val);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void hardDataPut(Quest key, QuestData val) {
		try {
			questData.put(key, val);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void resetCompass() {
		if (!plugin.getSettings().canUseCompass())
			return;
		Player player = getPlayer();
		if (player == null)
			return;
		Location defaultLocation = player.getBedSpawnLocation();
		if (defaultLocation == null) {
			defaultLocation = player.getWorld().getSpawnLocation();
		}
		player.setCompassTarget(defaultLocation);
	}

	public void findCompassTarget() {
		if (!plugin.getSettings().canUseCompass())
			return;
		Player player = getPlayer();
		if (player == null)
			return;
		for (Quest quest : currentQuests.keySet()) {
			Stage stage = getCurrentStage(quest);
			if (stage != null && quest.updateCompass(this, stage))
				break;
		}
	}
	
	public boolean hasItem(ItemStack is) {
		Inventory inv = getPlayer().getInventory();
		int playerAmount = 0;
		for (ItemStack stack : inv.getContents()) {
			if (stack != null) {
				if (ItemUtil.compareItems(is, stack, false) == 0) {
					playerAmount += stack.getAmount();
				}
			}
		}
		return playerAmount >= is.getAmount();
	}
}
