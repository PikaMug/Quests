package me.blackvein.quests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class Requirements {
	private int money = 0;
	private int questPoints = 0;
	private List<ItemStack> items = new LinkedList<ItemStack>();
	private List<Boolean> removeItems = new LinkedList<Boolean>();
	private List<String> neededQuests = new LinkedList<String>();
	private List<String> blockQuests = new LinkedList<String>();
	private List<String> permissions = new LinkedList<String>();
	private List<String> mcmmoSkills = new LinkedList<String>();
	private List<Integer> mcmmoAmounts = new LinkedList<Integer>();
	private String heroesPrimaryClass = null;
	private String heroesSecondaryClass = null;
	private Map<String, Map<String, Object>> customRequirements = new HashMap<String, Map<String, Object>>();
	private String failRequirements = null;
	
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
	public List<ItemStack> getItems() {
		return items;
	}
	public void setItems(List<ItemStack> items) {
		this.items = items;
	}
	public List<Boolean> getRemoveItems() {
		return removeItems;
	}
	public void setRemoveItems(List<Boolean> removeItems) {
		this.removeItems = removeItems;
	}
	public List<String> getNeededQuests() {
		return neededQuests;
	}
	public void setNeededQuests(List<String> neededQuests) {
		this.neededQuests = neededQuests;
	}
	public List<String> getBlockQuests() {
		return blockQuests;
	}
	public void setBlockQuests(List<String> blockQuests) {
		this.blockQuests = blockQuests;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
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
	public String getHeroesPrimaryClass() {
		return heroesPrimaryClass;
	}
	public void setHeroesPrimaryClass(String heroesPrimaryClass) {
		this.heroesPrimaryClass = heroesPrimaryClass;
	}
	public String getHeroesSecondaryClass() {
		return heroesSecondaryClass;
	}
	public void setHeroesSecondaryClass(String heroesSecondaryClass) {
		this.heroesSecondaryClass = heroesSecondaryClass;
	}
	public Map<String, Map<String, Object>> getCustomRequirements() {
		return customRequirements;
	}
	protected void setCustomRequirements(
			Map<String, Map<String, Object>> customRequirements) {
		this.customRequirements = customRequirements;
	}
	public String getFailRequirements() {
		return failRequirements;
	}
	public void setFailRequirements(String failRequirements) {
		this.failRequirements = failRequirements;
	}
}