package main.java.me.blackvein.quests;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import org.bukkit.entity.Player;

public class QuestTaskTrigger {

    public boolean parseQuestTaskTrigger(String theScriptName, Player thePlayer) {
        if (!ScriptRegistry.containsScript(theScriptName)) {
            return false;
        }
        TaskScriptContainer task_script = ScriptRegistry.getScriptContainerAs(theScriptName, TaskScriptContainer.class);
        task_script.runTaskScript(thePlayer, null, null);
        return true;
    }
}