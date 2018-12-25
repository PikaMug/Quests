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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.characters.Hero;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quest {

	public String name;
	public String description;
	public String finished;
	public String region = null;
	public ItemStack guiDisplay = null;
	public int parties = 0;
	private LinkedList<Stage> orderedStages = new LinkedList<Stage>();
	NPC npcStart;
	Location blockStart;
	Quests plugin;
	Event initialEvent;
	// Requirements
	int moneyReq = 0;
	int questPointsReq = 0;
	List<ItemStack> items = new LinkedList<ItemStack>();
	List<Boolean> removeItems = new LinkedList<Boolean>();
	List<String> neededQuests = new LinkedList<String>();
	List<String> blockQuests = new LinkedList<String>();
	List<String> permissionReqs = new LinkedList<String>();
	List<String> mcMMOSkillReqs = new LinkedList<String>();
	List<Integer> mcMMOAmountReqs = new LinkedList<Integer>();
	String heroesPrimaryClassReq = null;
	String heroesSecondaryClassReq = null;
	Map<String, Map<String, Object>> customRequirements = new HashMap<String, Map<String, Object>>();
	Map<String, Map<String, Object>> customRewards = new HashMap<String, Map<String, Object>>();
	public String failRequirements = null;
	// Planner
	public String startPlanner = null;
	public String endPlanner = null;
	public long repeatPlanner = -1;
	public long cooldownPlanner = -1;
	// Rewards
	int moneyReward = 0;
	int questPoints = 0;
	int exp = 0;
	List<String> commands = new LinkedList<String>();
	List<String> permissions = new LinkedList<String>();
	LinkedList<ItemStack> itemRewards = new LinkedList<ItemStack>();
	List<String> mcmmoSkills = new LinkedList<String>();
	List<Integer> mcmmoAmounts = new LinkedList<Integer>();
	List<String> heroesClasses = new LinkedList<String>();
	List<Double> heroesAmounts = new LinkedList<Double>();
	List<String> phatLootRewards = new LinkedList<String>();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Stage getStage(int index) {
		try {
			return orderedStages.get(index);
		} catch (Exception e) {
			return null;
		}
	}
	
	public LinkedList<Stage> getStages() {
		return orderedStages;
	}

	public void nextStage(Quester q) {
		String stageCompleteMessage = q.getCurrentStage(this).completeMessage;
		if (stageCompleteMessage != null) {
			String s = Quests.parseString(stageCompleteMessage, this);
			if(Quests.placeholder != null) {
				s = PlaceholderAPI.setPlaceholders(q.getPlayer(), s);
			}
			q.getPlayer().sendMessage(s);
		}
		if (plugin.useCompass) {
			q.resetCompass();
			q.findCompassTarget();
		}
		if (q.getCurrentStage(this).delay < 0) {
			Player player = q.getPlayer();
			if (q.currentQuests.get(this) == (orderedStages.size() - 1)) {
				if (q.getCurrentStage(this).script != null) {
					plugin.trigger.parseQuestTaskTrigger(q.getCurrentStage(this).script, player);
				}
				if (q.getCurrentStage(this).finishEvent != null) {
					q.getCurrentStage(this).finishEvent.fire(q, this);
				}
				completeQuest(q);
			} else {
				try {
					if (q.getCurrentStage(this).finishEvent != null) {
						q.getCurrentStage(this).finishEvent.fire(q, this);
					}
					setStage(q, q.currentQuests.get(this) + 1);
				} catch (InvalidStageException e) {
					e.printStackTrace();
				}
			}
			if (q.getQuestData(this) != null) {
				q.getQuestData(this).delayStartTime = 0;
				q.getQuestData(this).delayTimeLeft = -1;
			}
		} else {
			q.startStageTimer(this);
		}
		q.updateJournal();
	}

	public void setStage(Quester quester, int stage) throws InvalidStageException {
		if (orderedStages.size() - 1 < stage) {
			throw new InvalidStageException(this, stage);
		}
		Stage currentStage = quester.getCurrentStage(this);
		quester.hardQuit(this);
		quester.hardStagePut(this, stage);
		quester.addEmptiesFor(this, stage);
		if (currentStage.script != null) {
			plugin.trigger.parseQuestTaskTrigger(currentStage.script, quester.getPlayer());
		}
		/*
		 * if (quester.getCurrentStage(this).finishEvent != null) { quester.getCurrentStage(this).finishEvent.fire(quester); }
		 */
		Stage nextStage = quester.getCurrentStage(this);
		if (nextStage.startEvent != null) {
			nextStage.startEvent.fire(quester, this);
		}
		updateCompass(quester, nextStage);
		String msg = Lang.get(quester.getPlayer(), "questObjectivesTitle");
		msg = msg.replaceAll("<quest>", name);
		quester.getPlayer().sendMessage(ChatColor.GOLD + msg);
		plugin.showObjectives(this, quester, false);
		String stageStartMessage = quester.getCurrentStage(this).startMessage;
		if (stageStartMessage != null) {
			quester.getPlayer().sendMessage(Quests.parseString(stageStartMessage, this));
		}
		quester.updateJournal();
	}
	
	/*protected boolean updateGPS(Quester quester, Stage nextStage) {
		if (Quests.gpsapi == null) {
			return false;
		}
		if (!plugin.useCompass) {
			return false;
		}
		if (nextStage == null) {
			return false;
		}
		Location targetLocation = null;
		if (nextStage.citizensToInteract != null && nextStage.citizensToInteract.size() > 0) {
			targetLocation = plugin.getNPCLocation(nextStage.citizensToInteract.getFirst());
		} else if (nextStage.citizensToKill != null && nextStage.citizensToKill.size() > 0) {
			targetLocation = plugin.getNPCLocation(nextStage.citizensToKill.getFirst());
		} else if (nextStage.locationsToReach != null && nextStage.locationsToReach.size() > 0) {
			targetLocation = nextStage.locationsToReach.getFirst();
		}
		if (targetLocation != null) {
			if (targetLocation.getWorld().getName().equals(quester.getPlayer().getWorld().getName())) {
				Quests.gpsapi.addPoint("questsObjective-" + System.currentTimeMillis(), targetLocation);
				if (!Quests.gpsapi.gpsIsActive(quester.getPlayer())) {
					Quests.gpsapi.startGPS(quester.getPlayer(), "questsObjective-" + System.currentTimeMillis());
				}
				
			}
		}
		return targetLocation != null;
	}*/
	
	/*protected boolean startGPS(Quester quester) {
		if (Quests.gpsapi == null) {
			return false;
		}
		Stage stage = quester.getCurrentStage(this);
		LinkedList<Location> targetLocations = new LinkedList<Location>();
		if (stage == null) {
			return false;
		}
		if (stage.citizensToInteract != null && stage.citizensToInteract.size() > 0) {
			for (Integer i : stage.citizensToInteract) {
				targetLocations.add(plugin.getNPCLocation(i));
			}
		} else if (stage.citizensToKill != null && stage.citizensToKill.size() > 0) {
			for (Integer i : stage.citizensToKill) {
				targetLocations.add(plugin.getNPCLocation(i));
			}
		} else if (stage.locationsToReach != null && stage.locationsToReach.size() > 0) {
			targetLocations = stage.locationsToReach;
		}
		if (targetLocations != null && !targetLocations.isEmpty()) {
			int index = 1;
			for (Location l : targetLocations) {
				if (l.getWorld().getName().equals(quester.getPlayer().getWorld().getName())) {
					if (!Quests.gpsapi.gpsIsActive(quester.getPlayer())) {
						System.out.println("adding point " + index);
						Quests.gpsapi.addPoint("target" + index, l);
						index++;
					}
				}
			}
			for (int i = 1 ; i < targetLocations.size(); i++) {
				if (!Quests.gpsapi.gpsIsActive(quester.getPlayer())) {
					System.out.println("connecting point " + i + " and point " + (i + 1));
					Quests.gpsapi.connect("target" + i, "target" + (i + 1), true);
				}
			}
			System.out.println("destination set to " + "point " + (index - 1));
			Quests.gpsapi.startGPS(quester.getPlayer(), "target1", "target" + (index - 1));
		}
		return targetLocations != null && !targetLocations.isEmpty();
	}*/

	public boolean updateCompass(Quester quester, Stage nextStage) {
		if (!plugin.useCompass) {
			return false;
		}
		if (nextStage == null) {
			return false;
		}
		Location targetLocation = null;
		if (nextStage.citizensToInteract != null && nextStage.citizensToInteract.size() > 0) {
			targetLocation = plugin.getNPCLocation(nextStage.citizensToInteract.getFirst());
		} else if (nextStage.citizensToKill != null && nextStage.citizensToKill.size() > 0) {
			targetLocation = plugin.getNPCLocation(nextStage.citizensToKill.getFirst());
		} else if (nextStage.locationsToReach != null && nextStage.locationsToReach.size() > 0) {
			targetLocation = nextStage.locationsToReach.getFirst();
		}
		if (targetLocation != null) {
			if (targetLocation.getWorld().getName().equals(quester.getPlayer().getWorld().getName())) {
				quester.getPlayer().setCompassTarget(targetLocation);
			}
		}
		return targetLocation != null;
	}

	public boolean testRequirements(Quester quester) {
		return testRequirements(quester.getPlayer());
	}

	public boolean testRequirements(Player player) {
		Quester quester = plugin.getQuester(player.getUniqueId());
		if (moneyReq != 0 && Quests.economy != null) {
			if (Quests.economy.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < moneyReq) {
				return false;
			}
		}
		PlayerInventory inventory = player.getInventory();
		int num = 0;
		for (ItemStack is : items) {
			for (ItemStack stack : inventory.getContents()) {
				if (stack != null) {
					if (ItemUtil.compareItems(is, stack, true) == 0) {
						num += stack.getAmount();
					}
				}
			}
			if (num < is.getAmount()) {
				return false;
			}
			num = 0;
		}
		for (String s : permissionReqs) {
			if (player.hasPermission(s) == false) {
				return false;
			}
		}
		for (String s : mcMMOSkillReqs) {
			final SkillType st = Quests.getMcMMOSkill(s);
			final int lvl = mcMMOAmountReqs.get(mcMMOSkillReqs.indexOf(s));
			if (UserManager.getPlayer(player).getProfile().getSkillLevel(st) < lvl) {
				return false;
			}
		}
		if (heroesPrimaryClassReq != null) {
			if (plugin.testPrimaryHeroesClass(heroesPrimaryClassReq, player.getUniqueId()) == false) {
				return false;
			}
		}
		if (heroesSecondaryClassReq != null) {
			if (plugin.testSecondaryHeroesClass(heroesSecondaryClassReq, player.getUniqueId()) == false) {
				return false;
			}
		}
		for (String s : customRequirements.keySet()) {
			CustomRequirement found = null;
			for (CustomRequirement cr : plugin.customRequirements) {
				if (cr.getName().equalsIgnoreCase(s)) {
					found = cr;
					break;
				}
			}
			if (found != null) {
				if (found.testRequirement(player, customRequirements.get(s)) == false) {
					return false;
				}
			} else {
				plugin.getLogger().warning("Quester \"" + player.getName() + "\" attempted to take Quest \"" + name + "\", but the Custom Requirement \"" + s 
						+ "\" could not be found. Does it still exist?");
			}
		}
		if (quester.questPoints < questPointsReq) {
			return false;
		}
		if (quester.completedQuests.containsAll(neededQuests) == false) {
			return false;
		}
		for (String q : blockQuests) {
			Quest questObject = new Quest();
			questObject.name = q;
			if (quester.completedQuests.contains(q) || quester.currentQuests.containsKey(questObject)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void completeQuest(Quester q) {
		final Player player = plugin.getServer().getPlayer(q.id);
		q.hardQuit(this);
		if (!q.completedQuests.contains(name)) {
			q.completedQuests.add(name);
		}
		String none = ChatColor.GRAY + "- (" + Lang.get(player, "none") + ")";
		final String ps = Quests.parseString(finished, this);
		for (Map.Entry<Integer, Quest> entry : q.timers.entrySet()) {
			if (entry.getValue().getName().equals(getName())) {
				plugin.getServer().getScheduler().cancelTask(entry.getKey());
				q.timers.remove(entry.getKey());
			}
		}
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

			@Override
			public void run() {
				for (String msg : ps.split("<br>")) {
					player.sendMessage(ChatColor.AQUA + msg);
				}
			}
		}, 40);
		if (moneyReward > 0 && Quests.economy != null) {
			Quests.economy.depositPlayer(q.getOfflinePlayer(), moneyReward);
			none = null;
		}
		if (cooldownPlanner > -1) {
			q.completedTimes.put(this.name, System.currentTimeMillis());
			if (q.amountsCompleted.containsKey(this.name)) {
				q.amountsCompleted.put(this.name, q.amountsCompleted.get(this.name) + 1);
			} else {
				q.amountsCompleted.put(this.name, 1);
			}
		}
		for (ItemStack i : itemRewards) {
			try {
				Quests.addItem(player, i);
			} catch (Exception e) {
				plugin.getLogger().severe("Unable to add null reward item to inventory of " 
						+ player.getName() + " upon completion of quest " + name);
				player.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
						+ "Please contact an administrator.");
			}
			none = null;
		}
		for (String s : commands) {
			final String command = s.replaceAll("<player>", player.getName());
			if (Bukkit.isPrimaryThread()) {
				Bukkit.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			} else {
				Bukkit.getScheduler().runTask(plugin, new Runnable() {
					
					@Override
					public void run() {
						Bukkit.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
					}
				});
			}
			none = null;
		}
		for (String s : permissions) {
			if (Quests.permission != null) {
				Quests.permission.playerAdd(player, s);
			}
			none = null;
		}
		for (String s : mcmmoSkills) {
			UserManager.getPlayer(player).getProfile().addLevels(Quests.getMcMMOSkill(s), mcmmoAmounts.get(mcmmoSkills.indexOf(s)));
			none = null;
		}
		for (String s : heroesClasses) {
			Hero hero = plugin.getHero(player.getUniqueId());
			hero.addExp(heroesAmounts.get(heroesClasses.indexOf(s)), Quests.heroes.getClassManager().getClass(s), player.getLocation());
			none = null;
		}
		LinkedList<ItemStack> phatLootItems = new LinkedList<ItemStack>();
		int phatLootExp = 0;
		LinkedList<String> phatLootMessages = new LinkedList<String>();
		for (String s : phatLootRewards) {
			LootBundle lb = PhatLootsAPI.getPhatLoot(s).rollForLoot();
			if (lb.getExp() > 0) {
				phatLootExp += lb.getExp();
				player.giveExp(lb.getExp());
			}
			if (lb.getMoney() > 0) {
				if (Quests.economy != null) {
					Quests.economy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), lb.getMoney());
				}
			}
			if (lb.getItemList().isEmpty() == false) {
				phatLootItems.addAll(lb.getItemList());
				for (ItemStack is : lb.getItemList()) {
					try {
						Quests.addItem(player, is);
					} catch (Exception e) {
						plugin.getLogger().severe("Unable to add PhatLoots item to inventory of " 
								+ player.getName() + " upon completion of quest " + name);
						player.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
								+ "Please contact an administrator.");
					}
				}
			}
			if (lb.getCommandList().isEmpty() == false) {
				for (CommandLoot cl : lb.getCommandList()) {
					cl.execute(player);
				}
			}
			if (lb.getMessageList().isEmpty() == false) {
				phatLootMessages.addAll(lb.getMessageList());
			}
		}
		if (exp > 0) {
			player.giveExp(exp);
			none = null;
		}
		String complete = Lang.get(player, "questCompleteTitle");
		complete = complete.replaceAll("<quest>", ChatColor.YELLOW + name + ChatColor.GOLD);
		player.sendMessage(ChatColor.GOLD + complete);
		player.sendMessage(ChatColor.GREEN + Lang.get(player, "questRewardsTitle"));
		if (plugin.showQuestTitles) {
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "title " + player.getName()
					+ " title " + "{\"text\":\"" + Lang.get(player, "quest") + " " + Lang.get(player, "complete") +  "\",\"color\":\"gold\"}");
			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "title " + player.getName()
					+ " subtitle " + "{\"text\":\"" + name + "\",\"color\":\"yellow\"}");
		}
		if (questPoints > 0) {
			player.sendMessage("- " + ChatColor.DARK_GREEN + questPoints + " " + Lang.get(player, "questPoints"));
			q.questPoints += questPoints;
			none = null;
		}
		for (ItemStack i : itemRewards) {
			String text = "error";
			if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
				if (i.getEnchantments().isEmpty()) {
					text = "- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount();
				} else {
					text = "- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET;
					if (!i.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
						text +=  ChatColor.GRAY + " " + Lang.get(player, "with") + ChatColor.DARK_PURPLE;
						for (Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
							text += " " + Quester.prettyEnchantmentString(e.getKey()) + ":" + e.getValue();
						}
					}
					text += ChatColor.GRAY + " x " + i.getAmount();
				}
			} else if (i.getDurability() != 0) {
				if (i.getEnchantments().isEmpty()) {
					text = "- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount();
				} else {
					text = "- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ":" + i.getDurability() + ChatColor.GRAY + " " + Lang.get(player, "with");
					for (Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
						text += " " + Quester.prettyEnchantmentString(e.getKey()) + ":" + e.getValue();
					}
					text += ChatColor.GRAY + " x " + i.getAmount();
				}
			} else {
				if (i.getEnchantments().isEmpty()) {
					text = "- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ChatColor.GRAY + " x " + i.getAmount();
				} else {
					text = "- " + ChatColor.DARK_GREEN + ItemUtil.getName(i);
					if (!i.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
						text += ChatColor.GRAY + " " + Lang.get(player, "with");
						for (Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
							text += " " + Quester.prettyEnchantmentString(e.getKey()) + ":" + e.getValue();
						}
					}
					text += ChatColor.GRAY + " x " + i.getAmount();
				}
			}
			player.sendMessage(text);
			none = null;
		}
		for (ItemStack i : phatLootItems) {
			if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
				if (i.getEnchantments().isEmpty()) {
					player.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount());
				} else {
					player.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " " + Lang.get(player, "enchantedItem"));
				}
			} else if (i.getDurability() != 0) {
				if (i.getEnchantments().isEmpty()) {
					player.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount());
				} else {
					player.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " " + Lang.get(player, "enchantedItem"));
				}
			} else {
				if (i.getEnchantments().isEmpty()) {
					player.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ChatColor.GRAY + " x " + i.getAmount());
				} else {
					player.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " " + Lang.get(player, "enchantedItem"));
				}
			}
			none = null;
		}
		if (moneyReward > 1) {
			player.sendMessage("- " + ChatColor.DARK_GREEN + moneyReward + " " + ChatColor.DARK_PURPLE + Quests.getCurrency(true));
			none = null;
		} else if (moneyReward == 1) {
			player.sendMessage("- " + ChatColor.DARK_GREEN + moneyReward + " " + ChatColor.DARK_PURPLE + Quests.getCurrency(false));
			none = null;
		}
		if (exp > 0 || phatLootExp > 0) {
			int tot = exp + phatLootExp;
			player.sendMessage("- " + ChatColor.DARK_GREEN + tot + ChatColor.DARK_PURPLE + " " + Lang.get(player, "experience"));
			none = null;
		}
		if (mcmmoSkills.isEmpty() == false) {
			for (String s : mcmmoSkills) {
				player.sendMessage("- " + ChatColor.DARK_GREEN + mcmmoAmounts.get(mcmmoSkills.indexOf(s)) + " " + ChatColor.DARK_PURPLE + s + " " + Lang.get(player, "experience"));
			}
			none = null;
		}
		if (heroesClasses.isEmpty() == false) {
			for (String s : heroesClasses) {
				player.sendMessage("- " + ChatColor.AQUA + heroesAmounts.get(heroesClasses.indexOf(s)) + " " + ChatColor.BLUE + s + " " + Lang.get(player, "experience"));
			}
			none = null;
		}
		if (phatLootMessages.isEmpty() == false) {
			for (String s : phatLootMessages) {
				player.sendMessage("- " + s);
			}
			none = null;
		}
		for (String s : customRewards.keySet()) {
			CustomReward found = null;
			for (CustomReward cr : plugin.customRewards) {
				if (cr.getName().equalsIgnoreCase(s)) {
					found = cr;
					break;
				}
			}
			if (found != null) {
				Map<String, Object> datamap = customRewards.get(found.getName());
				String message = found.getRewardName();
				if (message != null) {
					for (String key : datamap.keySet()) {
						message = message.replaceAll("%" + ((String) key) + "%", ((String) datamap.get(key)));
					}
					player.sendMessage("- " + ChatColor.GOLD + message);
				} else {
					plugin.getLogger().warning("Failed to notify player: Custom Reward does not have an assigned name");
				}
				found.giveReward(player, customRewards.get(s));
			} else {
				plugin.getLogger().warning("Quester \"" + player.getName() + "\" completed the Quest \"" + name + "\", but the Custom Reward \"" + s + "\" could not be found. Does it still exist?");
			}
			none = null;
		}
		if (none != null) {
			player.sendMessage(none);
		}
		q.saveData();
		player.updateInventory();
		q.updateJournal();
		if (Quests.gpsapi != null) {
			if (Quests.gpsapi.gpsIsActive(player)) {
				Quests.gpsapi.stopGPS(player);
			}
		}
		q.findCompassTarget();
	}

	@SuppressWarnings("deprecation")
	public void failQuest(Quester q) {
		if (plugin.getServer().getPlayer(q.id) != null) {
			Player player = plugin.getServer().getPlayer(q.id);
			String title = Lang.get(player, "questTitle");
			title = title.replaceAll("<quest>", ChatColor.DARK_PURPLE + name + ChatColor.AQUA);
			player.sendMessage(ChatColor.AQUA + title);
			player.sendMessage(ChatColor.RED + Lang.get(player, "questFailed"));
			q.hardQuit(this);
			q.saveData();
			player.updateInventory();
		} else {
			q.hardQuit(this);
			q.saveData();
		}
		q.updateJournal();
	}

	public boolean isInRegion(Player player) {
		if (region == null) {
			return true;
		} else {
			ApplicableRegionSet ars = Quests.worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
			Iterator<ProtectedRegion> i = ars.iterator();
			while (i.hasNext()) {
				ProtectedRegion pr = i.next();
				if (pr.getId().equalsIgnoreCase(region)) {
					return true;
				}
			}
			return false;
		}
	}
}
