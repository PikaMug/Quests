package me.blackvein.quests;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import org.bukkit.entity.Player;

public class QuestTaskTrigger {

    public boolean parseQuestTaskTrigger(String theScriptName, Player player) {
        if (!ScriptRegistry.containsScript(theScriptName)) {
            return false;
        }
        TaskScriptContainer task_script = ScriptRegistry.getScriptContainerAs(theScriptName, TaskScriptContainer.class);
        task_script.runTaskScript(dPlayer.mirrorBukkitPlayer(player), null, null);
        return true;
    }
}