package me.blackvein.quests.objectives;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.Stage;
import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.objectives.core.EventWrapper;
import me.blackvein.quests.objectives.core.ObjectiveEvent;
import me.blackvein.quests.objectives.core.ObjectiveType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

public class ObjectiveDamageBlock extends Objective{

	private HashMap<Quester, HashMap<Material, Integer>> questerMap = new HashMap<Quester, HashMap<Material, Integer>>();
	private HashMap<Stage, HashMap<Material, Integer>> stageMap = new HashMap<Stage, HashMap<Material, Integer>>();

	@Override
	public boolean isFinished(Quester quester) {
		if (questerMap.containsKey(quester)) {
			for (int i : questerMap.get(quester).values()) {
				if (i > 0) return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void setObjectives(Quester quester, Stage stage) {
		if (stageMap.containsKey(stage)) {
			questerMap.put(quester, stageMap.get(stage));
		} else {
			questerMap.remove(quester);
		}
	}

	@Override
	public void initiageStage(Stage stage, ConfigurationSection section) throws InvalidStageException {

		List<Integer> damageIds = new LinkedList<Integer>();
		List<Integer> damageAmounts = new LinkedList<Integer>();

		if (section.contains("damage-block-ids")) {
			if (!section.getIntegerList("damage-block-ids").isEmpty()) {
				damageIds = section.getIntegerList("damage-block-ids");
			} else {
				printSevere("[Quests] damage-block-ids: in Stage " + stage.getIndex() + " of Quest " + stage.getQuestName() + " is not a list of numbers!");
				throw new InvalidStageException(stage.getQuestName(), stage.getIndex());
			}

			if (section.contains("damage-block-amounts")) {
				if (!section.getIntegerList("damage-block-amounts").isEmpty()) {
					damageAmounts = section.getIntegerList("damage-block-amounts");
				} else {
					printSevere("[Quests] damage-block-amounts: in Stage " + stage.getIndex() + " of Quest " + stage.getQuestName() + " is not a list of numbers!");
					throw new InvalidStageException(stage.getQuestName(), stage.getIndex());
				}

			} else {
				printSevere("[Quests] Stage " + stage.getIndex() + " of Quest " + stage.getQuestName() + " is missing damage-block-amounts:");
				throw new InvalidStageException(stage.getQuestName(), stage.getIndex());
			}
		}

		HashMap<Material, Integer> tempMap = new HashMap<Material, Integer>();
		for (int i = 0; i < damageIds.size(); i++) {
			if (i < damageAmounts.size()) {
				Material m = Material.getMaterial(damageIds.get(i));
				int amounts = damageAmounts.get(i);
				tempMap.put(m, amounts);
			}
		}
		if (!tempMap.isEmpty()) {
			stageMap.put(stage, tempMap);
			stage.getObjectives().add(getType());
		}

	}

	@ObjectiveEvent(BlockDamageEvent.class)
	public void onDamageBlock(EventWrapper wrapper) {
		BlockDamageEvent event = (BlockDamageEvent) wrapper.getEvent();

		Quester quester = Quests.getInstance().getQuester(event.getPlayer().getName());
		if (questerMap.containsKey(quester)) {

			Material material = event.getBlock().getType();

			if (questerMap.get(quester).containsKey(material)) {

				int toDamage = questerMap.get(quester).get(material) - 1;
				questerMap.get(quester).put(material, toDamage);

				if (toDamage <= 0) {
					int totalBlocksToDamage = stageMap.get(quester.currentStage).get(material);
					String finishMessage = ChatColor.GREEN + "(Completed) Damage " + Quester.prettyItemString(material) + " " + totalBlocksToDamage + "/" + totalBlocksToDamage;
					quester.finishObjective(this, finishMessage);
				}
			}
		}
	}

	@Override
	public ObjectiveType getType() {
		return ObjectiveType.DAMAGE_BLOCK;
	}

}
