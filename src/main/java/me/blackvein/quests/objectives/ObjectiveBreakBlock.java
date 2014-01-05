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

public class ObjectiveBreakBlock extends Objective {
	
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
		List<Integer> breakIds = new LinkedList<Integer>();
        List<Integer> breakAmounts = new LinkedList<Integer>();
		
		if (section.contains("break-block-ids")) {
			if (!section.getIntegerList("break-block-ids").isEmpty()) {
				breakIds = section.getIntegerList("break-block-ids");
            } else {
                printSevere("[Quests] break-block-ids: in Stage " + stage.getIndex() + " of Quest " + stage.getQuestName() + " is not a list of numbers!");
                throw new InvalidStageException(stage.getQuestName(), stage.getIndex());
            }

            if (section.contains("break-block-amounts")) {
                if (!section.getIntegerList("break-block-amounts").isEmpty()) {
                	breakAmounts = section.getIntegerList("break-block-amounts");
                } else {
                    printSevere("[Quests] break-block-amounts: in Stage " + stage.getIndex() + " of Quest " + stage.getQuestName() + " is not a list of numbers!");
                    throw new InvalidStageException(stage.getQuestName(), stage.getIndex());
                }

            } else {
                printSevere("[Quests] Stage " + stage.getIndex() + " of Quest " + stage.getQuestName() + " is missing break-block-amounts:");
                throw new InvalidStageException(stage.getQuestName(), stage.getIndex());
            }
        }
		HashMap<Material, Integer> tempMap = new HashMap<Material, Integer>();
		for (int i = 0; i < breakIds.size(); i++) {
			if (i < breakAmounts.size()) {
				Material m = Material.getMaterial(breakIds.get(i));
				int amounts = breakAmounts.get(i);
				tempMap.put(m, amounts);
			}
		}
		if (!tempMap.isEmpty()) {
			stageMap.put(stage, tempMap);
			stage.getObjectives().add(getType());
		}
	}
	
	@Override
	public ObjectiveType getType() {
		return ObjectiveType.BREAK_BLOCK;
	}
	
	@ObjectiveEvent(BlockBreakEvent.class)
	public void executeEvent(EventWrapper wrapper) {
		BlockBreakEvent event = (BlockBreakEvent) wrapper.getEvent();
		
		if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) == true || event.isCancelled() == true) {
			return;
		}
		
		Quester quester = Quests.getInstance().getQuester(event.getPlayer().getName());
		if (questerMap.containsKey(quester)) {
			
			Material material = event.getBlock().getType();
			
			if (questerMap.get(quester).containsKey(material)) {
				
				int toBreak = questerMap.get(quester).get(material) - 1;
				questerMap.get(quester).put(material, toBreak);
				
				if (toBreak <= 0) {
					int totalBlocksToBeak = stageMap.get(quester.currentStage).get(material);
					String finishMessage = ChatColor.GREEN + "(Completed) Break " + Quester.prettyItemString(material) + " " + totalBlocksToBeak + "/" + totalBlocksToBeak;
					quester.finishObjective(this, finishMessage);
				}
			}
		}
	}
}
