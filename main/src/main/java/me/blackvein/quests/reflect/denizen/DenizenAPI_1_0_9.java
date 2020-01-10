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

package me.blackvein.quests.reflect.denizen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.blackvein.quests.Quests;
import net.citizensnpcs.api.npc.NPC;

public class DenizenAPI_1_0_9 {
    
    private static Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
    private static DenizenAPI api = quests.getDependencies().getDenizenAPI();
    
    @Nullable
    public static boolean containsScript(String input) {
        if (api.scriptRegistry == null || api.containsScriptMethod == null) return false;
        boolean script = false;
        try {
            script = (boolean)api.containsScriptMethod.invoke(api.scriptRegistry, input);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen ScriptRegistry#containsScript", e);
        }
        return script;
    }
    
    @Nullable
    public static String getScriptContainerName(String input) {
        if (api.scriptRegistry == null || api.scriptContainer == null) return null;
        String name = null;
        Object instance;
        try {
            Constructor<?> constructor = api.scriptRegistry.getConstructor(YamlConfiguration.class, String.class);
            instance = constructor.newInstance(null, input);
            name = (String)instance.getClass().getMethod("getName").invoke(api.scriptContainer);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen ScriptContainer#getName", e);
        }
        return name;
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public static Set<String> getScriptNames() {
        if (api.scriptRegistry == null || api.getScriptNamesMethod == null) return null;
        Set<String> names = null;
        try {
            names = (Set<String>)api.getScriptNamesMethod.invoke(api.scriptRegistry);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen ScriptRegistry#_getScriptNames", e);
        }
        return names;
    }
    
    @Nullable
    public static Object getScriptContainerAs(String scriptName) {
        if (api.scriptRegistry == null || api.taskScriptContainer == null) return null;
        Object container = null;
        try {
            container = api.getScriptContainerAsMethod.invoke(api.scriptRegistry, scriptName, api.taskScriptContainer);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen #getScriptContainerAs", e);
        }
        return container;
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(Player player) {
        if (api.dPlayer == null || api.mirrorBukkitPlayerMethod == null) return null;
        Object dp = null;
        try {
            dp = api.mirrorBukkitPlayerMethod.invoke(api.dPlayer, player);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen dPlayer#mirrorBukkitPlayer", e);
        }
        return dp;
    }
    
    @Nullable
    public static Object mirrorCitizensNPC(NPC npc) {
        if (api.dNPC == null || api.mirrorCitizensNPCMethod == null) return null;
        Object dp = null;
        try {
            dp = api.mirrorCitizensNPCMethod.invoke(api.dNPC, npc);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen dNPC#mirrorCitizensNPC", e);
        }
        return dp;
    }
    
    @Nullable
    public static void runTaskScript(String scriptName, Player player) {
        if (api.scriptRegistry == null || api.bukkitScriptEntryData == null || api.scriptEntryData == null) return;
        try {
            Constructor<?> constructor = api.bukkitScriptEntryData.getConstructors()[0];
            Object tsc = getScriptContainerAs(scriptName);
            Method runTaskScript = tsc.getClass().getMethod("runTaskScript", api.scriptEntryData, Map.class);
            runTaskScript.invoke(tsc, constructor.newInstance(mirrorBukkitPlayer(player), null), null);
        } catch (Exception e) {
            quests.getLogger().log(Level.WARNING, "Error invoking Denizen TaskScriptContainer#runTaskScript", e);
        }
    }
}
