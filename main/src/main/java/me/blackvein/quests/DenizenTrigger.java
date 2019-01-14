package me.blackvein.quests;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.containers.core.TaskScriptContainer;

public class DenizenTrigger {
	protected boolean runDenizenScript(String scriptName, Quester quester) {
		if (scriptName == null) {
			return false;
		}
		if (ScriptRegistry.containsScript(scriptName)) {
			TaskScriptContainer task_script = ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
			BukkitScriptEntryData entryData = new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(quester.getPlayer()), null);
			task_script.runTaskScript(entryData, null);
		}
		return true;
    }
}
