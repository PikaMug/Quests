/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

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

import net.citizensnpcs.api.npc.NPC;

public class DenizenAPI {
    private Class<?> denizen = null;
    private Class<?> scriptRegistry = null;
    private Method containsScriptMethod = null;
    private Method getScriptNamesMethod = null;
    private Class<?> scriptContainer = null;
    private Class<?> taskScriptContainer = null;
    private Method getScriptContainerAsMethod = null;
    private Class<?> dPlayer = null;
    private Class<?> dNPC = null;
    private Method mirrorBukkitPlayerMethod = null;
    private Method mirrorCitizensNPCMethod = null;
    private Class<?> scriptEntryData = null;
    private Class<?> bukkitScriptEntryData = null;
    private boolean initialized = false;

    public boolean isEnabled() {
        return denizen != null;
    }

    public DenizenAPI() {
        try {
            denizen = Class.forName("com.denizenscript.denizen.Denizen");
            // Denizen 1.1.0+
        } catch (Exception e) {
            try {
                denizen = Class.forName("net.aufdemrand.denizen.Denizen");
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
    
    /**
     * Initialize Denizen <1.1.0 methods
     */
    private void initialize() {
        if (!initialized) {
            initialized = true;
            
            try {
                containsScriptMethod = scriptRegistry.getMethod("containsScript", String.class);
                getScriptNamesMethod = scriptRegistry.getMethod("_getScriptNames");
                getScriptContainerAsMethod = scriptRegistry.getMethod("getScriptContainerAs", String.class, taskScriptContainer.getClass());
                mirrorBukkitPlayerMethod = dPlayer.getMethod("mirrorBukkitPlayer", OfflinePlayer.class);
                mirrorCitizensNPCMethod = dNPC.getMethod("mirrorCitizensNPC", NPC.class);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Quests failed to bind to Denizen, integration will not work!", e);
                return;
            }
        }
    }
    
    public Class<?> getDenizenClass() {
        return denizen;
    }
    
    @Nullable
    public boolean containsScript(String input) {
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            return DenizenAPI_1_1_0.containsScript(input);
        } else {
            initialize();
            if (scriptRegistry == null || containsScriptMethod == null) return false;
            boolean script = false;
            try {
                script = (boolean)containsScriptMethod.invoke(scriptRegistry, input);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen ScriptRegistry#containsScript", e);
            }
            return script;
        }
    }
    
    @Nullable
    public String getScriptContainerName(String input) {
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            return DenizenAPI_1_1_0.getScriptContainerName(input);
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
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            return DenizenAPI_1_1_0._getScriptNames();
        } else {
            initialize();
            if (scriptRegistry == null || getScriptNamesMethod == null) return null;
            Set<String> names = null;
            try {
                names = (Set<String>)getScriptNamesMethod.invoke(scriptRegistry);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen ScriptRegistry#_getScriptNames", e);
            }
            return names;
        }
    }
    
    @Nullable
    public Object getScriptContainerAs(String scriptName) {
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            return DenizenAPI_1_1_0.getScriptContainerAs(scriptName);
        } else {
            initialize();
            if (scriptRegistry == null || taskScriptContainer == null) return null;
            Object container = null;
            try {
                container = getScriptContainerAsMethod.invoke(scriptRegistry, scriptName, taskScriptContainer);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen #getScriptContainerAs", e);
            }
            return container;
        }
    }
    
    @Nullable
    public Object mirrorBukkitPlayer(Player player) {
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            return DenizenAPI_1_1_0.mirrorBukkitPlayer(player);
        } else {
            initialize();
            if (dPlayer == null || mirrorBukkitPlayerMethod == null) return null;
            Object dp = null;
            try {
                dp = mirrorBukkitPlayerMethod.invoke(dPlayer, player);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen dPlayer#mirrorBukkitPlayer", e);
            }
            return dp;
        }
    }
    
    @Nullable
    public Object mirrorCitizensNPC(NPC npc) {
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            return DenizenAPI_1_1_0.mirrorCitizensNPC(npc);
        } else {
            initialize();
            if (dNPC == null || mirrorCitizensNPCMethod == null) return null;
            Object dp = null;
            try {
                dp = mirrorCitizensNPCMethod.invoke(dNPC, npc);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Quests encountered an error invoking Denizen dNPC#mirrorCitizensNPC", e);
            }
            return dp;
        }
    }
    
    @Nullable
    public void runTaskScript(String scriptName, Player player) {
        if (denizen.getName().startsWith("c")) { // com.denizenscript.denizen.*
            DenizenAPI_1_1_0.runTaskScript(scriptName, player);
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
