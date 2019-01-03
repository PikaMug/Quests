package me.blackvein.quests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class Rewards {
	int money = 0;
	int questPoints = 0;
	int exp = 0;
	List<String> commands = new LinkedList<String>();
	List<String> permissions = new LinkedList<String>();
	LinkedList<ItemStack> items = new LinkedList<ItemStack>();
	List<String> mcmmoSkills = new LinkedList<String>();
	List<Integer> mcmmoAmounts = new LinkedList<Integer>();
	List<String> heroesClasses = new LinkedList<String>();
	List<Double> heroesAmounts = new LinkedList<Double>();
	List<String> phatLoots = new LinkedList<String>();
	private Map<String, Map<String, Object>> customRewards = new HashMap<String, Map<String, Object>>();
	
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	public int getQuestPoints() {
		return questPoints;
	}
	public void setQuestPoints(int questPoints) {
		this.questPoints = questPoints;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	public List<String> getCommands() {
		return commands;
	}
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
	public LinkedList<ItemStack> getItems() {
		return items;
	}
	public void setItems(LinkedList<ItemStack> items) {
		this.items = items;
	}
	public List<String> getMcmmoSkills() {
		return mcmmoSkills;
	}
	public void setMcmmoSkills(List<String> mcmmoSkills) {
		this.mcmmoSkills = mcmmoSkills;
	}
	public List<Integer> getMcmmoAmounts() {
		return mcmmoAmounts;
	}
	public void setMcmmoAmounts(List<Integer> mcmmoAmounts) {
		this.mcmmoAmounts = mcmmoAmounts;
	}
	public List<String> getHeroesClasses() {
		return heroesClasses;
	}
	public void setHeroesClasses(List<String> heroesClasses) {
		this.heroesClasses = heroesClasses;
	}
	public List<Double> getHeroesAmounts() {
		return heroesAmounts;
	}
	public void setHeroesAmounts(List<Double> heroesAmounts) {
		this.heroesAmounts = heroesAmounts;
	}
	public List<String> getPhatLoots() {
		return phatLoots;
	}
	public void setPhatLoots(List<String> phatLoots) {
		this.phatLoots = phatLoots;
	}
	public Map<String, Map<String, Object>> getCustomRewards() {
		return customRewards;
	}
	protected void setCustomRewards(Map<String, Map<String, Object>> customRewards) {
		this.customRewards = customRewards;
	}
}