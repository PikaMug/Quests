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

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import me.pikamug.quests.BukkitQuestsPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class DenizenAPI_1_1_1 {

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
        return ScriptRegistry.getScriptContainer(input).getName();
    }
    
    public static @NotNull Set<String> getScriptNames() {
        return ScriptRegistry.scriptContainers.keySet();
    }
    
    @Nullable
    public static Object getScriptContainerAs(final String scriptName) {
        return ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(final Player player) {
        return PlayerTag.mirrorBukkitPlayer(player);
    }
    
    public static @NotNull Object mirrorCitizensNPC(final NPC npc) {
        return NPCTag.fromEntity(npc.getEntity());
    }

    public static void runTaskScript(final String scriptName, final Player player, final NPC npc) {
        final TaskScriptContainer taskScript = ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
        final BukkitScriptEntryData entryData = new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(player),
                npc != null ? NPCTag.fromEntity(npc.getEntity()) : null);
        final InstantQueue queue = new InstantQueue(taskScript.getName());

        if (quests == null) {
            return;
        }
        try {
            final Method addEntries = queue.getClass().getMethod("addEntries", List.class);
            addEntries.invoke(queue, taskScript.getBaseEntries(entryData.clone()));
            queue.start();
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen InstantQueue#addEntries", e);
        }
    }
}
