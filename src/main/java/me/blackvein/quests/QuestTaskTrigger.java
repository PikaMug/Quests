package me.blackvein.quests;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.containers.core.TaskScriptContainer;

public class QuestTaskTrigger {

	public boolean parseQuestTaskTrigger(String theScriptName, Player player) {
		if (!ScriptRegistry.containsScript(theScriptName)) {
			return false;
		}
		TaskScriptContainer task_script = ScriptRegistry.getScriptContainerAs(theScriptName, TaskScriptContainer.class);
		BukkitScriptEntryData entryData = new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(player), null);
		task_script.runTaskScript(entryData, null);
		return true;
	}
}
