package me.blackvein.quests;

import java.util.List;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.triggers.AbstractTrigger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestTaskTrigger extends AbstractTrigger {

  public boolean parseQuestTaskTrigger(String theScriptName, Player thePlayer) {

    CommandSender cs = Bukkit.getConsoleSender();
    ScriptHelper sE = plugin.getScriptEngine().helper;

    if (theScriptName == null) return false;

    List<String> theScript = sE.getScript(theScriptName + ".Script");

    if (theScript.isEmpty()) return false;

    sE.queueScriptEntries(thePlayer, sE.buildScriptEntries(thePlayer, theScript, theScriptName), QueueType.TASK);

    return true;
  }

}