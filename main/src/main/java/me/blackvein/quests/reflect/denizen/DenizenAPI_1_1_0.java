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
import org.bukkit.entity.Player;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;

import me.blackvein.quests.Quests;
import net.citizensnpcs.api.npc.NPC;

public class DenizenAPI_1_1_0 {
    
    private static Quests quests = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
    private static DenizenAPI api = quests.getDependencies().getDenizenApi();
    
    @Nullable
    public static boolean containsScript(String input) {
        return ScriptRegistry.containsScript(input);
    }
    
    @Nullable
    public static String getScriptContainerName(String input) {
        return ScriptRegistry.getScriptContainer(input).getName();
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
        return ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(Player player) {
        return PlayerTag.mirrorBukkitPlayer(player);
    }
    
    @Nullable
    public static Object mirrorCitizensNPC(NPC npc) {
        return NPCTag.mirrorCitizensNPC(npc);
    }
    
    @Nullable
    public static void runTaskScript(String scriptName, Player player) {
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
