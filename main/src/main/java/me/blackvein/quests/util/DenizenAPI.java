package me.blackvein.quests.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;

import net.citizensnpcs.api.npc.NPC;

public class DenizenAPI {
    private Object denizen = null;
    private Class<?> scriptRegistry = null;
    private Method containsScriptGetMethod = null;
    private Method _getScriptNamesGetMethod = null;
    private Class<?> scriptContainer = null;
    private Class<?> taskScriptContainer = null;
    private Method getScriptContainerAsGetMethod = null;
    private Class<?> dPlayer = null;
    private Class<?> dNPC = null;
    private Method mirrorBukkitPlayerGetMethod = null;
    private Method mirrorCitizensNPCGetMethod = null;
    private Class<?> scriptEntryData = null;
    private Class<?> bukkitScriptEntryData = null;
    public boolean isLegacyVersion = false;

    public boolean isEnabled() {
        return denizen != null;
    }

    public DenizenAPI() {
    	try {
    		denizen = Class.forName("com.denizenscript.denizen.Denizen");
    		// Denizen 1.1.0+
    	} catch (Exception e) {
    		try {
				//denizen = Class.forName("net.aufdemrand.denizen.Denizen");
				scriptRegistry = Class.forName("net.aufdemrand.denizencore.scripts.ScriptRegistry");
				scriptContainer = Class.forName("net.aufdemrand.denizencore.scripts.containers.ScriptContainer");
				taskScriptContainer = Class.forName("net.aufdemrand.denizencore.scripts.containers.core.TaskScriptContainer");
				dPlayer = Class.forName("net.aufdemrand.denizen.objects.dPlayer");
				dNPC = Class.forName("net.aufdemrand.denizen.objects.dNPC");
				scriptEntryData = Class.forName("net.aufdemrand.denizencore.scripts.ScriptEntryData");
				bukkitScriptEntryData = Class.forName("net.aufdemrand.denizen.BukkitScriptEntryData");
				// Denizen <1.1.0
			} catch (Exception e2) {
				// Fail silently
			}
    	}
    }
    
    private void initialize() {
        if (!isLegacyVersion) {
            isLegacyVersion = true;
            if (denizen == null) {
                try {
                	containsScriptGetMethod = scriptRegistry.getMethod("containsScript", String.class);
                    _getScriptNamesGetMethod = scriptRegistry.getMethod("_getScriptNames");
                	getScriptContainerAsGetMethod = scriptRegistry.getMethod("getScriptContainerAs", String.class, taskScriptContainer.getClass());
                	mirrorBukkitPlayerGetMethod = dPlayer.getMethod("mirrorBukkitPlayer", OfflinePlayer.class);
                	mirrorCitizensNPCGetMethod = dNPC.getMethod("mirrorCitizensNPC", NPC.class);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Quests failed to bind to Denizen, integration will not work!", e);
                    return;
                }
            }
        }
    }
    
    @Nullable
    public boolean containsScript(String input) {
    	if (denizen != null) {
    		// Denizen 1.1.0+
    		return ScriptRegistry.containsScript(input);
    	} else {
    		// Denizen <1.1.0
    		initialize();
    		if (scriptRegistry == null || containsScriptGetMethod == null) return false;
    		boolean script = false;
    		try {
    			script = (boolean)containsScriptGetMethod.invoke(scriptRegistry, input);
    		} catch (Exception e) {
    			Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen ScriptRegistry#containsScript", e);
    		}
    		return script;
    	}
    }
    
    @Nullable
    public String getScriptContainerName(String input) {
    	if (denizen != null) {
    		return ScriptRegistry.getScriptContainer(input).getName();
    	} else {
    		initialize();
            if (scriptRegistry == null || scriptContainer == null) return null;
            String name = null;
            Object instance;
            try {
            	Constructor<?> constructor = scriptRegistry.getConstructor(YamlConfiguration.class, String.class);
            	instance = constructor.newInstance(null, input);
            	name = (String)instance.getClass().getMethod("getName").invoke(scriptContainer);
            } catch (Exception e) {
            	Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen ScriptContainer#getName", e);
            }
            return name;
    	}
    }
    
    @SuppressWarnings("unchecked")
	@Nullable
    public Set<String> _getScriptNames() {
    	if (denizen != null) {
    		return ScriptRegistry._getScriptNames();
    	} else {
    		initialize();
            if (scriptRegistry == null || _getScriptNamesGetMethod == null) return null;
            Set<String> names = null;
            try {
            	names = (Set<String>)_getScriptNamesGetMethod.invoke(scriptRegistry);
            } catch (Exception e) {
            	Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen ScriptRegistry#_getScriptNames", e);
            }
            return names;
    	}
    }
    
    @Nullable
    public Object getScriptContainerAs(String scriptName) {
    	if (denizen != null) {
    		return ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
    	} else {
    		initialize();
            if (scriptRegistry == null || taskScriptContainer == null) return null;
            Object container = null;
            try {
            	container = getScriptContainerAsGetMethod.invoke(scriptRegistry, scriptName, taskScriptContainer);
            } catch (Exception e) {
            	Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen #getScriptContainerAs", e);
            }
            return container;
    	}
    }
    
    @Nullable
    public Object mirrorBukkitPlayer(Player player) {
    	if (denizen != null) {
    		return PlayerTag.mirrorBukkitPlayer(player);
    	} else {
    		initialize();
            if (dPlayer == null || mirrorBukkitPlayerGetMethod == null) return null;
            Object dp = null;
            try {
            	dp = mirrorBukkitPlayerGetMethod.invoke(dPlayer, player);
            } catch (Exception e) {
            	Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen dPlayer#mirrorBukkitPlayer", e);
            }
            return dp;
    	}
    }
    
    @Nullable
    public Object mirrorCitizensNPC(NPC npc) {
    	if (denizen != null) {
    		return NPCTag.mirrorCitizensNPC(npc);
    	} else {
    		initialize();
            if (dNPC == null || mirrorCitizensNPCGetMethod == null) return null;
            Object dp = null;
            try {
            	dp = mirrorCitizensNPCGetMethod.invoke(dNPC, npc);
            } catch (Exception e) {
            	Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen dNPC#mirrorCitizensNPC", e);
            }
            return dp;
    	}
    }
    
    @Nullable
    public void runTaskScript(String scriptName, Player player) {
    	if (denizen != null) {
    		TaskScriptContainer taskScript = ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
			BukkitScriptEntryData entryData = new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(player), null);
			taskScript.runTaskScript(entryData, null);
    	} else {
    		initialize();
            if (scriptRegistry == null || bukkitScriptEntryData == null || scriptEntryData == null) return;
            try {
    			Constructor<?> constructor = bukkitScriptEntryData.getConstructors()[0];
    			Object tsc = getScriptContainerAs(scriptName);
    			Method runTaskScript = tsc.getClass().getMethod("runTaskScript", scriptEntryData, Map.class);
    			runTaskScript.invoke(tsc, constructor.newInstance(mirrorBukkitPlayer(player), null), null);
    		} catch (Exception e) {
    			Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen TaskScriptContainer#runTaskScript", e);
    		}
    	}
    }
}