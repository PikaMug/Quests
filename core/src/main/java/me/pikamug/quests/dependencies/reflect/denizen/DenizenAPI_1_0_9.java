/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.dependencies.reflect.denizen;

import me.pikamug.quests.BukkitQuestsPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class DenizenAPI_1_0_9 {
    
    private static final BukkitQuestsPlugin quests = (BukkitQuestsPlugin) Bukkit.getPluginManager().getPlugin("Quests");
    private static final DenizenAPI api = quests != null ? quests.getDependencies().getDenizenApi() : null;
    
    public static boolean containsScript(final String input) {
        if (quests == null || api.scriptRegistry == null || api.containsScriptMethod == null) return false;
        boolean script = false;
        try {
            script = (boolean)api.containsScriptMethod.invoke(api.scriptRegistry, input);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen ScriptRegistry#containsScript", e);
        }
        return script;
    }
    
    @Nullable
    public static String getScriptContainerName(final String input) {
        if (quests == null || api.scriptRegistry == null || api.scriptContainer == null) return null;
        String name = null;
        final Object instance;
        try {
            final Constructor<?> constructor = api.scriptRegistry.getConstructor(YamlConfiguration.class, String.class);
            instance = constructor.newInstance(null, input);
            name = (String)instance.getClass().getMethod("getName").invoke(api.scriptContainer);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen ScriptContainer#getName", e);
        }
        return name;
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public static Set<String> getScriptNames() {
        if (quests == null || api.scriptRegistry == null || api.getScriptNamesMethod == null) return null;
        Set<String> names = null;
        try {
            names = (Set<String>)api.getScriptNamesMethod.invoke(api.scriptRegistry);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen ScriptRegistry#_getScriptNames", e);
        }
        return names;
    }
    
    @Nullable
    public static Object getScriptContainerAs(final String scriptName) {
        if (quests == null || api.scriptRegistry == null || api.taskScriptContainer == null) return null;
        Object container = null;
        try {
            container = api.getScriptContainerAsMethod.invoke(api.scriptRegistry, scriptName, api.taskScriptContainer);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen #getScriptContainerAs", e);
        }
        return container;
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(final Player player) {
        if (quests == null || api.dPlayer == null || api.mirrorBukkitPlayerMethod == null) return null;
        Object dp = null;
        try {
            dp = api.mirrorBukkitPlayerMethod.invoke(api.dPlayer, player);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen dPlayer#mirrorBukkitPlayer", e);
        }
        return dp;
    }
    
    @Nullable
    public static Object mirrorCitizensNPC(final NPC npc) {
        if (quests == null || api.dNPC == null || api.mirrorCitizensNPCMethod == null) return null;
        Object dp = null;
        try {
            dp = api.mirrorCitizensNPCMethod.invoke(api.dNPC, npc);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen dNPC#mirrorCitizensNPC", e);
        }
        return dp;
    }
    
    public static void runTaskScript(final String scriptName, final Player player, final NPC npc) {
        if (quests == null || api.scriptRegistry == null || api.bukkitScriptEntryData == null || api.scriptEntryData == null) return;
        try {
            final Constructor<?> constructor = api.bukkitScriptEntryData.getConstructors()[0];
            final Object tsc = getScriptContainerAs(scriptName);
            if (tsc != null) {
                final Method runTaskScript = tsc.getClass().getMethod("runTaskScript", api.scriptEntryData, Map.class);
                runTaskScript.invoke(tsc, constructor.newInstance(mirrorBukkitPlayer(player), mirrorCitizensNPC(npc)), null);
            }
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen TaskScriptContainer#runTaskScript", e);
        }
    }
}
